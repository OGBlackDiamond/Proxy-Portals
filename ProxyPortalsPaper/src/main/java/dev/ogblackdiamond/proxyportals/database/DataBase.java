package dev.ogblackdiamond.proxyportals.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 *  Class to manage all database interactions.
 */
public class DataBase {

    Connection connection;

    /**
     *  Big messy connection class.
     *  Initializes the database and a connection instance, and creates an empty table if one doesn't exist.
     *  Will yell if something fails.
     */
    public void connect() {
        // ensure database is properly initialized
        
        // register the JDBC SQL driver
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().warning("[proxymessages] Error initializing database!");
            Bukkit.getLogger().warning(e.toString());
            return;
        }

        // setup a database connection
        try {
            connection = DriverManager
                    .getConnection("jdbc:sqlite:plugins/ProxyPortals/database.db");
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[proxymessages] Error initializing database!");
            Bukkit.getLogger().warning(e.toString());
            return;
        } 

        // creates an empty table for the database to use if one is not already present
        try (Statement statement = connection.createStatement()) {
            String sqlStatement = "CREATE TABLE IF NOT EXISTS portals (x int, y int, z int, server varchar(255));";
            statement.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[proxymessages] Error initializing database!");
            Bukkit.getLogger().warning(e.toString());
            return;
        }
    }

    /**
     *  Registers a new server into the database.
     *  Iterates over all detected blocks and assigns them to a server.
     */
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

    public void deregisterServer(String server) {
        try (Statement statement = connection.createStatement()) {
            String sqlStatement = "DELETE FROM portals WHERE server='" + server + "'";
            statement.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[proxymessages] Error deregistering server from the database!");
            Bukkit.getLogger().warning(e.toString());
            return;
        }
    }


    /**
     *  Checks the entire database to see if a location has a registered portal block.
     *  Returns the name of the server if it does, null if not.
     */
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
