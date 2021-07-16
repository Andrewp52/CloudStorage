package com.pae.cloudstorage.server.data.connectors;

import com.mysql.cj.jdbc.MysqlDataSource;
import com.pae.cloudstorage.server.ConfigReader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class MysqlConnector implements DataConnector {
    Map<String, String> conf;
    private static final MysqlDataSource ds = new MysqlDataSource();
    private static MysqlConnector connector;
    private Connection connection;

    private MysqlConnector(Map<String, String> conf) throws IOException {
        this.conf = conf;
    }

    public static MysqlConnector getConnector(Map<String, String> conf) throws IOException {
        if(connector == null){
            connector = new MysqlConnector(conf);
        }
        return connector;
    }

    @Override
    public Connection getConnection(){
        if(this.connection == null){
            try {
                ds.setUser(conf.get("dblogin"));
                ds.setPassword(conf.get("dbpass"));
                ds.setDatabaseName(conf.get("dbname"));
                ds.setServerName(conf.get("dbhost"));
                ds.setPort(Integer.parseInt(conf.get("dbport")));
                ds.setLoginTimeout(Integer.parseInt(conf.get("dbtimeout")));
                this.connection = ds.getConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return this.connection;
    }

    @Override
    public void closeConnection() {
        try {
            if (this.connection != null && !this.connection.isClosed()){
                this.connection.close();
                this.connection = null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
