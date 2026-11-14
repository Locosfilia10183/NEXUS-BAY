package de.nexusbay.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Basic crew system (create, invite, accept, leave, info, bank deposit).
 * Requires database.enabled = true.
 */
public final class CrewService {

    private final NexusBay plugin;

    public CrewService(NexusBay plugin) {
        this.plugin = plugin;
    }

    private boolean db() {
        return plugin.database() != null && plugin.database().isEnabled();
    }

    public String create(Player leader, String name) {
        if (!db()) return "§cCrews benötigen die Datenbank (database.enabled=true).";
        name = name.replaceAll("[^a-zA-Z0-9_äöüÄÖÜß-]", "");
        if (name.length() < 3 || name.length() > 24) return "§cCrew-Name: 3–24 Zeichen.";
        if (getCrewId(leader.getUniqueId()) != null) return "§cDu bist bereits in einer Crew.";

        try (Connection c = plugin.database().getConnection()) {
            try (PreparedStatement check = c.prepareStatement("SELECT id FROM crews WHERE name = ?")) {
                check.setString(1, name);
                if (check.executeQuery().next()) return "§cDieser Crew-Name ist bereits vergeben.";
            }
            try (PreparedStatement ins = c.prepareStatement(
                    "INSERT INTO crews (name, leader_uuid) VALUES (?,?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                ins.setString(1, name);
                ins.setString(2, leader.getUniqueId().toString());
                ins.executeUpdate();
                try (ResultSet keys = ins.getGeneratedKeys()) {
                    if (!keys.next()) return "§cCrew konnte nicht erstellt werden.";
                    int crewId = keys.getInt(1);
                    try (PreparedStatement mem = c.prepareStatement(
                            "INSERT INTO crew_members (crew_id, player_uuid, rank) VALUES (?,?, 'leader')")) {
                        mem.setInt(1, crewId);
                        mem.setString(2, leader.getUniqueId().toString());
                        mem.executeUpdate();
                    }
                    try (PreparedStatement up = c.prepareStatement("UPDATE players SET crew_id = ? WHERE uuid = ?")) {
                        up.setInt(1, crewId);
                        up.setString(2, leader.getUniqueId().toString());
                        up.executeUpdate();
                    }
                }
            }
            return "§aCrew §f" + name + " §awurde gegründet. Du bist der Captain.";
        } catch (Exception e) {
            plugin.getLogger().warning("Crew create: " + e.getMessage());
            return "§cFehler beim Erstellen der Crew.";
        }
    }

    public String invite(Player leader, String targetName) {
        if (!db()) return "§cDatenbank erforderlich.";
        Integer crewId = getCrewId(leader.getUniqueId());
        if (crewId == null) return "§cDu bist in keiner Crew.";
        if (!isLeaderOrOfficer(crewId, leader.getUniqueId())) return "§cNur Captain/Officer können einladen.";

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) return "§cSpieler muss online sein.";
        if (getCrewId(target.getUniqueId()) != null) return "§cSpieler ist bereits in einer Crew.";

        target.sendMessage("§6§lNEXUS-BAY §8» §e" + leader.getName() + " §7lädt dich in die Crew ein.");
        target.sendMessage("§7Nutze §f/baycrew accept §7innerhalb von 60 Sekunden.");
        pendingInvites.put(target.getUniqueId(), new Invite(crewId, leader.getUniqueId(), System.currentTimeMillis()));
        return "§aEinladung an §f" + target.getName() + " §agesendet.";
    }

    public String accept(Player player) {
        if (!db()) return "§cDatenbank erforderlich.";
        Invite inv = pendingInvites.remove(player.getUniqueId());
        if (inv == null || System.currentTimeMillis() - inv.time > 60_000)
            return "§cKeine gültige Einladung.";
        if (getCrewId(player.getUniqueId()) != null) return "§cDu bist bereits in einer Crew.";

        try (Connection c = plugin.database().getConnection()) {
            try (PreparedStatement mem = c.prepareStatement(
                    "INSERT INTO crew_members (crew_id, player_uuid, rank) VALUES (?,?, 'member')")) {
                mem.setInt(1, inv.crewId);
                mem.setString(2, player.getUniqueId().toString());
                mem.executeUpdate();
            }
            try (PreparedStatement up = c.prepareStatement("UPDATE players SET crew_id = ? WHERE uuid = ?")) {
                up.setInt(1, inv.crewId);
                up.setString(2, player.getUniqueId().toString());
                up.executeUpdate();
            }
            String name = getCrewName(inv.crewId);
            return "§aDu bist der Crew §f" + name + " §abeigetreten.";
        } catch (Exception e) {
            return "§cBeitritt fehlgeschlagen.";
        }
    }

