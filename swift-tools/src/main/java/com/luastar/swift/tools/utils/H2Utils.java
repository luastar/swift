package com.luastar.swift.tools.utils;

import com.google.common.collect.Maps;
import com.luastar.swift.base.utils.ClassLoaderUtils;
import com.luastar.swift.base.utils.ObjUtils;
import com.luastar.swift.tools.enums.ConfigCode;
import com.luastar.swift.tools.enums.ConfigGroupCode;
import com.luastar.swift.tools.enums.ConfigStatus;
import com.luastar.swift.tools.enums.DbType;
import com.luastar.swift.tools.model.gui.DbConfig;
import com.luastar.swift.tools.model.gui.PubConfig;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.io.IOUtils;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * H2数据库
 */
public class H2Utils {

    private static final Logger logger = LoggerFactory.getLogger(H2Utils.class);

    private static Server server;

    public static Server getServer() {
        return server;
    }

    public static DataBaseUtils getInstance() {
        return SingleH2DbUtils.instance;
    }

    public static void startTcpServer(String... args) {
        try {
            stop();
            server = Server.createTcpServer(args);
            server.start();
            logger.info("启动H2数据库成功！");
        } catch (SQLException e) {
            logger.error("启动H2数据库异常：" + e.getMessage(), e);
        }
    }

    public static void startWebServer(String... args) {
        try {
            stop();
            server = Server.createWebServer(args);
            server.start();
            logger.info("启动H2数据库成功！");
        } catch (SQLException e) {
            logger.error("启动H2数据库异常：" + e.getMessage(), e);
        }
    }

    public static void stop() {
        if (server != null) {
            server.stop();
            server = null;
            logger.info("停止H2数据库成功！");
        }
    }

    /**
     * 初始化H2数据库表
     */
    public static void initH2Db() {
        try {
            logger.info("初始化数据库开始");
            String sql = IOUtils.toString(ClassLoaderUtils.getInputStream("classpath:sqls/h2_init.sql"));
            getInstance().getQueryRunner().execute(sql);
            logger.info("初始化数据库成功");
        } catch (Exception e) {
            logger.error("初始化数据库异常：" + e.getMessage(), e);
        }
    }

