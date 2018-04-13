package com.luastar.swift.tools.controller;

import com.luastar.swift.tools.MainUI;
import com.luastar.swift.tools.view.GuiLoggerAppender;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import org.apache.log4j.Appender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 主界面控制器
 */
public class MainController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(MainUI.class);

    @FXML
    private TextArea consoleTextArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initConsoleTextArea();
    }

    /**
     * 初始化控制台
     */
    private void initConsoleTextArea() {
        // 不可编辑
        consoleTextArea.setEditable(false);
        // 右键清除按钮
        MenuItem clear = new MenuItem("清除");
        clear.setOnAction(event -> consoleTextArea.clear());
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().add(clear);
        consoleTextArea.setContextMenu(contextMenu);
        // 设置日志属性
        Appender appender = org.apache.log4j.Logger.getRootLogger().getAppender("Gui");
        if (appender == null || !(appender instanceof GuiLoggerAppender)) {
            logger.warn("找不到名称为Gui的日志配置！");
            return;
        }
        ((GuiLoggerAppender) appender).setConsoleTextArea(consoleTextArea);
    }

}
