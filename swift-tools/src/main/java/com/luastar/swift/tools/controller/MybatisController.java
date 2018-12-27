package com.luastar.swift.tools.controller;

import com.luastar.swift.base.utils.ObjUtils;
import com.luastar.swift.tools.MainUI;
import com.luastar.swift.tools.enums.ConfigCode;
import com.luastar.swift.tools.enums.ConfigGroupCode;
import com.luastar.swift.tools.enums.DbType;
import com.luastar.swift.tools.func.mybatis.MybatisGen;
import com.luastar.swift.tools.model.db.TableVO;
import com.luastar.swift.tools.model.gui.DbConfig;
import com.luastar.swift.tools.model.gui.PubConfig;
import com.luastar.swift.tools.utils.DataBaseUtils;
import com.luastar.swift.tools.utils.H2Utils;
import com.luastar.swift.tools.view.AlertUI;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * mybatis生成 控制器
 */
public class MybatisController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(MybatisController.class);

    @FXML
    private ChoiceBox<DbConfig> dbChoiceBox;
    @FXML
    private TextField tableTextField;
    @FXML
    private ListView<String> tableListView;
    @FXML
    private TextField outputTextField;
    @FXML
    private TextField modelTextField;
    @FXML
    private TextField daoTextField;
    @FXML
    private CheckBox useDbNameCheckBox;

    private PubConfig dbIdCofig;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 设置数据库列表
        dbChoiceBox.setTooltip(new Tooltip("请先在数据库配置中添加！"));
        List<DbConfig> dbConfigList = H2Utils.getDbConfigList();
        if (ObjUtils.isNotEmpty(dbConfigList)) {
            dbChoiceBox.getItems().addAll(dbConfigList);
        }
        // 选择数据库后保存以备后用
        dbChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            if (dbIdCofig == null
                    || !newValue.getId().equals(ObjUtils.toInteger(dbIdCofig.getData_content()))) {
                // 没有选中数据库或选中了其他数据库
                dbIdCofig = H2Utils.savePubConfig(ConfigGroupCode.mybatis, ConfigCode.mybatis_db_id, ObjUtils.toString(newValue.getId()));
                tableListView.getItems().clear();
            }
        });
        // 设置表可以多选
        tableListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // 设置使用表属性名称提示信息
        useDbNameCheckBox.setTooltip(new Tooltip("选中后会使用数据库表的列名定义类属性，不会使用驼峰"));
        useDbNameCheckBox.setSelected(false);
        // 设置默认配置
        initDefaultValue();
    }

    /**
     * 设置默认值
     */
    private void initDefaultValue() {
        Map<ConfigCode, PubConfig> configMap = H2Utils.getPubConfigMap(ConfigGroupCode.mybatis);
        if (ObjUtils.isEmpty(configMap)) {
            return;
        }
        // 设置默认的数据库
        dbIdCofig = configMap.get(ConfigCode.mybatis_db_id);
        setDbChoiceBoxValue();
        // 设置默认的输出目录
        PubConfig outputConfig = configMap.get(ConfigCode.mybatis_output);
        if (outputConfig != null) {
            outputTextField.setText(outputConfig.getData_content());
        }
        // 设置包名
        PubConfig modelConfig = configMap.get(ConfigCode.mybatis_model_package);
        if (modelConfig != null) {
            modelTextField.setText(modelConfig.getData_content());
        }
        PubConfig daoConfig = configMap.get(ConfigCode.mybatis_dao_package);
        if (daoConfig != null) {
            daoTextField.setText(daoConfig.getData_content());
        }
    }

    /**
     * 设置数据库
     */
    private void setDbChoiceBoxValue() {
        if (dbIdCofig == null) {
            return;
        }
        int defaultDbId = ObjUtils.toInteger(dbIdCofig.getData_content(), 0);
        List<DbConfig> dbConfigList = dbChoiceBox.getItems();
        if (ObjUtils.isNotEmpty(dbConfigList)) {
            for (DbConfig dbConfig : dbConfigList) {
                if (dbConfig.getId() == defaultDbId) {
                    dbChoiceBox.setValue(dbConfig);
                    break;
                }
            }
        }
    }

    @FXML
    public void loadDbConfigAction() {
        dbChoiceBox.getItems().clear();
        List<DbConfig> dbConfigList = H2Utils.getDbConfigList();
        if (ObjUtils.isNotEmpty(dbConfigList)) {
            dbChoiceBox.getItems().addAll(dbConfigList);
        }
        setDbChoiceBoxValue();
    }

    @FXML
    public void loadTableListAction() {
        tableListView.getItems().clear();
        String table = tableTextField.getText();
        DbConfig dbConfig = dbChoiceBox.getSelectionModel().getSelectedItem();
        List<TableVO> tableVOList = new DataBaseUtils(
                DbType.parse(dbConfig.getDb_type()).getDriver(),
                dbConfig.getJdbc_url(),
                dbConfig.getUser_name(),
                dbConfig.getPassword()).getDbTableList(table);
        if (ObjUtils.isNotEmpty(tableVOList)) {
            tableListView.getItems().addAll(tableVOList.stream().map(TableVO::getTableName).collect(Collectors.toList()));
        }
    }

    @FXML
    public void setOutputAction() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedFolder = directoryChooser.showDialog(MainUI.getPrimaryStage());
        if (selectedFolder != null) {
            String dir = selectedFolder.getAbsolutePath();
            outputTextField.setText(dir);
            H2Utils.savePubConfig(ConfigGroupCode.mybatis, ConfigCode.mybatis_output, dir);
        }
    }

    @FXML
    public void genAction() {
        DbConfig dbConfig = dbChoiceBox.getSelectionModel().getSelectedItem();
        List<String> tableList = tableListView.getSelectionModel().getSelectedItems();
        String output = outputTextField.getText();
        String modelPackage = modelTextField.getText();
        Boolean useDbName = useDbNameCheckBox.isSelected();
        // 校验
        StringJoiner msg = new StringJoiner("\n");
        if (ObjUtils.isEmpty(tableList)) {
            msg.add("请选择需要生成的数据库表！");
        }
        if (ObjUtils.isEmpty(output)) {
            msg.add("请选择输出目录！");
        }
        if (ObjUtils.isEmpty(modelPackage)) {
            msg.add("请填写model包路径！");
        }
        String daoPackage = daoTextField.getText();
        if (ObjUtils.isEmpty(daoPackage)) {
            msg.add("请填写dao包路径！");
        }
        if (msg.length() > 0) {
            AlertUI.warn(msg.toString());
            return;
        }
        // 生成文件
        MybatisGen mybatisGen = new MybatisGen(DbType.parse(dbConfig.getDb_type()).getDriver(),
                dbConfig.getJdbc_url(),
                dbConfig.getUser_name(),
                dbConfig.getPassword(),
                output,
                modelPackage,
                daoPackage,
                daoPackage,
                tableList.toArray(new String[tableList.size()]),
                useDbName.toString());
        mybatisGen.gen();
        // 保存最近使用包名
        H2Utils.savePubConfig(ConfigGroupCode.mybatis, ConfigCode.mybatis_model_package, modelPackage);
        H2Utils.savePubConfig(ConfigGroupCode.mybatis, ConfigCode.mybatis_dao_package, daoPackage);
    }

}
