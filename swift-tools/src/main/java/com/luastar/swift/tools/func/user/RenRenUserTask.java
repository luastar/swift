package com.luastar.swift.tools.func.user;

import com.google.common.collect.Lists;
import com.luastar.swift.base.net.HttpClientUtils;
import com.luastar.swift.base.net.HttpResult;
import com.luastar.swift.base.threads.ItfRejectable;
import com.luastar.swift.tools.utils.DataBaseUtils;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

public class RenRenUserTask implements Callable<Boolean>, Comparable<RenRenUserTask>, ItfRejectable {

    private static final Logger logger = LoggerFactory.getLogger(RenRenUserTask.class);

    // 数据库驱动
    private String dbDriver = "com.mysql.jdbc.Driver";
    // 数据库链接
    private String dbUrl = "jdbc:mysql://rm-2ze93441r0jlmzrit.mysql.rds.aliyuncs.com:3306/lajin-uc?useUnicode=true&amp;autoReconnect=true&amp;characterEncoding=UTF8";
    // 数据库用户名
    private String dbUsername = "lajin";
    // 数据库密码
    private String dbPassword = "mPi4m8ETENDKPQ";
    // 数据库工具
    private DataBaseUtils dbUtils;

    // 用户id
    private long uid;

    public RenRenUserTask(long uid) {
        this.uid = uid;
        dbUtils = new DataBaseUtils(dbDriver, dbUrl, dbUsername, dbPassword);
    }

    public Boolean call() throws Exception {
        for (int page = 0; page <= 24; page++) {
            String url = "http://page.renren.com/" + uid + "/channel-fanslist?curpage=" + page;
            logger.info("执行uid={},page={}", uid, page);
            HttpResult result = HttpClientUtils.getHttpResult(url);
            if (result.getStatus() != 200 || StringUtils.isEmpty(result.getResult())) {
                if (page == 0) {
                    return false;
                } else {
                    continue;
                }
            }
            List<User> userList = getUserList(result.getResult());
            if (userList == null) {
                if (page == 0) {
                    return false;
                } else {
                    continue;
                }
            }
            for (User user : userList) {
                saveUser(dbUtils, user);
            }
        }
        return true;
    }

    public int compareTo(RenRenUserTask o) {
        return 0;
    }

    public void reject() {
        logger.info("任务被拒绝啦！");
    }

    private static List<User> getUserList(String data) {
        if (StringUtils.isEmpty(data)) {
            return null;
        }
        try {
            Document doc = Jsoup.parse(data);
            Element friend = doc.getElementById("friend");
            if (friend == null) {
                return null;
            }
            Elements friendList = friend.getElementsByClass("p-friend-list");
            if (friendList == null || friendList.isEmpty()) {
                return null;
            }
            Elements liList = friendList.get(0).getElementsByTag("li");
            List<User> userList = Lists.newArrayList();
            for (Element li : liList) {
                Elements nameList = li.getElementsByClass("friend-name");
                if (nameList == null || nameList.isEmpty()) {
                    continue;
                }
                Elements aList = nameList.get(0).getElementsByTag("a");
                if (aList == null || aList.isEmpty()) {
                    continue;
                }
                Element a = aList.get(0);
                String nickName = a.text();
                if (StringUtils.isEmpty(nickName)) {
                    continue;
                }
                if (nickName.length() > 12) {
                    nickName = nickName.substring(0, 12);
                }
                Elements picList = li.getElementsByClass("friend-section");
                if (picList == null || picList.isEmpty()) {
                    continue;
                }
                Elements imgList = picList.get(0).getElementsByTag("img");
                if (imgList == null || imgList.isEmpty()) {
                    continue;
                }
                Element img = imgList.get(0);
                String picUrl = img.attr("src");
                //logger.info("用户信息：{},{}", nickName, picUrl);
                userList.add(new User(nickName, picUrl));
            }
            return userList;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private static void saveUser(DataBaseUtils dbUtils, User user) {
        try {
            if (dbUtils == null || user == null) {
                return;
            }
            String sql = "insert into USER_RENREN(NICK_NAME, PIC_URL, CREATED_TIME)values(?,?,sysdate())";
            dbUtils.getQueryRunner().insert(sql, new ArrayHandler(), user.getNickName(), user.getPicUrl());
            logger.info("保存用户信息成功：{},{}", user.getNickName(), user.getPicUrl());
        } catch (SQLException e) {
            logger.info("保存用户信息失败：{},{}", user.getNickName(), user.getPicUrl());
            logger.warn(e.getMessage());
        }
    }

}
