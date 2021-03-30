package me.ddquin.quake.match;

import me.ddquin.quake.Main;
import me.ddquin.quake.arena.Arena;
import me.ddquin.quake.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchManager {

    private Main main;
    private Map<Arena, Match> matches;


    public MatchManager(Main main) {
        this.main = main;
        matches = new HashMap<>();
    }


    public void tick() {
        for (Match m: matches.values()) {
            m.tick();
            if (m.getState() == MatchState.FINISHED) {
                deleteMatch(m);
            }
        }
    }

    public void joinPlayer(Player p, Arena a) {
        Match m;
        if (isNewMadeMatch(a)) {
            m = new Match(a, main);
            matches.put(a, m);
        } else {
            m = matches.get(a);
        }
        m.addPlayer(p);
        p.sendMessage(Util.prefix + ChatColor.AQUA +  "You have joined the match");

    }

    public boolean isNewMadeMatch(Arena a) {
        return !matches.containsKey(a) && a.isReady();
    }

    public boolean isNewMatch(Arena a) {
        if (matches.containsKey(a)) {
            Match m = matches.get(a);
            return m.getState() == MatchState.NEW;
        }
       return false;
    }

    public List<String> getMatchArenas() {
        List<String> arenaInMatch = new ArrayList<>();
        matches.forEach((a, m) -> arenaInMatch.add(a.getName()));
        return arenaInMatch;
    }

    public boolean isInSameMatch(Player p, Player p2) {
        for (Match m: matches.values()) {
            if (m.isPlaying(p) && m.isPlaying(p2)) {
                return true;
            }
        }
        return false;
    }

    public void deleteMatch(Match m) {
        matches.remove(m.getArena());
    }

    public PlayerInfo getMatchPlayer(Player p)  {
        for (Match m: matches.values()) {
            if (m.isPlaying(p)) {
                return m.getMatchPlayer(p);
            }
        }
        return null;
    }

    public boolean arenaReadyToJoin(Arena a) {
        return (!arenaInUse(a) && a.isReady()) || (arenaInUse(a) && matches.get(a).canJoin());
    }

    public boolean waitingIsFull(Arena a) {
        return (arenaInUse(a) && matches.get(a).isWaitingFull());
    }

    public boolean isNewState(Arena a) {
        return (arenaInUse(a) && matches.get(a).isNewState());
    }

    public boolean arenaInUse(Arena a) {
        return matches.containsKey(a);
    }

    public Match getMatch(Player p) {
        for (Match m: matches.values()) {
            if (m.isPlaying(p)) {
                return m;
            }
        }
        return null;
    }

    public Match getMatch(Arena a) {
        return matches.get(a);
    }

    public void forceEndAll() {
        matches.forEach((a, m) -> m.forceEnd());
    }

    public List<String> getMatchInfos() {
        List<String> matchInfos = new ArrayList<>();
        for (Match m: matches.values()) {
            String matchInfo = m.toString();
            matchInfos.add(matchInfo);
        }
        return matchInfos;
    }

    public void forceEnd(Arena a) {
        if (arenaInUse(a)) {
            matches.get(a).forceEnd();
        }
    }

    public boolean isPlayingMatch(Player p) {
        for (Match m: matches.values()) {
            if (m.isPlaying(p) && m.getState() == MatchState.STARTED) {
                return true;
            }
        }
        return false;
    }

    public void sendMatchesMessage(Player p) {
        matches.forEach((a, m) -> p.spigot().sendMessage(m.toText()));
    }

    public boolean isInMatch(Player p) {
        for (Match m: matches.values()) {
            if (m.isPlaying(p)) {
                return true;
            }
        }
        return false;
    }

}
