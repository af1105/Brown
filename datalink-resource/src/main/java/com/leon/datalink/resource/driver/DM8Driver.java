package com.leon.datalink.resource.driver;

import cn.hutool.core.exceptions.ValidateException;
import com.leon.datalink.core.config.ConfigProperties;
import com.leon.datalink.resource.JdbcAbstractDriver;
import org.springframework.util.StringUtils;

public class DM8Driver extends JdbcAbstractDriver {

    @Override
    public String url(ConfigProperties properties) {
        if (StringUtils.isEmpty(properties.getString("ip"))) throw new ValidateException();
        if (StringUtils.isEmpty(properties.getString("port"))) throw new ValidateException();
        return String.format("jdbc:dm://%s:%s",
                properties.getString("ip"),
                properties.getString("port"));
    }

    @Override
    public String driverClassName() {
        return "dm.jdbc.driver.DmDriver";
    }

    @Override
    public String validationQuery() {
        return "select 1;";
    }

}
