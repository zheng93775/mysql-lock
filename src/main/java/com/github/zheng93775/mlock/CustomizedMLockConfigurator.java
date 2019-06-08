package com.github.zheng93775.mlock;

import com.mysql.jdbc.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Sql2o;

/**
 * MySQL分布式锁 - 自定义参数配置器
 * 使用者可以自由地使用任何配置方式，然后手动将配置项写入到字段中，手动完成初始化配置
 *
 * Created by zheng93775 on 2019/6/5.
 */
public class CustomizedMLockConfigurator extends BaseMLockConfigurator {
    private static final Logger logger = LoggerFactory.getLogger(CustomizedMLockConfigurator.class);

    /**
     * 数据库连接地址
     */
    private String url;
    /**
     * 数据库用户名
     */
    private String username;
    /**
     * 数据库密码
     */
    private String password;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 初始化配置
     * 如果url/username/password为空，会抛出异常 MysqlLockInitException
     * @return 返回sql2o数据库管理器
     */
    @Override
    protected Sql2o doConfigure() {
        if (StringUtils.isEmptyOrWhitespaceOnly(url)) {
            throw new MysqlLockInitException("url required");
        }
        if (StringUtils.isEmptyOrWhitespaceOnly(username)) {
            throw new MysqlLockInitException("username required");
        }
        if (StringUtils.isEmptyOrWhitespaceOnly(password)) {
            throw new MysqlLockInitException("password required");
        }
        logger.debug("load properties url={}, username={} password={}", url, username, "******");

        Sql2o sql2o = new Sql2o(url, username, password);
        return sql2o;
    }

}
