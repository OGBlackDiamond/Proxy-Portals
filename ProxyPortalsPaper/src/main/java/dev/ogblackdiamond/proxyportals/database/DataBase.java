package dev.ogblackdiamond.proxyportals.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class DataBase {

    Connection connection;

    public void connect() {
        // ensure database is properly initialized
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().warning("[proxymessages] Error initializing database!");
            Bukkit.getLogger().warning(e.toString());
            return;
        }
        try {
            connection = DriverManager
                    .getConnection("jdbc:sqlite:plugins/ProxyPortals/database.db");
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[proxymessages] Error initializing database!");
            Bukkit.getLogger().warning(e.toString());
            return;
        } 

        try (Statement statement = connection.createStatement()) {
            String sqlStatement = "CREATE TABLE IF NOT EXISTS portals (x int, y int, z int, server varchar(255));";
            statement.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[proxymessages] Error initializing database!");
            Bukkit.getLogger().warning(e.toString());
            return;
        }
    }

    public void registerServer(ArrayList<Location> list, String server) {
        try (Statement statement = connection.createStatement()) {
            String sqlStatement = "";
            for (Location loc : list) {
                sqlStatement += "INSERT INTO portals (x, y, z, server) VALUES ("
                        + loc.getBlockX() + ", "
                        + loc.getBlockY() + ", "
                        + loc.getBlockZ() + ", '"
                        + server + "');";
            }
            statement.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[proxymessages] Error registering server to the database!");
            Bukkit.getLogger().warning(e.toString());
            return;
        }
    }


    public String checkPortal(Location location) {
        try (Statement statement = connection.createStatement()) {
            String sqlStatement = "SELECT * FROM portals;";
            ResultSet response = statement.executeQuery(sqlStatement);

            do {
                if (response.getInt(1) == location.getBlockX()
                    && response.getInt(2) == location.getBlockY()
                    && response.getInt(3) == location.getBlockZ())
                    return response.getString(4);

            } while (response.next());
            
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[proxymessages] Error getting server data!");
            Bukkit.getLogger().warning(e.toString());
        }
        return null;
    }
}
