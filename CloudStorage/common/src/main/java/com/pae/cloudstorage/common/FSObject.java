package com.pae.cloudstorage.common;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class represents a file system object
 * such as file or directory.
 * Universal for local & remote storage.
 */
public class FSObject implements Serializable {

    private static final long serialVersionUID = -6743567631108323096L;         // SERIALIZATION MAGIC....
   private String name;
   private String pathLocRel;                                                   // Location relative path
   private String pathOrigin;                                                   // Path from server root
   private String type;
   private long modifiedTime;
   private boolean isDirectory;
   private boolean isReadOnly;
   private long size;

   public FSObject(String name, String pathLocRel, long size, boolean isDirectory){
       this.name = name;
       this.pathLocRel = pathLocRel;
       this.size = size;
       this.isDirectory = isDirectory;
   }

    public FSObject(Path p, Path location, Path root) {
        if(p.getFileName() == null){
            name = p.getRoot().toString();
            pathLocRel = name;
            isDirectory = true;
        } else {
            name = p.getFileName().toString();
            pathLocRel = location.relativize(p).toString();
            pathOrigin = p.toString();
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
                modifiedTime = Files.getLastModifiedTime(p).toMillis();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public long getModifiedTime() {
        return modifiedTime;
    }

    public FSObject getObject(){
        return this;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return pathLocRel;
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

    public String getOrigin(){
       return this.pathOrigin;
    }
}
