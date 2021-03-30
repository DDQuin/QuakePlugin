package me.ddquin.quake;

import me.ddquin.quake.util.ConfigManager;
import me.ddquin.quake.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQL {

    public Connection connection;
    public String host, database, username, password;
    public int port;
    private String tableName;
    private Main main;

    public SQL(Main main) {
        this.main = main;
        this.tableName = "QuakeShotStats";
        ConfigManager c = new ConfigManager("config", main, false);
        host = c.getString("host");
        port = c.getInt("port");
        database = c.getString("database");
        username = c.getString("username");
        password = c.getString("password");
        try {

            synchronized (this) {
                if (getConnection() != null && !getConnection().isClosed()) {
                    return;
                }

                Class.forName("com.mysql.jdbc.Driver");
                setConnection(DriverManager.getConnection("jdbc:mysql://" + this.host + ":"
                        + this.port + "/" + this.database, this.username, this.password));

                Bukkit.getConsoleSender().sendMessage(Util.prefix + ChatColor.GREEN + "MYSQL CONNECTED");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            main.onDisable();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            main.onDisable();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public String getTableName() { return tableName; }

    public boolean tableExist(String tableName) throws SQLException {
        boolean tExists = false;
        try (ResultSet rs = connection.getMetaData().getTables(null, null, tableName, null)) {
            while (rs.next()) {
                String tName = rs.getString("TABLE_NAME");
                if (tName != null && tName.equals(tableName)) {
                    tExists = true;
                    break;
                }
            }
        }
        return tExists;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

}
