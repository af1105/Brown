package com.leon.datalink.resource.driver;

import cn.hutool.core.exceptions.ValidateException;
import com.leon.datalink.core.config.ConfigProperties;
import com.leon.datalink.resource.JdbcAbstractDriver;
import org.springframework.util.StringUtils;

public class TDengineDriver extends JdbcAbstractDriver {

    @Override
    public String url(ConfigProperties properties) {
        if (StringUtils.isEmpty(properties.getString("ip"))) throw new ValidateException();
        if (StringUtils.isEmpty(properties.getString("port"))) throw new ValidateException();
        if (StringUtils.isEmpty(properties.getString("databaseName"))) throw new ValidateException();
        return String.format("jdbc:TAOS-RS://%s:%s/%s",
                properties.getString("ip"),
                properties.getString("port"),
                properties.getString("databaseName"));
    }

    @Override
    public String driverClassName() {
        return "com.taosdata.jdbc.rs.RestfulDriver";
    }

    @Override
    public String validationQuery() {
        return "select server_status();";
    }

}
