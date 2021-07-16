package com.pae.cloudstorage.server;

import com.pae.cloudstorage.server.data.DataService;
import com.pae.cloudstorage.server.data.connectors.DataConnector;
import com.pae.cloudstorage.server.data.connectors.MysqlConnector;
import com.pae.cloudstorage.server.network.NettyServer;

import java.io.*;
import java.util.Map;

public class Main {
    static Map<String, String> srvConfig;
    static Map<String, String> dbConfig;

    public static void main(String[] args) throws IOException {
        dbConfig = ConfigReader.readConfFile("/database.conf");
        srvConfig = ConfigReader.readConfFile("/netserver.conf");
        DataConnector connector = MysqlConnector.getConnector(dbConfig);
        new NettyServer(srvConfig, new DataService(connector));
    }

    public static Map<String, String> getSrvConfig() {
        return srvConfig;
    }

    public static Map<String, String> getDbConfig() {
        return dbConfig;
    }
}
