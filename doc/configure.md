## properties文件配置方式

默认的配置方式为将mysql-lock.properties置于classpath下（不使用连接池）

属性|含义
---|---
mysql-lock.url|数据库连接地址
mysql-lock.username|数据库用户名
mysql-lock.password|数据库密码
mysql-lock.table-name|分布式锁表名
mysql-lock.lock-key-max-length|lockKey字段在数据库中支持的最大长度
mysql-lock.expire-seconds|允许持有锁的默认最长时间，单位为秒（MLock中未定义时使用此默认值）
mysql-lock.try-lock-interval|阻塞式获取锁的时候，重复尝试获取锁的间隔毫秒数

## DataSource配置方式

使用DataSourceMLockConfigurator类进行配置可以复用已有的DataSource连接池，不必重复配置数据库连接
```
DataSourceMLockConfigurator dataSourceConfigurator = new DataSourceMLockConfigurator();
dataSourceConfigurator.setDataSource(dataSource);
dataSourceConfigurator.configure();
```

## 自定义配置方式

使用CustomizedMLockConfigurator类进行配置可以灵活地自定义配置存储方式，自由地加载配置，然后传入配置类

```
CustomizedMLockConfigurator customizedConfigurator = new CustomizedMLockConfigurator();
customizedConfigurator.setUrl(url);
customizedConfigurator.setUsername(username);
customizedConfigurator.setPassword(password);
customizedConfigurator.setExpireSeconds(expireSeconds);
customizedConfigurator.setTableName(tableName);
customizedConfigurator.setLockKeyMaxLength(lockKeyMaxLength);
customizedConfigurator.setTryLockInterval(tryLockInterval);
customizedConfigurator.configure();
```