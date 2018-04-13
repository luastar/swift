package com.luastar.swift.tools.view;

import javafx.scene.control.TextArea;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * 日志输出
 */
public class GuiLoggerAppender extends AppenderSkeleton {

    private TextArea consoleTextArea;

    public void setConsoleTextArea(TextArea consoleTextArea) {
        this.consoleTextArea = consoleTextArea;
    }

    @Override
    public void append(LoggingEvent event) {
        if (consoleTextArea == null) {
            return;
        }
        this.consoleTextArea.appendText(layout.format(event));
        if (layout.ignoresThrowable()) {
            String[] logAry = event.getThrowableStrRep();
            if (logAry != null) {
                for (String log : logAry) {
                    this.consoleTextArea.appendText(log);
                    this.consoleTextArea.appendText(Layout.LINE_SEP);
                }
            }
        }
    }

    @Override
    public void close() {
        consoleTextArea.clear();
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

}
