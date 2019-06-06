package com.github.zheng93775.mlock;

import org.sql2o.Sql2o;

/**
 * Created by zheng93775 on 2019/6/5.
 */
class MLockManager {
    public static volatile boolean initialized = false;
    public static Sql2o SQL2O;
    public static String DEFAULT_TABLE_NAME = "tb_distributed_lock";
    public static int DEFAULT_LOCK_KEY_MAX_LENGTH = 64;
    public static int DEFAULT_EXPIRE_SECONDS = 60;
    public static int DEFAULT_PARALLEL_NUM = 1;
}
