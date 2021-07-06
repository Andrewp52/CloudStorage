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

//    public FSObject(String name, String path, boolean isDirectory) {
//        this.name = name;
//        this.path = path;
//        this.isDirectory = isDirectory;
//    }

    public FSObject(Path p) {
        if(p.getFileName() == null){
            name = p.getRoot().toString();
            isDirectory = true;
        } else {
            name = p.getFileName().toString();
            path = p.toString();
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

    @Override
    public String toString() {
        return (isDirectory? "<D>" : "") + name;
    }
}
