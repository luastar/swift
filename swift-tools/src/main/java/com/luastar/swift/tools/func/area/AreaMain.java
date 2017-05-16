package com.luastar.swift.tools.func.area;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.luastar.swift.base.utils.ObjUtils;
import com.luastar.swift.tools.utils.DataBaseUtils;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AreaMain {

    private static final Logger logger = LoggerFactory.getLogger(AreaMain.class);

    public static void main(String[] args) {
        try {
            // 数据库驱动
            String dbDriver = "com.mysql.jdbc.Driver";
            // 数据库链接
            String dbUrl = "jdbc:mysql://10.1.1.4:3306/lajin-huomiao?useUnicode=true&amp;autoReconnect=true&amp;characterEncoding=UTF8";
            // 数据库用户名
            String dbUsername = "root";
            // 数据库密码
            String dbPassword = "lajin2015";
            // 数据库工具
            DataBaseUtils dbUtils = new DataBaseUtils(dbDriver, dbUrl, dbUsername, dbPassword);
            // 查询出地区
            String sql = "select * from PUB_AREA";
            List<Map<String, Object>> areaList = dbUtils.getQueryRunner().query(sql, new MapListHandler());
            if (areaList == null || areaList.isEmpty()) {
                logger.info("area is empty!");
                return;
            }
            String updateSql = "update PUB_AREA set AREA_PY_SHORT=?,AREA_PY=? where ID=?";
            for (Map<String, Object> areaMap : areaList) {
                Integer id = ObjUtils.toInteger(areaMap.get("ID"));
                String areaName = ObjUtils.toString(areaMap.get("AREA_NAME"));
                if (StringUtils.isBlank(areaName)) {
                    continue;
                }
                String pyShort = PinyinHelper.getShortPinyin(areaName).toUpperCase();
                String py = PinyinHelper.convertToPinyinString(areaName, "", PinyinFormat.WITHOUT_TONE).toUpperCase();
                logger.info("转换拼音：{},{},{}", areaName, pyShort, py);
                dbUtils.getQueryRunner().update(updateSql, pyShort, py, id);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } catch (PinyinException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
