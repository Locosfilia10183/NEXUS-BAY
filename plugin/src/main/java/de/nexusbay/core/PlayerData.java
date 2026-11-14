package de.nexusbay.core;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Player profiles: level, XP, coins.
 * Uses MariaDB when DatabaseService is enabled, otherwise YAML fallback.
 */
public final class PlayerData implements Listener {

    private final NexusBay plugin;
    private final Map<UUID, Profile> cache = new ConcurrentHashMap<>();

    public PlayerData(NexusBay plugin) {
        this.plugin = plugin;
    }

    private boolean useDb() {
        return plugin.database() != null && plugin.database().isEnabled();
    }

    private Profile load(Player p) {
        UUID id = p.getUniqueId();
        Profile cached = cache.get(id);
        if (cached != null) {
            cached.name = p.getName();
            return cached;
        }

        if (useDb()) {
            try (Connection c = plugin.database().getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "SELECT level, xp, coins FROM players WHERE uuid = ?")) {
                ps.setString(1, id.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Profile pr = new Profile(id, p.getName(),
                                rs.getInt("level"), rs.getLong("xp"), rs.getLong("coins"));
                        cache.put(id, pr);
                        touchDb(p);
                        return pr;
                    }
                }
                // insert new
                int start = plugin.getConfig().getInt("settings.starting-coins", 250);
                try (PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO players (uuid, name, level, xp, coins) VALUES (?,?,1,0,?)")) {
                    ins.setString(1, id.toString());
                    ins.setString(2, p.getName());
                    ins.setLong(3, start);
                    ins.executeUpdate();
                }
                Profile pr = new Profile(id, p.getName(), 1, 0, start);
                cache.put(id, pr);
                return pr;
            } catch (Exception e) {
                plugin.getLogger().warning("DB load failed for " + p.getName() + ": " + e.getMessage());
            }
        }

        // YAML fallback
        String k = id.toString();
        var section = plugin.getConfig().getConfigurationSection("players");
        if (section == null) section = plugin.getConfig().createSection("players");
        if (!section.contains(k)) {
            section.set(k + ".name", p.getName());
            section.set(k + ".level", 1);
            section.set(k + ".xp", 0);
            section.set(k + ".coins", plugin.getConfig().getInt("settings.starting-coins", 250));
        }
        section.set(k + ".name", p.getName());
        Profile pr = new Profile(id, p.getName(),
                section.getInt(k + ".level", 1),
                section.getLong(k + ".xp", 0),
                section.getLong(k + ".coins", 0));
        cache.put(id, pr);
        return pr;
    }

    private void touchDb(Player p) {
        if (!useDb()) return;
        try (Connection c = plugin.database().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE players SET name = ?, last_seen = CURRENT_TIMESTAMP WHERE uuid = ?")) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getUniqueId().toString());
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    private void persist(Profile pr) {
        if (useDb()) {
            try (Connection c = plugin.database().getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "UPDATE players SET name=?, level=?, xp=?, coins=? WHERE uuid=?")) {
                ps.setString(1, pr.name);
                ps.setInt(2, pr.level);
                ps.setLong(3, pr.xp);
                ps.setLong(4, pr.coins);
                ps.setString(5, pr.uuid.toString());
                ps.executeUpdate();
            } catch (Exception e) {
                plugin.getLogger().warning("DB save failed: " + e.getMessage());
            }
            return;
        }
        String k = pr.uuid.toString();
        plugin.getConfig().set("players." + k + ".name", pr.name);
        plugin.getConfig().set("players." + k + ".level", pr.level);
        plugin.getConfig().set("players." + k + ".xp", pr.xp);
        plugin.getConfig().set("players." + k + ".coins", pr.coins);
        plugin.saveConfig();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        load(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Profile pr = cache.remove(event.getPlayer().getUniqueId());
        if (pr != null) persist(pr);
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null && !(event.getEntity() instanceof Player)) {
            addXp(killer, 10);
            killer.sendMessage("§b+10 XP §7für einen besiegten Gegner");
        }
    }

    public int level(Player p) { return load(p).level; }
    public long xp(Player p) { return load(p).xp; }
    public long coins(Player p) { return load(p).coins; }

    public int nextLevelXp(Player p) {
        return level(p) * plugin.getConfig().getInt("settings.xp-per-level", 100);
    }

    public void addXp(Player p, long amount) {
        if (amount <= 0) return;
        Profile pr = load(p);
        pr.xp += amount;
        int needed = pr.level * plugin.getConfig().getInt("settings.xp-per-level", 100);
        while (pr.xp >= needed) {
            pr.xp -= needed;
            pr.level++;
            needed = pr.level * plugin.getConfig().getInt("settings.xp-per-level", 100);
            p.sendMessage("§6§lLEVEL UP! §eDu bist jetzt Level " + pr.level + "!");
        }
        persist(pr);
    }

    public boolean transfer(Player from, Player to, long amount) {
        if (amount < 1) return false;
        Profile a = load(from);
        Profile b = load(to);
        if (a.coins < amount) return false;
        a.coins -= amount;
        b.coins += amount;
        persist(a);
        persist(b);
        if (useDb()) logEconomy(from.getUniqueId(), to.getUniqueId(), amount, "transfer");
        return true;
    }

    public void addCoins(Player p, long amount) {
        Profile pr = load(p);
        pr.coins = Math.max(0, pr.coins + amount);
        persist(pr);
        if (useDb() && amount != 0) {
            logEconomy(null, p.getUniqueId(), amount, amount > 0 ? "reward" : "deduct");
        }
    }

    private void logEconomy(UUID from, UUID to, long amount, String reason) {
        try (Connection c = plugin.database().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO economy_log (from_uuid, to_uuid, amount, reason) VALUES (?,?,?,?)")) {
            ps.setString(1, from == null ? null : from.toString());
            ps.setString(2, to == null ? null : to.toString());
            ps.setLong(3, amount);
            ps.setString(4, reason);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    public void save() {
        for (Profile pr : cache.values()) persist(pr);
        if (!useDb()) plugin.saveConfig();
    }

    private static final class Profile {
        final UUID uuid;
        String name;
        int level;
        long xp;
        long coins;

        Profile(UUID uuid, String name, int level, long xp, long coins) {
            this.uuid = uuid;
            this.name = name;
            this.level = level;
            this.xp = xp;
            this.coins = coins;
        }
    }
}
