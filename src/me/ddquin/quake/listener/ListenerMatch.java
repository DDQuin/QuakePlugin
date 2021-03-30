package me.ddquin.quake.listener;

import me.ddquin.quake.Main;
import me.ddquin.quake.match.Match;
import me.ddquin.quake.match.MatchManager;
import me.ddquin.quake.match.MatchState;
import me.ddquin.quake.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ListenerMatch implements Listener {

    private Main main;

    public ListenerMatch(Main main) {
        this.main = main;
    }

    @EventHandler
    private void onLeaveMatch(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        MatchManager matchManager = main.getHandler().getMatchManager();
        Match m = matchManager.getMatch(p);
        if (m == null) return;
        if (m.isNewState() || m.getState() == MatchState.WAITING) {
            m.leavePlayerForce(p);
        } else {
            m.leavePlayer(p);
        }
    }

    @EventHandler
    private void onHunger(FoodLevelChangeEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) {
            return;
        }
        Player p = (Player) e.getEntity();
        MatchManager matchManager = main.getHandler().getMatchManager();
        if (matchManager.isInMatch(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlace(BlockPlaceEvent e) {
        Player p = (Player) e.getPlayer();
        MatchManager matchManager = main.getHandler().getMatchManager();
        if (matchManager.isInMatch(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onHit(EntityDamageEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) {
            return;
        }
        Player p = (Player) e.getEntity();
        MatchManager matchManager = main.getHandler().getMatchManager();
        if (matchManager.isInMatch(p)) {
            e.setCancelled(true);
        }
    }



    @EventHandler
    private void onBreak(BlockBreakEvent e) {
        Player p = (Player) e.getPlayer();
        MatchManager matchManager = main.getHandler().getMatchManager();
        if (matchManager.isInMatch(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void shootGun(PlayerInteractEvent e) {
        MatchManager matchManager = main.getHandler().getMatchManager();
        if (e.getItem() == null) return;
        if (e.getItem().getType() != Material.DIAMOND_HOE) return;
        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction()  == Action.RIGHT_CLICK_BLOCK) && matchManager.isPlayingMatch(e.getPlayer())) {
            Match m = matchManager.getMatch(e.getPlayer());
            m.shoot(e.getPlayer());
        }
    }

    @EventHandler
    private void damageTaken(EntityDamageEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) return;
        Player p = (Player) e.getEntity();
        MatchManager matchManager = main.getHandler().getMatchManager();
        if (matchManager.isInMatch(p)) e.setCancelled(true);
    }

    @EventHandler
    private void fireWork(EntityDamageByEntityEvent e) {
        if (e.getDamager().getType() != EntityType.FIREWORK) return;
        Firework fw = (Firework) e.getDamager();
        if (fw.getFireworkMeta().getLore().get(0).equalsIgnoreCase("quake")) e.setCancelled(true);

    }

    @EventHandler
    private void pickUp(EntityPickupItemEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) return;
        Player p = (Player) e.getEntity();
        MatchManager matchManager = main.getHandler().getMatchManager();
        if (matchManager.isInMatch(p)) e.setCancelled(true);
    }


}
