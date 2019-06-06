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
 * Created by zheng93775 on 2019/6/4.
 */
public class PropertyConfigurator extends BaseConfigurator {
    private static final Logger logger = LoggerFactory.getLogger(PropertyConfigurator.class);

    public final static String PROPERTY_URL = "mysql-lock.url";
    public final static String PROPERTY_USERNAME = "mysql-lock.username";
    public final static String PROPERTY_PASSWORD = "mysql-lock.password";

    private String resource;

    public PropertyConfigurator(String resource) {
        this.resource = resource;
    }

    @Override
    protected Sql2o doConfigure() {
        URL configURL = Loader.getResource(resource);
        Properties props = this.loadProperties(configURL);
        if (props == null) {
            return null;
        }
        String url = props.getProperty(PROPERTY_URL);
        String username = props.getProperty(PROPERTY_USERNAME);
        String password = props.getProperty(PROPERTY_PASSWORD);
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
            logger.error("Could not read configuration file from URL [" + configURL
                    + "].", e);
            logger.error("Ignoring configuration file [" + configURL + "].");
            return null;
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
