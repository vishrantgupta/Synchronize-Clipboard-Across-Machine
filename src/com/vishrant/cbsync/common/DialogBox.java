/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vishrant.cbsync.common;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 *
 * @author Vishrant
 */
public class DialogBox {

    private final Alert alert;

    public DialogBox(String title, String message, AlertType type) {

        alert = new Alert(AlertType.INFORMATION);

        alert.setAlertType(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

    }

    public void show() {
        alert.showAndWait();
    }

}
