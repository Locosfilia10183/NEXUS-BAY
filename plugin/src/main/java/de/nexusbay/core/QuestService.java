package de.nexusbay.core;

import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public final class QuestService implements Listener {
    private static final int TARGET = 10;
    private static final int REWARD_COINS = 100;
    private static final int REWARD_XP = 50;
    private final NexusBay plugin;

    public QuestService(NexusBay plugin) { this.plugin = plugin; }

    private String path(Player player) { return "quests." + player.getUniqueId(); }

    public boolean handle(Player player, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            showStatus(player);
            return true;
        }
        String action = args[0].toLowerCase();
        if (action.equals("accept")) {
            if (plugin.getConfig().getBoolean(path(player) + ".active", false)) {
                player.sendMessage("§eDu hast bereits einen aktiven Kopfgeldauftrag. Nutze §f/bayquest status§e.");
                return true;
            }
            plugin.getConfig().set(path(player) + ".active", true);
            plugin.getConfig().set(path(player) + ".progress", 0);
            plugin.saveConfig();
            player.sendMessage("§6§lKOPFGELD ANGENOMMEN! §eBesiege 10 Monster und kassiere 100 Bay-Coins + 50 XP.");
            return true;
        }
        if (action.equals("abandon")) {
            if (!plugin.getConfig().getBoolean(path(player) + ".active", false)) {
                player.sendMessage("§cDu hast keinen aktiven Auftrag.");
                return true;
            }
            plugin.getConfig().set(path(player) + ".active", false);
            plugin.getConfig().set(path(player) + ".progress", 0);
            plugin.saveConfig();
            player.sendMessage("§7Dein Kopfgeldauftrag wurde abgebrochen.");
            return true;
        }
        player.sendMessage("§e/bayquest [status|accept|abandon]");
        return true;
    }

    private void showStatus(Player player) {
        if (!plugin.getConfig().getBoolean(path(player) + ".active", false)) {
            player.sendMessage("§6§lNEXUS-BAY §8» §7Kein aktiver Kopfgeldauftrag.");
            player.sendMessage("§e/bayquest accept §7– Auftrag annehmen (10 Monster).");
            return;
        }
        int progress = plugin.getConfig().getInt(path(player) + ".progress", 0);
        player.sendMessage("§6§lKOPFGELD §8» §eMonster besiegen");
        player.sendMessage("§7Fortschritt: §b" + progress + "§7/§b" + TARGET);
        player.sendMessage("§7Belohnung: §6" + REWARD_COINS + " Bay-Coins §7+ §b" + REWARD_XP + " XP");
    }

    @EventHandler
    public void onMonsterKill(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Monster) || event.getEntity().getKiller() == null) return;
        Player player = event.getEntity().getKiller();
        if (!plugin.getConfig().getBoolean(path(player) + ".active", false)) return;

        String progressPath = path(player) + ".progress";
        int progress = plugin.getConfig().getInt(progressPath, 0) + 1;
        if (progress >= TARGET) {
            plugin.getConfig().set(path(player) + ".active", false);
            plugin.getConfig().set(progressPath, 0);
            plugin.data().addCoins(player, REWARD_COINS);
            plugin.data().addXp(player, REWARD_XP);
            player.sendMessage("§6§lKOPFGELD ERLEDIGT! §e+" + REWARD_COINS + " Bay-Coins §7und §b+" + REWARD_XP + " XP");
        } else {
            plugin.getConfig().set(progressPath, progress);
            player.sendMessage("§bKopfgeld-Fortschritt: §f" + progress + "/" + TARGET);
        }
        plugin.saveConfig();
    }
}
