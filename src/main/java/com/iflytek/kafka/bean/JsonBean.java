package com.iflytek.kafka.bean;

import java.io.Serializable;

/**
 * @author cyh
 * @Date 15:45 2019/7/30
 * @description
 * @since 2.0
 */
public class JsonBean implements Serializable {
    private static final long serialVersionUID = 6920498662321737422L;
    private String threadName;//线程名
    private String level;//日志级别
    private Long logTime;//日志打印时间
    private String logCaller;//打印日志服务名
    private String appLog;//日志内容
    private String hostIp;//ip地址
    private String appName;//应用名称/实例标识
    private String orderId;//日志打印顺序
    private String systemCode;//系统标识

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Long getLogTime() {
        return logTime;
    }

    public void setLogTime(Long logTime) {
        this.logTime = logTime;
    }

    public String getLogCaller() {
        return logCaller;
    }

    public void setLogCaller(String logCaller) {
        this.logCaller = logCaller;
    }

    public String getAppLog() {
        return appLog;
    }

    public void setAppLog(String appLog) {
        this.appLog = appLog;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getSystemCode() {
        return systemCode;
    }

    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }
}
