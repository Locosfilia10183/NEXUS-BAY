package de.nexusbay.core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class NexusBay extends JavaPlugin implements CommandExecutor {
    private IslandService islands;
    private PlayerData data;
    private AdminMenu adminMenu;
    private QuestService quests;

    @Override public void onEnable() {
        saveDefaultConfig();
        data = new PlayerData(this);
        islands = new IslandService(this);
        adminMenu = new AdminMenu(this);
        quests = new QuestService(this);
        for (String command : new String[]{"bayadmin","bayisland","bayprofile","baypay","bayinv","bayquest"}) {
            PluginCommand cmd = getCommand(command);
            if (cmd != null) cmd.setExecutor(this);
        }
        getServer().getPluginManager().registerEvents(adminMenu, this);
        getServer().getPluginManager().registerEvents(data, this);
        getServer().getPluginManager().registerEvents(quests, this);
        getLogger().info("NEXUS-BAY Core enabled. RPG, islands, bounty quests and admin tools ready.");
    }

    @Override public void onDisable() { if (data != null) data.save(); }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase();
        if (name.equals("bayadmin")) {
            if (!(sender instanceof Player p) || !p.hasPermission("nexusbay.admin")) { sender.sendMessage("§cNur Admins können das Panel öffnen."); return true; }
            adminMenu.open(p); return true;
        }
        if (name.equals("bayquest")) {
            if (!(sender instanceof Player p)) { sender.sendMessage("§cDieser Befehl ist nur im Spiel verfügbar."); return true; }
            return quests.handle(p, args);
        }
        if (name.equals("bayisland")) {
            if (args.length == 2 && args[0].equalsIgnoreCase("visit") && sender instanceof Player player) {
                if (!islands.visit(player,args[1])) sender.sendMessage("§cInsel nicht gefunden.");
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
                var section=getConfig().getConfigurationSection("islands");
                sender.sendMessage("§6NEXUS-BAY Inseln: §f"+(section==null ? "Noch keine" : String.join(", ",section.getKeys(false))));
                return true;
            }
            if (!sender.hasPermission("nexusbay.admin")) { sender.sendMessage("§cKeine Berechtigung."); return true; }
            if (args.length < 2 || !args[0].equalsIgnoreCase("create")) { sender.sendMessage("§e/bayisland create <name> | /bayisland visit <name> | /bayisland list"); return true; }
            String islandName = args[1].replaceAll("[^a-zA-Z0-9_-]", "");
            if (islandName.isBlank()) { sender.sendMessage("§cUngültiger Inselname."); return true; }
            if (islands.create(islandName)) sender.sendMessage("§aInsel §f" + islandName + " §awurde erstellt.");
            else sender.sendMessage("§cDiese Insel existiert bereits oder konnte nicht erstellt werden.");
            return true;
        }
        if (name.equals("bayprofile")) {
            if (!(sender instanceof Player p)) return true;
            sender.sendMessage("§6§lNEXUS-BAY §8» §fDein Piratenprofil");
            sender.sendMessage("§7Level: §e" + data.level(p) + " §8| §7XP: §b" + data.xp(p) + "§7/§b" + data.nextLevelXp(p));
            sender.sendMessage("§7Bay-Coins: §6" + data.coins(p));
            return true;
        }
        if (name.equals("bayinv")) {
            if (!(sender instanceof Player p) || !sender.hasPermission("nexusbay.admin") || args.length != 1) { sender.sendMessage("§e/bayinv <spieler>"); return true; }
            Player target=Bukkit.getPlayerExact(args[0]);
            if (target==null) { sender.sendMessage("§cSpieler muss online sein."); return true; }
            adminMenu.inspect(p,target); return true;
        }
        if (name.equals("baypay")) {
            if (!(sender instanceof Player p) || args.length != 2) { sender.sendMessage("§e/baypay <spieler> <betrag>"); return true; }
            Player target = Bukkit.getPlayerExact(args[0]);
            int amount;
            try { amount = Integer.parseInt(args[1]); } catch (NumberFormatException ex) { sender.sendMessage("§cBetrag muss eine Zahl sein."); return true; }
            if (target == null || target.equals(p) || amount < 1) { sender.sendMessage("§cSpieler oder Betrag ungültig."); return true; }
            if (!data.transfer(p, target, amount)) { sender.sendMessage("§cDu hast nicht genügend Bay-Coins."); return true; }
            p.sendMessage("§aDu hast §6" + amount + " Bay-Coins §aan " + target.getName() + " gesendet.");
            target.sendMessage("§aDu hast §6" + amount + " Bay-Coins §avon " + p.getName() + " erhalten.");
            return true;
        }
        return false;
    }

    public IslandService islands() { return islands; }
    public PlayerData data() { return data; }
    public void broadcast(String message) { Bukkit.broadcastMessage("§6§lNEXUS-BAY §8» §f" + ChatColor.translateAlternateColorCodes('&', message)); }
}
