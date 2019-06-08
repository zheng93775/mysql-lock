package com.github.zheng93775.mlock;

import org.sql2o.StatementRunnableWithResult;

/**
 * MySQL分布式锁，负责与数据库进行sql交互的DAO类
 *
 * Created by zheng93775 on 2019/6/5.
 */
class MLockDao {
    /**
     * 延迟加载的单例模式，用于判断是否已经初始化
     */
    private static volatile boolean initialized = false;
    /**
     * 唯一单例
     */
    private static MLockDao instance;

    private MLockDao() {
    }

    /**
     * 获取单例
     * @return
     */
    public static MLockDao getInstance() {
        if (instance == null) {
            synchronized (MLockDao.class) {
                if (initialized == false) {
                    MLockDao newInstance = new MLockDao();
                    newInstance.initTable();
                    newInstance.clean();
                    instance = newInstance;
                    initialized = true;
                }
            }
        }
        return instance;
    }

    /**
     * 初始化分布式锁的表
     * 如果表已经存在，不会对已有的表产生影响
     */
    private void initTable() {
        String sql =
                "CREATE TABLE IF NOT EXISTS `" + MLockManager.DEFAULT_TABLE_NAME + "` ( " +
                "  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID', " +
                "  `lock_key` varchar(" + MLockManager.DEFAULT_LOCK_KEY_MAX_LENGTH + ") NOT NULL COMMENT '锁Key', " +
                "  `owner` char(36) NOT NULL COMMENT '锁的持有者', " +
                "  `expire_seconds` int(11) NOT NULL COMMENT '过期时间，单位为秒', " +
                "  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                "  PRIMARY KEY (`id`), " +
                "  UNIQUE KEY `ukey_lock_key_owner` (`lock_key`,`owner`) " +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分布式锁' ";
        MLockManager.SQL2O.runInTransaction((connection, argument) -> {
            connection.createQuery(sql).executeUpdate();
        });
    }

    /**
     * 尝试清除进程异常退出未正常释放的过期锁
     * 理论上发生的概率很小，不清除也不会产生大影响
     * 只在启动的时候进行这一操作
     */
    private void clean() {
        String sql = "delete from " + MLockManager.DEFAULT_TABLE_NAME + " where expire_seconds < TIMESTAMPDIFF(SECOND, create_time, NOW()) ";
        MLockManager.SQL2O.runInTransaction((connection, argument) -> {
            connection.createQuery(sql).executeUpdate();
        });
    }

    /**
     * 插入一条记录，标志着占有锁
     *
     * @param lockKey 锁唯一标识
     * @param owner 锁持有者
     * @param expireSeconds 锁的过期时间，单位为秒
     * @param parallelNum 最多允许多少个线程同时持有锁
     * @return 返回影响的记录行数，如果为0代表获取锁失败，大于0代表获取锁成功
     */
    public int insert(String lockKey, String owner, int expireSeconds, int parallelNum) {
        String sql =
                "    insert into " + MLockManager.DEFAULT_TABLE_NAME + "(lock_key, owner, expire_seconds) " +
                "    select :lockKey, :owner, :expireSeconds " +
                "    from (select 1) as T " +
                "    where :parallelNum > ( " +
                "        select count(id) " +
                "        from " + MLockManager.DEFAULT_TABLE_NAME +
                "        where lock_key = :lockKey and expire_seconds >= TIMESTAMPDIFF(SECOND, create_time, NOW()) " +
                "    )";
        StatementRunnableWithResult<Integer> runnable = (connection, argument) -> {
            connection.createQuery(sql)
                    .addParameter("lockKey", lockKey)
                    .addParameter("owner", owner)
                    .addParameter("expireSeconds", expireSeconds)
                    .addParameter("parallelNum", parallelNum)
                    .executeUpdate();
            return connection.getResult();
        };
        int affectRows = MLockManager.SQL2O.runInTransaction(runnable);
        return affectRows;
    }

    /**
     * 释放锁
     *
     * @param lockKey 锁唯一标识
     * @param owner 锁持有者
     * @return 返回影响的记录行数，如果为0代表当前未占有锁（可能已经被别的进程clean），大于0代表释放锁成功
     */
    public int delete(String lockKey, String owner) {
        String sql = "delete from " + MLockManager.DEFAULT_TABLE_NAME + " where lock_key = :lockKey and owner = :owner ";
        StatementRunnableWithResult<Integer> runnable = (connection, argument) -> {
            connection.createQuery(sql)
                    .addParameter("lockKey", lockKey)
                    .addParameter("owner", owner)
                    .executeUpdate();
            return connection.getResult();
        };
        int affectRows = MLockManager.SQL2O.runInTransaction(runnable);
        return affectRows;
    }
}
