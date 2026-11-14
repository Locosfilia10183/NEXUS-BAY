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
    private DatabaseService database;
    private CrewService crews;
    private SkillService skills;
    private QuestService quests;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        database = new DatabaseService(this);
        boolean dbOk = database.enable();

        data = new PlayerData(this);
        islands = new IslandService(this);
        adminMenu = new AdminMenu(this);
        crews = new CrewService(this);
        skills = new SkillService(this);
        quests = new QuestService(this);

        if (dbOk) quests.ensureSeed();

        for (String command : new String[]{
                "bayadmin", "bayisland", "bayprofile", "baypay", "bayinv",
                "baycrew", "bayskill", "bayquest"}) {
            PluginCommand cmd = getCommand(command);
            if (cmd != null) cmd.setExecutor(this);
        }

        getServer().getPluginManager().registerEvents(adminMenu, this);
        getServer().getPluginManager().registerEvents(data, this);

        getLogger().info("NEXUS-BAY Core enabled." + (dbOk ? " (MariaDB + Crews/Skills/Quests)" : " (YAML storage)"));
    }

    @Override
    public void onDisable() {
        if (data != null) data.save();
        if (database != null) database.disable();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase();

        if (name.equals("bayadmin")) {
            if (!(sender instanceof Player p) || !p.hasPermission("nexusbay.admin")) {
                sender.sendMessage("§cNur Admins können das Panel öffnen.");
                return true;
            }
            adminMenu.open(p);
            return true;
        }

        if (name.equals("bayisland")) {
            if (args.length == 2 && args[0].equalsIgnoreCase("visit") && sender instanceof Player player) {
                if (!islands.visit(player, args[1])) sender.sendMessage("§cInsel nicht gefunden.");
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
                var section = getConfig().getConfigurationSection("islands");
                sender.sendMessage("§6NEXUS-BAY Inseln: §f" +
                        (section == null ? "Noch keine" : String.join(", ", section.getKeys(false))));
                return true;
            }
            if (!sender.hasPermission("nexusbay.admin")) {
                sender.sendMessage("§cKeine Berechtigung.");
                return true;
            }
            if (args.length < 2 || !args[0].equalsIgnoreCase("create")) {
                sender.sendMessage("§e/bayisland create <name> | visit <name> | list");
                return true;
            }
            String islandName = args[1].replaceAll("[^a-zA-Z0-9_-]", "");
            if (islandName.isBlank()) {
                sender.sendMessage("§cUngültiger Inselname.");
                return true;
            }
            if (islands.create(islandName))
                sender.sendMessage("§aInsel §f" + islandName + " §awurde erstellt.");
            else
                sender.sendMessage("§cDiese Insel existiert bereits oder konnte nicht erstellt werden.");
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
            if (!(sender instanceof Player p) || !sender.hasPermission("nexusbay.admin") || args.length != 1) {
                sender.sendMessage("§e/bayinv <spieler>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage("§cSpieler muss online sein.");
                return true;
            }
            adminMenu.inspect(p, target);
            return true;
        }

        if (name.equals("baypay")) {
            if (!(sender instanceof Player p) || args.length != 2) {
                sender.sendMessage("§e/baypay <spieler> <betrag>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            long amount;
            try {
                amount = Long.parseLong(args[1]);
            } catch (NumberFormatException ex) {
                sender.sendMessage("§cBetrag muss eine Zahl sein.");
                return true;
            }
            if (target == null || target.equals(p) || amount < 1) {
                sender.sendMessage("§cSpieler oder Betrag ungültig.");
                return true;
            }
            if (!data.transfer(p, target, amount)) {
                sender.sendMessage("§cDu hast nicht genügend Bay-Coins.");
                return true;
            }
            p.sendMessage("§aDu hast §6" + amount + " Bay-Coins §aan " + target.getName() + " gesendet.");
            target.sendMessage("§aDu hast §6" + amount + " Bay-Coins §avon " + p.getName() + " erhalten.");
            return true;
        }

        if (name.equals("baycrew")) {
            if (!(sender instanceof Player p)) return true;
            if (args.length == 0) {
                for (String line : crews.info(p)) sender.sendMessage(line);
                return true;
            }
            String sub = args[0].toLowerCase();
            if (sub.equals("create") && args.length >= 2) {
                sender.sendMessage(crews.create(p, args[1]));
                return true;
            }
            if (sub.equals("invite") && args.length >= 2) {
                sender.sendMessage(crews.invite(p, args[1]));
                return true;
            }
            if (sub.equals("accept")) {
                sender.sendMessage(crews.accept(p));
                return true;
            }
            if (sub.equals("leave")) {
                sender.sendMessage(crews.leave(p));
                return true;
            }
            if (sub.equals("deposit") && args.length >= 2) {
                try {
                    sender.sendMessage(crews.deposit(p, Long.parseLong(args[1])));
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cBetrag ungültig.");
                }
                return true;
            }
            if (sub.equals("info")) {
                for (String line : crews.info(p)) sender.sendMessage(line);
                return true;
            }
            sender.sendMessage("§e/baycrew <create|invite|accept|leave|deposit|info>");
            return true;
        }

        if (name.equals("bayskill")) {
            if (!(sender instanceof Player p)) return true;
            if (args.length == 0) {
                sender.sendMessage("§e/bayskill list <tree> | unlock <tree> <node>");
                sender.sendMessage("§7Trees: §f" + String.join(", ", SkillService.TREES.keySet()));
                return true;
            }
            if (args[0].equalsIgnoreCase("list") && args.length >= 2) {
                for (String line : skills.list(p, args[1].toLowerCase())) sender.sendMessage(line);
                return true;
            }
            if (args[0].equalsIgnoreCase("unlock") && args.length >= 3) {
                sender.sendMessage(skills.unlock(p, args[1].toLowerCase(), args[2].toLowerCase()));
                return true;
            }
            sender.sendMessage("§e/bayskill list <tree> | unlock <tree> <node>");
            return true;
        }

        if (name.equals("bayquest")) {
            if (!(sender instanceof Player p)) return true;
            if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
                for (String line : quests.listActive(p)) sender.sendMessage(line);
                return true;
            }
            if (args[0].equalsIgnoreCase("status")) {
                for (String line : quests.status(p)) sender.sendMessage(line);
                return true;
            }
            if (args[0].equalsIgnoreCase("accept") && args.length >= 2) {
                sender.sendMessage(quests.accept(p, args[1].toLowerCase()));
                return true;
            }
            if (args[0].equalsIgnoreCase("abandon") && args.length >= 2) {
                sender.sendMessage(quests.abandon(p, args[1].toLowerCase()));
                return true;
            }
            sender.sendMessage("§e/bayquest list | status | accept <code> | abandon <code>");
            return true;
        }

        return false;
    }

    public IslandService islands() { return islands; }
    public PlayerData data() { return data; }
    public DatabaseService database() { return database; }
    public CrewService crews() { return crews; }
    public SkillService skills() { return skills; }
    public QuestService quests() { return quests; }

    public void broadcast(String message) {
        Bukkit.broadcastMessage("§6§lNEXUS-BAY §8» §f" + ChatColor.translateAlternateColorCodes('&', message));
    }
}
