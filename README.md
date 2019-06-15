# MySQL-based distributed lock

## Introduction 简介

mysql-lock is a simple implementation of distributed lock based on MySQL. It is easy to build high availability projects in distributed systems.

mysql-lock是基于MySQL的分布式锁简单实现，易于使用，保障系统高可用。默认使用tb_distributed_lock这张表来记录锁，唯一键lockKey插入成功的即视为占有锁，删除时释放锁。

## Use Scenarios 适用场景

- 在系统的多个节点配置了定时任务，希望任务只执行一次
- 分布式系统在处理并发请求的过程中，要求特定条件下的代码只执行一次
- 构建一个基于MySQL的简单系统，要求高可用，并且易于维护；业务场景需要分布式锁，但是不想因为要使用分布式锁而引入Redis、Zookeeper等其他组件

## Out Of The Box 开箱即用
添加maven依赖
```
<dependency>
    <groupId>com.github.zheng93775</groupId>
    <artifactId>mysql-lock</artifactId>
    <version>1.0.0</version>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.40</version>
</dependency>
```
classpath下配置好mysql-lock.properties
```
mysql-lock.url=jdbc:mysql://127.0.0.1:3306/test
mysql-lock.username=db_user
mysql-lock.password=db_pass
```
代码里直接使用MLock
```
MLock mLock = new MLock("DailyJob");
try {
    if (mLock.tryLock()) {
        // TODO add your code here
    }
} finally {
    mLock.unlock();
}
```

## 详细介绍

- [MLock使用介绍](https://github.com/zheng93775/mysql-lock/blob/master/doc/MLock.md)
- [配置方式](https://github.com/zheng93775/mysql-lock/blob/master/doc/configure.md)
- [表结构及原理介绍](https://github.com/zheng93775/mysql-lock/blob/master/doc/table.md)
- [spring-boot-starter-mysql-lock 零配置使用方式 ](https://github.com/zheng93775/spring-boot-starter-mysql-lock/README.md)
