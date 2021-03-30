package me.ddquin.quake.match;

import me.ddquin.quake.Main;
import me.ddquin.quake.Settings;
import me.ddquin.quake.arena.Arena;
import me.ddquin.quake.stat.Stat;
import me.ddquin.quake.util.Scoreboarder;
import me.ddquin.quake.util.Util;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.Color;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.RayTraceResult;


import java.util.*;
import java.util.List;

public class Match {

    private Arena arena;
    private Map<UUID, PlayerInfo> players;
    private Map<Location, Integer> onCooldownLocs;
    private MatchState state;
    private int ticks;
    private Main main;
    private Scoreboarder sb;

    public Match(Arena a, Main main) {
        this.arena = a;
        this.main = main;
        onCooldownLocs = new HashMap<>();
        players = new HashMap<>();
        state = MatchState.NEW;
        this.sb = new Scoreboarder(Util.prefix);
        final org.bukkit.scoreboard.Team team = this.sb.sb.registerNewTeam("Quake");
        team.setColor(ChatColor.AQUA);
    }

    public void tick() {
        if (state == MatchState.NEW) {
            if (players.isEmpty()) {
                state = MatchState.FINISHED;
            }
            else if (players.size() >= arena.getMinPlayers()) {
                state = MatchState.WAITING;
                ticks = Settings.waitTime * 20;
            }
        }
        else if (state == MatchState.WAITING) {
            ticks--;
            if (players.size() < arena.getMinPlayers()) {
                state = MatchState.NEW;
            }
            else if (ticks <= 0) {
                state = MatchState.STARTED;
                ticks = arena.getDuration() * 20;
                startMatch();
            }
        }
        else if (state == MatchState.STARTED) {
            ticks--;
            tickLocCD();
            players.values().forEach(PlayerInfo::tickCd);
            if (getHighest().getStat(Stat.KILLS) >= arena.getWinKills()) {
                finish(getHighest());
            }
            else if (ticks <= 0) {
                finish(getHighest());
            } else if (players.size() == 1) {
                finish(getHighest());
            }
        }
        updateScoreboard();

    }

    public void addPlayer(Player p) {
        PlayerInfo pi = new PlayerInfo(p.getUniqueId(), main);
        players.put(p.getUniqueId(), pi);
        sb.apply(p);
        this.sb.sb.getTeam("Quake").addEntry(p.getName());
        p.setGameMode(GameMode.SURVIVAL);
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setFlying(false);
        p.getInventory().clear();
        p.teleport(arena.getSpawns().get(0));
    }

    private void tickLocCD() {
        List<Location> locsToRemove = new ArrayList<>();
        for (Location loc : onCooldownLocs.keySet()) {
            int ticksLeft = onCooldownLocs.get(loc);
            if (ticksLeft == 0) locsToRemove.add(loc);
            else onCooldownLocs.put(loc, ticksLeft - 1);
        }
        locsToRemove.forEach(l -> onCooldownLocs.remove(l));
    }

    public void updateScoreboard() {
        if (players.size() <= 0) {
            return;
        }
        this.sb.display.clear();
        this.sb.add("&6Arena: &b" + arena.getName());
        if (state == MatchState.NEW) {
            this.sb.add("&6Min Players needed to start &7" + players.size() + "/" + arena.getMinPlayers());
        }
        else if (state == MatchState.WAITING) {
            this.sb.add("&6Match is starting soon &7" + players.size() + "/" + arena.getMaxPlayers());
            this.sb.add("&6Time Left");
            int seconds = (int)Math.ceil((double)ticks/20.0);
            this.sb.display.add("&7" + (int)Math.floor((float)seconds / 60) + ":" + String.format("%02d", seconds % 60));
        }
        else if (state == MatchState.STARTED) {
            List<PlayerInfo> sortedPlayers = new ArrayList<>();
            sortedPlayers.addAll(players.values());
            Collections.sort(sortedPlayers);
            for (PlayerInfo pi: sortedPlayers) {
                this.sb.add("&6" + pi.getName() + ": &b" + pi.getStatString(Stat.KILLS));
            }
            this.sb.add("&6Time Left");
            int seconds = (int)Math.ceil((double)ticks/20.0);
            this.sb.display.add("&7" + (int)Math.floor((float)seconds / 60) + ":" + String.format("%02d", seconds % 60));
        }
        this.sb.update(true);
    }

    private void startMatch() {
        players.values().forEach(p -> teleportPlayer(p.getPlayer(), 60));
        players.values().forEach(PlayerInfo::giveGun);
    }

    private void teleportPlayer(Player p, int cooldownTicks) {
        List<Location> freeLocs = new ArrayList<>();
        for (Location loc: arena.getSpawns()) {
            if (!onCooldownLocs.containsKey(loc)) {
                freeLocs.add(loc);
            }
        }
        Random rand = new Random();
        Location locToTp = freeLocs.get(rand.nextInt(freeLocs.size()));
        onCooldownLocs.put(locToTp, cooldownTicks);
        if (state == MatchState.FINISHED) return;
        p.teleport(locToTp);
    }

    public int getPlaying() {
        return players.size();
    }

    public int getSecondsLeft() {
        return (int)Math.ceil((double)ticks/20.0);
    }

