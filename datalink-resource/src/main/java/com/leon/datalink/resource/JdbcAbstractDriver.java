package com.leon.datalink.resource;

import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.db.Entity;
import cn.hutool.db.handler.EntityListHandler;
import cn.hutool.db.sql.SqlExecutor;
import com.alibaba.druid.pool.DruidDataSource;
import com.leon.datalink.core.config.ConfigProperties;
import com.leon.datalink.core.utils.Loggers;
import com.leon.datalink.resource.constans.DriverModeEnum;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.List;

/**
 * JDBC驱动基础类
 */
public abstract class JdbcAbstractDriver extends AbstractDriver {

    private DruidDataSource dataSource;

    @Override
    public void create(DriverModeEnum driverMode, ConfigProperties properties) throws Exception {
        if (StringUtils.isEmpty(properties.getString("username"))) throw new ValidateException();
        if (StringUtils.isEmpty(properties.getString("password"))) throw new ValidateException();

        DruidDataSource dataSource = new DruidDataSource(); // 创建Druid连接池
        dataSource.setDriverClassName(driverClassName());
        dataSource.setUrl(url(properties)); // 设置数据库的连接地址
        dataSource.setUsername(properties.getString("username")); // 数据库的用户名
        dataSource.setPassword(properties.getString("password")); // 数据库的密码
        dataSource.setInitialSize(properties.getInteger("initSize", 8)); // 设置连接池的初始大小
        dataSource.setMinIdle(properties.getInteger("minIdle", 1)); // 设置连接池大小的下限
        dataSource.setMaxActive(properties.getInteger("maxActive", 20)); // 设置连接池大小的上限
        dataSource.setValidationQuery(validationQuery());
        this.dataSource = dataSource;
    }


    @Override
    public void destroy(DriverModeEnum driverMode, ConfigProperties properties) throws Exception {
        dataSource.close();
    }

    @Override
    public boolean test(ConfigProperties properties) {
        if (StringUtils.isEmpty(properties.getString("username"))) return false;
        if (StringUtils.isEmpty(properties.getString("password"))) return false;

        try {
            Class.forName(driverClassName());
            DriverManager.getConnection(url(properties), properties.getString("username"), properties.getString("password"));
            return true;
        } catch (Exception e) {
            Loggers.DRIVER.error("jdbc driver test {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void scheduleTrigger(ConfigProperties properties) throws Exception {
        String sql = properties.getString("sql");
        if (StringUtils.isEmpty(sql)) throw new ValidateException();

        List<Entity> result = null;
        try (Connection connection = dataSource.getConnection()) {
            if (connection != null) {
                String render = this.templateAnalysis(sql, getVariable(null));
                if (!StringUtils.isEmpty(render)) sql = render;
                result = SqlExecutor.query(connection, sql, new EntityListHandler());
            }
        } catch (Exception e) {
            Loggers.DRIVER.error("jdbc driver error {}", e.getMessage());
            throw e;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("sql", sql);
        map.put("result", result);
        produceData(map);
    }

    @Override
    public Object handleData(Object data, ConfigProperties properties) throws Exception {
        String sql = properties.getString("sql");
        if (StringUtils.isEmpty(sql)) throw new ValidateException();
        Boolean result = null;
        try (Connection connection = dataSource.getConnection()) {
            if (connection != null) {
                String render = this.templateAnalysis(sql, getVariable(data));
                if (!StringUtils.isEmpty(render)) sql = render;
                result = connection.createStatement().execute(sql);
            }
        } catch (Exception e) {
            Loggers.DRIVER.error("jdbc driver error {}", e.getMessage());
            throw e;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("sql", sql);
        map.put("result", result);
        return map;
    }


    public abstract String url(ConfigProperties properties);

    public abstract String driverClassName();

    public abstract String validationQuery();


}
