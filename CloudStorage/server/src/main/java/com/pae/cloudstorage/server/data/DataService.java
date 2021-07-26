package com.pae.cloudstorage.server.data;

import com.pae.cloudstorage.common.User;
import com.pae.cloudstorage.server.data.connectors.DataConnector;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Dataservice provides database interaction methods
 */
public class DataService {
    private final Logger logger = LogManager.getLogger(DataService.class);
    private final Connection conn;
    public DataService(DataConnector connector) {
        conn = connector.getConnection();
    }

    // Retrieves user data from database using login & password for searching.
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
            logger.error("DB Auth error: ", e);
        }
        return u;
    }

    // Adds new user into the database.
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
            logger.error("DB Register error: ", e);
        }
        return false;
    }

    // Updates user`s profile (all except password).
    public boolean updateProfile(int id, String fname, String lname, String email, String nick){
        try {
            PreparedStatement ps = conn.prepareStatement("update users set nick = ?, fname = ?, lname = ?, email = ? where id = ?");
            ps.setString(1, nick);
            ps.setString(2, fname);
            ps.setString(3, lname);
            ps.setString(4, email);
            ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("DB Prof update error: ", e);
        }
        return false;
    }

    // Retrieves User object by it`s id
    public User getUserById(int id) {
        User u = null;
        try {
            PreparedStatement ps = conn.prepareStatement("select * from users where id = ?");
            ps.setInt(1, id);
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
            logger.error("DB User selection error: ", e);
        }
        return u;
    }
}
