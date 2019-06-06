package com.github.zheng93775.mlock;

import java.util.UUID;

/**
 * Created by zheng93775 on 2019/6/4.
 */
public class MLock {
    private String lockKey;
    private int expireSeconds;
    private int parallelNum;
    private ThreadLocal<String> ownerThreadLocal;

    public MLock(String lockKey) {
        this(lockKey, MLockManager.DEFAULT_EXPIRE_SECONDS, MLockManager.DEFAULT_PARALLEL_NUM);
    }

    public MLock(String lockKey, int expireSeconds) {
        this(lockKey, expireSeconds, MLockManager.DEFAULT_PARALLEL_NUM);
    }

    public MLock(String lockKey, int expireSeconds, int parallelNum) {
        this.lockKey = lockKey;
        this.expireSeconds = expireSeconds;
        this.parallelNum = parallelNum;
        this.ownerThreadLocal = new ThreadLocal<>();
        MLockInitializer.init();
    }

    public boolean tryLock() {
        String owner = UUID.randomUUID().toString();
        int affectRows = MLockDao.getInstance().insert(lockKey, owner, expireSeconds, parallelNum);
        if (affectRows > 0) {
            ownerThreadLocal.set(owner);
            return true;
        } else {
            return false;
        }
    }

    public boolean unlock() {
        String owner = ownerThreadLocal.get();
        if (owner == null) {
            return false;
        } else {
            ownerThreadLocal.remove();
            MLockDao.getInstance().delete(lockKey, owner);
            return true;
        }
    }
}
