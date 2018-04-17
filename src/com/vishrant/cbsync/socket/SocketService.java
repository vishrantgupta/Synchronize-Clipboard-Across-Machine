/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vishrant.cbsync.socket;

import com.vishrant.cbsync.clipboard.ClipBoardListener;
import com.vishrant.cbsync.common.ApplicationContext;
import com.vishrant.cbsync.common.CommonUtilities;
import javafx.application.Platform;

import javafx.concurrent.Task;

/**
 *
 * @author Vishrant
 */
public class SocketService extends javafx.concurrent.Service<Boolean> {

    private final static ApplicationContext CONTEXT = ApplicationContext.getInstance();

    @Override
    public boolean cancel() {
        if (clipBoardListener != null) {
            clipBoardListener.stop();
            clipBoardListener = null;
        }
        return super.cancel();
    }

    private final String email;
    private final String secretCode;

    public SocketService(String email, String secretCode) {
        this.email = email;
        this.secretCode = secretCode;
    }
    private ClipBoardListener clipBoardListener;

    @Override
    protected Task<Boolean> createTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {

                    CommonUtilities utilities = new CommonUtilities();

                    if (!utilities.isInternetReachable()) {

                        updateMessage("Please check your internet connection");
                        updateProgress(10, 10);

                        return false;
                    }

                    SocketConnection socket = new SocketConnection(email, secretCode);
                    socket.start();

                    while (!socket.isConnected()) {
                    }

                    if (socket.isConnected()) {
                        ApplicationContext.getInstance().setSocketConnection(socket);
                    } else {
                        ApplicationContext.getInstance().setSocketConnection(null);
                    }

                    clipBoardListener = new ClipBoardListener(socket, 500);

                    updateProgress(10, 10);
                    updateMessage(null);

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            ApplicationContext.getInstance().getProgressIndicator().visibleProperty().unbind();
                            ApplicationContext.getInstance().getProgressIndicator().visibleProperty().set(false);
                        }
                    });

                    clipBoardListener.start();

                    while (socket.isConnected()) {
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        };

    }

}
