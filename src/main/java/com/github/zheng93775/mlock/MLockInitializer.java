package com.github.zheng93775.mlock;

/**
 * MySQL分布式锁默认的初始化触发器
 *
 * 在MLock类的构造函数中触发加载此类
 * 然后判断MySQL分布式锁是否已经使用过别的方式初始化
 * 没有的话使用PropertyMLockConfigurator配置器，加载配置文件mysql-lock.properties
 *
 * Created by zheng93775 on 2019/6/4.
 */
class MLockInitializer {
    public static final String DEFAULT_CONFIGURATION_FILE = "mysql-lock.properties";

    static {
        synchronized (MLockManager.class) {
            if (MLockManager.initialized == false) {
                PropertyMLockConfigurator propertyConfigurator = new PropertyMLockConfigurator(DEFAULT_CONFIGURATION_FILE);
                propertyConfigurator.configure();
            }
        }
    }

    /**
     * 空方法，目的是为了在MLock类的构造函数中触发加载此类，从而执行一次static方法块
     */
    public static void init() {
    }
}
