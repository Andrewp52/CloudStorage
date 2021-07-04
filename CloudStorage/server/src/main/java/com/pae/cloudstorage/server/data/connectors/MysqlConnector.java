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

    private MysqlConnector(String confFile) throws IOException {
        conf = ConfigReader.readConfFile(confFile);
    }

    public static MysqlConnector getConnector(String confFile) throws IOException {
        if(connector == null){
            connector = new MysqlConnector(confFile);
        }
        return connector;
    }

    @Override
    public Connection getConnection(){
        if(this.connection == null){
            try {
                this.ds.setUser(conf.get("dblogin"));
                this.ds.setPassword(conf.get("dbpass"));
                this.ds.setDatabaseName(conf.get("dbname"));
                this.ds.setServerName(conf.get("dbhost"));
                this.ds.setPort(Integer.parseInt(conf.get("dbport")));
                this.ds.setLoginTimeout(Integer.parseInt(conf.get("dbtimeout")));
                this.connection = this.ds.getConnection();
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
