## 表结构

```
CREATE TABLE `tb_distributed_lock` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `lock_key` varchar(64) NOT NULL COMMENT '锁Key',
  `owner` char(36) NOT NULL COMMENT '锁的持有者',
  `expire_seconds` int(11) NOT NULL COMMENT '过期时间，单位为秒',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ukey_lock_key_owner` (`lock_key`,`owner`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分布式锁'
```

其中表名和lock_key的长度可配置，启动时如果没有该表就会尝试创建表

## 获取锁的sql

```
insert into tb_distributed_lock(lock_key, owner, expire_seconds)
select :lockKey, :owner, :expireSeconds
from (select 1) as T
where :parallelNum > (
    select count(id)
    from tb_distributed_lock
    where lock_key = :lockKey and expire_seconds >= TIMESTAMPDIFF(SECOND, create_time, NOW())
)
```

插入时检查lockKey当前有效锁的数量是否小于parallelNum，是则允许插入。
根据插入语句返回影响的行数判断是否成功占有锁。

## 释放锁的sql

```
delete from tb_distributed_lock where lock_key = :lockKey and owner = :owner
```

owner使用UUID生成保证唯一性，确保自己只能释放自己占有的锁

## 数据库访问层

数据库访问层使用轻量简约的sql2o组件。获取锁和释放的数据库事务立即提交，和正常业务的数据库事务互不影响。