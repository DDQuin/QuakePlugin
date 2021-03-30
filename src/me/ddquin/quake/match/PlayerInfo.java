package me.ddquin.quake.match;

import me.ddquin.quake.Main;
import me.ddquin.quake.Settings;
import me.ddquin.quake.stat.PlayerStat;
import me.ddquin.quake.stat.Stat;
import me.ddquin.quake.util.Util;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInfo implements Comparable<PlayerInfo> {

    private Map<Stat, Integer> stats;
    private UUID id;
    private Main main;
    private int cdTicks;
    private StoredPlayerData data;

    public PlayerInfo(UUID id, Main main) {
        this.id = id;
        this.main = main;
        stats = new HashMap<>();
        resetCd();
        for (Stat stat: Stat.getStoredStats()) {
            stats.put(stat, 0);
        }
        stats.put(Stat.LOSSES, 1);
        data = new StoredPlayerData(Bukkit.getPlayer(id));
    }

    public int getCdTicks() {
        return cdTicks;
    }

    public boolean isCooldown() {
        return cdTicks > 0;
    }

    public void resetCd() {
        cdTicks = Settings.hitCD;
    }

    public void makeWinner() {
        stats.put(Stat.LOSSES, 0);
        stats.put(Stat.WINS, 1);
    }

    public void tickCd(){
        Player p = getPlayer();
        if (p != null && cdTicks > 0) {
           TextComponent progress = new TextComponent(Util.getProgressBar(Util.getPercent(cdTicks - 1, Settings.hitCD )));
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, progress);
        }
        if (cdTicks > 0) cdTicks--;
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
        if (stat.isStored) return stats.get(stat);
        if (stat == Stat.HITRATIO)  {
            int hits = stats.get(Stat.KILLS);
            int misses = stats.get(Stat.MISSES);
            if (hits + misses == 0) return 0;
            return Util.round(((double) hits / (hits + misses) * 100), 1);
        }

        if (stat == Stat.KD)  {
            int kills = stats.get(Stat.KILLS);
            int deaths = stats.get(Stat.DEATHS);
            if (deaths == 0) return 0;
            return Util.round(((double) kills / deaths), 1);
        }
        
        return 0;
    }

    public void giveGun() {
        getPlayer().getInventory().setItem(getPlayer().getInventory().firstEmpty(), new ItemStack(Material.DIAMOND_HOE));
    }

    public void transferGameStatToMainStats() {
        PlayerStat ps = main.getHandler().getStatManager().getStat(id);
        for (Stat stat: Stat.getStoredStats()) {
            ps.addToStat(stat, stats.get(stat));
        }
    }

    public void loadData() {
        data.loadData(Bukkit.getPlayer(id));
    }

    public void incrementStat(Stat stat) {
        stats.put(stat, stats.get(stat) + 1);
    }

    public String getName() {
        return Bukkit.getOfflinePlayer(id).getName();
    }

    public Player getPlayer() {
        OfflinePlayer of = Bukkit.getOfflinePlayer(id);
        if (of.isOnline()) {
            return Bukkit.getPlayer(id);
        }
        return null;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public int compareTo(PlayerInfo o) {
        if (o.getStat(Stat.KILLS) > this.getStat(Stat.KILLS)) {
            return 1;
        } else if (o.getStat(Stat.KILLS) == this.getStat(Stat.KILLS)) return 0;
        return -1;
    }
}
