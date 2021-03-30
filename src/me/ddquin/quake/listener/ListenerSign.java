package me.ddquin.quake.listener;

import me.ddquin.quake.Main;
import me.ddquin.quake.sign.SignManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;

public class ListenerSign implements Listener {

    private Main main;

    public ListenerSign(Main main) {
        this.main = main;
    }

    @EventHandler
    private void onMakeSign(SignChangeEvent e) {
        if (!e.getPlayer().hasPermission("quakeshot.admin")) {
            return;
        }

        Block block = e.getBlock();
        if (block.getType() != Material.SIGN && block.getType() != Material.WALL_SIGN)  {
            return;
        }
        Sign sign = (Sign) block.getState();
        main.getHandler().getSignManager().addSign(sign, e.getLines());
    }

    @EventHandler
    private void onInteractSign(PlayerInteractEvent e) {
        SignManager signManager = main.getHandler().getSignManager();
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = e.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.SIGN && block.getType() != Material.WALL_SIGN) return;
        Sign sign = (Sign) block.getState();
        if (!e.getPlayer().hasPermission("quakeshot.default")) {
            return;
        }
        signManager.interactSign(e.getPlayer(), sign);
    }

}
