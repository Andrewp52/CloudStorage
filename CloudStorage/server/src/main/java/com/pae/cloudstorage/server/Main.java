package com.pae.cloudstorage.server;

import com.pae.cloudstorage.server.data.DataService;
import com.pae.cloudstorage.server.data.connectors.DataConnector;
import com.pae.cloudstorage.server.data.connectors.MysqlConnector;
import com.pae.cloudstorage.server.network.NettyServer;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

public class Main {

    static Map<String, String> srvConfig;
    static Map<String, String> dbConfig;

    // Entry point
    // Configures logger, reads configs & starts server.

    public static void main(String[] args) throws IOException, URISyntaxException {
        Path location = Path.of(new File(ConfigReader.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath()).getParent();
        Properties p = new Properties();
        p.load(new FileInputStream(location.resolve("log4j.properties").toString()));
        PropertyConfigurator.configure(p);
        dbConfig = ConfigReader.readConfFile("database.conf");
        srvConfig = ConfigReader.readConfFile("netserver.conf");
        DataConnector connector = MysqlConnector.getConnector(dbConfig);
        new NettyServer(srvConfig, new DataService(connector));
    }

    public static Map<String, String> getSrvConfig() {
        return srvConfig;
    }

}
