package de.nexusbay.core;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kopfgeld-Quest (Standard) – funktioniert ohne Datenbank (YAML).
 *
 * /bayquest accept   → 10 Monster, 100 Bay-Coins, 50 XP
 * /bayquest status
 * /bayquest abandon
 * /bayquest list
 */
public final class QuestService implements Listener {

    public static final String BOUNTY_CODE = "bounty";

    private final NexusBay plugin;
    private final Map<UUID, Bounty> cache = new ConcurrentHashMap<>();

    public QuestService(NexusBay plugin) {
        this.plugin = plugin;
    }

    private int target() {
        return Math.max(1, plugin.getConfig().getInt("quests.bounty-kills", 10));
    }

    private int rewardCoins() {
        return plugin.getConfig().getInt("quests.bounty-coins", 100);
    }

    private int rewardXp() {
        return plugin.getConfig().getInt("quests.bounty-xp", 50);
    }

    private Bounty load(Player p) {
        UUID id = p.getUniqueId();
        Bounty cached = cache.get(id);
        if (cached != null) return cached;

        String path = "players." + id + ".quest";
        Bounty b = new Bounty(
                plugin.getConfig().getBoolean(path + ".active", false),
                plugin.getConfig().getInt(path + ".progress", 0),
                plugin.getConfig().getBoolean(path + ".completed", false)
        );
        cache.put(id, b);
        return b;
    }

    private void save(Player p, Bounty b) {
        String path = "players." + p.getUniqueId() + ".quest";
        plugin.getConfig().set(path + ".active", b.active);
        plugin.getConfig().set(path + ".progress", b.progress);
        plugin.getConfig().set(path + ".completed", b.completed);
        plugin.getConfig().set(path + ".code", BOUNTY_CODE);
        plugin.saveConfig();
        cache.put(p.getUniqueId(), b);
    }

    public String accept(Player player) {
        Bounty b = load(player);
        if (b.active && !b.completed) {
            return "§eDu hast bereits einen offenen Kopfgeld-Auftrag. §f/bayquest status";
        }
        b.active = true;
        b.progress = 0;
        b.completed = false;
        save(player, b);
        return "§aKopfgeld angenommen: §fBesiege " + target() + " Monster"
                + " §7→ §6" + rewardCoins() + " Coins §7+ §b" + rewardXp() + " XP";
    }

    /** Accept by code; unknown codes fall back to bounty. */
    public String accept(Player player, String code) {
        if (code == null || code.isBlank() || code.equalsIgnoreCase(BOUNTY_CODE)
                || code.equalsIgnoreCase("kopfgeld")) {
            return accept(player);
        }
        // Known story codes still map to same bounty for now
        if (code.equalsIgnoreCase("first_blood") || code.equalsIgnoreCase("dock_worker")
                || code.equalsIgnoreCase("crew_raid")) {
            return accept(player);
        }
        return "§cUnbekannter Quest-Code. Nutze §f/bayquest accept";
    }

    public String abandon(Player player) {
        Bounty b = load(player);
        if (!b.active || b.completed) {
            return "§cKein offener Kopfgeld-Auftrag.";
        }
        b.active = false;
        b.progress = 0;
        save(player, b);
        return "§eKopfgeld-Auftrag abgebrochen.";
    }

    public String abandon(Player player, String code) {
        return abandon(player);
    }

    public List<String> status(Player player) {
        List<String> lines = new ArrayList<>();
        lines.add("§6§lNEXUS-BAY §8» §fQuest-Status");
        Bounty b = load(player);
        if (b.active && !b.completed) {
            lines.add("§e● Kopfgeld §8(" + BOUNTY_CODE + ") §7"
                    + b.progress + "/" + target() + " Monster");
            lines.add("§7Belohnung: §6" + rewardCoins() + " Coins §7+ §b" + rewardXp() + " XP");
            lines.add("§7Abbruch: §f/bayquest abandon");
        } else if (b.completed) {
            lines.add("§a✔ Letztes Kopfgeld erledigt.");
            lines.add("§7Neu starten: §f/bayquest accept");
        } else {
            lines.add("§7Kein aktiver Auftrag.");
            lines.add("§7Starten: §f/bayquest accept");
        }
        return lines;
    }

    public List<String> listActive(Player player) {
        List<String> lines = new ArrayList<>();
        lines.add("§6§lLogbuch §8» §fAufträge");
        Bounty b = load(player);
        String mark = b.completed ? "§a✔" : (b.active ? "§e●" : "§7○");
        lines.add(mark + " §fKopfgeld §8[bounty]");
        lines.add("  §7Ziel: " + target() + " Monster | §6" + rewardCoins() + "c §b" + rewardXp() + "xp");
        if (b.active && !b.completed) {
            lines.add("  §eFortschritt: " + b.progress + "/" + target());
        }
        lines.add("§7/bayquest accept §8| §7status §8| §7abandon");
        return lines;
    }

    /** Kill tracking for active bounty. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMobKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null || event.getEntity() instanceof Player) return;

        Bounty b = load(killer);
        if (!b.active || b.completed) return;

        b.progress++;
        int need = target();
        if (b.progress >= need) {
            b.progress = need;
            b.completed = true;
            b.active = false;
            save(killer, b);
            plugin.data().addCoins(killer, rewardCoins());
            plugin.data().addXp(killer, rewardXp());
            killer.sendMessage("§6§lKOPFGELD ERLEDIGT! §e+" + rewardCoins() + " Coins §7+ §b" + rewardXp() + " XP");
        } else {
            save(killer, b);
            killer.sendMessage("§eKopfgeld: §f" + b.progress + "/" + need + " §7Monster");
        }
    }

    /** Kept for API compatibility with older call sites. */
    public void ensureSeed() {
        // no-op: bounty is YAML-only and needs no seed
    }

    public void addProgress(Player player, String code, int amount) {
        if (amount <= 0) return;
        Bounty b = load(player);
        if (!b.active || b.completed) return;
        for (int i = 0; i < amount; i++) {
            // reuse kill logic by simulating
        }
        b.progress = Math.min(target(), b.progress + amount);
        if (b.progress >= target()) {
            b.completed = true;
            b.active = false;
            save(player, b);
            plugin.data().addCoins(player, rewardCoins());
            plugin.data().addXp(player, rewardXp());
            player.sendMessage("§6§lKOPFGELD ERLEDIGT!");
        } else {
            save(player, b);
        }
    }

    private static final class Bounty {
        boolean active;
        int progress;
        boolean completed;

        Bounty(boolean active, int progress, boolean completed) {
            this.active = active;
            this.progress = progress;
            this.completed = completed;
        }
    }
}
