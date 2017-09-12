package com.luastar.swift.tools.func.mybatis;

import com.google.common.collect.Lists;
import com.luastar.swift.base.utils.CollectionUtils;
import com.luastar.swift.tools.func.mybatis.ext.JavaTypeResolverImpl;
import com.luastar.swift.tools.func.mybatis.ext.MybatisLimitPlugin;
import com.luastar.swift.tools.model.ColumnVO;
import com.luastar.swift.tools.model.TableVO;
import com.luastar.swift.tools.utils.DataBaseUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.config.*;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.NullProgressCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MybatisGen {

    private static final Logger logger = LoggerFactory.getLogger(MybatisGen.class);

    private String dbType;
    private String driverClass;
    private String connectionURL;
    private String userId;
    private String password;

    private String output;
    private String modelPackage;
    private String daoPackage;
    private String xmlPackage;

    private String[] tableNameArray;

    private String useActualColumnNames = "false";

    private DataBaseUtils dataBaseUtils;

    public MybatisGen(String dbType,
                      String driverClass,
                      String connectionURL,
                      String userId,
                      String password,
                      String output,
                      String modelPackage,
                      String daoPackage,
                      String xmlPackage,
                      String[] tableNameArray,
                      String useActualColumnNames) {
        if (StringUtils.isEmpty(dbType)
                || StringUtils.isEmpty(driverClass)
                || StringUtils.isEmpty(connectionURL)
                || StringUtils.isEmpty(userId)
                || StringUtils.isEmpty(output)
                || StringUtils.isEmpty(modelPackage)
                || StringUtils.isEmpty(daoPackage)
                || StringUtils.isEmpty(xmlPackage)
                || ArrayUtils.isEmpty(tableNameArray)) {
            logger.warn("参数不能为空！");
            throw new IllegalArgumentException("参数不能为空！");
        }
        this.dbType = dbType;
        this.driverClass = driverClass;
        this.connectionURL = connectionURL;
        this.userId = userId;
        this.password = password;
        this.output = output;
        this.modelPackage = modelPackage;
        this.daoPackage = daoPackage;
        this.xmlPackage = xmlPackage;
        this.tableNameArray = tableNameArray;
        this.useActualColumnNames = useActualColumnNames;
        this.dataBaseUtils = new DataBaseUtils(this.driverClass, this.connectionURL, this.userId, this.password);
    }

    /**
     * 获取插件列表
     *
     * @return
     */
    protected List<PluginConfiguration> getPluginConfigurationList() {
        List<PluginConfiguration> pluginConfigurationList = Lists.newArrayList();
        // limit plugin
        PluginConfiguration limitPlugin = new PluginConfiguration();
        limitPlugin.setConfigurationType(MybatisLimitPlugin.class.getName());
        limitPlugin.addProperty("dbType", dbType);
        pluginConfigurationList.add(limitPlugin);
        return pluginConfigurationList;
    }

    /**
     * 注释配置
     *
     * @return
     */
    protected CommentGeneratorConfiguration getCommentGeneratorConfiguration() {
        CommentGeneratorConfiguration commentGeneratorConfiguration = new CommentGeneratorConfiguration();
        // 去掉注释
        // commentGeneratorConfiguration.addProperty("suppressAllComments", "true");
        // 去掉生成日期
        commentGeneratorConfiguration.addProperty("suppressDate", "true");
        // 使用数据库备注作为注释
        commentGeneratorConfiguration.addProperty("addRemarkComments", "true");
        return commentGeneratorConfiguration;
    }

    /**
     * 获取jDBCConnection配置
     *
     * @return
     */
    protected JDBCConnectionConfiguration getJDBCConnectionConfiguration() {
        JDBCConnectionConfiguration jdbcConnectionConfiguration = new JDBCConnectionConfiguration();
        jdbcConnectionConfiguration.setDriverClass(driverClass);
        jdbcConnectionConfiguration.setConnectionURL(connectionURL);
        jdbcConnectionConfiguration.setUserId(userId);
        jdbcConnectionConfiguration.setPassword(password);
        return jdbcConnectionConfiguration;
    }

    /**
     * @return
     */
    protected JavaTypeResolverConfiguration getJavaTypeResolverConfiguration() {
        JavaTypeResolverConfiguration javaTypeResolverConfiguration = new JavaTypeResolverConfiguration();
        javaTypeResolverConfiguration.setConfigurationType(JavaTypeResolverImpl.class.getName());
        javaTypeResolverConfiguration.addProperty("forceBigDecimals", "false");
        return javaTypeResolverConfiguration;
    }

    /**
     * @return
     */
    protected JavaModelGeneratorConfiguration getJavaModelGeneratorConfiguration() {
        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();
        javaModelGeneratorConfiguration.setTargetPackage(modelPackage);
        javaModelGeneratorConfiguration.setTargetProject(output);
        // 自动为每一个生成的类创建一个构造方法，构造方法包含了所有的field；而不是使用setter；
        javaModelGeneratorConfiguration.addProperty("constructorBased", "false");
        // 在targetPackage的基础上，根据数据库的schema再生成一层package，最终生成的类放在这个package下，默认为false
        javaModelGeneratorConfiguration.addProperty("enableSubPackages", "false");
        // 是否创建一个不可变的类，如果为true， 那么MBG会创建一个没有setter方法的类，取而代之的是类似constructorBased的类
        javaModelGeneratorConfiguration.addProperty("immutable", "false");
        // 设置是否在getter方法中，对String类型字段调用trim()方法
        javaModelGeneratorConfiguration.addProperty("trimStrings", "true");
        // 设置一个根对象
        // javaModelGeneratorConfiguration.addProperty("rootClass","");
        return javaModelGeneratorConfiguration;
    }

    /**
     * @return
     */
    protected SqlMapGeneratorConfiguration getSqlMapGeneratorConfiguration() {
        SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = new SqlMapGeneratorConfiguration();
        sqlMapGeneratorConfiguration.setTargetPackage(xmlPackage);
        sqlMapGeneratorConfiguration.setTargetProject(output);
        sqlMapGeneratorConfiguration.addProperty("enableSubPackages", "false");
        return sqlMapGeneratorConfiguration;
    }

    /**
     * @return
     */
    protected JavaClientGeneratorConfiguration getJavaClientGeneratorConfiguration() {
        JavaClientGeneratorConfiguration javaClientGeneratorConfiguration = new JavaClientGeneratorConfiguration();
        javaClientGeneratorConfiguration.setTargetPackage(daoPackage);
        javaClientGeneratorConfiguration.setTargetProject(output);
        javaClientGeneratorConfiguration.setConfigurationType("XMLMAPPER");
        javaClientGeneratorConfiguration.addProperty("enableSubPackages", "false");
        return javaClientGeneratorConfiguration;
    }

    protected List<TableConfiguration> getTableConfigurationList(Context context) {
        List<TableConfiguration> tableConfigurationList = Lists.newArrayList();
        // table里面很多参数都是对javaModelGenerator，context等元素的默认属性的一个复写
        for (String tableName : tableNameArray) {
            TableConfiguration tableConfig = new TableConfiguration(context);
            tableConfig.setTableName(tableName);
            // tableConfig.setDomainObjectName();
            // 参考 javaModelGenerator 的 constructorBased属性
            tableConfig.addProperty("constructorBased", "false");
            // 默认为false，如果设置为true，在生成的SQL中，table名字不会加上catalog或schema
            tableConfig.addProperty("ignoreQualifiersAtRuntime", "false");
            // 参考 javaModelGenerator 的 immutable 属性
            tableConfig.addProperty("immutable", "false");
            // 指定是否只生成domain类，如果设置为true，只生成domain类，如果还配置了sqlMapGenerator，那么在mapper XML文件中，只生成resultMap元素
            tableConfig.addProperty("modelOnly", "false");
            // 如果设置为true，生成的model类会直接使用column本身的名字，而不会再使用驼峰命名方法，比如BORN_DATE，生成的属性名字就是BORN_DATE,而不会是bornDate
            tableConfig.addProperty("useActualColumnNames", useActualColumnNames);
            // 自增插入时返回主键值
            TableVO tableVO = dataBaseUtils.getDbTableInfo(tableName, false);
            ColumnVO primaryKey = tableVO.getPrimaryKey();
            if (primaryKey != null){
                if (ColumnVO.JAVA_INT.equalsIgnoreCase(primaryKey.getJavaType())
                        || ColumnVO.JAVA_LONG.equalsIgnoreCase(primaryKey.getJavaType())){
                    GeneratedKey generatedKey = new GeneratedKey(primaryKey.getDbColumnName(), "JDBC", true, "post");
                    tableConfig.setGeneratedKey(generatedKey);
                }
            }
            // 属性覆盖
            List<ColumnVO> columnVOList = tableVO.getColumns();
            if (CollectionUtils.isNotEmpty(columnVOList)) {
                columnVOList.stream().filter(columnVO ->
                        columnVO.getColumnType().toUpperCase().contains("TEXT")
                ).map(columnVO -> {
                    ColumnOverride overrideConfig = new ColumnOverride(columnVO.getDbColumnName());
                    overrideConfig.setJdbcType("VARCHAR");
                    return overrideConfig;
                }).collect(Collectors.toList()).forEach(override -> tableConfig.addColumnOverride(override));
            }
            tableConfigurationList.add(tableConfig);
        }
        return tableConfigurationList;
    }

    public void gen() {
        try {
            Configuration configuration = new Configuration();
            // context
            Context context = new Context(ModelType.FLAT);
            context.setId(dbType);
            /*
                MyBatis3：默认的值，生成基于MyBatis3.x以上版本的内容，包括XXXBySample；
                MyBatis3Simple：类似MyBatis3，只是不生成XXXBySample；
             */
            context.setTargetRuntime("MyBatis3");
            // 自动识别数据库关键字，默认false，如果设置为true
            context.addProperty("autoDelimitKeywords", "false");
            // 生成的Java文件的编码
            context.addProperty("javaFileEncoding", "UTF-8");
            // 格式化java代码
            context.addProperty("javaFormatter", "org.mybatis.generator.api.dom.DefaultJavaFormatter");
            // 格式化XML代码
            context.addProperty("xmlFormatter", "org.mybatis.generator.api.dom.DefaultXmlFormatter");
            // 指明数据库的用于标记数据库对象名的符号，比如ORACLE就是双引号，MYSQL默认是`反引号；
            context.addProperty("beginningDelimiter", "`");
            context.addProperty("endingDelimiter", "`");
            // plugin
            List<PluginConfiguration> pluginConfigurationList = getPluginConfigurationList();
            for (PluginConfiguration pluginConfiguration : pluginConfigurationList) {
                context.addPluginConfiguration(pluginConfiguration);
            }
            // commentGenerator
            context.setCommentGeneratorConfiguration(getCommentGeneratorConfiguration());
            // jdbcConnection
            context.setJdbcConnectionConfiguration(getJDBCConnectionConfiguration());
            // javaTypeResolver
            context.setJavaTypeResolverConfiguration(getJavaTypeResolverConfiguration());
            // javaModelGenerator
            context.setJavaModelGeneratorConfiguration(getJavaModelGeneratorConfiguration());
            // sqlMapGenerator
            context.setSqlMapGeneratorConfiguration(getSqlMapGeneratorConfiguration());
            // javaClientGenerator
            context.setJavaClientGeneratorConfiguration(getJavaClientGeneratorConfiguration());
            // table
            List<TableConfiguration> tableConfigurationList = getTableConfigurationList(context);
            for (TableConfiguration tableConfiguration : tableConfigurationList) {
                context.addTableConfiguration(tableConfiguration);
            }
            configuration.addContext(context);
            // generate
            List<String> warnings = new ArrayList<String>();
            ShellCallback shellCallback = new DefaultShellCallback(true);
            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(configuration, shellCallback, warnings);
            myBatisGenerator.generate(new NullProgressCallback());
            logger.info("gen success, see {}", output);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
