package me.ddquin.quake.stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public enum Stat {

    WINS(true, true, false),
    LOSSES(true, true, false),
    PLAYED(false, true, false),
    KILLS(true, true, true),
    MISSES(true, true, true),
    DEATHS(true, true, true),
    WINRATIO(false, false, false),
    KD(false, false, true),
    HITRATIO(false, false, true);

    public boolean isStored;
    public boolean isInGame;
    public boolean isInt;

    Stat(boolean isStored, boolean isInt, boolean isInGame) {
        this.isStored = isStored;
        this.isInt = isInt;
        this.isInGame = isInGame;
    }

    public Comparator<PlayerStat> getComparator() {
        return (o1, o2) -> {
            double diff = o2.getStat(this) - o1.getStat(this);
            if (diff > 0) return 1;
            if (diff < 0) return -1;
            return 0;
        };
    }

    public String title() {
        return this.toString().substring(0, 1).toUpperCase() + this.toString().substring(1).toLowerCase();
    }

    public static List<Stat> getInGameStats() {
        List<Stat> inGame = new ArrayList<>();
        for (Stat stat: Stat.values()) {
            if (stat.isInGame) inGame.add(stat);
        }
        return inGame;
    }

    public static List<Stat> getStoredStats() {
        List<Stat> stored = new ArrayList<>();
        for (Stat stat: Stat.values()) {
            if (stat.isStored) stored.add(stat);
        }
        return stored;
    }

    public static List<String> getStoredStatString() {
        List<String> storedString = new ArrayList<>();
        for (Stat stat: Stat.values()) {
            if (stat.isStored) {
                storedString.add(stat.title());
            }
        }
        return storedString;
    }

    public String getKey() {
        return this.toString().toLowerCase();
    }
}
