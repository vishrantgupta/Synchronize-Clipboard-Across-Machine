/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.vishrant.cbsync.socket;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import info.vishrant.cbsync.clipboard.ClipBoardListener;
import info.vishrant.cbsync.common.ApplicationContext;

import javafx.application.Platform;

/**
 *
 * @author Vishrant
 */
public class SocketConnectionListener implements SocketListener {

    private ClipBoardListener clipBoardListener = null;
    private final SocketConnection socketConnection;

    public SocketConnectionListener(SocketConnection socketConnection) {
        this.socketConnection = socketConnection;
    }

    @Override
    public void connected() {
//        try {
////            clipBoardListener = new ClipBoardListener(socketConnection, 500);
////            ApplicationContext.getInstance().setClipBoardListener(clipBoardListener);
////            clipBoardListener.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void update(Object... os) {
        try {
            if (os != null && os.length > 0) {
                StringSelection selection = new StringSelection(os[os.length - 1].toString());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnected() {

        try {
            if (clipBoardListener != null) {
                clipBoardListener.stop();
                clipBoardListener = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void message(Object... os) {

        if (socketConnection.isConnected() && os != null && os.length > 0) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    ApplicationContext.getInstance().getLblError().setText(os[os.length - 1].toString());
                }
            });
        }
    }

    @Override
    public void connecting() {

    }

    @Override
    public void update(SocketConnectionStatus status) {

        if (status == SocketConnectionStatus.CONNECTING) {
            connecting();
        }

        if (status == SocketConnectionStatus.CONNECTED) {
            connected();
        }

        if (status == SocketConnectionStatus.DISCONNECTED) {
            disconnected();
        }

    }

}
