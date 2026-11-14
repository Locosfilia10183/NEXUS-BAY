package de.nexusbay.core;

import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerData implements Listener {
    private final NexusBay plugin;
    private final ConfigurationSection players;
    public PlayerData(NexusBay plugin) {
        this.plugin = plugin;
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("players");
        this.players = section == null ? plugin.getConfig().createSection("players") : section;
    }
    private String key(Player p) { return p.getUniqueId().toString(); }
    private void init(Player p) {
        String k = key(p);
        if (!players.contains(k)) {
            players.set(k + ".name", p.getName());
            players.set(k + ".level", 1);
            players.set(k + ".xp", 0);
            players.set(k + ".coins", plugin.getConfig().getInt("settings.starting-coins", 250));
        }
        players.set(k + ".name", p.getName());
    }
    @EventHandler public void onJoin(PlayerJoinEvent event) { init(event.getPlayer()); save(); }
    public int level(Player p) { init(p); return players.getInt(key(p)+".level",1); }
    public int xp(Player p) { init(p); return players.getInt(key(p)+".xp",0); }
    public int coins(Player p) { init(p); return players.getInt(key(p)+".coins",0); }
    public int nextLevelXp(Player p) { return level(p) * plugin.getConfig().getInt("settings.xp-per-level",100); }
    public void addXp(Player p, int amount) {
        if (amount <= 0) return;
        init(p); String k=key(p); int xp=xp(p)+amount; int level=level(p);
        int needed=level*plugin.getConfig().getInt("settings.xp-per-level",100);
        while (xp >= needed) { xp-=needed; level++; needed=level*plugin.getConfig().getInt("settings.xp-per-level",100); p.sendMessage("§6§lLEVEL UP! §eDu bist jetzt Level "+level+"!"); }
        players.set(k+".xp",xp); players.set(k+".level",level); save();
    }
    public boolean transfer(Player from, Player to, int amount) {
        init(from); init(to); if (coins(from)<amount) return false;
        players.set(key(from)+".coins",coins(from)-amount);
        players.set(key(to)+".coins",coins(to)+amount); save(); return true;
    }
    public void addCoins(Player p,int amount) { init(p); players.set(key(p)+".coins",Math.max(0,coins(p)+amount)); save(); }
    public void save() { plugin.saveConfig(); }
}