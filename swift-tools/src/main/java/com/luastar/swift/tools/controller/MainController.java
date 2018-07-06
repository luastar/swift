package com.luastar.swift.tools.controller;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.luastar.swift.tools.view.GuiLoggerAppender;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 主界面控制器
 */
public class MainController extends AbstractController {

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
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        Appender<ILoggingEvent> appender = rootLogger.getAppender("gui");
        if (appender != null && (appender instanceof GuiLoggerAppender)) {
            ((GuiLoggerAppender) appender).setConsoleTextArea(consoleTextArea);
        }
    }

}
