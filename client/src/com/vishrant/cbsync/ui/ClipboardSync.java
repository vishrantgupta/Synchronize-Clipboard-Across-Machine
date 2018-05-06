/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vishrant.cbsync.ui;

import com.vishrant.cbsync.common.ApplicationContext;
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Vishrant
 */
public class ClipboardSync extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        try {

//            FXMLLoader.load(new URL("UI.fxml"));
//            Parent parent = FXMLLoader.load(getClass().getResource("/com/vishrant/cbsync/ui/UI.fxml"));
//            Scene scene = new Scene(parent);
//            primaryStage.setScene(scene);
//
////            primaryStage.setMaximized(false);
//            primaryStage.resizableProperty().setValue(Boolean.FALSE);
//            primaryStage.show();
            ClipBoardController controller = new ClipBoardController();

            ApplicationContext context = ApplicationContext.getInstance();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vishrant/cbsync/ui/UI.fxml"));
            loader.setController(controller);

            context.setController(controller);

            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);

            // primaryStage.setMaximized(false);
            primaryStage.resizableProperty().setValue(Boolean.FALSE);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        System.exit(0);
    }

}