    /**
     * 获取数据库配置列表
     *
     * @return
     */
    public static List<DbConfig> getDbConfigList() {
        try {
            String sql = "SELECT * FROM DB_CONFIG ORDER BY DB_NAME,CREATE_TIME DESC";
            return getInstance().getQueryRunner().query(sql, new BeanListHandler<>(DbConfig.class));
        } catch (Exception e) {
            logger.error("获取数据库配置异常：" + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 新增数据库配置
     * 返回新增配置的ID
     *
     * @param dbConfig
     * @return
     */
    public static Integer insertDbConfig(DbConfig dbConfig) {
        try {
            if (dbConfig == null) {
                return 0;
            }
            dbConfig.setCreate_time(new Date());
            String sql = "INSERT INTO DB_CONFIG(DB_TYPE, DB_NAME, JDBC_URL, USER_NAME, PASSWORD, CREATE_TIME) VALUES (?, ?, ?, ?, ?, ?)";
            Object[] rsAry = getInstance().getQueryRunner().insert(sql, new ArrayHandler(),
                    dbConfig.getDb_type(),
                    dbConfig.getDb_name(),
                    dbConfig.getJdbc_url(),
                    dbConfig.getUser_name(),
                    dbConfig.getPassword(),
                    dbConfig.getCreate_time());
            return ObjUtils.toInteger(rsAry[0]);
        } catch (Exception e) {
            logger.error("获取数据库配置异常：" + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 修改数据库配置
     * 返回修改数据的数量
     *
     * @param dbConfig
     * @return
     */
    public static int updateDbConfig(DbConfig dbConfig) {
        try {
            if (dbConfig == null || dbConfig.getId() == null) {
                return 0;
            }
            String sql = "UPDATE DB_CONFIG SET DB_TYPE=?, DB_NAME=?, JDBC_URL=?, USER_NAME=?, PASSWORD=? WHERE ID=?";
            return H2Utils.getInstance().getQueryRunner().update(sql,
                    dbConfig.getDb_type(),
                    dbConfig.getDb_name(),
                    dbConfig.getJdbc_url(),
                    dbConfig.getUser_name(),
                    dbConfig.getPassword(),
                    dbConfig.getId());
        } catch (Exception e) {
            logger.error("获取数据库配置异常：" + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 删除数据库配置
     * 返回删除的数量
     *
     * @param id
     */
    public static int deleteDbConfig(Integer id) {
        try {
            if (id == null) {
                return 0;
            }
            String sql = "DELETE FROM DB_CONFIG WHERE ID=?";
            return H2Utils.getInstance().getQueryRunner().update(sql, id);
        } catch (Exception e) {
            logger.error("获取数据库配置异常：" + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 获取一组配置信息
     *
     * @param groupCode
     * @return
     */
    public static Map<ConfigCode, PubConfig> getPubConfigMap(ConfigGroupCode groupCode) {
        try {
            if (groupCode == null) {
                return null;
            }
            String sql = "SELECT * FROM PUB_CONFIG WHERE GROUP_CODE=?";
            List<PubConfig> configList = getInstance().getQueryRunner().query(sql, new BeanListHandler<>(PubConfig.class), groupCode.getGroupCode());
            if (ObjUtils.isEmpty(configList)) {
                return null;
            }
            Map<ConfigCode, PubConfig> resMap = Maps.newHashMap();
            for (PubConfig config : configList) {
                ConfigCode configCode = ConfigCode.parse(config.getConfig_code());
                if (configCode == null) {
                    continue;
                }
                resMap.put(configCode, config);
            }
            return resMap;
        } catch (Exception e) {
            logger.error("获取常用配置异常：" + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取一条配置信息
     *
     * @param configCode
     * @return
     */
    public static PubConfig getPubConfig(ConfigCode configCode) {
        try {
            if (configCode == null) {
                return null;
            }
            String sql = "SELECT * FROM PUB_CONFIG WHERE CONFIG_CODE=? LIMIT 1";
            return getInstance().getQueryRunner().query(sql, new BeanHandler<>(PubConfig.class), configCode.getConfigCode());
        } catch (Exception e) {
            logger.error("获取常用配置异常：" + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 保存配置信息
     *
     * @param configCode
     * @param dataContent
     * @return
     */
    public static PubConfig savePubConfig(ConfigCode configCode, String dataContent) {
        if (configCode == null || ObjUtils.isEmpty(dataContent)) {
            return null;
        }
        PubConfig pubConfig = new PubConfig();
        pubConfig.setConfig_code(configCode.getConfigCode());
        pubConfig.setConfig_name(configCode.getConfigName());
        pubConfig.setData_content(dataContent);
        if (savePubConfig(pubConfig)) {
            return pubConfig;
        } else {
            return null;
        }
    }

    /**
     * 保存配置信息
     *
     * @param groupCode
     * @param configCode
     * @param dataContent
     * @return
     */
    public static PubConfig savePubConfig(ConfigGroupCode groupCode, ConfigCode configCode, String dataContent) {
        if (groupCode == null || configCode == null || ObjUtils.isEmpty(dataContent)) {
            return null;
        }
        PubConfig pubConfig = new PubConfig();
        pubConfig.setGroup_code(groupCode.getGroupCode());
        pubConfig.setGroup_name(groupCode.getGroupName());
        pubConfig.setConfig_code(configCode.getConfigCode());
        pubConfig.setConfig_name(configCode.getConfigName());
        pubConfig.setData_content(dataContent);
        if (savePubConfig(pubConfig)) {
            return pubConfig;
        } else {
            return null;
        }
    }

    /**
     * 保存配置信息
     *
     * @param pubConfig
     * @return
     */
    public static boolean savePubConfig(PubConfig pubConfig) {
        try {
            if (pubConfig == null || ObjUtils.isEmpty(pubConfig.getConfig_code())) {
                logger.warn("需要保存的常用配置编码为空，不保存！");
                return false;
            }
            String querySQL = "SELECT * FROM PUB_CONFIG WHERE CONFIG_CODE=? LIMIT 1";
            PubConfig dbPubConfig = getInstance().getQueryRunner().query(querySQL, new BeanHandler<>(PubConfig.class), pubConfig.getConfig_code());
            if (dbPubConfig == null) {
                // 设置默认值
                pubConfig.setData_sort(ObjUtils.ifNull(pubConfig.getData_sort(), System.currentTimeMillis()));
                pubConfig.setData_status(ObjUtils.ifNull(pubConfig.getData_status(), ConfigStatus.ENABLE.getStatus()));
                pubConfig.setCreate_time(new Date());
                String insertSQL = "INSERT INTO PUB_CONFIG(GROUP_CODE, GROUP_NAME, CONFIG_CODE, CONFIG_NAME, DATA_ID, DATA_TYPE, DATA_CONTENT, DATA_SORT, DATA_STATUS, DATA_P1, DATA_P2, DATA_P3, DATA_P4, DATA_P5, CREATE_TIME) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                Object[] rsAry = getInstance().getQueryRunner().insert(insertSQL, new ArrayHandler(),
                        pubConfig.getGroup_code(),
                        pubConfig.getGroup_name(),
                        pubConfig.getConfig_code(),
                        pubConfig.getConfig_name(),
                        pubConfig.getData_id(),
                        pubConfig.getData_type(),
                        pubConfig.getData_content(),
                        pubConfig.getData_sort(),
                        pubConfig.getData_status(),
                        pubConfig.getData_p1(),
                        pubConfig.getData_p2(),
                        pubConfig.getData_p3(),
                        pubConfig.getData_p4(),
                        pubConfig.getData_p5(),
                        pubConfig.getCreate_time());
                Integer id = ObjUtils.toInteger(rsAry[0], 0);
                pubConfig.setId(id);
                return id > 0;
            } else {
                // 如果值没有设置，则取数据库中的值
                pubConfig.setGroup_code(ObjUtils.ifNull(pubConfig.getGroup_code(), dbPubConfig.getGroup_code()));
                pubConfig.setGroup_name(ObjUtils.ifNull(pubConfig.getGroup_name(), dbPubConfig.getGroup_name()));
                pubConfig.setConfig_name(ObjUtils.ifNull(pubConfig.getConfig_name(), dbPubConfig.getConfig_name()));
                pubConfig.setData_id(ObjUtils.ifNull(pubConfig.getData_id(), dbPubConfig.getData_id()));
                pubConfig.setData_type(ObjUtils.ifNull(pubConfig.getData_type(), dbPubConfig.getData_type()));
                pubConfig.setData_content(ObjUtils.ifNull(pubConfig.getData_content(), dbPubConfig.getData_content()));
                pubConfig.setData_sort(ObjUtils.ifNull(pubConfig.getData_sort(), dbPubConfig.getData_sort()));
                pubConfig.setData_status(ObjUtils.ifNull(pubConfig.getData_status(), dbPubConfig.getData_status()));
                pubConfig.setData_p1(ObjUtils.ifNull(pubConfig.getData_p1(), dbPubConfig.getData_p1()));
                pubConfig.setData_p2(ObjUtils.ifNull(pubConfig.getData_p2(), dbPubConfig.getData_p2()));
                pubConfig.setData_p3(ObjUtils.ifNull(pubConfig.getData_p3(), dbPubConfig.getData_p3()));
                pubConfig.setData_p4(ObjUtils.ifNull(pubConfig.getData_p4(), dbPubConfig.getData_p4()));
                pubConfig.setData_p5(ObjUtils.ifNull(pubConfig.getData_p5(), dbPubConfig.getData_p5()));
                pubConfig.setUpdate_time(new Date());
                String updateSQL = "UPDATE PUB_CONFIG SET GROUP_CODE=?, GROUP_NAME=?, CONFIG_NAME=?, DATA_ID=?, DATA_TYPE=?, DATA_CONTENT=?, DATA_SORT=?, DATA_STATUS=?, DATA_P1=?, DATA_P2=?, DATA_P3=?, DATA_P4=?, DATA_P5=? WHERE CONFIG_CODE=?";
                return getInstance().getQueryRunner().update(updateSQL,
                        pubConfig.getGroup_code(),
                        pubConfig.getGroup_name(),
                        pubConfig.getConfig_name(),
                        pubConfig.getData_id(),
                        pubConfig.getData_type(),
                        pubConfig.getData_content(),
                        pubConfig.getData_sort(),
                        pubConfig.getData_status(),
                        pubConfig.getData_p1(),
                        pubConfig.getData_p2(),
                        pubConfig.getData_p3(),
                        pubConfig.getData_p4(),
                        pubConfig.getData_p5(),
                        pubConfig.getConfig_code()) > 0;
            }
        } catch (Exception e) {
            logger.error("获取常用配置异常：" + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 静态内部类实现的懒汉线程安全单例
     */
    private static class SingleH2DbUtils {
        private static DataBaseUtils instance = new DataBaseUtils(
                DbType.H2.getDriver(),
                "jdbc:h2:~/h2/swift;AUTO_SERVER=TRUE;MODE=MySQL",
                "root",
                "root123");
    }

}