    public String leave(Player player) {
        if (!db()) return "§cDatenbank erforderlich.";
        Integer crewId = getCrewId(player.getUniqueId());
        if (crewId == null) return "§cDu bist in keiner Crew.";

        try (Connection c = plugin.database().getConnection()) {
            String rank = getRank(crewId, player.getUniqueId());
            if ("leader".equals(rank)) {
                // disband if leader leaves
                try (PreparedStatement del = c.prepareStatement("DELETE FROM crews WHERE id = ?")) {
                    del.setInt(1, crewId);
                    del.executeUpdate();
                }
                try (PreparedStatement up = c.prepareStatement("UPDATE players SET crew_id = NULL WHERE crew_id = ?")) {
                    up.setInt(1, crewId);
                    up.executeUpdate();
                }
                return "§eCrew wurde aufgelöst (Captain hat verlassen).";
            }
            try (PreparedStatement del = c.prepareStatement(
                    "DELETE FROM crew_members WHERE crew_id = ? AND player_uuid = ?")) {
                del.setInt(1, crewId);
                del.setString(2, player.getUniqueId().toString());
                del.executeUpdate();
            }
            try (PreparedStatement up = c.prepareStatement("UPDATE players SET crew_id = NULL WHERE uuid = ?")) {
                up.setString(1, player.getUniqueId().toString());
                up.executeUpdate();
            }
            return "§aDu hast die Crew verlassen.";
        } catch (Exception e) {
            return "§cFehler beim Verlassen.";
        }
    }

    public List<String> info(Player player) {
        List<String> lines = new ArrayList<>();
        if (!db()) {
            lines.add("§cDatenbank erforderlich für Crew-Info.");
            return lines;
        }
        Integer crewId = getCrewId(player.getUniqueId());
        if (crewId == null) {
            lines.add("§7Du bist in keiner Crew. §f/baycrew create <name>");
            return lines;
        }
        try (Connection c = plugin.database().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT name, tag, level, bank, leader_uuid FROM crews WHERE id = ?")) {
            ps.setInt(1, crewId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    lines.add("§cCrew nicht gefunden.");
                    return lines;
                }
                lines.add("§6§lCrew §8» §f" + rs.getString("name") +
                        (rs.getString("tag") != null ? " §7[" + rs.getString("tag") + "]" : ""));
                lines.add("§7Level: §e" + rs.getInt("level") + " §8| §7Bank: §6" + rs.getLong("bank") + " Coins");
                lines.add("§7Mitglieder:");
            }
            try (PreparedStatement mem = c.prepareStatement(
                    "SELECT player_uuid, rank FROM crew_members WHERE crew_id = ?")) {
                mem.setInt(1, crewId);
                try (ResultSet rs = mem.executeQuery()) {
                    while (rs.next()) {
                        String uuid = rs.getString("player_uuid");
                        String rank = rs.getString("rank");
                        String name = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
                        lines.add(" §8- §f" + (name != null ? name : uuid.substring(0, 8)) + " §7(" + rank + ")");
                    }
                }
            }
        } catch (Exception e) {
            lines.add("§cFehler beim Laden.");
        }
        return lines;
    }

    public String deposit(Player player, long amount) {
        if (!db()) return "§cDatenbank erforderlich.";
        if (amount < 1) return "§cUngültiger Betrag.";
        Integer crewId = getCrewId(player.getUniqueId());
        if (crewId == null) return "§cKeine Crew.";
        if (plugin.data().coins(player) < amount) return "§cNicht genug Bay-Coins.";

        plugin.data().addCoins(player, -amount);
        try (Connection c = plugin.database().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE crews SET bank = bank + ? WHERE id = ?")) {
            ps.setLong(1, amount);
            ps.setInt(2, crewId);
            ps.executeUpdate();
            return "§a" + amount + " Bay-Coins in die Crew-Bank eingezahlt.";
        } catch (Exception e) {
            plugin.data().addCoins(player, amount); // rollback
            return "§cEinzahlung fehlgeschlagen.";
        }
    }

    private Integer getCrewId(UUID uuid) {
        if (!db()) return null;
        try (Connection c = plugin.database().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT crew_id FROM players WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("crew_id");
                    return rs.wasNull() ? null : id;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String getCrewName(int crewId) {
        try (Connection c = plugin.database().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT name FROM crews WHERE id = ?")) {
            ps.setInt(1, crewId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("name");
            }
        } catch (Exception ignored) {}
        return "?";
    }

    private String getRank(int crewId, UUID uuid) {
        try (Connection c = plugin.database().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT rank FROM crew_members WHERE crew_id = ? AND player_uuid = ?")) {
            ps.setInt(1, crewId);
            ps.setString(2, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("rank");
            }
        } catch (Exception ignored) {}
        return "member";
    }

    private boolean isLeaderOrOfficer(int crewId, UUID uuid) {
        String r = getRank(crewId, uuid);
        return "leader".equals(r) || "officer".equals(r);
    }

    private final java.util.Map<UUID, Invite> pendingInvites = new java.util.concurrent.ConcurrentHashMap<>();

    private record Invite(int crewId, UUID from, long time) {}
}
