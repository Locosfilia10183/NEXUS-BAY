package de.nexusbay.core;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Quest / logbook framework. Seeds a few starter quests on first enable.
 */
public final class QuestService {

    private final NexusBay plugin;
    private boolean seeded;

    public QuestService(NexusBay plugin) {
        this.plugin = plugin;
    }

    private boolean db() {
        return plugin.database() != null && plugin.database().isEnabled();
    }

    public void ensureSeed() {
        if (!db() || seeded) return;
        try (Connection c = plugin.database().getConnection()) {
            try (PreparedStatement count = c.prepareStatement("SELECT COUNT(*) FROM quests")) {
                try (ResultSet rs = count.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        seeded = true;
                        return;
                    }
                }
            }
            seed(c, "first_blood", "story", "The First Blade",
                    "Besiege 5 Monster und kehre zurück.", 100, 50);
            seed(c, "dock_worker", "daily", "Hafenarbeit",
                    "Sammle 32 Eichenholz (Story-Platzhalter).", 40, 20);
            seed(c, "crew_raid", "crew", "Plünderzug",
                    "Als Crew gemeinsam 20 Monster besiegen.", 200, 80);
            seeded = true;
            plugin.getLogger().info("Starter-Quests in DB geladen.");
        } catch (Exception e) {
            plugin.getLogger().warning("Quest seed failed: " + e.getMessage());
        }
    }

    private void seed(Connection c, String code, String type, String title, String desc, int coins, int xp)
            throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO quests (code, type, title, description, reward_coins, reward_xp) VALUES (?,?,?,?,?,?)")) {
            ps.setString(1, code);
            ps.setString(2, type);
            ps.setString(3, title);
            ps.setString(4, desc);
            ps.setInt(5, coins);
            ps.setInt(6, xp);
            ps.executeUpdate();
        }
    }

    public List<String> listActive(Player player) {
        List<String> lines = new ArrayList<>();
        if (!db()) {
            lines.add("§cQuests benötigen die Datenbank (database.enabled=true).");
            return lines;
        }
        ensureSeed();
        lines.add("§6§lLogbuch §8» §fAlle Aufträge");
        try (Connection c = plugin.database().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT q.code, q.title, q.type, q.reward_coins, q.reward_xp, " +
                             "COALESCE(pq.progress,0) AS progress, COALESCE(pq.target,1) AS target, " +
                             "COALESCE(pq.completed,0) AS completed, " +
                             "(pq.player_uuid IS NOT NULL) AS accepted " +
                             "FROM quests q LEFT JOIN player_quests pq ON pq.quest_id = q.id AND pq.player_uuid = ? " +
                             "WHERE q.active = 1 ORDER BY q.id")) {
            ps.setString(1, player.getUniqueId().toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    boolean done = rs.getInt("completed") == 1;
                    boolean accepted = rs.getInt("accepted") == 1;
                    String mark = done ? "§a✔" : (accepted ? "§e●" : "§7○");
                    lines.add(mark + " §f" + rs.getString("title") +
                            " §8[" + rs.getString("type") + "|" + rs.getString("code") + "] " +
                            "§7" + rs.getInt("progress") + "/" + rs.getInt("target") +
                            " §8| §6" + rs.getInt("reward_coins") + "c §b" + rs.getInt("reward_xp") + "xp");
                }
            }
        } catch (Exception e) {
            lines.add("§cFehler beim Laden der Quests.");
        }
        lines.add("§7/bayquest accept <code> §8| §7status §8| §7abandon <code>");
        return lines;
    }

    public List<String> status(Player player) {
        List<String> lines = new ArrayList<>();
        if (!db()) {
            lines.add("§cDatenbank erforderlich.");
            return lines;
        }
        ensureSeed();
        lines.add("§6§lLogbuch §8» §fDein Fortschritt");
        try (Connection c = plugin.database().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT q.code, q.title, q.type, pq.progress, pq.target, pq.completed " +
                             "FROM player_quests pq JOIN quests q ON q.id = pq.quest_id " +
                             "WHERE pq.player_uuid = ? ORDER BY pq.completed, q.id")) {
            ps.setString(1, player.getUniqueId().toString());
            try (ResultSet rs = ps.executeQuery()) {
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    boolean done = rs.getInt("completed") == 1;
                    lines.add((done ? "§a✔ " : "§e● ") + rs.getString("title") +
                            " §8(" + rs.getString("code") + ") §7" +
                            rs.getInt("progress") + "/" + rs.getInt("target"));
                }
                if (!any) lines.add("§7Keine angenommenen Quests. §f/bayquest list");
            }
        } catch (Exception e) {
            lines.add("§cFehler beim Laden.");
        }
        return lines;
    }

    public String accept(Player player, String code) {
        if (!db()) return "§cDatenbank erforderlich.";
        ensureSeed();
        try (Connection c = plugin.database().getConnection()) {
            int questId = -1;
            int target = 5;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT id FROM quests WHERE code = ? AND active = 1")) {
                ps.setString(1, code);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return "§cUnbekannter Quest-Code. Nutze /bayquest list";
                    questId = rs.getInt("id");
                }
            }
            try (PreparedStatement check = c.prepareStatement(
                    "SELECT completed FROM player_quests WHERE player_uuid = ? AND quest_id = ?")) {
                check.setString(1, player.getUniqueId().toString());
                check.setInt(2, questId);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getInt("completed") == 1) return "§eQuest bereits erledigt.";
                        return "§eQuest bereits angenommen. §f/bayquest status";
                    }
                }
            }
            try (PreparedStatement ins = c.prepareStatement(
                    "INSERT INTO player_quests (player_uuid, quest_id, progress, target) VALUES (?,?,0,?)")) {
                ins.setString(1, player.getUniqueId().toString());
                ins.setInt(2, questId);
                ins.setInt(3, target);
                ins.executeUpdate();
            }
            return "§aQuest angenommen: §f" + code + " §7→ /bayquest status";
        } catch (Exception e) {
            return "§cAnnahme fehlgeschlagen.";
        }
    }

    public String abandon(Player player, String code) {
        if (!db()) return "§cDatenbank erforderlich.";
        try (Connection c = plugin.database().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE pq FROM player_quests pq " +
                             "JOIN quests q ON q.id = pq.quest_id " +
                             "WHERE pq.player_uuid = ? AND q.code = ? AND pq.completed = 0")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, code);
            int n = ps.executeUpdate();
            if (n == 0) return "§cKeine offene Quest mit diesem Code.";
            return "§eQuest abgebrochen: §f" + code;
        } catch (Exception e) {
            return "§cAbbruch fehlgeschlagen.";
        }
    }

    public void addProgress(Player player, String code, int amount) {
        if (!db() || amount <= 0) return;
        try (Connection c = plugin.database().getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE player_quests pq JOIN quests q ON q.id = pq.quest_id " +
                            "SET pq.progress = LEAST(pq.target, pq.progress + ?) " +
                            "WHERE pq.player_uuid = ? AND q.code = ? AND pq.completed = 0")) {
                ps.setInt(1, amount);
                ps.setString(2, player.getUniqueId().toString());
                ps.setString(3, code);
                ps.executeUpdate();
            }
            try (PreparedStatement done = c.prepareStatement(
                    "UPDATE player_quests pq JOIN quests q ON q.id = pq.quest_id " +
                            "SET pq.completed = 1, pq.completed_at = CURRENT_TIMESTAMP " +
                            "WHERE pq.player_uuid = ? AND q.code = ? AND pq.progress >= pq.target AND pq.completed = 0")) {
                done.setString(1, player.getUniqueId().toString());
                done.setString(2, code);
                int n = done.executeUpdate();
                if (n > 0) {
                    try (PreparedStatement rew = c.prepareStatement(
                            "SELECT reward_coins, reward_xp FROM quests WHERE code = ?")) {
                        rew.setString(1, code);
                        try (ResultSet rs = rew.executeQuery()) {
                            if (rs.next()) {
                                plugin.data().addCoins(player, rs.getInt("reward_coins"));
                                plugin.data().addXp(player, rs.getInt("reward_xp"));
                                player.sendMessage("§6§lQUEST ABGESCHLOSSEN! §e" + code);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}
