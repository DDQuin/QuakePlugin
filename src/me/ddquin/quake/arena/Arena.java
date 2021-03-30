package me.ddquin.quake.arena;

import me.ddquin.quake.util.Util;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import javax.xml.soap.Text;
import java.util.ArrayList;
import java.util.List;
import static me.ddquin.quake.util.Util.color;

public class Arena {

    private String name;
    private int minPlayers;
    private int maxPlayers;
    private int winKills;
    private int duration;
    private List<Location> spawnLocs;

    public Arena(String name) {
        this.name = name;
        minPlayers = 2;
        winKills = 20;
        maxPlayers = 10;
        duration = 300;
        spawnLocs = new ArrayList<>();
    }

    public boolean isReady() {
        return spawnLocs.size() >= maxPlayers;
    }

    public void addSpawn(Location newSpawn) {
        spawnLocs.add(newSpawn.clone());
    }

    public void removeSpawns() {
        spawnLocs.clear();
    }

    public void setMinPlayers(int min) { minPlayers = min; }

    public void setWinKills(int winKills) { this.winKills = winKills; }

    public void setMaxPlayers(int max) { maxPlayers = max; }

    public void setSpawns(List<Location> spawns) {
        spawnLocs = spawns;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getDuration() {
        return duration;
    }

    public int getWinKills() {
        return winKills;
    }

    public List<Location> getSpawns() {
        return spawnLocs;
    }

    public TextComponent getText() {
        TextComponent textMain = new TextComponent();
        String isReady = !isReady() ? isReady = color("&c Not ready!") : "";
        textMain.addExtra(color("&6 " + name + ": &9" + duration + "s" + " &6Min: &9"+ minPlayers + " &6Max: &9" + maxPlayers + " &6Win kills: &9" + winKills +  isReady));
        List<String> spawnStrings = new ArrayList<>();
        spawnLocs.forEach(s -> spawnStrings.add(Util.getLocString(s)));
        TextComponent spawnText = new TextComponent(color("&6 Spawns: &9" + spawnStrings.size()));
        spawnText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GOLD + spawnStrings.toString()).create()));
        textMain.addExtra(spawnText);

        return textMain;
    }


}
