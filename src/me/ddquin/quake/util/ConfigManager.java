package me.ddquin.quake.util;

import me.ddquin.quake.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigManager {

    private File configFile;
    private YamlConfiguration config;

    public ConfigManager(final String name, Main main, boolean startNew) {
        this.configFile = getConfig(name, main, startNew);
        this.reload();
    }

    public void set(final String k, final Object v) {
        if (v instanceof Location) {
            this.setLocation(k, (Location)v);
        }
        else {
            this.config.set(k, v);
        }
        this.save();
    }

    private void setLocation(final String k, final Location l) {
        final String v = l.getWorld().getName() + ";" + Math.round(l.getX() * 10.0) / 10.0 + ";" + Math.round(l.getY() * 10.0) / 10.0 + ";" + Math.round(l.getZ() * 10.0) / 10.0 + ";" + Math.round(l.getYaw() * 10.0f) / 10.0f + ";" + Math.round(l.getPitch() * 10.0f) / 10.0f + ";";
        this.set(k, v);
    }

    public Object get(final String key) {
        return this.config.get(key);
    }

    public boolean hasKey(final String key) {
        return this.config.contains(key);
    }

    public String getString(final String key) {
        return this.config.getString(key);
    }

    public int getIntOrSetIfEmpty(final String key, int defaultNumber) {
        if (!hasKey(key)) {
            set(key, defaultNumber);
            return defaultNumber;
        }
        return getInt(key);
    }

    public double getDoubleOrSetIfEmpty(final String key, double defaultNumber) {
        if (!hasKey(key)) {
            set(key, defaultNumber);
            return defaultNumber;
        }
        return getDouble(key);
    }

    public String getStringOrSetIfEmpty(final String key, String defaultString) {
        if (!hasKey(key)) {
            set(key, defaultString);
            return defaultString;
        }
        return getString(key);
    }

    public List<String> getStringListOrSetIfEmpty(final String key, List<String> defaultArray) {
        if (!hasKey(key)) {
            set(key, defaultArray);
            return defaultArray;
        }
        return getStringList(key);
    }



    public boolean getBoolOrSetIfEmpty(final String key, boolean defaultBoolean) {
        if (!hasKey(key)) {
            set(key, defaultBoolean);
            return defaultBoolean;
        }
        return getBoolean(key);
    }


    public int getInt(final String key) {
        return this.config.getInt(key);
    }

    public double getDouble(final String key) {
        return this.config.getDouble(key);
    }

    public boolean getBoolean(final String key) {
        return this.config.getBoolean(key);
    }

    public List<?> getList(final String key) {
        List<?> data = (List<?>)this.config.getList(key);
        if (data == null) {
            data = new ArrayList<Object>();
        }
        return data;
    }

    public List<String> getStringList(final String key) {
        return this.config.getStringList(key);

    }

    public List<Location> getLocList(String key) {
        return (List<Location>)this.config.getList(key);
    }

    public Location getLocation(final String key) {
        try {
            final String[] args = this.getString(key).split(";");
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

    /*public ItemStack getItemStack(final String key) {
        final ItemBuilder b = new ItemBuilder().material(Material.valueOf(this.getString(key + ".material")));
        if (this.getString(key + ".name") != null) {
            b.name(this.getString(key + ".name"));
        }
        if (this.getList(key + ".lores") != null) {
            b.lores((List<String>)this.getList(key + ".lores"));
        }
        if (this.getInt(key + ".amount") != 0) {
            b.amount(this.getInt(key + ".amount"));
        }
        b.glow(this.getBoolean(key + ".glow"));
        if (this.get(key + ".enchant") != null) {
            for (final String e : this.section(key + ".enchant")) {
                final Enchantment enchant = Enchantment.getByName(e.toLowerCase());
                final int power = this.getInt(key + ".enchant." + e);
                b.enchant(enchant, power);
            }
        }
        return b.create();
    }*/

    public Set<String> section(final String key) {
        return (this.config.get(key) == null) ? new HashSet<String>() : this.config.getConfigurationSection(key).getKeys(false);
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
    }

    public void save() {
        try {
            this.config.save(this.configFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getConfig(final String name, final Plugin plugin, boolean startNew) {
        final File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        final File configFile = new File(plugin.getDataFolder() + File.separator + name + ".yml");
        if (startNew) {
            if (configFile.exists()) {
                configFile.delete();
            }
        }
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return configFile;
    }
}
