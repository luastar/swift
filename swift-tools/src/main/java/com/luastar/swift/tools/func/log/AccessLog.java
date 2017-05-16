package com.luastar.swift.tools.func.log;

import com.google.common.collect.Lists;
import com.luastar.swift.base.utils.ObjUtils;
import com.luastar.swift.tools.utils.DataBaseUtils;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccessLog {

    private static final Logger logger = LoggerFactory.getLogger(AccessLog.class);

    public static void main(String[] args) {
        try {
            String inFile = "/Users/zhuminghua/Desktop/commentIp2.txt";
            // 数据库驱动
            String dbDriver = "com.mysql.jdbc.Driver";
            // 数据库链接
            String dbUrl = "jdbc:mysql://10.1.1.2:3306/lajin-live?useUnicode=true&amp;autoReconnect=true&amp;characterEncoding=UTF8";
            // 数据库用户名
            String dbUsername = "root";
            // 数据库密码
            String dbPassword = "lajin2015";

            LineIterator lineIterator = FileUtils.lineIterator(new File(inFile), "UTF-8");
            Pattern pattern = Pattern.compile("\"(.*?)\"");
            long index = 0;
            while (lineIterator.hasNext()) {
                index++;
                logger.info("---处理第{}行数据---", index);
                String line = lineIterator.next();
                Matcher matcher = pattern.matcher(line);
                List<String> logList = Lists.newArrayList();
                while (matcher.find()) {
                    logList.add(matcher.group(1));
                }
                Integer uid = ObjUtils.toInteger(logList.get(0), 0);
                String ip = logList.get(1);
                String ua = logList.get(4);
                logger.info("uid={},ip={},ua={}", uid, ip, ua);
                if (0 == uid) {
                    continue;
                }
                DataBaseUtils dbUtils = new DataBaseUtils(dbDriver, dbUrl, dbUsername, dbPassword);
                String sql = "select * from USER_IP where UID=? and USER_IP=? and OP_TYPE=1";
                List<Object[]> rs = dbUtils.getQueryRunner().query(sql, new ArrayListHandler(), uid, ip);
                if (rs == null || rs.isEmpty()) {
                    sql = "insert into USER_IP(UID, USER_IP, USER_AGENT, OP_TYPE, CREATED_TIME) values (?,?,?,1,sysdate())";
                    dbUtils.getQueryRunner().insert(sql, new ArrayHandler(), uid, ip, ua);
                } else {
                    logger.info("user ip exists!");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
