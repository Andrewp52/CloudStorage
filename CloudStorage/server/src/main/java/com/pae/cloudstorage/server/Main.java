package com.pae.cloudstorage.server;

import com.pae.cloudstorage.server.network.NettyServer;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        //DataConnector connector = MysqlConnector.getConnector("/database.conf");
       new NettyServer(null);
    }
}
