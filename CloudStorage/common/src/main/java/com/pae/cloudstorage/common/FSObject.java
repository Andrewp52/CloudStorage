package com.pae.cloudstorage.common;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public class FSObject implements Serializable {
   private String name;
   private String path;
   private String type;
   private Boolean isDirectory;
   private long size;

   public FSObject(String name, String path, long size, boolean isDirectory){
       this.name = name;
       this.path = path;
       this.size = size;
       this.isDirectory = isDirectory;
   }
    public FSObject(Path p, Path location) {
        if(p.getFileName() == null){
            name = p.getRoot().toString();
            path = name;
            isDirectory = true;
        } else {
            name = p.getFileName().toString();
            path = location.relativize(p).toString();
            isDirectory = Files.isDirectory(p);
        }
        if(!isDirectory){
            try {
                size = Files.size(p);
                type = Files.probeContentType(p);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public FSObject getObject(){
        return this;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public Boolean isDirectory() {
        return isDirectory;
    }
}
