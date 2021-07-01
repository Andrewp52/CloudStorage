package com.pae.cloudstorage.common;

import java.util.StringJoiner;

// Class contains user profile (Filling by DataService from DB).
public class User {
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

    // This toString is compatible for transfer to client.
    @Override
    public String toString() {
        return new StringJoiner(" ", "<PROFILE>", "")
                .add(nick)
                .add(firstName)
                .add(lastName)
                .add(email)
                .add(String.valueOf(quota)).toString();
    }
}
