package com.leon.datalink.resource.driver;

import cn.hutool.core.exceptions.ValidateException;
import com.leon.datalink.core.config.ConfigProperties;
import com.leon.datalink.core.utils.Loggers;
import com.leon.datalink.resource.AbstractDriver;
import com.leon.datalink.resource.constans.DriverModeEnum;
import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModbusTcpDriver extends AbstractDriver {

    private PlcConnection plcConnection;

    @Override
    public void create(DriverModeEnum driverMode, ConfigProperties properties) throws Exception {
        if (StringUtils.isEmpty(properties.getString("ip"))) throw new ValidateException();
        if (StringUtils.isEmpty(properties.getString("port"))) throw new ValidateException();
        if (StringUtils.isEmpty(properties.getString("salve"))) throw new ValidateException();
        if (StringUtils.isEmpty(properties.getString("timeout"))) throw new ValidateException();
        plcConnection = new PlcDriverManager().getConnection(String.format("modbus-tcp:tcp://%s:%s?unit-identifier=%s&request-timeout=%s",
                properties.getString("ip"),
                properties.getString("port"),
                properties.getString("salve"),
                properties.getString("timeout")));
    }

    @Override
    public void destroy(DriverModeEnum driverMode, ConfigProperties properties) throws Exception {
        plcConnection.close();
    }

    @Override
    public boolean test(ConfigProperties properties) {
        if (StringUtils.isEmpty(properties.getString("ip"))) return false;
        if (StringUtils.isEmpty(properties.getString("port"))) return false;
        if (StringUtils.isEmpty(properties.getString("salve"))) return false;
        try {
            PlcConnection plcConnection = new PlcDriverManager().getConnection(String.format("modbus-tcp:tcp://%s:%s?unit-identifier=%s",
                    properties.getString("ip"),
                    properties.getString("port"),
                    properties.getString("salve")));
            return plcConnection.isConnected();
        } catch (PlcConnectionException e) {
            Loggers.DRIVER.error("driver test {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void scheduleTrigger(ConfigProperties properties) {
        List<Map<String, Object>> points = properties.getList("points");
        if (null == points || points.isEmpty()) throw new ValidateException();

        try {
            PlcReadRequest.Builder builder = plcConnection.readRequestBuilder();

            points.forEach(point -> {
                String item = String.format("%s:%s:%s", point.get("func"), point.get("address"), point.get("type"));
                builder.addItem((String) point.get("tag"), item);
            });

            PlcReadResponse response = builder.build().execute().get();
            long timestamp = System.currentTimeMillis();
            List<Map<String, Object>> resultList = response.getFieldNames().stream().map(tag -> {
                Map<String, Object> result = new HashMap<>();
                boolean success = PlcResponseCode.OK.equals(response.getResponseCode(tag));
                result.put("tag", tag);
                result.put("success", success);
                result.put("value", success ? response.getObject(tag) : null);
                result.put("timestamp", timestamp);
                return result;
            }).collect(Collectors.toList());
            String transferType = properties.getString("transferType", "single");
            if ("single".equals(transferType)) {
                resultList.forEach(this::produceData);
            } else {
                produceData(resultList);
            }
        } catch (Exception e) {
            produceDataError(e.getMessage());
            Loggers.DRIVER.error("modbus read error: {}", e.getMessage());
        }
    }

}
