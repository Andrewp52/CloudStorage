package com.pae.cloudstorage.common;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;

public class FSObject implements Serializable {

    private static final long serialVersionUID = -6743567631108323096L;         // SERIALIZATION MAGIC....
   private String name;
   private String pathLocRel;                                                   // Location relative path
   private String pathOrigin;                                                   // Path from server root
   private String type;
   private long modified;
   private boolean isDirectory;
   private boolean isReadOnly;
   private boolean isSearchResult;                                              // When it`s directory is not current location
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
                modified = Files.getLastModifiedTime(p).toMillis();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public FSObject(Path name, Path location, Path root, boolean isSearchResult) {
        this(name, location, root);
        this.pathLocRel = name.getFileName().toString();
        this.isSearchResult = isSearchResult;
    }

    public long getModified() {
        return modified;
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
