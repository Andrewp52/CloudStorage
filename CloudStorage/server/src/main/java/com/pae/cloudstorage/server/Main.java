package com.pae.cloudstorage.server;

import com.pae.cloudstorage.server.data.DataService;
import com.pae.cloudstorage.server.data.connectors.DataConnector;
import com.pae.cloudstorage.server.data.connectors.MysqlConnector;
import com.pae.cloudstorage.server.network.NettyServer;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args){
        //DataConnector connector = MysqlConnector.getConnector("test", "test", "cloudstor", "localhost", 3306);
        new NettyServer(9999, null);
    }
}
