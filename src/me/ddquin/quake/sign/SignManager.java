package me.ddquin.quake.sign;

import me.ddquin.quake.Main;
import me.ddquin.quake.arena.Arena;
import me.ddquin.quake.arena.ArenaManager;
import me.ddquin.quake.match.Match;
import me.ddquin.quake.match.MatchManager;
import me.ddquin.quake.match.MatchState;
import me.ddquin.quake.util.ConfigManager;
import me.ddquin.quake.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.*;

public class SignManager {

    private Main main;
    private List<Location> signs;

    public SignManager(Main main) {
        this.main = main;
        signs = new ArrayList<>();
    }

    public void loadSigns() {
        ConfigManager c = new ConfigManager("signs", main, false);
        if (c.hasKey("signs") && !c.getString("signs").equalsIgnoreCase("empty")) {
            signs.addAll(c.getLocList("signs"));
           // for (final String locString : c.getStringList("signs")) {
           //     signs.add(Util.getLocFromString(locString));
          //  }
        }

    }

    public void saveSigns() {
        ConfigManager c = new ConfigManager("signs", main, true);
        if (signs.isEmpty()) {
            c.set("signs", "EMPTY");
        } else {
           // List<String> locStrings = new ArrayList<>();
            //signs.forEach(l -> locStrings.add(Util.getRawLocString(l)));
            c.set("signs", signs);
        }
    }

    public void tick() {
        ArenaManager arenaManager = main.getHandler().getArenaManager();
        MatchManager matchManager = main.getHandler().getMatchManager();
        List<Location> signsToRemove = new ArrayList<>();
        for (Location loc : signs) {
            Block block = loc.getBlock();
            if (block.getType() != Material.SIGN && block.getType() != Material.WALL_SIGN) {
                signsToRemove.add(loc);
            } else {
                Sign sign = (Sign) block.getState();
                String arenaName = ChatColor.stripColor(sign.getLines()[1]);
                if (!arenaManager.isArena(arenaName)) {
                    signsToRemove.add(loc);
                } else {
                    Arena a = arenaManager.getArena(arenaName);
                    sign.setLine(1, Util.color("&1" + arenaName));
                    sign.setLine(0, Util.color("&2QuakeShot"));
                    if (matchManager.arenaInUse(a)) {
                        Match m = matchManager.getMatch(a);
                        sign.setLine(2, Util.color("&4" + m.getState().toString()));

                        int playersNeeded = m.getState() == MatchState.NEW ? a.getMinPlayers() : a.getMaxPlayers();
                        sign.setLine(3, Util.color("&8" + m.getPlaying() + "/" + playersNeeded + " &9" + m.getSecondsLeft() + "s"));
                    } else {
                        sign.setLine(2, Util.color("&5OPEN"));
                        sign.setLine(3, Util.color(""));
                    }
                }
                sign.update();
            }
        }
        signs.removeAll(signsToRemove);
    }

    public void interactSign(Player p, Sign sign) {
        if (!signs.contains(sign.getBlock().getLocation())) return;
        p.performCommand("qs j " + ChatColor.stripColor(sign.getLine(1)));
    }


    public void addSign(Sign sign, String[] lines) {
        ArenaManager arenaManager = main.getHandler().getArenaManager();
        if (arenaManager.isArena(lines[1]) && lines[0].equalsIgnoreCase("QuakeShot")) {
            signs.add(sign.getBlock().getLocation());
        }
    }


}
