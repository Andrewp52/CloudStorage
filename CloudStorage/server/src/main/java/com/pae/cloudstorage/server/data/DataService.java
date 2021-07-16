package com.pae.cloudstorage.server.data;

import com.pae.cloudstorage.common.User;
import com.pae.cloudstorage.server.data.connectors.DataConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataService {
    private Connection conn;
    public DataService(DataConnector connector) {
        conn = connector.getConnection();
    }

    public User authUser(String nick, String pass){
        User u = null;
        try {
            PreparedStatement ps = conn.prepareStatement("select * from users where nick = ? and pass = ?");
            ps.setString(1, nick);
            ps.setInt(2, pass.hashCode());
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                 u = new User(
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getString(7),
                         0                                              // QUOTA IS NOT AVAILABLE NOW
                );
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return u;
    }

    public boolean registerUser(String fname, String lname, String email, String nick, String pass){
        try {
            PreparedStatement ps = conn.prepareStatement("insert into users (nick, pass, fname, lname, email) values (?, ?, ?, ?, ?)");
            ps.setString(1, nick);
            ps.setInt(2, pass.hashCode());
            ps.setString(3, fname);
            ps.setString(4, lname);
            ps.setString(5, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}
