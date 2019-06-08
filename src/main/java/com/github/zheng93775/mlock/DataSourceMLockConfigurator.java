package com.github.zheng93775.mlock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Sql2o;

import javax.sql.DataSource;

/**
 * Created by zheng93775 on 2019/6/5.
 */
public class DataSourceMLockConfigurator extends BaseMLockConfigurator {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceMLockConfigurator.class);

    private DataSource dataSource;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected Sql2o doConfigure() {
        if (this.dataSource == null) {
            throw new MysqlLockInitException("dataSource can not be null");
        }
        Sql2o sql2o = new Sql2o(this.dataSource);
        return sql2o;
    }
}
