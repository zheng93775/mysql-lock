### 基本介绍

MLock继承自java.util.concurrent.locks.Lock接口，实现了锁的常用方法

### 重载的构造函数
public MLock(String lockKey)

public MLock(String lockKey, int expireSeconds)

public MLock(String lockKey, int expireSeconds, int parallelNum)

参数|含义
---|---
lockKey|锁唯一标识。不同的MLock对象，如果lockKey相同则意味着是同一把锁
expireSeconds|允许持有锁的最长时间，单位为秒，过期之后其他执行线程可获得锁，默认值为60
parallelNum|可同时获得锁进入临界区的数量，有限并发锁，用于限制同时执行的并发数，默认值为1

### public方法

```
/**
 * 获取锁，如果当前锁不可用，每秒重复尝试，直到占有锁
 */
public void lock()


/**
 * 获取锁，如果当前锁不可用，每秒重复尝试，直到占有锁或者被打断
 */
public void lockInterruptibly() throws InterruptedException


/**
 * 尝试获取锁
 * @return 返回true代表已经获得锁，false代表获取锁失败（锁已经被别的进程占有）
 */
public boolean tryLock()


/**
 * 在指定的时间内，重复尝试获取锁，得到锁之后返回true
 * 如果在指定时间内获取不到锁，那么返回false
 *
 * @param time 时间值
 * @param unit 时间单位
 * @return true代表得到锁，false代表在指定时间内获取不到锁
 * @throws InterruptedException
 */
public boolean tryLock(long time, TimeUnit unit) throws InterruptedException


/**
 * 释放锁
 * 如果之前从未调用过任何尝试获取锁的方法，那么抛出异常 IllegalMonitorStateException
 * 如果之前尝试获取锁，但是没有成功，调用unlock()不会产生任何副作用
 */
public void unlock()
```