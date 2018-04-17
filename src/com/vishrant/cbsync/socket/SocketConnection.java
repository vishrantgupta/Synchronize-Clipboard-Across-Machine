/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vishrant.cbsync.socket;

import com.vishrant.cbsync.common.AppConstant;
import com.vishrant.cbsync.common.ApplicationContext;

import io.socket.client.IO;
import io.socket.client.Socket;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import javafx.application.Platform;

/**
 *
 * @author Vishrant
 */
public class SocketConnection extends Thread {

    private final SocketListener socketListener = new SocketConnectionListener(this);

    private String emailId;
    private String secretCode;

    private volatile Socket socket = null;

    private final ReentrantLock lock = new ReentrantLock();

    public SocketConnection(String emailId, String secretCode) {
        this.emailId = emailId;
        this.secretCode = secretCode;
    }

    private SocketConnection() {
        setName("Socket connection");
    }

    @Override
    public void run() {
        connect(emailId, secretCode);
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public Socket connect(String emailId, String secretCode) {

        try {
            if (socket != null && socket.connected()) {
                return socket;
            }

            socket = IO.socket(AppConstant.APP_URL);
            socket.connect().emit("handshake", emailId, secretCode);

            socketListener.update(SocketConnectionStatus.CONNECTING);

            socket.on(Socket.EVENT_CONNECT, (Object... os) -> {
                try {
                    lock.tryLock(AppConstant.LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
//                    socket.emit("handshake", emailId, secretCode);
                    socketListener.update(SocketConnectionStatus.CONNECTED);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    lock.unlock(); // problem
                }
            }).on(Socket.EVENT_MESSAGE, (Object... os) -> {
                try {
                    lock.tryLock(AppConstant.LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
                    socketListener.message(os);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }).on("update", (Object... os) -> {
                try {
                    lock.tryLock(AppConstant.LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
                    socketListener.update(os);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }).on(Socket.EVENT_DISCONNECT, (Object... os) -> {
                try {
                    lock.tryLock(AppConstant.LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
                    socketListener.update(SocketConnectionStatus.DISCONNECTED);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    lock.unlock();
                }
            });

            long startTime = System.currentTimeMillis();
            while (socket != null && !socket.connected()) {
                if (System.currentTimeMillis() - startTime < AppConstant.RESPONSE_TIMEOUT) {
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return socket;
    }

    public boolean isConnected() {
        if (socket != null) {
            return socket.connected();
        }
        return false;
    }

    public void inform(final String message) {
        try {
            lock.tryLock(AppConstant.LOCK_TIMEOUT, TimeUnit.MILLISECONDS);

            if (socket != null && socket.connected()) {
                socket.emit("inform", message);

                if (ApplicationContext.getInstance().isLoggingEnabled()
                        && socket.connected()) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {

                            int size = message.length();

                            ApplicationContext.getInstance().getLblError().setText("Synced at: " + new Date().toString() + "\n" + message.substring(0, size > 10 ? 10 : size)
                                    + (size > 10 ? "..." : "")
                            );
                        }
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void disconnect() {
        if (socket != null) {
            socket.disconnect();

            long startTime = System.currentTimeMillis();
            while (socket.connected()) {
                if (System.currentTimeMillis() - startTime < AppConstant.RESPONSE_TIMEOUT) {
                    break;
                }
            }
            // socket = null;
        }
        socketListener.disconnected();
    }

}
