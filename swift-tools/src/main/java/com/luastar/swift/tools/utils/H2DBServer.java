package com.luastar.swift.tools.utils;

import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * H2数据库
 */
public class H2DBServer {

    private static final Logger logger = LoggerFactory.getLogger(H2DBServer.class);

    private Server server;

    public void start(int port) {
        start("-tcpPort", String.valueOf(port));
    }

    public void start(String... args) {
        try {
            logger.info("启动H2数据库...");
            server = Server.createTcpServer(args);
            server.start();
            logger.info("启动H2数据库成功！");
        } catch (SQLException e) {
            logger.error("启动H2数据库异常：" + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        if (this.server != null) {
            logger.info("停止H2数据库...");
            this.server.stop();
            this.server = null;
        }
    }

}
