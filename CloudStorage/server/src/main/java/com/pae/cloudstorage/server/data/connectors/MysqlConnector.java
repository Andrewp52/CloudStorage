package com.pae.cloudstorage.server.data.connectors;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class MysqlConnector extends DataConnector {
    private static final MysqlDataSource ds = new MysqlDataSource();
    private static MysqlConnector connector;

    private MysqlConnector(String login, String pass, String db, String host, int port) {
        super.login = login;
        super.pass = pass;
        super.db = db;
        super.host = host;
        super.port = port;
        super.TIMEOUT = 10;
    }

    public static MysqlConnector getConnector(String login, String pass, String db, String host, int port){
        if(connector == null){
            connector = new MysqlConnector(login, pass, db, host, port);
        }
        return connector;
    }

    public Connection getConnection(){
        if(this.conn == null){
            try {
                ds.setLoginTimeout(TIMEOUT);
                ds.setUser(super.login);
                ds.setPassword(super.pass);
                ds.setDatabaseName(super.db);
                ds.setPort(this.port);
                ds.setServerName(this.host);
                super.conn = ds.getConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return conn;
    }

    @Override
    public void closeConnection() {
        try {
            if (super.conn != null && !conn.isClosed()){
                super.conn.close();
                super.conn = null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
