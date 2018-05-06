/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.vishrant.cbsync.common;

import info.vishrant.cbsync.clipboard.ClipBoardListener;
import info.vishrant.cbsync.socket.SocketConnection;
import info.vishrant.cbsync.ui.ClipBoardController;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;

/**
 *
 * @author Vishrant
 */
public class ApplicationContext {

    private static final ApplicationContext CONTEXT = new ApplicationContext();

    public static ApplicationContext getInstance() {
        return CONTEXT;
    }

    private ClipBoardController controller;

//    private ClipBoardListener clipBoardListener;

//    public ClipBoardListener getClipBoardListener() {
//        return clipBoardListener;
//    }
//
//    public void setClipBoardListener(ClipBoardListener clipBoardListener) {
//        this.clipBoardListener = clipBoardListener;
//    }

    private Label lblError;
    private volatile boolean loggingEnabled = true;
    private Button btnPause;

    private ProgressIndicator progressIndicator;

    public ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    public void setProgressIndicator(ProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
    }
    
    private GridPane gridSyncClipboard;

    private SocketConnection socketConnection;

    private ApplicationContext() {
    }

//    public ClipBoardListener getClipBoardListener() {
//        return clipBoardListener;
//    }
//
//    public void setClipBoardListener(ClipBoardListener clipBoardListener) {
//        this.clipBoardListener = clipBoardListener;
//    }

    public ClipBoardController getController() {
        return controller;
    }

    public void setController(ClipBoardController controller) {
        this.controller = controller;
    }

    public SocketConnection getSocketConnection() {
        return socketConnection;
    }

    public void setSocketConnection(SocketConnection socketConnection) {
        this.socketConnection = socketConnection;
    }

    public Button getBtnPause() {
        return btnPause;
    }

    public void setBtnPause(Button btnPause) {
        this.btnPause = btnPause;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    public Label getLblError() {
        return lblError;
    }

    public GridPane getGridSyncClipboard() {
        return gridSyncClipboard;
    }

    public void setGridSyncClipboard(GridPane gridSyncClipboard) {
        this.gridSyncClipboard = gridSyncClipboard;
    }

    public void setLblError(Label lblError) {
        this.lblError = lblError;
    }

}