    public void shoot(Player p) {
        if (players.get(p.getUniqueId()).isCooldown()) {
            // Make some sound
            return;
        }
        Player toKill = null;
        Location startLoc = p.getEyeLocation().clone();
        startLoc.setYaw(p.getEyeLocation().getYaw() + 90);
        startLoc.setPitch(0);
        Location handLoc = p.getLocation().clone().add(0, 1, 0);
        handLoc.add(startLoc.getDirection().normalize().multiply(0.2));
        RayTraceResult ray = p.getWorld().rayTrace(handLoc, p.getEyeLocation().getDirection(), 20, FluidCollisionMode.ALWAYS, false, 0.2, (e -> !e.equals(p)));
        if (ray != null && ray.getHitEntity() != null) {
            if (ray.getHitEntity().getType() == EntityType.PLAYER) {
                Player hit = (Player) ray.getHitEntity();
                if (isPlaying(hit) && hit != p) toKill = hit;
            }
        }
        Util.drawLine(handLoc, p.getEyeLocation().clone().add(p.getEyeLocation().getDirection().normalize().multiply(20)), 0.1, Color.BLUE);
        if (toKill != null) {
            killPlayer(p, toKill);
        } else {
            players.get(p.getUniqueId()).incrementStat(Stat.MISSES);
        }
        players.get(p.getUniqueId()).resetCd();
    }

    public void killPlayer(Player shooter, Player dead) {
        players.get(shooter.getUniqueId()).incrementStat(Stat.KILLS);
        //Make sound and firework where player died
        players.get(dead.getUniqueId()).incrementStat(Stat.DEATHS);
        Random rand = new Random();
        String randomDeathMessage =  Settings.deathMessages == null ? "&9%victim% &6was killed by &9%killer%" : Settings.deathMessages.get(rand.nextInt(Settings.deathMessages.size()));
        broadcast(Util.getDeathMessage(randomDeathMessage, shooter.getName(), dead.getName()));
        Util.spawnFireworks(dead.getLocation().clone());
        main.getServer().getScheduler().runTaskLater(main, () -> teleportPlayer(dead, 60), 1L);
    }

    public Location getRandomSpawn() {
        Random rand = new Random();
        return arena.getSpawns().get(rand.nextInt(arena.getSpawns().size())).clone();
    }

    public boolean isNewState() {
        return state == MatchState.NEW;
    }

    public boolean isWaitingFull() {
        return (state == MatchState.WAITING && players.size() > arena.getMaxPlayers());
    }


    public boolean canJoin() {
        return isNewState() || (!isWaitingFull() && state != MatchState.STARTED);
    }

    public PlayerInfo getHighest() {
        List<PlayerInfo> playersToSort = new ArrayList<>();
        playersToSort.addAll(players.values());
        Collections.sort(playersToSort);
        return playersToSort.get(0);
    }

    public void finish(PlayerInfo winnerPi) {
        winnerPi.makeWinner();
        broadcast(Util.color(Util.prefix + "&bMatch has been finished and &6" + winnerPi.getName() + "&b won with &7" + winnerPi.getStatString(Stat.KILLS) + "&b kills"));
        for (PlayerInfo pi: players.values()) {
            Player playerToSend = pi.getPlayer();
            if (playerToSend != null) {
                playerToSend.sendMessage(Util.LINE_SEPERATOR);
                playerToSend.sendMessage(Util.color("&b" + pi.getName() +"'s stats"));
                for (Stat stat: Stat.getInGameStats()) {
                    playerToSend.sendMessage(Util.color("&b" + stat.title() + ": &7" + pi.getStatString(stat)));
                }
                playerToSend.sendMessage(Util.LINE_SEPERATOR);
            }
        }
        players.values().forEach(PlayerInfo::transferGameStatToMainStats);
        if (Settings.SQLOn) players.values().forEach(pi -> main.getHandler().getStatManager().savePlayer(pi.getId()));
        players.values().forEach(PlayerInfo::loadData);
        players.clear();
        state = MatchState.FINISHED;
    }

    public void forceEnd() {
        broadcast(Util.prefix + ChatColor.RED + "Match has been force ended");
        players.values().forEach(PlayerInfo::loadData);
        players.clear();
        state = MatchState.FINISHED;
    }

    public void broadcast(String message) {
        for (PlayerInfo pi: players.values()) {
            Player p = pi.getPlayer();
            if (p != null) p.sendMessage(message);
        }
    }

    public void leavePlayerForce(Player p) {
        players.get(p.getUniqueId()).loadData();
        players.remove(p.getUniqueId());
    }

    public Arena getArena() {
        return arena;
    }

    public boolean isPlaying(Player p) {
        for (PlayerInfo pi: players.values()) {
            if (pi.getPlayer().equals(p)) return true;
        }
        return false;
    }

    public PlayerInfo getMatchPlayer(Player p) {
        if (players.containsKey(p.getUniqueId())) {
            return players.get(p.getUniqueId());
        }
        return null;
    }

    public void leavePlayer(Player p) {
        players.get(p.getUniqueId()).loadData();
        players.get(p.getUniqueId()).transferGameStatToMainStats();
        players.remove(p.getUniqueId());
    }

    public TextComponent toText() {
        int seconds = (int)Math.ceil((double)ticks/20.0);
        TextComponent main = new TextComponent(Util.color("&6Arena: &9" + arena.getName() + " &6State: &9" + state + " &6Duration: &9" + seconds + "s "));
        TextComponent points = new TextComponent(ChatColor.AQUA + "Points");
        String playerPoints = "";
        for (PlayerInfo pi: players.values()) {
            playerPoints += ChatColor.GOLD + pi.getName() + ": " + ChatColor.BLUE + pi.getStatString(Stat.KILLS) + ", ";
        }
        points.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(playerPoints).create()));
        main.addExtra(points);

        return main;
    }

    public MatchState getState() {
        return state;
    }
}
