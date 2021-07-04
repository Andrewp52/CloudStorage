package com.pae.cloudstorage.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class ConfigReader {
    public static Map<String, String> readConfFile(String name) throws IOException {
        Map<String, String> conf = new HashMap<>();
        try(FileReader fr = new FileReader(ConfigReader.class.getResource(name).getFile());
            BufferedReader br = new BufferedReader(fr)
        ) {
            String s;
            String[] tokens;
            while ((s = br.readLine()) != null ){
                if(!s.isBlank()){
                    tokens = s.replace("\r", "").replace("\n", "").toLowerCase().split("=");
                    conf.put(tokens[0].trim(), tokens[1].trim());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return conf;
    }
}
