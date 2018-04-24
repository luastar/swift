package com.luastar.swift.tools.controller;

import com.luastar.swift.base.utils.ClassLoaderUtils;
import com.luastar.swift.base.utils.ObjUtils;
import com.luastar.swift.tools.MainUI;
import com.luastar.swift.tools.model.gui.DbConfig;
import com.luastar.swift.tools.utils.H2Utils;
import com.luastar.swift.tools.view.AlertUI;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * 数据源配置 控制器
 */
public class DbConfigController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(DbConfigController.class);

    @FXML
    private TableView<DbConfig> dbTableView;
    @FXML
    private TableColumn<DbConfig, String> dbNameTableColumn;
    @FXML
    private TableColumn<DbConfig, String> dbTypeTableColumn;
    @FXML
    private TableColumn<DbConfig, String> jdbcUrlTableColumn;

    private Stage dialog;
    private DbConfigEditController dbConfigEditController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 设置只能单选
        dbTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        // 双击修改
        dbTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                DbConfig dbConfig = dbTableView.getSelectionModel().getSelectedItem();
                if (dbConfig != null) {
                    initDialog();
                    dbConfigEditController.setDbConfig(dbConfig);
                    dialog.setTitle("修改数据库配置");
                    dialog.show();
                }
            }
        });
        // 列属性和宽度绑定
        dbNameTableColumn.setCellValueFactory(new PropertyValueFactory<>("db_name"));
        dbNameTableColumn.prefWidthProperty().bind(dbTableView.widthProperty().multiply(0.2));
        dbTypeTableColumn.setCellValueFactory(new PropertyValueFactory<>("db_type"));
        dbTypeTableColumn.prefWidthProperty().bind(dbTableView.widthProperty().multiply(0.2));
        jdbcUrlTableColumn.setCellValueFactory(new PropertyValueFactory<>("jdbc_url"));
        jdbcUrlTableColumn.prefWidthProperty().bind(dbTableView.widthProperty().multiply(0.5));
        // 加载数据
        loadDbTableViewItems();
    }

    @FXML
    public void createDbAction() {
        initDialog();
        dbConfigEditController.setDbConfig(new DbConfig());
        dialog.setTitle("新建数据库配置");
        dialog.show();
    }

    @FXML
    public void updateDbAction() {
        DbConfig dbConfig = dbTableView.getSelectionModel().getSelectedItem();
        if (dbConfig == null) {
            AlertUI.warn("请选中需要修改的行！");
            return;
        }
        initDialog();
        dbConfigEditController.setDbConfig(dbConfig);
        dialog.setTitle("修改数据库配置");
        dialog.show();
    }

    @FXML
    public void deleteDbAction() {
        DbConfig dbConfig = dbTableView.getSelectionModel().getSelectedItem();
        if (dbConfig == null) {
            AlertUI.warn("请选中需要删除的行！");
            return;
        }
        Alert confirm = AlertUI.confirm("是否确定删除？");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
            H2Utils.deleteDbConfig(dbConfig.getId());
            loadDbTableViewItems();
        }
    }

    /**
     * 加载/刷新表格数据
     */
    public void loadDbTableViewItems() {
        List<DbConfig> dbConfigList = H2Utils.getDbConfigList();
        if (ObjUtils.isEmpty(dbConfigList)) {
            return;
        }
        dbTableView.getItems().clear();
        dbTableView.getItems().addAll(dbConfigList);
    }

    /**
     * 初始化对话框
     */
    private void initDialog() {
        if (dialog == null) {
            try {
                dialog = new Stage();
                dialog.initOwner(MainUI.getPrimaryStage());
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.setWidth(600);
                dialog.setMaximized(false);
                dialog.setResizable(false);
                FXMLLoader loader = new FXMLLoader(ClassLoaderUtils.getClassLoader().getResource("fxml/db_config_edit.fxml"));
                dialog.setScene(new Scene(loader.load()));
                dbConfigEditController = loader.getController();
                dbConfigEditController.setDbConfigController(this);
            } catch (Exception e) {
                logger.error("加载数据配置界面异常：" + e.getMessage(), e);
            }
        }
    }

    /**
     * 关闭对话框
     */
    public void closeDialog() {
        if (dialog != null) {
            dialog.close();
        }
    }

}
