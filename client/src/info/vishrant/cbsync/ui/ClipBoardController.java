/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.vishrant.cbsync.ui;

import java.net.URL;
import java.util.ResourceBundle;

import info.vishrant.cbsync.common.AppConstant;
import info.vishrant.cbsync.common.ApplicationContext;
import info.vishrant.cbsync.common.DialogBox;
import info.vishrant.cbsync.http.HttpRequestService;
import info.vishrant.cbsync.socket.SocketService;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author Vishrant
 */
public class ClipBoardController implements Initializable {

    private static final ApplicationContext CONTEXT = ApplicationContext.getInstance();

//    public void setMessage(String message) {
//        lblError.setText(message);
//    }
    @FXML
    private TextField txtEmailId;
    @FXML
    private Button btnStartSync;
    @FXML
    private Button btnPause;
    @FXML
    private Label lblError;
    @FXML
    private CheckBox chkLog;
    @FXML
    private PasswordField txtSecretCode;
    @FXML
    private Button btnGenerateSecretCode;

    @FXML
    private GridPane gridSyncClipboard;

    @FXML
    private MenuItem menuAbout;

    @FXML
    ProgressIndicator progress;

    @FXML
    private MenuItem menuExit;

    private HttpRequestService httpService;
    private SocketService socketService;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        CONTEXT.setLblError(lblError);
        CONTEXT.setGridSyncClipboard(gridSyncClipboard);
        CONTEXT.setBtnPause(btnPause);
        CONTEXT.setProgressIndicator(progress);

        menuAbout.setOnAction(menuAboutEvent -> {
            DialogBox dialogBox = new DialogBox("About",
                    "Version 1.0 \nCopyright \u00a9 Vishrant Gupta.\n E-mail: vishrant.gupta@gmail.com\n License: Freeware, not allowed to modify",
                    Alert.AlertType.INFORMATION);
            dialogBox.show();
        });

        menuExit.setOnAction(menuAboutEvent -> {
            System.exit(0);
        });

        BooleanBinding booleanBindBtnGenerateSecretCode = txtEmailId.textProperty().isEmpty();

        btnGenerateSecretCode.disableProperty().bind(booleanBindBtnGenerateSecretCode);

        BooleanBinding booleanBindBtnStartSync = txtEmailId.textProperty().isEmpty()
                .or(txtSecretCode.textProperty().isEmpty());

        btnStartSync.disableProperty().bind(booleanBindBtnStartSync);

        BooleanBinding booleanBindBtnPause = btnStartSync.disabledProperty().isEqualTo(booleanBindBtnStartSync);
        btnPause.disableProperty().bind(booleanBindBtnPause);

        btnGenerateSecretCode.setOnAction(generateSecretCodeEvent -> {

            lblError.setText("");

            if (txtEmailId != null && !txtEmailId.getText().isEmpty()) {

                String param = "?email=" + txtEmailId.getText();
                String urlString = AppConstant.REQUEST_TOKEN + param;

                httpService = new HttpRequestService(urlString);
                httpService.start();

                progress.progressProperty().bind(httpService.progressProperty());
                progress.visibleProperty().bind(BooleanExpression.booleanExpression(httpService.runningProperty()));

                txtEmailId.disableProperty().bind(httpService.runningProperty());
                txtSecretCode.disableProperty().bind(httpService.runningProperty());
                btnGenerateSecretCode.disableProperty().bind(httpService.runningProperty());
                btnStartSync.disableProperty().bind(httpService.runningProperty());
                btnPause.disableProperty().bind(httpService.runningProperty().not());

                httpService.setOnSucceeded(completeEvent -> {
                    lblError.setText(httpService.getMessage());

                    btnGenerateSecretCode.disableProperty().bind(booleanBindBtnGenerateSecretCode);
                    btnStartSync.disableProperty().bind(booleanBindBtnStartSync);

                    btnPause.disableProperty().bind(booleanBindBtnPause);
                });

            }
        });

        chkLog.disableProperty().bind(booleanBindBtnStartSync);

        btnStartSync.setOnAction(startSyncEvent -> {
            try {

                lblError.setText("");

                socketService = new SocketService(txtEmailId.getText(), txtSecretCode.getText());
                socketService.start();

                txtEmailId.disableProperty().bind(socketService.runningProperty());
                txtSecretCode.disableProperty().bind(socketService.runningProperty());
                btnGenerateSecretCode.disableProperty().bind(socketService.runningProperty());
                btnStartSync.disableProperty().bind(socketService.runningProperty());
                btnPause.disableProperty().bind(socketService.runningProperty().not());

                progress.progressProperty().bind(socketService.progressProperty());
                progress.visibleProperty().bind(socketService.runningProperty());

                socketService.setOnSucceeded(completeEvent -> {

                    btnGenerateSecretCode.disableProperty().bind(booleanBindBtnGenerateSecretCode);
                    btnStartSync.disableProperty().bind(booleanBindBtnStartSync);

                    btnPause.disableProperty().bind(booleanBindBtnPause);

//                    if (socketService.isRunning()) {
//
//                        txtEmailId.disableProperty().unbind();
//                        txtEmailId.disableProperty().set(false);
//
//                        txtSecretCode.disableProperty().unbind();
//                        txtSecretCode.disableProperty().set(false);
//
//                        btnGenerateSecretCode.disableProperty().unbind();
//                        btnGenerateSecretCode.disableProperty().set(false);
//
//                        btnStartSync.disableProperty().unbind();
//                        btnStartSync.disableProperty().set(false);
//
//                        btnPause.disableProperty().unbind();
//                        btnPause.disableProperty().set(false);
//                    }

                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        );

        btnPause.setOnAction(pauseBtnEvent
                -> {
            try {

                if (socketService != null) {
                    socketService.cancel();
                    socketService = null;
                }
                if (httpService != null) {
                    httpService.cancel();
                    httpService = null;
                }

                if (CONTEXT.getSocketConnection() != null) {
                    CONTEXT.getSocketConnection().disconnect();
                    CONTEXT.getSocketConnection().stop();
                    CONTEXT.setSocketConnection(null);
                }

                lblError.setText("");

                btnGenerateSecretCode.disableProperty().bind(booleanBindBtnGenerateSecretCode);
                btnStartSync.disableProperty().bind(booleanBindBtnStartSync);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        );

        chkLog.setOnAction(logEvent
                -> {
            ApplicationContext.getInstance().setLoggingEnabled(chkLog.isSelected());
            lblError.setText("");
        }
        );

    }

}
