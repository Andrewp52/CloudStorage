package com.pae.cloudstorage.common;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public class FSObject implements Serializable {

    private static final long serialVersionUID = -6743567631108323096L;         // SERIALIZATION MAGIC....
   private String name;
   private String path;
   private String type;
   private boolean isDirectory;
   private boolean isReadOnly;
   private boolean isSearchResult;                                              // When it`s directory is not current location
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
            try {
                isReadOnly = (boolean) Files.getAttribute(p, "dos:readonly");
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public FSObject(Path name, Path location, boolean isSearchResult) {
        this(name, location);
        this.isSearchResult = isSearchResult;
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

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isReadOnly(){
       return this.isReadOnly;
    }

    public boolean isSearchResult(){
       return this.isSearchResult;
    }
}
