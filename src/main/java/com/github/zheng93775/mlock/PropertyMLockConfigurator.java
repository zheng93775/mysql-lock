package com.github.zheng93775.mlock;

import com.mysql.jdbc.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Sql2o;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

/**
 * properties文件配置器，也是默认的配置器
 *
 * Created by zheng93775 on 2019/6/4.
 */
public class PropertyMLockConfigurator extends BaseMLockConfigurator {
    private static final Logger logger = LoggerFactory.getLogger(PropertyMLockConfigurator.class);

    /**
     * 数据库连接地址
     */
    public final static String PROPERTY_URL = "mysql-lock.url";
    /**
     * 数据库用户名
     */
    public final static String PROPERTY_USERNAME = "mysql-lock.username";
    /**
     * 数据库密码
     */
    public final static String PROPERTY_PASSWORD = "mysql-lock.password";
    /**
     * 分布式锁表名
     */
    public final static String PROPERTY_TABLE_NAME = "mysql-lock.table-name";
    /**
     * lockKey字段支持的最大长度
     */
    public final static String PROPERTY_LOCK_KEY_MAX_LENGTH = "mysql-lock.lock-key-max-length";
    /**
     * 允许持有锁的最长时间，单位为秒，过期之后可能会被别的执行线程抢占
     */
    public final static String PROPERTY_EXPIRE_SECONDS = "mysql-lock.expire-seconds";
    /**
     * 阻塞式获取锁的时候，如果获取不到锁，会一直不断尝试，这个配置是尝试的间隔
     */
    public final static String PROPERTY_TRY_LOCK_INTERVAL = "mysql-lock.try-lock-interval";

    /**
     * 资源对象的路径
     */
    private String resource;

    public PropertyMLockConfigurator(String resource) {
        this.resource = resource;
    }

    /**
     * 初始化配置，加载必填配置项，如果不存在抛出异常 MysqlLockInitException
     * @return 返回sql2o数据库配置器
     */
    @Override
    protected Sql2o doConfigure() {
        URL configURL = Loader.getResource(resource);
        if (configURL == null) {
            throw new MysqlLockInitException(resource + " not found");
        }
        Properties props = this.loadProperties(configURL);
        if (props == null) {
            throw new MysqlLockInitException(configURL + " not found");
        }

        logger.debug("load {}", resource);

        String url = props.getProperty(PROPERTY_URL);
        if (StringUtils.isEmptyOrWhitespaceOnly(url)) {
            throw new MysqlLockInitException(PROPERTY_URL + " required");
        }
        logger.debug("{}={}", PROPERTY_URL, url);

        String username = props.getProperty(PROPERTY_USERNAME);
        if (StringUtils.isEmptyOrWhitespaceOnly(username)) {
            throw new MysqlLockInitException(PROPERTY_USERNAME + " required");
        }
        logger.debug("{}={}", PROPERTY_USERNAME, username);

        String password = props.getProperty(PROPERTY_PASSWORD);
        if (StringUtils.isEmptyOrWhitespaceOnly(password)) {
            throw new MysqlLockInitException(PROPERTY_PASSWORD + " required");
        }
        logger.debug("{}=******", PROPERTY_PASSWORD);

        this.loadOptionalProperty(props);

        Sql2o sql2o = new Sql2o(url, username, password);
        return sql2o;
    }

    /**
     * 加载可选项，不存在则忽略，使用默认值
     * @param props
     */
    private void loadOptionalProperty(Properties props) {
        String tableName = props.getProperty(PROPERTY_TABLE_NAME);
        if (!StringUtils.isEmptyOrWhitespaceOnly(tableName)) {
            logger.debug("{}={}", PROPERTY_TABLE_NAME, tableName);
            this.setTableName(tableName);
        }

        String lockKeyMaxLengthStr = props.getProperty(PROPERTY_LOCK_KEY_MAX_LENGTH);
        if (!StringUtils.isEmptyOrWhitespaceOnly(lockKeyMaxLengthStr)) {
            logger.debug("{}={}", PROPERTY_LOCK_KEY_MAX_LENGTH, lockKeyMaxLengthStr);
            Integer lockKeyMaxLength = Integer.valueOf(lockKeyMaxLengthStr);
            this.setLockKeyMaxLength(lockKeyMaxLength);
        }

        String expireSecondsStr = props.getProperty(PROPERTY_EXPIRE_SECONDS);
        if (!StringUtils.isEmptyOrWhitespaceOnly(expireSecondsStr)) {
            logger.debug("{}={}", PROPERTY_EXPIRE_SECONDS, expireSecondsStr);
            Integer expireSeconds = Integer.valueOf(expireSecondsStr);
            this.setExpireSeconds(expireSeconds);
        }

        String tryLockIntervalStr = props.getProperty(PROPERTY_TRY_LOCK_INTERVAL);
        if (!StringUtils.isEmptyOrWhitespaceOnly(tryLockIntervalStr)) {
            logger.debug("{}={}", PROPERTY_TRY_LOCK_INTERVAL, tryLockIntervalStr);
            Long tryLockInterval = Long.valueOf(tryLockIntervalStr);
            this.setTryLockInterval(tryLockInterval);
        }
    }

    /**
     * 加载properties文件
     *
     * @param configURL 配置文件地址
     * @return 返回加载成功的配置项
     */
    private Properties loadProperties(URL configURL) {
        Properties props = new Properties();
        logger.debug("Reading configuration from URL " + configURL);
        InputStream istream = null;
        URLConnection uConn = null;
        try {
            uConn = configURL.openConnection();
            uConn.setUseCaches(false);
            istream = uConn.getInputStream();
            props.load(istream);
            return props;
        } catch (Exception e) {
            if (e instanceof InterruptedIOException || e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new MysqlLockInitException("Could not read configuration file from URL [" + configURL + "].", e);
        } finally {
            if (istream != null) {
                try {
                    istream.close();
                } catch (InterruptedIOException ignore) {
                    Thread.currentThread().interrupt();
                } catch (IOException ignore) {
                } catch (RuntimeException ignore) {
                }
            }
        }
    }
}
