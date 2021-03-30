package me.ddquin.quake.commands;

import me.ddquin.quake.Main;
import me.ddquin.quake.Settings;
import me.ddquin.quake.arena.Arena;
import me.ddquin.quake.arena.ArenaManager;
import me.ddquin.quake.match.Match;
import me.ddquin.quake.match.MatchManager;
import me.ddquin.quake.match.MatchState;
import me.ddquin.quake.stat.Stat;
import me.ddquin.quake.util.ConfigManager;
import me.ddquin.quake.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

import static me.ddquin.quake.util.Util.color;
import static me.ddquin.quake.util.Util.getLineSeperator;

public class CommandsQuakeShot implements TabExecutor {

    private static final String[] COMMANDS = {"help", "credits", "stat", "j", "join", "l", "leave",
            "hub", "matches", "top", "end", "removehub"};

    private Main main;

    public CommandsQuakeShot(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Util.NO_PERMISSION);
            return false;
        }

        if (args.length == 0) {
            argHelp(commandSender);
        } else if (args[0].equalsIgnoreCase("stat")) {
            argStat(commandSender, args);
        } else if (args[0].equalsIgnoreCase("top")) {
            argShowTop(commandSender, args);
        } else if (args[0].equalsIgnoreCase("help")) {
            argHelp(commandSender);
        } else if (args[0].equalsIgnoreCase("credits")) {
            argCredits(commandSender);
        } else if (args[0].equalsIgnoreCase("hub")) {
            argHub(commandSender);
        } else if (args[0].equalsIgnoreCase("removehub")) {
            argRMHub(commandSender, args);
        } else if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("j")) {
            argJoin(commandSender, args);
        } else if (args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("l")) {
            argLeave(commandSender, args);
        } else if (args[0].equalsIgnoreCase("end")) {
            argEnd(commandSender, args);
        } else if (args[0].equalsIgnoreCase("matches")) {
            argListMatches(commandSender, args);
        }

        return false;
    }

    private void argRMHub(CommandSender s, String[] args) {
        if (!s.hasPermission("quakeshot.admin")) {
            s.sendMessage(Util.NO_PERMISSION);
            return;
        }
        s.sendMessage(Util.prefix  + ChatColor.RED + "Removed hub, now players will teleport back to last location when finishing matches");
        Settings.hub = null;
        ConfigManager c = new ConfigManager("config", main , false);
        c.set("hub", Settings.hub);
    }

    private void argHub(CommandSender s) {
        Player p = (Player) s;
        if (!p.hasPermission("quakeshot.admin")) {
            p.sendMessage(Util.NO_PERMISSION);
        } else {
            Settings.hub = p.getLocation();
            ConfigManager c = new ConfigManager("config", main , false);
            c.set("hub", Settings.hub);
            p.sendMessage(Util.prefix + ChatColor.AQUA + "The hub has been set to this location");
        }
    }

    private void argCredits(CommandSender s) {
        s.sendMessage(getLineSeperator(10) + color("&6QuakeShot Credits") + getLineSeperator(10));
        s.sendMessage(ChatColor.AQUA + "QuakeShot Plugin created by " + main.getDescription().getAuthors().get(0));
        s.sendMessage(ChatColor.AQUA + "Version: " + main.getDescription().getVersion());
        s.sendMessage(getLineSeperator(35));
    }

    private void argJoin(CommandSender s, String[] args) {
        MatchManager matchManager = main.getHandler().getMatchManager();
        Player p = (Player) s;
        if (matchManager.isInMatch(p)) {
            p.sendMessage(Util.prefix + ChatColor.RED + "You are already in a match");
            return;
        }
        if (args.length == 1) {
            s.sendMessage(Util.prefix + ChatColor.RED + "Please specify an arena");
            return;
        }
        ArenaManager arenaManager = main.getHandler().getArenaManager();
        String arenaName = args[1];
        if (!arenaManager.isArena(arenaName)) {
            s.sendMessage(Util.prefix + ChatColor.RED + "This is not a valid arena");
            return;
        }
        Arena a = arenaManager.getArena(arenaName);
        if (!arenaManager.getFreeArenas().contains(a)) {
            s.sendMessage(Util.prefix + ChatColor.RED + "The arena is not ready to be played in");
            return;
        }
        if (matchManager.waitingIsFull(a)) {
            s.sendMessage(Util.prefix + ChatColor.RED + "Match is full");
            return;
        }
        if (!matchManager.arenaReadyToJoin(a)) {
            s.sendMessage(Util.prefix + ChatColor.RED + "Match already started");
            return;
        }
        matchManager.joinPlayer((Player) s, a);
    }

    private void argLeave(CommandSender s, String[] args) {
        Player p = (Player) s;
        MatchManager matchManager = main.getHandler().getMatchManager();
        if (!matchManager.isInMatch(p)) {
            p.sendMessage(Util.prefix + ChatColor.RED + "You are currently not in a match");
            return;
        }
        Match m = matchManager.getMatch(p);
        if (m.isNewState() || m.getState() == MatchState.WAITING) {
            p.sendMessage(Util.prefix + ChatColor.AQUA + "You have left the match");
            m.leavePlayerForce(p);
            return;
        }
        p.sendMessage(Util.prefix + ChatColor.RED + "You cannot leave in the middle of a match");
    }

    private void argListMatches(CommandSender s, String[] args) {
        if (!s.hasPermission("quakeshot.admin")) {
            s.sendMessage(Util.NO_PERMISSION);
            return;
        }
        s.sendMessage(getLineSeperator(10) + color("&6 QuakeShot Matches") + getLineSeperator(10));
        main.getHandler().getMatchManager().sendMatchesMessage((Player) s);
        s.sendMessage(getLineSeperator(35));
    }


    private void argHelp(CommandSender s) {
        s.sendMessage(getLineSeperator(10) + color("&6QuakeShot Help") + getLineSeperator(10));
        s.sendMessage(Util.color( "&b/qs help - &7Shows the commands available"));
        s.sendMessage(Util.color( "&b/qs credits - &7Shows the credits for the plugin"));
        s.sendMessage(Util.color( "&b/qs stat <playername> - &7Shows the stats of the given player or /qs stat shows your stats"));
        s.sendMessage(Util.color( "&b/qs j/join <arenaname> - &7Joins you to the the specified arena"));
        s.sendMessage(Util.color( "&b/qs l/leave - &7Leaves the arena"));
        s.sendMessage(Util.color( "&b/qs end <arenaname> - &7Force ends the match"));
        s.sendMessage(Util.color( "&b/qs hub - &7Sets the hub to where you are standing, which is where players will be teleported after a game finishes"));
        s.sendMessage(Util.color( "&b/qs removehub - &7Removes the hub so players will teleport back to their last location after a game finishes"));
        s.sendMessage(Util.color( "&b/qs matches - &7Lists the current matches going on"));
        s.sendMessage(Util.color( "&b/qs top <wins/losses/played/kills/deaths/kd/winratio/hitratio> - &7Shows the top players for a stat, defaulted to wins"));
        s.sendMessage(getLineSeperator(35));
    }

    private void argStat(CommandSender s, String[] args) {
        Player p = (Player) s;
        if (args.length == 1) {
            main.getHandler().getStatManager().sendStatMessage(p, p.getUniqueId());
            return;
        }
        OfflinePlayer of = Bukkit.getOfflinePlayer(args[1]);
        UUID id;
        if (of != null && main.getHandler().getStatManager().containsId(of.getUniqueId())) {
            id = of.getUniqueId();
        } else {
            Player playerStat = Bukkit.getPlayer(args[1]);
            if (playerStat == null) {
                p.sendMessage(Util.prefix + ChatColor.RED + args[1] + " is not a valid player");
                return;
            }
            id = playerStat.getUniqueId();
        }
        main.getHandler().getStatManager().sendStatMessage(p, id);
    }

    private void argEnd(CommandSender s, String[] args) {
        if (!s.hasPermission("quakeshot.admin")) {
            s.sendMessage(Util.NO_PERMISSION);
            return;
        }
        MatchManager matchManager = main.getHandler().getMatchManager();
        ArenaManager arenaManager = main.getHandler().getArenaManager();
        String arenaName = args[1];
        Arena a = arenaManager.getArena(arenaName);
        if (a == null) {
            s.sendMessage(Util.prefix + ChatColor.RED + "That is not a valid arena");
            return;
        }
        if (!matchManager.arenaInUse(a)) {
            s.sendMessage(Util.prefix + ChatColor.RED + "The arena is not in use");
            return;
        }
        matchManager.forceEnd(a);
        s.sendMessage(Util.prefix + ChatColor.AQUA + "Arena " + a.getName() + " has been force ended");
    }

    private void argShowTop(CommandSender s, String[] args) {
        Player p = (Player) s;
        if (args.length == 1) {
            main.getHandler().getStatManager().showTop(p, Stat.WINS, 20);
            return;
        }
        String stat = args[1];
        if (main.getHandler().getStatManager().getStats().contains(stat.toLowerCase())) {
            main.getHandler().getStatManager().showTop(p, Stat.valueOf(stat.toUpperCase()), 20);
        } else {
            p.sendMessage(Util.prefix + ChatColor.RED + "That is not a valid stat");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        final List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), completions);
            Collections.sort(completions);
            return completions;
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("stat"))) {
            List<String> playerNames = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> playerNames.add(p.getName()));
            StringUtil.copyPartialMatches(args[1], playerNames, completions);
            Collections.sort(completions);
            return completions;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("top")) {
            StringUtil.copyPartialMatches(args[1], main.getHandler().getStatManager().getStats(), completions);
            Collections.sort(completions);
            return completions;
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("j") || args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("end"))) {
            StringUtil.copyPartialMatches(args[1], main.getHandler().getArenaManager().arenaNames(), completions);
            Collections.sort(completions);
            return completions;
        }
        return null;
    }
}
