package com.github.zheng93775.mlock;

/**
 * MySQL分布式锁配置器初始化失败会抛出此异常
 *
 * Created by Zheng on 2019/6/7.
 */
public class MysqlLockInitException extends RuntimeException {
    public MysqlLockInitException() {
        super();
    }

    public MysqlLockInitException(String message) {
        super(message);
    }

    public MysqlLockInitException(String message, Throwable cause) {
        super(message, cause);
    }
}
