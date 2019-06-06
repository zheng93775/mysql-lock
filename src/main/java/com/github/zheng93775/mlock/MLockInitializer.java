package com.github.zheng93775.mlock;

/**
 * Created by zheng93775 on 2019/6/4.
 */
class MLockInitializer {
    public static final String DEFAULT_CONFIGURATION_FILE = "mysql-lock.properties";

    static {
        synchronized (MLockManager.class) {
            if (MLockManager.initialized == false) {
                PropertyConfigurator propertyConfigurator = new PropertyConfigurator(DEFAULT_CONFIGURATION_FILE);
                propertyConfigurator.configure();
            }
        }
    }

    public static void init() {
    }
}
