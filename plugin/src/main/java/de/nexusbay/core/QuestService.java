package de.nexusbay.core;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kopfgeld-Quest (Standard) + optionale DB-Quests.
 *
 * Standard ohne Argumente:
 *   /bayquest accept  → 10 Monster, 100 Bay-Coins, 50 XP
 * Funktioniert mit YAML (immer) und optional MariaDB.
 */
public final class QuestService implements Listener {

    public static final String BOUNTY_CODE = "bounty";

    private final NexusBay plugin;
    private final Map<UUID, Bounty> bountyCache = new ConcurrentHashMap<>();
    private boolean seeded;

    public QuestService(NexusBay plugin) {
        this.plugin = plugin;
    }

    private boolean db() {
        return plugin.database() != null && plugin.database().isEnabled();
    }

    private int target() {
        return plugin.getConfig().getInt("quests.bounty-kills", 10);
    }

    private int rewardCoins() {
        return plugin.getConfig().getInt("quests.bounty-coins", 100);
    }

    private int rewardXp() {
        return plugin.getConfig().getInt("quests.bounty-xp", 50);
    }

    /* ---------- Bounty (YAML, always works) ---------- */

    private Bounty loadBounty(Player p) {
        UUID id = p.getUniqueId();
        Bounty cached = bountyCache.get(id);
        if (cached != null) return cached;

        String path = "players." + id + ".quest";
        boolean active = plugin.getConfig().getBoolean(path + ".active", false);
        int progress = plugin.getConfig().getInt(path + ".progress", 0);
        boolean completed = plugin.getConfig().getBoolean(path + ".completed", false);
        Bounty b = new Bounty(active, progress, completed);
        bountyCache.put(id, b);
        return b;
    }

    private void saveBounty(Player p, Bounty b) {
        String path = "players." + p.getUniqueId() + ".quest";
        plugin.getConfig().set(path + ".active", b.active);
        plugin.getConfig().set(path + ".progress", b.progress);
        plugin.getConfig().set(path + ".completed", b.completed);
        plugin.getConfig().set(path + ".code", BOUNTY_CODE);
        plugin.saveConfig();
        bountyCache.put(p.getUniqueId(), b);
    }

    public String acceptBounty(Player player) {
        Bounty b = loadBounty(player);
        if (b.active && !b.completed) {
            return "§eDu hast bereits einen offenen Kopfgeld-Auftrag. §f/bayquest status";
        }
        b.active = true;
        b.progress = 0;
        b.completed = false;
        saveBounty(player, b);

        // DB mirror if available
        if (db()) acceptDb(player, BOUNTY_CODE, target());

        return "§aKopfgeld angenommen: §fBesiege " + target() + " Monster"
                + " §7→ Belohnung §6" + rewardCoins() + " Coins §7+ §b" + rewardXp() + " XP";
    }

    public String abandonBounty(Player player) {
        Bounty b = loadBounty(player);
        if (!b.active || b.completed) {
            return "§cKein offener Kopfgeld-Auftrag.";
        }
        b.active = false;
        b.progress = 0;
        saveBounty(player, b);
        if (db()) abandonDb(player, BOUNTY_CODE);
        return "§eKopfgeld-Auftrag abgebrochen.";
    }

    public List<String> statusLines(Player player) {
        List<String> lines = new ArrayList<>();
        lines.add("§6§lNEXUS-BAY §8» §fQuest-Status");
        Bounty b = loadBounty(player);
        if (b.active && !b.completed) {
            lines.add("§e● Kopfgeld §8(" + BOUNTY_CODE + ") §7"
                    + b.progress + "/" + target() + " Monster");
            lines.add("§7Belohnung: §6" + rewardCoins() + " Coins §7+ §b" + rewardXp() + " XP");
            lines.add("§7Abbruch: §f/bayquest abandon");
        } else if (b.completed) {
            lines.add("§a✔ Letztes Kopfgeld erledigt. §7Neu starten: §f/bayquest accept");
        } else {
            lines.add("§7Kein aktiver Auftrag. §f/bayquest accept");
        }
        return lines;
    }

    public List<String> listLines(Player player) {
        List<String> lines = new ArrayList<>();
        lines.add("§6§lLogbuch §8» §fVerfügbare Aufträge");
        Bounty b = loadBounty(player);
        String mark = b.completed ? "§a✔" : (b.active ? "§e●" : "§7○");
        lines.add(mark + " §fKopfgeld §8[