package com.leon.datalink.resource.driver;

import cn.hutool.core.exceptions.ValidateException;
import com.leon.datalink.core.config.ConfigProperties;
import com.leon.datalink.resource.JdbcAbstractDriver;
import org.springframework.util.StringUtils;

public class KingbaseDriver extends JdbcAbstractDriver {

    @Override
    public String url(ConfigProperties properties) {
        if (StringUtils.isEmpty(properties.getString("ip"))) throw new ValidateException();
        if (StringUtils.isEmpty(properties.getString("port"))) throw new ValidateException();
        if (StringUtils.isEmpty(properties.getString("databaseName"))) throw new ValidateException();
        return String.format("jdbc:kingbase8://%s:%s/%s",
                properties.getString("ip"),
                properties.getString("port"),
                properties.getString("databaseName"));
    }

    @Override
    public String driverClassName() {
        return "com.kingbase8.Driver";
    }

    @Override
    public String validationQuery() {
        return "select 1;";
    }

}
