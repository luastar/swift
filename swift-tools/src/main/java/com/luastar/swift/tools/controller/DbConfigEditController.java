package com.luastar.swift.tools.controller;

import com.alibaba.fastjson.JSON;
import com.luastar.swift.base.utils.ObjUtils;
import com.luastar.swift.tools.enums.DbType;
import com.luastar.swift.tools.model.gui.DbConfig;
import com.luastar.swift.tools.utils.DataBaseUtils;
import com.luastar.swift.tools.utils.H2Utils;
import com.luastar.swift.tools.view.AlertUI;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.StringJoiner;

/**
 * 数据源编辑 控制器
 */
public class DbConfigEditController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(DbConfigEditController.class);

    private DbConfigController dbConfigController;

    @FXML
    private TextField idTextField;
    @FXML
    private ChoiceBox<String> dbTypeChoiceBox;
    @FXML
    private TextField dbNameTextField;
    @FXML
    private TextField jdbcUrlTextField;
    @FXML
    private TextField userNameTextField;
    @FXML
    private TextField passwordTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化数据库类型列表
        dbTypeChoiceBox.getItems().addAll(DbType.getDbList());
    }

    public void setDbConfigController(DbConfigController dbConfigController) {
        this.dbConfigController = dbConfigController;
    }

    /**
     * 设置界面数据
     *
     * @param dbConfig
     */
    public void setDbConfig(DbConfig dbConfig) {
        idTextField.setText(ObjUtils.toString(dbConfig.getId()));
        dbTypeChoiceBox.setValue(ObjUtils.toString(dbConfig.getDb_type(), DbType.MySQL.getName()));
        dbNameTextField.setText(dbConfig.getDb_name());
        jdbcUrlTextField.setText(dbConfig.getJdbc_url());
        userNameTextField.setText(dbConfig.getUser_name());
        passwordTextField.setText(dbConfig.getPassword());
    }

    /**
     * 获取界面数据
     *
     * @return
     */
    public DbConfig getDbConfig() {
        DbConfig dbConfig = new DbConfig();
        dbConfig.setId(ObjUtils.toInteger(idTextField.getText()));
        dbConfig.setDb_type(dbTypeChoiceBox.getValue());
        dbConfig.setDb_name(dbNameTextField.getText());
        dbConfig.setJdbc_url(jdbcUrlTextField.getText());
        dbConfig.setUser_name(userNameTextField.getText());
        dbConfig.setPassword(passwordTextField.getText());
        logger.info("获取数据库配置信息：{}", JSON.toJSONString(dbConfig));
        return dbConfig;
    }

    /**
     * 校验数据
     *
     * @param dbConfig
     * @return
     */
    private String checkDbConfig(DbConfig dbConfig) {
        if (dbConfig == null) {
            return "请填写正确的数据！";
        }
        StringJoiner msg = new StringJoiner("\n");
        DbType dbType = DbType.parse(dbConfig.getDb_type());
        if (dbType == null) {
            msg.add("请选择数据库类型;");
        }
        if (ObjUtils.isEmpty(dbConfig.getJdbc_url())) {
            msg.add("请填写数据库链接;");
        }
        if (ObjUtils.isEmpty(dbConfig.getUser_name())) {
            msg.add("请填写数据库用户名;");
        }
        return msg.toString();
    }


    @FXML
    public void testDbConfigAction() {
        DbConfig dbConfig = getDbConfig();
        String checkMsg = checkDbConfig(dbConfig);
        if (ObjUtils.isNotEmpty(checkMsg)) {
            AlertUI.warn(checkMsg.toString());
            return;
        }
        boolean isOK = DataBaseUtils.testConn(DbType.parse(dbConfig.getDb_type()).getDriver(),
                dbConfig.getJdbc_url(), dbConfig.getUser_name(), dbConfig.getPassword(), 2);
        if (isOK) {
            AlertUI.info("数据库连接成功！");
        } else {
            AlertUI.error("数据库连接失败！");
        }
    }

    @FXML
    public void saveDbConfigAction() {
        DbConfig dbConfig = getDbConfig();
        String checkMsg = checkDbConfig(dbConfig);
        if (ObjUtils.isNotEmpty(checkMsg)) {
            AlertUI.warn(checkMsg.toString());
            return;
        }
        if (dbConfig.getId() == null) {
            H2Utils.insertDbConfig(dbConfig);
        } else {
            H2Utils.updateDbConfig(dbConfig);
        }
        dbConfigController.closeDialog();
        dbConfigController.loadDbTableViewItems();
    }

}
