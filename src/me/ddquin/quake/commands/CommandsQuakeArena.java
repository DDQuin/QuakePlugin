package me.ddquin.quake.commands;

import me.ddquin.quake.Main;
import me.ddquin.quake.arena.Arena;
import me.ddquin.quake.arena.ArenaManager;
import me.ddquin.quake.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import static me.ddquin.quake.util.Util.color;
import static me.ddquin.quake.util.Util.prefix;
import static me.ddquin.quake.util.Util.LINE_SEPERATOR;
import static me.ddquin.quake.util.Util.NO_PERMISSION;
import static me.ddquin.quake.util.Util.getLineSeperator;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandsQuakeArena implements TabExecutor {
    
    private static final String[] COMMANDS = {"create", "help", "list", "delete"};
    private static final String[] EDIT_COMMANDS = {"clearspawns", "addspawn", "duration",  "min", "max", "winkills"};
    private static final String[] NUMBER = {"10"};

    private Main main;

    public CommandsQuakeArena(Main main) {
        this.main = main;
    }
    
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(NO_PERMISSION);
            return false;
        }
        if (args.length == 0) {
            argHelp(commandSender);
        }

        else if (args[0].equalsIgnoreCase("create")) {
            argCreate(commandSender, args);
        }
        else if (args[0].equalsIgnoreCase("delete")) {
            argDelete(commandSender, args);
        }
        else if (args[0].equalsIgnoreCase("help")){
            argHelp(commandSender);
        }
        else if (args[0].equalsIgnoreCase("list")){
            argListArenas(commandSender);
        }
        else if (args.length >= 1){
            argEditArena(commandSender, args);
        }
        return false;
    }

    private void argHelp(CommandSender s) {
        s.sendMessage(getLineSeperator(10) + color("&6QuakeShot Help") + getLineSeperator(10));
        s.sendMessage(color ("&b/qa help - &7Shows the arena commands available"));
        s.sendMessage(color ("&b/qa create <name> - &7Creates the arena with the given name, but is not ready to be played in until spawns are set"));
        s.sendMessage(color ("&b/qa list - &7Shows all arenas"));
        s.sendMessage(color ("&b/qa delete <name> - &7Deletes the given arena"));
        s.sendMessage(color ("&b/qa [name] addspawn - &7Adds the location of where you are standing to the spawn list of the arena"));
        s.sendMessage(color ("&b/qa [name] clearspawns - &7Clears all spawns"));
        s.sendMessage(color ("&b/qa [name] min <number> - &7Sets the min number of players for the match to start"));
        s.sendMessage(color ("&b/qa [name] max <number> - &7Sets the max number of players allowed"));
        s.sendMessage(color ("&b/qa [name] winkills <number> - &7Sets the number of kills required to win"));
        s.sendMessage(color ("&b/qa [name] duration <time> - &7Sets the duration of how long the match will go on until a winner is decided from highest points"));
        s.sendMessage(getLineSeperator(35));
    }

    private void argListArenas(CommandSender s) {
        s.sendMessage(getLineSeperator(10) + color("&6 QuakeShot Arenas") + getLineSeperator(10));
        main.getHandler().getArenaManager().sendArenaList((Player) s);
        s.sendMessage(getLineSeperator(35));
    }

    private void argCreate(CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(prefix + ChatColor.RED + "Please supply the name of the arena to create!");
        } else {
            String arenaName = args[1];
            try {
                main.getHandler().getArenaManager().createArena(arenaName);
                s.sendMessage(prefix + ChatColor.GREEN + "You have successfully created the arena " + arenaName + ", now to add the spawn locations and edit duration and min/max players check /qa help");
            } catch (IllegalArgumentException e) {
                s.sendMessage(prefix + ChatColor.RED + "An arena with the name " + arenaName + " already exists!");
            }
        }

    }

    private void argDelete(CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(prefix + ChatColor.RED + "Please supply the name of the arena to delete!");
            return;
        }
        String arenaName = args[1];
        Arena arenaToDelete = main.getHandler().getArenaManager().getArena(arenaName);
        if (arenaToDelete == null) {
            s.sendMessage(prefix + ChatColor.RED + "That is not a valid arena");
            return;
        }
        try {
            main.getHandler().getArenaManager().deleteArena(arenaToDelete);
            s.sendMessage(prefix + ChatColor.GREEN + "You have successfully deleted the arena " + ChatColor.AQUA + arenaName);
        } catch (IllegalStateException e) {
            s.sendMessage(prefix + ChatColor.RED + "Arena in use");
        }
    }

    private void argEditArena(CommandSender s, String[] args) {
        Player p = (Player) s;
        String arenaName = args[0];
        if (!main.getHandler().getArenaManager().isArena(arenaName)) {
            p.sendMessage(prefix + ChatColor.RED + arenaName + " is not an arena");
            return;
        }
        if (args.length == 1) {
            p.sendMessage(prefix  + ChatColor.RED + "Please specify what it is you want to edit in " + arenaName);
            return;
        }
        String statToEdit = args[1];
        if (statToEdit.equalsIgnoreCase("addspawn")) {
            main.getHandler().getArenaManager().setArenaSpawn(arenaName, p.getLocation());
            p.sendMessage(prefix + ChatColor.GREEN + "Added spawn to " + arenaName);
            return;
        }
        if (statToEdit.equalsIgnoreCase("clearspawns")) {
            main.getHandler().getArenaManager().removeSpawns(arenaName);
            p.sendMessage(prefix + ChatColor.GREEN + "All spawns of " + arenaName + " removed");
            return;
        }
        if (statToEdit.equalsIgnoreCase("duration")) {
            if (args.length == 2) {
                p.sendMessage(prefix + ChatColor.RED + "Please specify a duration for the arena");
                return;
            }
            try {
                int duration = Integer.parseInt(args[2]);
                main.getHandler().getArenaManager().setArenaDuration(arenaName, duration);
                p.sendMessage(prefix  + ChatColor.GREEN + "Duration of arena has been set to " + duration);
                return;
            } catch (IllegalArgumentException e) {
                p.sendMessage(prefix + ChatColor.RED + "Please enter a whole number for the duration");
                return;
            } catch (IndexOutOfBoundsException e) {
                p.sendMessage(prefix + ChatColor.RED + "The duration must be more than 4 seconds!");
                return;
            }
        }
        if (statToEdit.equalsIgnoreCase("min")) {
            if (args.length == 2) {
                p.sendMessage(prefix + ChatColor.RED + "Please specify a number for the min players of the arena");
                return;
            }
            try {
                int min = Integer.parseInt(args[2]);
                main.getHandler().getArenaManager().setArenaMin(arenaName, min);
                p.sendMessage(prefix  + ChatColor.GREEN + "Minimum number of players of arena has been set to " + min);
                return;
            } catch (IllegalArgumentException e) {
                p.sendMessage(prefix + ChatColor.RED + "Please enter a whole number for the min");
                return;
            } catch (IndexOutOfBoundsException e) {
                p.sendMessage(prefix + ChatColor.RED + "The min number has to be more than 1");
                return;
            } catch (IllegalStateException e) {
                p.sendMessage(prefix + ChatColor.RED + "The min number cannot be more than the max");
                return;
            }
        }
        if (statToEdit.equalsIgnoreCase("max")) {
            if (args.length == 2) {
                p.sendMessage(prefix + ChatColor.RED + "Please specify a number for the max players of the arena");
                return;
            }
            try {
                int max = Integer.parseInt(args[2]);
                main.getHandler().getArenaManager().setArenaMax(arenaName, max);
                p.sendMessage(prefix  + ChatColor.GREEN + "Maximum number of players of arena has been set to " + max);
                return;
            } catch (IllegalArgumentException e) {
                p.sendMessage(prefix + ChatColor.RED + "Please enter a whole number for the max");
                return;
            } catch (IndexOutOfBoundsException e) {
                p.sendMessage(prefix + ChatColor.RED + "The max number has to be more than 1");
                return;
            } catch (IllegalStateException e) {
                p.sendMessage(prefix + ChatColor.RED + "The max number cannot be less than the min");
                return;
            }
        }
        if(statToEdit.equalsIgnoreCase("winkills")) {
            if (args.length == 2) {
                p.sendMessage(prefix + ChatColor.RED + "Please specify a number for the kills need to win");
                return;
            }
            try {
                int kills = Integer.parseInt(args[2]);
                main.getHandler().getArenaManager().setArenaWinKills(arenaName, kills);
                p.sendMessage(prefix  + ChatColor.GREEN + "Kills needed to win have been set to " + kills);
                return;
            } catch (IllegalArgumentException e) {
                p.sendMessage(prefix + ChatColor.RED + "Please enter a whole number for the kills");
                return;
            } catch (IndexOutOfBoundsException e) {
                p.sendMessage(prefix + ChatColor.RED + "The kills has to be more than 0");
                return;
            }
        }

        p.sendMessage(prefix + ChatColor.RED + "" + statToEdit + " is not a valid stat to edit, use /qa help to see what you can edit");
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        final List<String> completions = new ArrayList<>();
        List<String> commandMatches = new ArrayList<>();
        ArenaManager arenaManager = main.getHandler().getArenaManager();
        if (args.length == 1) {
            commandMatches.addAll(Arrays.asList(COMMANDS));
            commandMatches.addAll(arenaManager.arenaNames());
            StringUtil.copyPartialMatches(args[0], commandMatches, completions);
            Collections.sort(completions);
            return completions;
        } else if (args.length == 2 && arenaManager.arenaNames().contains(args[0])) {
            commandMatches.addAll(Arrays.asList(EDIT_COMMANDS));
            StringUtil.copyPartialMatches(args[1], commandMatches, completions);
            Collections.sort(completions);
            return completions;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            commandMatches.addAll(arenaManager.arenaNames());
            StringUtil.copyPartialMatches(args[1], commandMatches, completions);
            Collections.sort(completions);
            return completions;
        } else if (args.length == 3 && arenaManager.arenaNames().contains(args[0]) && args[1].equalsIgnoreCase("duration") || args[1].equalsIgnoreCase("min") || args[1].equalsIgnoreCase("max") || args[1].equalsIgnoreCase("winkills") ) {
            commandMatches.addAll(Arrays.asList(NUMBER));
            StringUtil.copyPartialMatches(args[2], commandMatches, completions);
            Collections.sort(completions);
            return completions;
        } else {
            return null;
        }
    }
}
