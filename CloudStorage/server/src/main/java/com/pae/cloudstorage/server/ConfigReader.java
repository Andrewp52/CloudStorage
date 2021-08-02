package com.pae.cloudstorage.server;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


public class ConfigReader {
    static Logger logger = LogManager.getLogger(ConfigReader.class);
    public static Map<String, String> readConfFile(String name) throws IOException {
        logger.info("Reading " + name);
        Map<String, String> conf = new HashMap<>();
        Path location = null;
        try {
            location = Path.of(new File(ConfigReader.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath()).getParent();
        } catch (URISyntaxException e) {
            logger.error("Config reading error:", e);
        }
        Path p = location.resolve(name);
        try(FileReader fr = new FileReader(new File(p.toString()));
            BufferedReader br = new BufferedReader(fr)
        ) {
            String s;
            String[] tokens;
            while ((s = br.readLine()) != null ){
                if(!s.isBlank()){
                    tokens = s.replace("\r", "").replace("\n", "").split("=");
                    conf.put(tokens[0].toLowerCase().trim(), tokens[1].trim());
                }
            }
            logger.info(" Ok." + System.lineSeparator());
        } catch (FileNotFoundException e) {
            logger.error("Config reading error:", e);
        }
        return conf;
    }
}
