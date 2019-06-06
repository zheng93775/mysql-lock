package com.github.zheng93775.mlock;

import org.sql2o.StatementRunnableWithResult;

/**
 * Created by zheng93775 on 2019/6/5.
 */
class MLockDao {
    private static volatile boolean initialized = false;
    private static MLockDao instance;

    private MLockDao() {
    }

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

    private void clean() {
        String sql = "delete from " + MLockManager.DEFAULT_TABLE_NAME + " where expire_seconds < TIMESTAMPDIFF(SECOND, create_time, NOW()) ";
        MLockManager.SQL2O.runInTransaction((connection, argument) -> {
            connection.createQuery(sql).executeUpdate();
        });
    }

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
