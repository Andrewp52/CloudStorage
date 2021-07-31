package com.pae.cloudstorage.common;

import java.io.Serializable;

// Class contains user profile (Filling by DataService from DB).
public class User implements Serializable {
    private transient final int id;
    private String nick;
    private String firstName;
    private String lastName;
    private String email;
    private long quota;
    private long used;

    public User(int id, String nick, String firstName, String lastName, String email, long used, long quota) {
        this.id = id;
        this.nick = nick;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.quota = quota;
        this.used = used;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getNick() {
        return nick;
    }

    public long getQuota() {
        return quota;
    }

    public long getUsed() {
        return used;
    }

    public long getFree(){
        return quota - used;
    }

    public void setUsed(long bytes){
        this.used = bytes;
    }

    public void addUsed(long bytes){
        this.used += bytes;
    }

    public void remUsed(long bytes){
        this.used -= bytes;
    }
}
