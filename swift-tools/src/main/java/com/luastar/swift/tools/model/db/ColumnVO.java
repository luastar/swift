package com.luastar.swift.tools.model.db;

import com.luastar.swift.tools.model.PropBaseVO;
import org.apache.commons.lang3.StringUtils;

public class ColumnVO extends PropBaseVO {

    public static final String DB_INT = "INT";
    public static final String DB_INT2 = "INT2"; //postgresql
    public static final String DB_INT4 = "INT4"; //postgresql
    public static final String DB_INT8 = "INT8"; //postgresql
    public static final String DB_TINYINT = "TINYINT";
    public static final String DB_SMALLINT = "SMALLINT";
    public static final String DB_INTEGER = "INTEGER";
    public static final String DB_BIGINT = "BIGINT";
    public static final String DB_SERIAL = "SERIAL"; //postgresql
    public static final String DB_BIGSERIAL = "BIGSERIAL"; //postgresql
    public static final String DB_NUMBER = "NUMBER";
    public static final String DB_NUMERIC = "NUMERIC";
    public static final String DB_DECIMAL = "DECIMAL";
    public static final String DB_CHAR = "CHAR";
    public static final String DB_VARCHAR = "VARCHAR";
    public static final String DB_VARCHAR2 = "VARCHAR2";
    public static final String DB_DATETIME = "DATETIME";
    public static final String DB_TIMESTAMP = "TIMESTAMP";
    public static final String DB_TEXT = "TEXT";

    public static final String JAVA_INT = "Integer";
    public static final String JAVA_LONG = "Long";
    public static final String JAVA_BIGDECIMAL = "BigDecimal";
    public static final String JAVA_STRING = "String";
    public static final String JAVA_DATE = "Date";

    // 数据库字段名
    private String dbColumnName;
    // 属性名
    private String columnName;
    // 数据库字段类型
    private String columnType;
    // java类型
    private String javaType;
    // 字段长度
    private int columnSize;
    // 字段小数位长度
    private int columnDecimalDigits;
    // 字段显示长度(mysql int 特殊)
    private int columnDisSize;
    // 备注
    private String remark;
    // 是否为主键
    private boolean prikey;
    // 是否允许为空
    private boolean nullable;
    // 默认值
    private String defaultValue;

    // get方法名
    private String getMethodName;
    // set方法名
    private String setMethodName;

    public String getDbColumnName() {
        return dbColumnName;
    }

    public void setDbColumnName(String dbColumnName) {
        this.dbColumnName = dbColumnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getJavaType() {
        String colType = getColumnType();
        if (DB_TINYINT.equalsIgnoreCase(colType)
                || DB_SMALLINT.equalsIgnoreCase(colType)
                || DB_INT.equalsIgnoreCase(colType)
                || DB_INTEGER.equalsIgnoreCase(colType)
                || DB_SERIAL.equalsIgnoreCase(colType)) {
            if (getColumnDisSize() > 10) {
                javaType = JAVA_LONG;
            } else {
                javaType = JAVA_INT;
            }
        } else if (DB_INT2.equalsIgnoreCase(colType)
                || DB_INT4.equalsIgnoreCase(colType)) {
            javaType = JAVA_INT;
        } else if (DB_INT8.equalsIgnoreCase(colType)
                || DB_BIGINT.equalsIgnoreCase(colType)
                || DB_BIGSERIAL.equalsIgnoreCase(colType)) {
            javaType = JAVA_LONG;
        } else if (DB_DECIMAL.equalsIgnoreCase(colType)) {
            javaType = JAVA_BIGDECIMAL;
        } else if (DB_NUMBER.equalsIgnoreCase(colType)
                || DB_NUMERIC.equalsIgnoreCase(colType)) {
            if (getColumnDecimalDigits() > 0) {
                javaType = JAVA_BIGDECIMAL;
            } else {
                //javaType = JAVA_INT;
                javaType = JAVA_BIGDECIMAL;
            }
        } else if (DB_DATETIME.equalsIgnoreCase(colType)
                || DB_TIMESTAMP.equalsIgnoreCase(colType)) {
            javaType = JAVA_DATE;
        } else {
            javaType = JAVA_STRING;
        }
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }

    public int getColumnDecimalDigits() {
        return columnDecimalDigits;
    }

    public void setColumnDecimalDigits(int columnDecimalDigits) {
        this.columnDecimalDigits = columnDecimalDigits;
    }

    public int getColumnDisSize() {
        return columnDisSize;
    }

    public void setColumnDisSize(int columnDisSize) {
        this.columnDisSize = columnDisSize;
    }

    public String getRemark() {
        if (remark == null) {
            return "";
        }
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public boolean isPrikey() {
        return prikey;
    }

    public void setPrikey(boolean prikey) {
        this.prikey = prikey;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getGetMethodName() {
        if (StringUtils.isNotEmpty(columnName)) {
            try {
                // getMethodName = BeanUtilsBean.getInstance().getPropertyUtils().getPropertyDescriptor(this, columnName).getReadMethod().getName();
                getMethodName = "get" + columnName.substring(0, 1).toUpperCase().concat(columnName.substring(1));
            } catch (Exception e) {
                getMethodName = "get" + columnName.substring(0, 1).toUpperCase().concat(columnName.substring(1));
            }
        }
        return getMethodName;
    }

    public String getSetMethodName() {
        if (StringUtils.isNotEmpty(columnName)) {
//			try {
//				setMethodName = BeanUtilsBean.getInstance().getPropertyUtils().getPropertyDescriptor(this, columnName).getWriteMethod().getName();
//			} catch (Exception e) {
//				setMethodName = "set" + columnName.substring(0, 1).toUpperCase().concat(columnName.substring(1));
//			} 
            setMethodName = "set" + columnName.substring(0, 1).toUpperCase().concat(columnName.substring(1));
        }
        return setMethodName;
    }

}
