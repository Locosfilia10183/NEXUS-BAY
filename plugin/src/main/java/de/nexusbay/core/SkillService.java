package de.nexusbay.core;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Skill-tree progress (unlock nodes). Combat effects come in later phases.
 */
public final class SkillService {

    private final NexusBay plugin;

    /** tree -> ordered node ids with display names */
    public static final Map<String, Map<String, String>> TREES = new LinkedHashMap<>();

    static {
        Map<String, String> swords = new LinkedHashMap<>();
        swords.put("basic_slash", "Grundhieb");
        swords.put("parry", "Parade");
        swords.put("riposte", "Konter");
        swords.put("blade_master", "Klingenmeister");
        TREES.put("swords", swords);

        Map<String, String> pistols = new LinkedHashMap<>();
        pistols.put("quick_draw", "Schnellziehen");
        pistols.put("steady_aim", "Ruhige Hand");
        pistols.put("double_shot", "Doppelschuss");
        pistols.put("gunslinger", "Revolverheld");
        TREES.put("pistols", pistols);

        Map<String, String> harpoons = new LinkedHashMap<>();
        harpoons.put("throw", "Wurf");
        harpoons.put("reel", "Einholen");
        harpoons.put("impale", "Aufspießen");
        harpoons.put("harpoon_master", "Harpunenmeister");
        TREES.put("harpoons", harpoons);

        Map<String, String> crew = new LinkedHashMap<>();
        crew.put("raider", "Plünderer");
        crew.put("navigator", "Navigator");
        crew.put("quartermaster", "Quartiermeister");
        TREES.put("crew", crew);
    }

    public SkillService(NexusBay plugin) {
        this.plugin = plugin;
    }

    private boolean db() {
        return plugin.database() != null && plugin.database().isEnabled();
    }

    public List<String> list(Player player, String tree) {
        List<String> lines = new ArrayList<>();
        if (!TREES.containsKey(tree)) {
            lines.add("§cUnbekannter Skillbaum. Verfügbar: " + String.join(", ", TREES.keySet()));
            return lines;
        }
        lines.add("§6§lSkillbaum §8» §f" + tree);
        Map<String, String> nodes = TREES.get(tree);
        for (Map.Entry<String, String> e : nodes.entrySet()) {
            boolean unlocked = isUnlocked(player, tree, e.getKey());
            lines.add((unlocked ? "§a✔ " : "§7○ ") + e.getValue() + " §8(" + e.getKey() + ")");
        }
        lines.add("§7Freischalten: §f/bayskill unlock <tree> <node>");
        return lines;
    }

    public boolean isUnlocked(Player player, String tree, String node) {
        if (!db()) return false;
        try (Connection c = plugin.database().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT 1 FROM skill_progress WHERE player_uuid=? AND skill_tree=? AND node_id=?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, tree);
            ps.setString(3, node);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            return false;
        }
    }

    public String unlock(Player player, String tree, String node) {
        if (!db()) return "§cSkills benötigen die Datenbank.";
        if (!TREES.containsKey(tree) || !TREES.get(tree).containsKey(node))
            return "§cUnbekannter Tree oder Node.";
        if (isUnlocked(player, tree, node)) return "§eBereits freigeschaltet.";

        // simple gate: first node free, later need previous + level
        List<String> order = new ArrayList<>(TREES.get(tree).keySet());
        int idx = order.indexOf(node);
        if (idx > 0 && !isUnlocked(player, tree, order.get(idx - 1)))
            return "§cSchalte zuerst §f" + TREES.get(tree).get(order.get(idx - 1)) + " §cfrei.";
        int needLevel = 1 + idx * 3;
        if (plugin.data().level(player) < needLevel)
            return "§cBenötigt Level " + needLevel + ".";

        long cost = 50L * (idx + 1);
        if (plugin.data().coins(player) < cost) return "§cBenötigt " + cost + " Bay-Coins.";

        plugin.data().addCoins(player, -cost);
        try (Connection c = plugin.database().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO skill_progress (player_uuid, skill_tree, node_id) VALUES (?,?,?)")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, tree);
            ps.setString(3, node);
            ps.executeUpdate();
            return "§aSkill freigeschaltet: §f" + TREES.get(tree).get(node) + " §7(-" + cost + " Coins)";
        } catch (Exception e) {
            plugin.data().addCoins(player, cost);
            return "§cFreischaltung fehlgeschlagen.";
        }
    }
}
