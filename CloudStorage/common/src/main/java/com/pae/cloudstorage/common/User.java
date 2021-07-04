package com.pae.cloudstorage.common;

import java.io.Serializable;
import java.util.StringJoiner;

// Class contains user profile (Filling by DataService from DB).
public class User implements Serializable {
    private transient final int id;
    private String nick;
    private String firstName;
    private String lastName;
    private String email;
    private String root;
    private long quota;

    public User(int id, String nick, String firstName, String lastName, String email, String root, long quota) {
        this.id = id;
        this.nick = nick;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.root = root;
        this.quota = quota;
    }

    public int getId() {
        return id;
    }

    public String getNick() {
        return nick;
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

    public String getRoot() {
        return root;
    }

    public long getQuota() {
        return quota;
    }
}
