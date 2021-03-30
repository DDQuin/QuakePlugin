package me.ddquin.quake.stat;

import me.ddquin.quake.Main;
import me.ddquin.quake.Settings;
import me.ddquin.quake.util.ConfigManager;
import me.ddquin.quake.util.Util;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class StatManager {

    private Map<UUID, PlayerStat> playerStats;
    private Main main;

    public StatManager(Main main) {
        playerStats = new HashMap<>();
        this.main = main;
    }

    public void loadPlayerStats() {
        if (Settings.SQLOn) {
            loadPlayerStatsSQL();
        } else {
            loadPlayerStatsYAML();
        }
    }

    private void loadPlayerStatsYAML() {
        ConfigManager c = new ConfigManager("stats", main, false);
        for (String id: c.section("stat")) {
            String cur = "stat." + id;
            UUID uuid = UUID.fromString(id);
            PlayerStat playerStat = new PlayerStat(uuid);
            for (Stat stat: Stat.values()) {
                if (stat.isStored) {
                    playerStat.addToStat(stat, c.getInt(cur + "." + stat.getKey()));
                }
            }
            playerStats.put(uuid, playerStat);
        }
    }

    private String createTableStatement(String tableName, String primary, List<String> fields) {
        String seasonTable = "`" + tableName + "`";
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE " + seasonTable + "(\n");
        sb.append(" `" + primary + "` varchar(64) NOT NULL,\n");
        for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            sb.append(" `" + field + "` int(11) NOT NULL,\n");
        }
        sb.append(" PRIMARY KEY (`" + primary + "`)\n");
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8");

        return sb.toString();
    }

    private void loadPlayerStatsSQL() {
       try {
            if (!main.getSQL().tableExist(main.getSQL().getTableName())) {
                PreparedStatement statement = main.getSQL().connection.prepareStatement(createTableStatement(main.getSQL().getTableName(), "UUID", Stat.getStoredStatString()));
                statement.execute();
                System.out.println(Util.prefix + "Created new table " + main.getSQL().getTableName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String query = "SELECT * FROM " + main.getSQL().getTableName();
        try {
            PreparedStatement statement = main.getSQL().connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                final UUID id = UUID.fromString(rs.getString("UUID"));
                PlayerStat ps = new PlayerStat(id);
                for (Stat stat: Stat.getStoredStats()) {
                    int curStat = rs.getInt(stat.title());
                    ps.addToStat(stat, curStat);
                }
                playerStats.put(id, ps);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveAll() {
        playerStats.forEach((id, player) -> savePlayer(id));
    }

    public void savePlayer(UUID id) {
        if (Settings.SQLOn) {
            savePlayerSQL(id);
        } else {
            savePlayerYAML(id);
        }
    }

    public String insertIntoSQL(String tableName, List<String> fields, List<String> values) {
        StringBuilder insertSb = new StringBuilder();
        insertSb.append("INSERT INTO " + tableName);
        insertSb.append(" (");
        for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            if (i == fields.size() - 1) {
                insertSb.append("`" + field + "`)");
            } else {
                insertSb.append("`" + field + "`, ");
            }
        }
        insertSb.append(" VALUES (");
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            if (i == values.size() - 1) {
                insertSb.append("?" + ")");
            } else {
                insertSb.append("?" + ", ");
            }
        }
        insertSb.append(" ON DUPLICATE KEY UPDATE ");
        for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            if (i == fields.size() - 1) {
                insertSb.append(field + "=VALUES(" + field + ")");
            } else {
                insertSb.append(field + "=VALUES(" + field + "), ");
            }
        }
        return insertSb.toString();
    }

    private void savePlayerSQL(UUID id) {
        try {
            PlayerStat ps = playerStats.get(id);
            ArrayList<String> fields = new ArrayList<>();
            fields.add("UUID");
            fields.addAll(Stat.getStoredStatString());
            ArrayList<String> values = new ArrayList<>();
            values.add(id.toString());
            values.addAll(ps.getPlayerStoredStats());
            String sqlStat = insertIntoSQL(main.getSQL().getTableName(), fields, values);
            //System.out.println(sqlStat);
            PreparedStatement statement = main.getSQL().connection.prepareStatement(sqlStat);
            for (int i = 0; i < values.size(); i++) {
                String value = values.get(i);
                statement.setString(i + 1, value);
            }
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void savePlayerYAML(UUID id) {
        ConfigManager c = new ConfigManager("stats", main, false);
        PlayerStat playerStat = getStat(id);
        final String cur = "stat." + id;
        for (Stat stat: Stat.values()) {
            if (stat.isStored) {
                c.set(cur + "." + stat.getKey(), playerStat.getStat(stat) );
            }
        }
    }

    public PlayerStat getStat(UUID id) {
        if (!playerStats.containsKey(id)) {
            playerStats.put(id, new PlayerStat(id));
        }
        return playerStats.get(id);
    }

    public void showTop(Player p, Stat statToSort, int topPlayers) {
        List<PlayerStat> top = new ArrayList<>(playerStats.values());
        top.sort(statToSort.getComparator());
        topPlayers = Math.min(topPlayers, top.size());
        p.sendMessage(Util.LINE_SEPERATOR);
        for (int i = 0; i < topPlayers; i++) {
            int pos = i + 1;
            PlayerStat ps = top.get(i);
            p.spigot().sendMessage(
                    new ComponentBuilder(
                            Util.color("&b" + pos + ". &7")).append(
                            ps.getName()).event(
                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/qs stat " + ps.getName())
                    ).append(Util.color(" &b" + statToSort.title() + ": &7" + ps.getStatString(statToSort)))
                            .create()
            );
        }
        p.sendMessage(Util.LINE_SEPERATOR);

    }

    public boolean containsId(UUID id) {
        Set<UUID> allIdsOnline = new HashSet<>();
        Bukkit.getOnlinePlayers().forEach(p -> allIdsOnline.add(p.getUniqueId()));
        return playerStats.containsKey(id) || allIdsOnline.contains(id);
    }

    public void sendStatMessage(Player playerToSend, UUID playerToShowID) {
        PlayerStat playerStat = getStat(playerToShowID);
        OfflinePlayer p = Bukkit.getOfflinePlayer(playerToShowID);
        playerToSend.sendMessage(Util.LINE_SEPERATOR);
        playerToSend.sendMessage(Util.color("&b" + p.getName() +"'s stats"));
        for (Stat stat: Stat.values()) {
            playerToSend.sendMessage(Util.color("&b" + stat.title() + ": &7" + playerStat.getStatString(stat)));
        }
        playerToSend.sendMessage(Util.LINE_SEPERATOR);
    }

    public List<String> getStats() {
        List<String> stats = new ArrayList<>();
        Arrays.asList(Stat.values()).forEach(s -> stats.add(s.toString().toLowerCase()));
        return stats;
    }
}
