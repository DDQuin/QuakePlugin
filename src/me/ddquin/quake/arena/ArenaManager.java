package me.ddquin.quake.arena;

import me.ddquin.quake.Main;
import me.ddquin.quake.util.ConfigManager;
import me.ddquin.quake.util.Util;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class ArenaManager {

    private Map<String, Arena> arenas;
    private Main main;

    public ArenaManager(Main main) {
        this.main = main;
        arenas = new HashMap<>();
    }

    public void loadArenas() {
        final ConfigManager c = new ConfigManager("arenas", main, false);
        for (final String i : c.section("arena")) {
            final String cur = "arena." + i;
            final String name = c.getString(cur + ".name");
            final int duration = c.getInt(cur + ".duration");
            final int min = c.getInt(cur + ".min-players");
            final int max = c.getInt(cur + ".max-players");
            final int winkills = c.getInt(cur + ".win-kills");
            final List<Location> spawns = new ArrayList<>();
            if (!c.getString(cur + ".spawns").equalsIgnoreCase("empty")) {
                spawns.addAll(c.getLocList(cur + ".spawns"));
                //for (final String locString : c.getStringList(cur + ".spawns")) {
               //     spawns.add(Util.getLocFromString(locString));
               // }
            }
            Arena a = new Arena(name);
            a.setDuration(duration);
            a.setWinKills(winkills);
            a.setMinPlayers(min);
            a.setMaxPlayers(max);
            a.setSpawns(spawns);
            arenas.put(name, a);
        }
    }

    public void saveArenas() {
        ConfigManager c = new ConfigManager("arenas", main, true);
        int i = 0;
        for (Arena a: arenas.values()) {
            final String cur = "arena." + i;
            c.set(cur + ".name", a.getName());
            c.set(cur + ".duration", a.getDuration());
            c.set(cur + ".min-players", a.getMinPlayers());
            c.set(cur + ".max-players", a.getMaxPlayers());
            c.set(cur + ".win-kills", a.getWinKills());
            if (a.getSpawns().isEmpty()) {
                c.set(cur + ".spawns", "EMPTY");
            } else {
                c.set(cur + ".spawns", a.getSpawns());
               // List<String> locStrings = new ArrayList<>();
              //  a.getSpawns().forEach(s -> locStrings.add(Util.getRawLocString(s)));
               // c.set(cur + ".spawns", locStrings);
            }
            i++;
        }

    }

    public List<Arena> getFreeArenas() {
        List<Arena> freeArenas = new ArrayList<>();
        for (Arena a: arenas.values()) {
            if (a.isReady()) freeArenas.add(a);
        }
        return freeArenas;
    }

    public void createArena(String name) throws IllegalArgumentException {
        if (arenas.containsKey(name)) {
            throw new IllegalArgumentException();
        } else {
            arenas.put(name, new Arena(name));
        }
    }

    public void removeSpawns(String arenaName) {
        Arena a = arenas.get(arenaName);
        a.removeSpawns();
    }

    public void deleteArena(Arena a) throws IllegalStateException {
        if (main.getHandler().getMatchManager().arenaInUse(a)) {
            throw new IllegalStateException();
        }
        arenas.remove(a.getName());
    }

    public Arena getArena(String arenaName) {
        return arenas.get(arenaName);
    }

    public boolean isArena(String arenaName) {
        return arenas.containsKey(arenaName);
    }

    public void setArenaSpawn(String arenaName, Location loc) {
        Arena a = arenas.get(arenaName);
        a.addSpawn(loc);
    }

    public Collection<Arena> arenaList() {
        return arenas.values();
    }

    public Set<String> arenaNames() {
        return arenas.keySet();
    }

    public void setArenaDuration(String arenaName, int duration) throws IndexOutOfBoundsException {
        if (duration < 5) throw new IndexOutOfBoundsException();
        Arena a = arenas.get(arenaName);
        a.setDuration(duration);
    }

    public void setArenaMin(String arenaName, int min) throws IndexOutOfBoundsException, IllegalStateException {
        Arena a = arenas.get(arenaName);
        if (min <= 1) throw new IndexOutOfBoundsException();
        if (min > a.getMaxPlayers()) throw new IllegalStateException();
        a.setMinPlayers(min);
    }

    public void setArenaWinKills(String arenaName, int kills) throws IndexOutOfBoundsException, IllegalStateException {
        Arena a = arenas.get(arenaName);
        if (kills <= 0) throw new IndexOutOfBoundsException();
        a.setWinKills(kills);
    }

    public void sendArenaList(Player p) {
        arenas.forEach((n, a) -> p.spigot().sendMessage(a.getText()));
    }

    public void setArenaMax(String arenaName, int max) throws IndexOutOfBoundsException, IllegalStateException {
        Arena a = arenas.get(arenaName);
        if (max <= 1) throw new IndexOutOfBoundsException();
        if (max < a.getMinPlayers()) throw new IllegalStateException();
        a.setMaxPlayers(max);
    }

}
