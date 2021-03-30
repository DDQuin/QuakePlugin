package me.ddquin.quake.stat;

import me.ddquin.quake.util.Util;
import org.bukkit.Bukkit;

import java.util.*;


public class PlayerStat {

    private UUID id;
    private Map<Stat, Integer> statMap;

    public PlayerStat(UUID id, int wins, int losses, int kills, int misses, int deaths) {
        statMap = new HashMap<>();
        this.id = id;
        statMap.put(Stat.WINS, wins);
        statMap.put(Stat.LOSSES, losses);
        statMap.put(Stat.KILLS, kills);
        statMap.put(Stat.MISSES, misses);
        statMap.put(Stat.DEATHS, deaths);
    }

    public PlayerStat(UUID id) {
        this.id = id;
        statMap = new HashMap<>();
        for (Stat stat: Stat.values()) {
            if (stat.isStored) statMap.put(stat, 0);
        }
    }

    public void addToStat(Stat stat, int amount) {
        statMap.put(stat, statMap.get(stat) + amount);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return Bukkit.getOfflinePlayer(id).getName();
    }

    public String getStatString(Stat stat) {
        long statInt = Math.round(getStat(stat));
        String statString = "";
        if (stat.isInt) {
            statString = "" + statInt;
        } else {
            statString = "" + getStat(stat);
        }
        return statString;
    }

    public double getStat(Stat stat) {
        if (stat.isStored) return statMap.get(stat);
        if (stat == Stat.HITRATIO)  {
            int hits = statMap.get(Stat.KILLS);
            int misses = statMap.get(Stat.MISSES);
            if (hits + misses == 0) return 0;
            return Util.round(((double) hits / (hits + misses) * 100), 1);
        }

        if (stat == Stat.WINRATIO)  {
            int wins = statMap.get(Stat.WINS);
            int losses = statMap.get(Stat.LOSSES);
            if (wins + losses == 0) return 0;
            return Util.round(((double) wins / (wins + losses) * 100), 1);
        }

        if (stat == Stat.KD)  {
            int kills = statMap.get(Stat.KILLS);
            int deaths = statMap.get(Stat.DEATHS);
            if (deaths == 0) return 0;
            return Util.round(((double) kills / deaths), 1);
        }

        if (stat == Stat.PLAYED) {
            return statMap.get(Stat.WINS) + statMap.get(Stat.LOSSES);
        }
        return 0;
    }

    public List<String> getPlayerStoredStats() {
        List<String> storedStats = new ArrayList<>();
        for (Stat stat: Stat.values()) {
            if (stat.isStored) {
                storedStats.add(String.valueOf(statMap.get(stat)));
            }
        }
        return storedStats;
    }

}
