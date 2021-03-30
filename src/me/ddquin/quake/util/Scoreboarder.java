package me.ddquin.quake.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public class Scoreboarder {

    public Scoreboard sb;

    private Objective o;

    public List<String> display;

    public Scoreboarder(String title) {
        this.sb = Bukkit.getScoreboardManager().getNewScoreboard();
        this.o = this.sb.registerNewObjective("obj", "dummy");
        this.o.setDisplayName(Util.color(title));
        this.o.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.display = new ArrayList<>();
    }

    public void add(String s) {
        this.display.add(s);
    }

    public void apply(Player p) {
        p.setScoreboard(this.sb);
    }

    public void update(boolean ordered) {
        for (String s : this.sb.getEntries())
            this.sb.resetScores(s);
        int size = this.display.size();
        for (int i = 0; i < size; i++)
            this.o.getScore(Util.color(this.display.get(i))).setScore(ordered ? (size - i) : 0);
    }

    public static void reset(Player p) {
        if (p == null) return;
        p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}
