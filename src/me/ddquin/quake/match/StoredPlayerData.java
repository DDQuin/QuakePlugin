package me.ddquin.quake.match;

import me.ddquin.quake.Main;
import me.ddquin.quake.Settings;
import me.ddquin.quake.util.Scoreboarder;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

public class StoredPlayerData {

    private double health;
    private int hunger;
    private Scoreboard sb;
    private float saturation;
    private Location loc;
    private GameMode gameMode;
    private ItemStack[] storage;
    private ItemStack[] extraa;
    private ItemStack[] armor;

    public StoredPlayerData(Player p) {
        storage = p.getInventory().getStorageContents().clone();
        extraa = p.getInventory().getExtraContents().clone();
        armor = p.getInventory().getArmorContents().clone();
        this.health = p.getHealth();
        this.hunger = p.getFoodLevel();
        this.saturation = p.getSaturation();
        this.loc = p.getLocation().clone();
        this.sb = p.getScoreboard();
        this.gameMode = p.getGameMode();
    }

    public void loadData(Player p) {
        p.setHealth(health);
        p.setFoodLevel(hunger);
        p.setSaturation(saturation);
        p.getInventory().setArmorContents(armor);
        p.getInventory().setExtraContents(extraa);
        p.getInventory().setStorageContents(storage);
        if (Settings.hub != null) {
            p.teleport(Settings.hub);
        }
        else {
            p.teleport(loc);
        }
        if (sb != null) {
            p.setScoreboard(sb);
        }
        else {
            Scoreboarder.reset(p);
        }
        p.setGameMode(gameMode);
    }
}
