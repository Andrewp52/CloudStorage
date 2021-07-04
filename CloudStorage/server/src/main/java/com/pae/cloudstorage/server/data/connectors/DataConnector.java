package com.pae.cloudstorage.server.data.connectors;

import java.sql.Connection;

/**
 * Base interface for sql connectors
 */
public interface DataConnector {
    Connection getConnection();
    void closeConnection();
}
