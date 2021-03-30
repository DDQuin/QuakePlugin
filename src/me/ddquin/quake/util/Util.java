package me.ddquin.quake.util;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Util {

    public static final String prefix = ChatColor.DARK_GRAY + "[" + ChatColor.DARK_GREEN + "QuakeShot" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;

    public static final String NO_PERMISSION = prefix + ChatColor.RED + "You have no permission to use this command.";

    public static final String LINE_SEPERATOR = getLineSeperator(25);


    public static void spawnParticle(Location loc, Color color) {
        loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0 ,new Particle.DustOptions(color ,1));
    }

    public static String getLineSeperator(int number) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < number; i++) {
            sb.append(ChatColor.GRAY + "-");
        }
        return sb.toString();
    }

    public static int getPercent(int num, int maxNum) {
        int percent = (int)(((float)num/maxNum) * 100);
        //System.out.println(percent);
        return percent;


    }

    public static String getProgressBar(int percent) {
        List<String> progress = new ArrayList<>();
        for (int i = 0; i < 100; i+= 2) {
            if (i < percent) {
                progress.add(ChatColor.WHITE + "|");
            } else {
                progress.add(ChatColor.GOLD + "|");
            }
        }
        String progressBar = "";
        Collections.reverse(progress);
        for (String box: progress) {
            progressBar += box;
        }
        return  ChatColor.AQUA +  "[" +progressBar +  ChatColor.AQUA + "]" + ChatColor.RESET;
    }

    public static void drawLine(Location point1, Location point2, double space, Color color) {
        World world = point1.getWorld();
        double distance = point1.distance(point2);
        Vector p1 = point1.toVector();
        Vector p2 = point2.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
        double length = 0;
        for (; length < distance; p1.add(vector)) {
            world.spawnParticle(Particle.REDSTONE, p1.getX(), p1.getY(), p1.getZ(), 1, 0, 0, 0 ,new Particle.DustOptions(color ,1));
            length += space;
        }
    }

    public static void spawnFireworks(Location location){
        Location loc = location;
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.setLore(Arrays.asList("quake"));
        fwm.setPower(2);
        fwm.addEffect(FireworkEffect.builder().withColor(Color.RED).flicker(true).build());

        fw.setFireworkMeta(fwm);
        fw.detonate();
    }

    public static String getDeathMessage(String message, String killer, String victim) {
        message = message.replaceAll("%killer%", killer);
        message = message.replaceAll("%victim%", victim);
        return color(message);
    }



    public static String getLineSeperatorMessage(int number, String message) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < number; i++) {
            sb.append(ChatColor.GRAY + "-");
        }
        return sb.toString();
    }

    public static String getLocString(Location loc) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        return color("&7(&6X: &9" + x + " &6Y: &9" + y + " &6Z: &9" + z + "&7)&r");
    }

    public static String getRawLocString(Location l) {
        return l.getWorld().getName() + ";" + Math.round(l.getX() * 10.0) / 10.0 + ";" + Math.round(l.getY() * 10.0) / 10.0 + ";" + Math.round(l.getZ() * 10.0) / 10.0 + ";" + Math.round(l.getYaw() * 10.0f) / 10.0f + ";" + Math.round(l.getPitch() * 10.0f) / 10.0f + ";";
    }

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }


    public static Location getLocFromString(String locString) {
        try {
            final String[] args = locString.split(";");
            final World w = Bukkit.getWorld(args[0]);
            final double x = Double.parseDouble(args[1]);
            final double y = Double.parseDouble(args[2]);
            final double z = Double.parseDouble(args[3]);
            final float yaw = Float.parseFloat(args[4]);
            final float pitch = Float.parseFloat(args[5]);
            return new Location(w, x, y, z, yaw, pitch);
        }
        catch (NullPointerException e) {
            return null;
        }
    }


    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
