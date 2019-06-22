package com.github.zheng93775.mlock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 基于MySQL实现的分布式锁
 *
 * Created by zheng93775 on 2019/6/4.
 */
public class MLock implements Lock {
    private static final Logger logger = LoggerFactory.getLogger(MLock.class);

    /**
     * 锁唯一标识。不同的进程中，相同的lockKey意味着是同一把锁
     */
    private String lockKey;
    /**
     * 最长可以持有锁的时间，单位为秒。超时后，即使不释放锁也可能会被别的进程抢占
     */
    private int expireSeconds;
    /**
     * 可以作为共享锁，此值代表着可以同时有多少个执行线程持有锁
     */
    private int parallelNum;
    /**
     * 使用线程本地变量记录锁的持有者
     */
    private ThreadLocal<String> ownerThreadLocal;

    public MLock(String lockKey) {
        this(lockKey, MLockManager.DEFAULT_EXPIRE_SECONDS, MLockManager.DEFAULT_PARALLEL_NUM);
    }

    public MLock(String lockKey, int expireSeconds) {
        this(lockKey, expireSeconds, MLockManager.DEFAULT_PARALLEL_NUM);
    }

    public MLock(String lockKey, int expireSeconds, int parallelNum) {
        if (lockKey.length() > MLockManager.DEFAULT_LOCK_KEY_MAX_LENGTH) {
            throw new InvalidParameterException("lockKey.length() > " + MLockManager.DEFAULT_LOCK_KEY_MAX_LENGTH);
        }
        this.lockKey = lockKey;
        this.expireSeconds = expireSeconds;
        this.parallelNum = parallelNum;
        this.ownerThreadLocal = new ThreadLocal<>();
        MLockInitializer.init();
    }

    /**
     * 获取锁，如果当前锁不可用，会一直重复尝试，直到进入锁
     */
    @Override
    public void lock() {
        while (true) {
            try {
                lockInterruptibly();
                return;
            } catch (InterruptedException e) {
                logger.warn("lock interrupted, {}", e.getMessage());
                continue;
            }
        }
    }

    /**
     * 获取锁，如果当前锁不可用，会一直重复尝试，直到进入锁（可以被别的线程打断）
     *
     * @throws InterruptedException
     */
    @Override
    public void lockInterruptibly() throws InterruptedException {
        while (!tryLock()) {
            Thread.sleep(MLockManager.DEFAULT_TRY_LOCK_INTERVAL_MILLISECONDS);
        }
    }

    /**
     * 尝试获取锁
     * @return 返回true代表已经获得锁，false代表获取锁失败（锁已经被别的进程占有）
     */
    @Override
    public boolean tryLock() {
        String owner = ownerThreadLocal.get();
        if (owner != null && !owner.equals(MLockManager.OPERATION_TRY_LOCK)) {
            // already hold a lock
            return true;
        }

        ownerThreadLocal.set(MLockManager.OPERATION_TRY_LOCK);
        owner = UUID.randomUUID().toString();
        int affectRows = MLockDao.getInstance().insert(lockKey, owner, expireSeconds, parallelNum);
        if (affectRows > 0) {
            ownerThreadLocal.set(owner);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 在指定的时间内，重复尝试获取锁，得到锁之后返回true
     * 如果在指定时间内获取不到锁，那么返回false
     *
     * @param time 时间值
     * @param unit 时间单位
     * @return true代表得到锁，false代表在指定时间内获取不到锁
     * @throws InterruptedException
     */
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        long startTimestamp = System.currentTimeMillis();
        long waitTime = unit.toMillis(time);
        while (!tryLock()) {
            if (System.currentTimeMillis() - startTimestamp > waitTime) {
                return false;
            }
            Thread.sleep(MLockManager.DEFAULT_TRY_LOCK_INTERVAL_MILLISECONDS);
        }
        return true;
    }

    /**
     * 释放锁
     * 如果之前从未调用过任何尝试获取锁的方法，那么抛出异常 IllegalMonitorStateException
     * 如果之前尝试获取锁，但是没有成功，调用unlock()不会产生任何副作用
     */
    @Override
    public void unlock() {
        String owner = ownerThreadLocal.get();
        if (owner == null) {
            throw new IllegalMonitorStateException("should not call unlock() without tryLock()/lock()/lockInterruptibly()");
        }
        ownerThreadLocal.remove();
        if (!MLockManager.OPERATION_TRY_LOCK.equals(owner)) {
            MLockDao.getInstance().delete(lockKey, owner);
        }
    }

    /**
     * 分布式锁不支持此方法，直接抛出异常 UnsupportedOperationException
     * @return
     */
    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }
}
