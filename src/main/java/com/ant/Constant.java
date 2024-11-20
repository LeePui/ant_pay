package com.ant;

/**
 * @description: 配置
 * @className: Constant
 * @author: lee
 **/
public interface Constant {

    // 缓存刷新间隔
    int CACHE_REFRESH_INTERVAL_SECONDS = 10;

    // 远程调用超时时间
    int REMOTE_TIMEOUT_SECONDS = 2;

    // 初始化超时时间
    int INIT_TIMEOUT_SECONDS = 2;

    // 模拟远程调用平均耗时
    int REMOTE_REQUEST_AVG_TIME_MILLIS = 500;

    // 消息主题
    String TOPIC = "payment_method_availability_change";
}
