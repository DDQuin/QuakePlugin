package me.ddquin.quake;

import me.ddquin.quake.commands.CommandsQuakeArena;
import me.ddquin.quake.commands.CommandsQuakeShot;
import me.ddquin.quake.listener.ListenerMatch;
import me.ddquin.quake.listener.ListenerPlayer;
import me.ddquin.quake.listener.ListenerSign;
import me.ddquin.quake.util.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Main extends JavaPlugin {
    private Handler handler;
    private SQL sql;

    @Override
    public void onEnable() {
        loadSettings();
        if (Settings.SQLOn) sql = new SQL(this);
        handler = new Handler(this);
        registerCommands();
        registerEvents();
        registerRunnables();

    }

    private void registerCommands() {
        CommandsQuakeShot quakeShot = new CommandsQuakeShot(this);
        CommandsQuakeArena quakeArena = new CommandsQuakeArena(this);
        getCommand("quakeshot").setExecutor(quakeShot);
        getCommand("quakeshot").setTabCompleter(quakeShot);
        getCommand("quakearena").setExecutor(quakeArena);
        getCommand("quakearena").setTabCompleter(quakeArena);

    }

    private void registerRunnables() {
        if (Settings.SQLOn) {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
                try {
                    sql.connection.prepareStatement("SELECT 1 FROM " + sql.getTableName()).execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }, 1L, 6000L);
        }

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> handler.tick(), 1L, 1L);
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new ListenerMatch(this), this);
        getServer().getPluginManager().registerEvents(new ListenerPlayer(this), this);
        getServer().getPluginManager().registerEvents(new ListenerSign(this), this);
    }

    private void loadSettings() {
        ConfigManager c = new ConfigManager("config", this, false);
        Settings.SQLOn = c.getBoolOrSetIfEmpty("sql-on", false);
        Settings.waitTime = c.getIntOrSetIfEmpty("wait-time", 20);
        Settings.hitCD = (int)Math.round(c.getDoubleOrSetIfEmpty("hit-cooldown", 3) * 20);
        //Set default values for SQL
        c.getStringOrSetIfEmpty("host", " ");
        c.getStringOrSetIfEmpty("port", " ");
        c.getStringOrSetIfEmpty("database", " ");
        c.getStringOrSetIfEmpty("username", " ");
        c.getStringOrSetIfEmpty("password", " ");

        Settings.deathMessages = c.getStringListOrSetIfEmpty("death-messages", Collections.singletonList("&9%victim% &6was killed by &9%killer%"));


    }

    @Override
    public void onDisable() {
        handler.getStatManager().saveAll();
        handler.getArenaManager().saveArenas();
        handler.getMatchManager().forceEndAll();
        handler.getSignManager().saveSigns();
    }

    public Handler getHandler() {
        return handler;
    }

    public SQL getSQL() { return sql; }
}
