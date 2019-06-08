package com.github.zheng93775.mlock;

import org.sql2o.Sql2o;

/**
 * MySQL分布式锁配置类的基类
 * 可以通过注入属性值，更改公共默认配置
 *
 * Created by zheng93775 on 2019/6/5.
 */
public abstract class BaseMLockConfigurator implements MLockConfigurator {

    /**
     * 分布式锁表名
     */
    private String tableName;
    /**
     * lockKey字段支持的最大长度
     */
    private Integer lockKeyMaxLength;
    /**
     * 允许持有锁的最长时间，单位为秒，过期之后可能会被别的执行线程抢占
     */
    private Integer expireSeconds;
    /**
     * 阻塞式获取锁的时候，如果获取不到锁，会一直不断尝试，这个配置是尝试的间隔
     */
    private Long tryLockInterval;

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

    public Long getTryLockInterval() {
        return tryLockInterval;
    }

    public void setTryLockInterval(Long tryLockInterval) {
        this.tryLockInterval = tryLockInterval;
    }

    /**
     * MySQL分布式锁配置类对外提供加载配置的方法
     * 先触发子类的doConfigure()，然后记录数据库配置及公共配置项
     */
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
                if (tryLockInterval != null) {
                    MLockManager.DEFAULT_TRY_LOCK_INTERVAL_MILLISECONDS = tryLockInterval;
                }
                MLockManager.initialized = true;
            }
        }
    }

    /**
     * 允许不同的配置子类自行实现初始化配置的方式
     * @return
     */
    protected abstract Sql2o doConfigure();

}
