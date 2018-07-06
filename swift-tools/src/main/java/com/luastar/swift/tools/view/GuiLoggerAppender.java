package com.luastar.swift.tools.view;

import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.status.ErrorStatus;
import javafx.scene.control.TextArea;
import org.apache.commons.lang3.StringUtils;


/**
 * 日志输出
 */
public class GuiLoggerAppender<E> extends UnsynchronizedAppenderBase<E> {

    protected Encoder<E> encoder;
    private TextArea consoleTextArea;

    public void setEncoder(Encoder<E> encoder) {
        this.encoder = encoder;
    }

    public void setConsoleTextArea(TextArea consoleTextArea) {
        this.consoleTextArea = consoleTextArea;
    }

    @Override
    public void start() {
        int errors = 0;
        if (this.encoder == null) {
            addStatus(new ErrorStatus("No encoder set for the appender named \"" + name + "\".", this));
            errors++;
        }
        // only error free appenders should be activated
        if (errors == 0) {
            super.start();
        }
    }

    @Override
    protected void append(E eventObject) {
        if (!isStarted() || consoleTextArea == null) {
            return;
        }
        consoleTextArea.appendText(StringUtils.toEncodedString(encoder.encode(eventObject), null));
    }

}
