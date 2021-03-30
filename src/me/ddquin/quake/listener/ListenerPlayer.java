package me.ddquin.quake.listener;

import me.ddquin.quake.Main;
import me.ddquin.quake.stat.Stat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class ListenerPlayer implements Listener {

    private Main main;

    public ListenerPlayer(Main main) {
        this.main = main;
    }


}
