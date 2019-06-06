package com.github.zheng93775.mlock;

import com.mysql.jdbc.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Sql2o;

/**
 * Created by zheng93775 on 2019/6/5.
 */
public class CustomizedConfigurator extends BaseConfigurator {
    private static final Logger logger = LoggerFactory.getLogger(CustomizedConfigurator.class);

    private String url;
    private String username;
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

    @Override
    protected Sql2o doConfigure() {
        if (StringUtils.isNullOrEmpty(url)
                || StringUtils.isNullOrEmpty(username)
                || StringUtils.isNullOrEmpty(password)) {
            logger.error("invalid properties, url={}, username={} password={}",
                    url, username, StringUtils.isNullOrEmpty(password) ? "" : "******");
            return null;
        }
        logger.debug("load properties url={}, username={} password={}", url, username, "******");

        Sql2o sql2o = new Sql2o(url, username, password);
        return sql2o;
    }

}
