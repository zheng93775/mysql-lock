package com.github.zheng93775.mlock;

import org.sql2o.Sql2o;

/**
 * Created by zheng93775 on 2019/6/5.
 */
public abstract class BaseConfigurator implements Configurator {

    private String tableName;
    private Integer lockKeyMaxLength;
    private Integer expireSeconds;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getLockKeyMaxLength() {
        return lockKeyMaxLength;
    }

    public void setLockKeyMaxLength(Integer lockKeyMaxLength) {
        this.lockKeyMaxLength = lockKeyMaxLength;
    }

    public Integer getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(Integer expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    @Override
    public void configure() {
        synchronized (MLockManager.class) {
            Sql2o sql2o = this.doConfigure();
            if (sql2o != null) {
                MLockManager.SQL2O = sql2o;
                if (tableName != null) {
                    MLockManager.DEFAULT_TABLE_NAME = this.tableName;
                }
                if (lockKeyMaxLength != null) {
                    MLockManager.DEFAULT_LOCK_KEY_MAX_LENGTH = this.lockKeyMaxLength;
                }
                if (expireSeconds != null) {
                    MLockManager.DEFAULT_EXPIRE_SECONDS = expireSeconds;
                }
                MLockManager.initialized = true;
            }
        }
    }

    protected abstract Sql2o doConfigure();

}
