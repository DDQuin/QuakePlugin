package me.ddquin.quake;

import me.ddquin.quake.arena.ArenaManager;
import me.ddquin.quake.match.MatchManager;
import me.ddquin.quake.sign.SignManager;
import me.ddquin.quake.stat.StatManager;

public class Handler {

    private ArenaManager arenaManager;
    private StatManager statManager;
    private MatchManager matchManager;
    private SignManager signManager;
    private Main main;

    public Handler(Main main) {
        this.main = main;
        arenaManager = new ArenaManager(main);
        statManager = new StatManager(main);
        statManager.loadPlayerStats();
        arenaManager.loadArenas();
        matchManager = new MatchManager(main);
        signManager = new SignManager(main);
        signManager.loadSigns();
    }

    public void tick() {
        matchManager.tick();
        signManager.tick();
    }

    public Main getMain() {
        return main;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public StatManager getStatManager() {
        return statManager;
    }

    public MatchManager getMatchManager() {
        return matchManager;
    }

    public SignManager getSignManager() {
        return signManager;
    }
}
