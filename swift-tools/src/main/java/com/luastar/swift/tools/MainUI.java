package com.luastar.swift.tools;

import com.luastar.swift.base.utils.ClassLoaderUtils;
import com.luastar.swift.base.utils.ObjUtils;
import com.luastar.swift.tools.utils.H2Utils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * 主程序入口
 */
public class MainUI extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainUI.class);

    private static final String version = "1.0.0";

    private static Stage primaryStage;

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        String version = System.getProperty("java.version");
        logger.info("java.version：{}", version);
        String[] versionAry = version.split("\\.");
        if (ObjUtils.toInteger(versionAry[0], 1) >= 1
                && ObjUtils.toInteger(versionAry[1], 5) >= 8) {
            launch(args);
        } else {
            JFrame frame = new JFrame("提示");
            frame.setSize(300, 100);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            JPanel jPanel = new JPanel(new BorderLayout());
            JLabel jLabel = new JLabel("JDK的版本不能低于1.8！", SwingConstants.CENTER);
            jPanel.add(jLabel, BorderLayout.CENTER);
            frame.add(jPanel);
            frame.setVisible(true);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainUI.primaryStage = primaryStage;
        FXMLLoader loader = new FXMLLoader(ClassLoaderUtils.getClassLoader().getResource("fxml/main.fxml"));
        Pane root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("SwiftTools " + version);
        primaryStage.setResizable(true);
        primaryStage.show();
        // 初始化数据库
        H2Utils.initH2Db();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

}
