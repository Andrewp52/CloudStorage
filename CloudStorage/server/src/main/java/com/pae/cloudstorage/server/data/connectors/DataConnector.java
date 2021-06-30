package com.pae.cloudstorage.server.data.connectors;

import java.sql.Connection;

/**
 * Base class for sql connectors
 */
public abstract class DataConnector {
    Connection conn;
    String login;
    String pass;
    String db;
    String host;
    int port;
    int TIMEOUT;

    public abstract Connection getConnection();
    public abstract void closeConnection();
}
