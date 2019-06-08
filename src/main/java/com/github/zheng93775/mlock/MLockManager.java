package com.github.zheng93775.mlock;

import org.sql2o.Sql2o;

/**
 * MySQL分布式锁资源管理器
 * 存储公共默认配置值，允许配置器覆盖默认值
 *
 * Created by zheng93775 on 2019/6/5.
 */
class MLockManager {
    /**
     * 标志着MySQL分布式锁是否已配置，如果已配置，MLockInitializer默认的配置方式就不工作
     */
    public static volatile boolean initialized = false;

    /**
     * sql2o数据库管理器
     */
    public static Sql2o SQL2O;

    /**
     * MySQL分布式锁表名
     */
    public static String DEFAULT_TABLE_NAME = "tb_distributed_lock";

    /**
     * lock_key字段在数据库表中定义的长度，varchar(x)
     */
    public static int DEFAULT_LOCK_KEY_MAX_LENGTH = 64;

    /**
     * 锁的默认过期时间
     */
    public static int DEFAULT_EXPIRE_SECONDS = 60;

    /**
     * 阻塞式获取锁时，不停地重复尝试获取锁的默认时间间隔
     */
    public static long DEFAULT_TRY_LOCK_INTERVAL_MILLISECONDS = 1000;

    /**
     * 默认的可以同时持有锁的线程数
     */
    public static int DEFAULT_PARALLEL_NUM = 1;

    /**
     * owner标志常量，用于标志是否做过tryLock()操作
     */
    public static String OPERATION_TRY_LOCK = "OPERATION_TRY_LOCK";
}
