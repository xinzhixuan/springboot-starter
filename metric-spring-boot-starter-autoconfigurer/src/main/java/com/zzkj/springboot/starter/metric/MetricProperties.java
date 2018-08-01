package com.zzkj.springboot.starter.metric;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author xinzhixuan
 * @version V1.0
 * @date 2018/8/1 11:36
 */
@ConfigurationProperties(prefix = "spring.metric")
public class MetricProperties {
    private boolean enable = false;
    private String metricScanPackage;
    private String influxdbIp;
    private Integer influxdbPort;
    private String influxdbDatabase;
    private String influxdbUsername;
    private String influxdbPassword;
    private String filter;
    private List<String> excludeFilters;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getMetricScanPackage() {
        return metricScanPackage;
    }

    public void setMetricScanPackage(String metricScanPackage) {
        this.metricScanPackage = metricScanPackage;
    }

    public String getInfluxdbIp() {
        return influxdbIp;
    }

    public void setInfluxdbIp(String influxdbIp) {
        this.influxdbIp = influxdbIp;
    }

    public Integer getInfluxdbPort() {
        return influxdbPort;
    }

    public void setInfluxdbPort(Integer influxdbPort) {
        this.influxdbPort = influxdbPort;
    }

    public String getInfluxdbDatabase() {
        return influxdbDatabase;
    }

    public void setInfluxdbDatabase(String influxdbDatabase) {
        this.influxdbDatabase = influxdbDatabase;
    }

    public String getInfluxdbUsername() {
        return influxdbUsername;
    }

    public void setInfluxdbUsername(String influxdbUsername) {
        this.influxdbUsername = influxdbUsername;
    }

    public String getInfluxdbPassword() {
        return influxdbPassword;
    }

    public void setInfluxdbPassword(String influxdbPassword) {
        this.influxdbPassword = influxdbPassword;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public List<String> getExcludeFilters() {
        return excludeFilters;
    }

    public void setExcludeFilters(List<String> excludeFilters) {
        this.excludeFilters = excludeFilters;
    }
}
