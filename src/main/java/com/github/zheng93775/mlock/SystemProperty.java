package com.github.zheng93775.mlock;

import com.mysql.jdbc.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * System.getProperty()的包装类
 * 无对应配置的时候不抛出异常，返回默认值或者null
 *
 * Created by zheng93775 on 2019/6/4.
 */
class SystemProperty {
    private static final Logger logger = LoggerFactory.getLogger(SystemProperty.class);

    public static String getString(String key) {
        return getString(key, null);
    }

    public static String getString(String key, String defaultValue) {
        try {
            String value = System.getProperty(key);
            if (StringUtils.isNullOrEmpty(value)) {
                return defaultValue;
            }
            return value;
        } catch(Throwable e) { // MS-Java throws com.ms.security.SecurityExceptionEx
            logger.debug("Was not allowed to read system property \""+key+"\".");
            return defaultValue;
        }
    }

    public static Boolean getBoolean(String key) {
        return getBoolean(key, null);
    }

    public static Boolean getBoolean(String key, Boolean defaultValue) {
        try {
            String value = System.getProperty(key);
            if (StringUtils.isNullOrEmpty(value)) {
                return defaultValue;
            }
            return Boolean.valueOf(value);
        } catch(Throwable e) { // MS-Java throws com.ms.security.SecurityExceptionEx
            logger.debug("Was not allowed to read system property \""+key+"\".");
            return defaultValue;
        }
    }
}
