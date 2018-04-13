package com.luastar.swift.tools.utils;

import com.luastar.swift.base.utils.StrUtils;
import com.luastar.swift.tools.model.db.ColAndRsVO;
import com.luastar.swift.tools.model.db.ColumnVO;
import com.luastar.swift.tools.model.db.TableVO;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class DataBaseUtils {

    private static final Logger logger = LoggerFactory.getLogger(DataBaseUtils.class);

    public static final String COLUMN_NAME = "COLUMN_NAME";

    public static final String TYPE_NAME = "TYPE_NAME";

    public static final String COLUMN_SIZE = "COLUMN_SIZE";

    public static final String REMARKS = "REMARKS";

    public static final String DECIMAL_DIGITS = "DECIMAL_DIGITS";

    public static final String DEFAULT_VALUE = "COLUMN_DEF";

    public static final String IS_NULLABLE = "IS_NULLABLE";

    public static final String TABLE = "TABLE";

    public static final String YES = "YES";

    private String dbDriver;

    private String dbUrl;

    private String dbUsername;

    private String dbPassword;

    private DataSource dataSource;

    private QueryRunner queryRunner;

    public DataBaseUtils(String dbDriver, String dbUrl, String dbUsername, String dbPassword) {
        this.dbDriver = dbDriver;
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(this.dbDriver);
        config.setJdbcUrl(this.dbUrl);
        config.setUsername(this.dbUsername);
        config.setPassword(this.dbPassword);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(10);
        config.setAutoCommit(true);
        dataSource = new HikariDataSource(config);
    }

    public QueryRunner getQueryRunner() {
        if (queryRunner == null) {
            queryRunner = new QueryRunner(dataSource);
        }
        return queryRunner;
    }

    public Connection getConn() throws SQLException {
        return dataSource.getConnection();
    }

    public static boolean testConn(String dbDriver, String dbUrl, String dbUsername, String dbPassword, int seconds) {
        Connection conn = null;
        try {
            DbUtils.loadDriver(dbDriver);
            DriverManager.setLoginTimeout(seconds);
            conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            return true;
        } catch (SQLException sqlExp) {
            return false;
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    /**
     * 得到数据库表信息
     *
     * @param table 表名前缀
     * @param needSchema 是否需要匹配模式 注：这个参数主要是因为在使用oracle时，不同用户下有相同表时，会查出所有表的属性，
     *                   为true时，将使用表名的用户进行匹配，过滤掉其他表，一般置为false就行了。
     * @return 返回表信息
     */
    public List<TableVO> getDbTables(String table, boolean needSchema) {
        logger.info("获取数据库所有表信息...");
        Connection conn = null;
        ResultSet rs_table = null;
        List<TableVO> tableList = new ArrayList<TableVO>();
        try {
            conn = getConn();
            DatabaseMetaData dmd = conn.getMetaData();
            // 要获得表所在的编目。串“""”意味着没有任何编目，Null表示所有编目。
            String catalog = getConn().getCatalog();
            // 要获得表所在的模式。串“""”意味着没有任何模式，Null表示所有模式。该参数可以包含单字符的通配符（“_”）,也可以包含多字符的通配符（“%”）。
            String schema = null;
            if (needSchema) {
                schema = dbUsername.toUpperCase();
            }
            if (StringUtils.isNotEmpty(table)) {
                table = "%" + table.toUpperCase() + "%";
            } else {
                table = null;
            }
            rs_table = dmd.getTables(catalog, schema, table, new String[]{TABLE});
            TableVO tbVO = null;
            String temp = null;
            while (rs_table.next()) {
                tbVO = new TableVO();
                temp = rs_table.getString("table_name");
                if (StringUtils.isNotEmpty(temp)) {
                    temp = temp.toUpperCase();
                }
                tbVO.setTableName(temp);
                tbVO.setRemark(rs_table.getString("remarks"));
                tableList.add(tbVO);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        } finally {
            DbUtils.closeQuietly(rs_table);
            DbUtils.closeQuietly(conn);
        }
        return tableList;
    }

    /**
     * 得到数据库表信息
     *
     * @param tableName  表名
     * @param needSchema 是否需要匹配模式 注：这个参数主要是因为在使用oracle时，不同用户下有相同表时，会查出所有表的属性，
     *                   为true时，将使用表名的用户进行匹配，过滤掉其他表，一般置为false就行了。
     * @return 返回表信息
     */
    public TableVO getDbTableInfo(String tableName, boolean needSchema) {

        if (StringUtils.isEmpty(tableName)) {
            return null;
        }
        logger.info("获取数据库表" + tableName + "信息...");
        TableVO tableVO = new TableVO();
        tableVO.setTableName(tableName);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSetMetaData rsmd = null;
        ResultSet prikey = null;
        ResultSet columns = null;
        ResultSet rs = null;
        try {
            conn = getConn();
            DatabaseMetaData dmd = conn.getMetaData();
            // 要获得表所在的编目。串“""”意味着没有任何编目，Null表示所有编目。
            String catalog = getConn().getCatalog();
            // 要获得表所在的模式。串“""”意味着没有任何模式，Null表示所有模式。该参数可以包含单字符的通配符（“_”）,也可以包含多字符的通配符（“%”）。
            String schema = null;
            if (needSchema) {
                schema = dbUsername.toUpperCase();
            }
            prikey = dmd.getPrimaryKeys(catalog, schema, tableName);
            List<String> pkList = new ArrayList<String>();
            while (prikey.next()) {
                pkList.add(prikey.getString(COLUMN_NAME));
            }
            columns = dmd.getColumns(catalog, schema, tableName, null);
            List<ColumnVO> pkColList = new ArrayList<ColumnVO>();
            Map<String, ColumnVO> colMap = new HashMap<String, ColumnVO>();
            ColumnVO colVO = null;
            String colName = null;
            while (columns.next()) {
                colVO = new ColumnVO();
                colName = columns.getString(COLUMN_NAME);
                colVO.setDbColumnName(colName);
                colVO.setColumnName(StrUtils.getCamelCaseString(colName.toLowerCase(), false));
                colVO.setColumnType(columns.getString(TYPE_NAME));
                colVO.setColumnSize(columns.getInt(COLUMN_SIZE));
                colVO.setColumnDecimalDigits(columns.getInt(DECIMAL_DIGITS));
                colVO.setRemark(columns.getString(REMARKS));
                colVO.setDefaultValue(columns.getString(DEFAULT_VALUE));
                if (YES.equalsIgnoreCase(columns.getString(IS_NULLABLE))) {
                    colVO.setNullable(true);
                }
                if (pkList.contains(colName)) {
                    colVO.setPrikey(true);
                    pkColList.add(colVO);
                }
                colMap.put(colName, colVO);
            }
            tableVO.setPrimaryKeys(pkColList);
            // 通过sql语句获取 ResultSetMetaData
            String sql = "select * from " + tableName + " where 1=2";
            stmt = conn.prepareStatement(sql);
            logger.info("执行SQL语句：" + sql);
            rs = stmt.executeQuery();
            rsmd = rs.getMetaData();
            int count = rsmd.getColumnCount();
            List<ColumnVO> allColList = new ArrayList<ColumnVO>();
            for (int i = 0; i < count; i++) {
                colName = rsmd.getColumnName(i + 1);
                colVO = colMap.get(colName);
                if (colVO == null) {
                    colVO = new ColumnVO();
                    colVO.setDbColumnName(colName);
                    colVO.setColumnName(StrUtils.getCamelCaseString(colName.toLowerCase(), false));
                    colVO.setColumnType(rsmd.getColumnTypeName(i + 1));
                    colVO.setColumnSize(rsmd.getPrecision(i + 1));
                    colVO.setColumnDecimalDigits(rsmd.getScale(i + 1));
                    colVO.setColumnDisSize(rsmd.getColumnDisplaySize(i + 1));
                    colVO.setNullable(rsmd.isNullable(i + 1) > 0);
                    if (pkList.contains(colName)) {
                        colVO.setPrikey(true);
                    }
                }
                // 获取显示宽度（mysql int(4) 类型）
                colVO.setColumnDisSize(rsmd.getColumnDisplaySize(i + 1));
                allColList.add(colVO);
            }
            tableVO.setColumns(allColList);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(prikey);
            DbUtils.closeQuietly(columns);
            DbUtils.closeQuietly(conn);
        }
        return tableVO;
    }

    /**
     * 获取DQL语句列信息和结果信息
     *
     * @param sql
     * @return ColAndRsVO
     * @throws SQLException
     */
    public ColAndRsVO queryColAndRs(String sql, Object... params) throws SQLException {

        ColAndRsVO rsVO = new ColAndRsVO();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConn();
            stmt = conn.prepareStatement(sql);
            getQueryRunner().fillStatement(stmt, params);
            logger.info("执行SQL语句：" + sql);
            rs = stmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            // 获取列名
            int count = rsmd.getColumnCount();
            List<ColumnVO> colList = new ArrayList<ColumnVO>();
            for (int i = 0; i < count; i++) {
                ColumnVO col = new ColumnVO();
                String colName = rsmd.getColumnName(i + 1);
                col.setDbColumnName(colName);
                col.setColumnName(StrUtils.getCamelCaseString(colName.toLowerCase(), false));
                col.setColumnType(rsmd.getColumnTypeName(i + 1));
                col.setColumnSize(rsmd.getColumnDisplaySize(i + 1));
                colList.add(col);
            }
            // 获取数据
            List<Map<String, Object>> valueList = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> value = new LinkedHashMap<>();
                for (int i = 0; i < count; i++) {
                    ColumnVO col = colList.get(i);
                    value.put(col.getDbColumnName(), rs.getObject(i + 1));
                }
                valueList.add(value);
            }
            rsVO.setColList(colList);
            rsVO.setValueList(valueList);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(conn);
        }
        return rsVO;
    }

}
