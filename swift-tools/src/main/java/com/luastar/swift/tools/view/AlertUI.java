package com.luastar.swift.tools.view;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

/**
 * 提示信息
 */
public class AlertUI {

    public static void info(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.show();
    }

    public static void warn(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(msg);
        alert.show();
    }

    public static void error(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }

    public static Alert confirm(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                msg,
                new ButtonType("是", ButtonBar.ButtonData.YES),
                new ButtonType("否", ButtonBar.ButtonData.NO));
        return alert;
    }

}
