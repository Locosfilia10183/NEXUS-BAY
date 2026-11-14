package de.nexusbay.core;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Modern multi-page Admin GUI (Java inventory menus).
 * Hub → Players / Moderation / Economy / Items / World / Quests
 */
public final class AdminMenu implements Listener {

    private final NexusBay plugin;

    public static final String HUB = "§8§l▸ §6NEXUS-BAY Admin§8 §l◂";
    public static final String PLAYERS = "§8§l▸ §bSpieler§8 §l◂";
    public static final String MOD = "§8§l▸ §cModeration§8 §l◂";
    public static final String ECO = "§8§l▸ §6Economy§8 §l◂";
    public static final String ITEMS = "§8§l▸ §aItems§8 §l◂";
    public static final String WORLD = "§8§l▸ §2Welt§8 §l◂";
    public static final String PLAYER_PREFIX = "§8§l▸ §eSpieler: ";
    public static final String INSPECT_PREFIX = "§8§l▸ §cInv: ";

    private final java.util.Map<UUID, String> selectedPlayer = new java.util.concurrent.ConcurrentHashMap<>();

    public AdminMenu(NexusBay plugin) {
        this.plugin = plugin;
    }

    private ItemStack item(Material mat, String name, String... lore) {
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) meta.setLore(Arrays.asList(lore));
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack pane() {
        return item(Material.GRAY_STAINED_GLASS_PANE, " ");
    }

    private void fillBorder(Inventory inv) {
        ItemStack p = pane();
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, p);
            inv.setItem(45 + i, p);
        }
        for (int i = 9; i < 45; i += 9) {
            inv.setItem(i, p);
            inv.setItem(i + 8, p);
        }
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, HUB);
        fillBorder(inv);
        inv.setItem(20, item(Material.PLAYER_HEAD, "§b§lSpieler",
                "§7Online-Spieler verwalten",
                "§8Level · Coins · Inventar · TP"));
        inv.setItem(22, item(Material.IRON_SWORD, "§c§lModeration",
                "§7Kick · Ban · Heal · Gamemode"));
        inv.setItem(24, item(Material.GOLD_INGOT, "§6§lEconomy",
                "§7Coins geben/setzen",
                "§7Kopfgeld verwalten"));
        inv.setItem(30, item(Material.CHEST, "§a§lItems",
                "§7Nützliche Items geben"));
        inv.setItem(32, item(Material.GRASS_BLOCK, "§2§lWelt & Spawn",
                "§7Zeit · Wetter · Spawn bauen",
                "§7Insel-Hinweis"));
        inv.setItem(40, item(Material.WRITABLE_BOOK, "§e§lServer-Info",
                "§7Online: §f" + Bukkit.getOnlinePlayers().size(),
                "§7TPS-Hinweis: §f/tps im Chat"));
        inv.setItem(49, item(Material.BARRIER, "§cSchließen"));
        p.openInventory(inv);
    }

    public void openPlayers(Player admin) {
        Inventory inv = Bukkit.createInventory(null, 54, PLAYERS);
        fillBorder(inv);
        int slot = 10;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (slot >= 44) break;
            if (slot % 9 == 0 || slot % 9 == 8) { slot++; continue; }
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) head.getItemMeta();
            sm.setOwningPlayer(online);
            sm.setDisplayName("§e" + online.getName());
            sm.setLore(List.of(
                    "§7Level: §f" + plugin.data().level(online),
                    "§7Coins: §6" + plugin.data().coins(online),
                    "§8Klick: Spieler-Menü"
            ));
            head.setItemMeta(sm);
            inv.setItem(slot++, head);
        }
        inv.setItem(45, item(Material.ARROW, "§7← Zurück"));
        inv.setItem(49, item(Material.BARRIER, "§cSchließen"));
        admin.openInventory(inv);
    }

    public void openPlayerMenu(Player admin, Player target) {
        selectedPlayer.put(admin.getUniqueId(), target.getName());
        Inventory inv = Bukkit.createInventory(null, 54, PLAYER_PREFIX + target.getName());
        fillBorder(inv);
        inv.setItem(20, item(Material.ENDER_PEARL, "§bTeleport zu Spieler"));
        inv.setItem(21, item(Material.ENDER_EYE, "§bSpieler herholen"));
        inv.setItem(23, item(Material.CHEST, "§eInventar ansehen"));
        inv.setItem(24, item(Material.EXPERIENCE_BOTTLE, "§a+5 Level"));
        inv.setItem(29, item(Material.GOLD_INGOT, "§6+1.000 Coins"));
        inv.setItem(30, item(Material.GOLD_BLOCK, "§6+10.000 Coins"));
        inv.setItem(32, item(Material.REDSTONE, "§cKick"));
        inv.setItem(33, item(Material.BARRIER, "§4Ban (Name)"));
        inv.setItem(40, item(Material.POTION, "§dHeilen + Füllen"));
        inv.setItem(45, item(Material.ARROW, "§7← Spielerliste"));
        inv.setItem(49, item(Material.BARRIER, "§cSchließen"));
        admin.openInventory(inv);
    }

    public void openMod(Player admin) {
        Inventory inv = Bukkit.createInventory(null, 54, MOD);
        fillBorder(inv);
        inv.setItem(20, item(Material.DIAMOND_SWORD, "§cAdmin-Modus", "§7Creative + Heal"));
        inv.setItem(22, item(Material.SHIELD, "§eSurvival-Modus"));
        inv.setItem(24, item(Material.POTION, "§dSelbst heilen"));
        inv.setItem(30, item(Material.PLAYER_HEAD, "§bZu Spieler-Menü", "§7Kick/Ban pro Spieler"));
        inv.setItem(45, item(Material.ARROW, "§7← Zurück"));
        inv.setItem(49, item(Material.BARRIER, "§cSchließen"));
        admin.openInventory(inv);
    }

    public void openEco(Player admin) {
        Inventory inv = Bukkit.createInventory(null, 54, ECO);
        fillBorder(inv);
        inv.setItem(20, item(Material.GOLD_NUGGET, "§6+100 Coins (selbst)"));
        inv.setItem(22, item(Material.GOLD_INGOT, "§6+1.000 Coins (selbst)"));
        inv.setItem(24, item(Material.GOLD_BLOCK, "§6+10.000 Coins (selbst)"));
        inv.setItem(31, item(Material.PLAYER_HEAD, "§eCoins an Spieler", "§7Über Spieler-Menü"));
        inv.setItem(33, item(Material.WRITABLE_BOOK, "§cKopfgeld-Hinweis",
                "§7Spieler-Kopfgeld: demnächst",
                "§7Quest-Kopfgeld: /bayquest"));
        inv.setItem(45, item(Material.ARROW, "§7← Zurück"));
        inv.setItem(49, item(Material.BARRIER, "§cSchließen"));
        admin.openInventory(inv);
    }

    public void openItems(Player admin) {
        Inventory inv = Bukkit.createInventory(null, 54, ITEMS);
        fillBorder(inv);
        inv.setItem(20, item(Material.DIAMOND_SWORD, "§bDiamantschwert"));
        inv.setItem(21, item(Material.NETHERITE_SWORD, "§5Netherite-Schwert"));
        inv.setItem(22, item(Material.BOW, "§aBogen"));
        inv.setItem(23, item(Material.ARROW, "§f64 Pfeile"));
        inv.setItem(24, item(Material.GOLDEN_APPLE, "§6Goldapfel x8"));
        inv.setItem(29, item(Material.COOKED_BEEF, "§eSteak x32"));
        inv.setItem(30, item(Material.TORCH, "§eFackeln x64"));
        inv.setItem(31, item(Material.ENDER_PEARL, "§5Enderperlen x16"));
        inv.setItem(32, item(Material.ELYTRA, "§dElytra"));
        inv.setItem(33, item(Material.FIREWORK_ROCKET, "§cRaketen x16"));
        inv.setItem(45, item(Material.ARROW, "§7← Zurück"));
        inv.setItem(49, item(Material.BARRIER, "§cSchließen"));
        admin.openInventory(inv);
    }

    public void openWorld(Player admin) {
        Inventory inv = Bukkit.createInventory(null, 54, WORLD);
        fillBorder(inv);
        inv.setItem(20, item(Material.COMPASS, "§eZum Spawn"));
        inv.setItem(22, item(Material.CLOCK, "§bTag"));
        inv.setItem(24, item(Material.BLAZE_ROD, "§6Wetter klar"));
        inv.setItem(30, item(Material.DARK_OAK_BOAT, "§6§lHafen-Spawn bauen",
                "§7Großer Pirate-Harbor an deiner Position",
                "§cÜberschreibt Blöcke im Umkreis!"));
        inv.setItem(32, item(Material.GRASS_BLOCK, "§aInsel erstellen",
                "§7/bayisland create <name>"));
        inv.setItem(34, item(Material.BELL, "§dBroadcast", "§7Willkommensnachricht"));
        inv.setItem(45, item(Material.ARROW, "§7← Zurück"));
        inv.setItem(49, item(Material.BARRIER, "§cSchließen"));
        admin.openInventory(inv);
    }

    public void inspect(Player admin, Player target) {
        Inventory inv = Bukkit.createInventory(null, 54, INSPECT_PREFIX + target.getName());
        ItemStack[] contents = target.getInventory().getStorageContents();
        for (int i = 0; i < Math.min(contents.length, 36); i++) {
            if (contents[i] != null) inv.setItem(i, contents[i].clone());
        }
        inv.setItem(49, item(Material.ARROW, "§7← Zurück"));
        admin.openInventory(inv);
    }

    private boolean isAdminInv(String title) {
        return title.equals(HUB) || title.equals(PLAYERS) || title.equals(MOD) || title.equals(ECO)
                || title.equals(ITEMS) || title.equals(WORLD) || title.startsWith(PLAYER_PREFIX)
                || title.startsWith(INSPECT_PREFIX);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        if (!isAdminInv(title)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!p.hasPermission("nexusbay.admin")) {
            p.closeInventory();
            return;
        }
        ItemStack cur = e.getCurrentItem();
        if (cur == null || cur.getType() == Material.AIR || cur.getType() == Material.GRAY_STAINED_GLASS_PANE)
            return;

        int slot = e.getSlot();

        if (title.equals(HUB)) {
            switch (slot) {
                case 20 -> openPlayers(p);
                case 22 -> openMod(p);
                case 24 -> openEco(p);
                case 30 -> openItems(p);
                case 32 -> openWorld(p);
                case 40 -> p.sendMessage("§6Online: §f" + Bukkit.getOnlinePlayers().size());
                case 49 -> p.closeInventory();
            }
            return;
        }

        if (title.equals(PLAYERS)) {
            if (slot == 45) { open(p); return; }
            if (slot == 49) { p.closeInventory(); return; }
            if (cur.getType() == Material.PLAYER_HEAD && cur.getItemMeta() instanceof SkullMeta sm) {
                OfflinePlayer op = sm.getOwningPlayer();
                if (op != null && op.isOnline()) openPlayerMenu(p, op.getPlayer());
            }
            return;
        }

        if (title.startsWith(PLAYER_PREFIX)) {
            String name = title.substring(PLAYER_PREFIX.length());
            Player target = Bukkit.getPlayerExact(name);
            if (slot == 45) { openPlayers(p); return; }
            if (slot == 49) { p.closeInventory(); return; }
            if (target == null) {
                p.sendMessage("§cSpieler offline.");
                return;
            }
            switch (slot) {
                case 20 -> { p.teleport(target.getLocation()); p.sendMessage("§aZu " + target.getName()); }
                case 21 -> { target.teleport(p.getLocation()); p.sendMessage("§a" + target.getName() + " hergeholt."); }
                case 23 -> inspect(p, target);
                case 24 -> {
                    for (int i = 0; i < 5; i++) plugin.data().addXp(target, plugin.data().nextLevelXp(target));
                    p.sendMessage("§a+Level bei " + target.getName());
                    target.sendMessage("§aEin Admin hat dein Level erhöht.");
                }
                case 29 -> {
                    plugin.data().addCoins(target, 1000);
                    p.sendMessage("§a+1000 Coins → " + target.getName());
                    target.sendMessage("§a+1000 Bay-Coins (Admin)");
                }
                case 30 -> {
                    plugin.data().addCoins(target, 10000);
                    p.sendMessage("§a+10000 Coins → " + target.getName());
                }
                case 32 -> {
                    target.kickPlayer("§cGekickt von " + p.getName());
                    p.sendMessage("§e" + name + " gekickt.");
                    openPlayers(p);
                }
                case 33 -> {
                    Bukkit.getBanList(BanList.Type.NAME).addBan(name, "Banned by " + p.getName(), null, p.getName());
                    if (target.isOnline()) target.kickPlayer("§4Gebannt.");
                    p.sendMessage("§c" + name + " gebannt.");
                    openPlayers(p);
                }
                case 40 -> {
                    target.setHealth(20);
                    target.setFoodLevel(20);
                    p.sendMessage("§a" + name + " geheilt.");
                }
            }
            return;
        }

        if (title.startsWith(INSPECT_PREFIX)) {
            if (slot == 49) {
                String name = selectedPlayer.get(p.getUniqueId());
                Player t = name == null ? null : Bukkit.getPlayerExact(name);
                if (t != null) openPlayerMenu(p, t);
                else openPlayers(p);
            }
            return;
        }

        if (title.equals(MOD)) {
            if (slot == 45) { open(p); return; }
            if (slot == 49) { p.closeInventory(); return; }
            switch (slot) {
                case 20 -> {
                    p.setGameMode(GameMode.CREATIVE);
                    p.setHealth(20);
                    p.setFoodLevel(20);
                    p.sendMessage("§aAdmin-Modus.");
                }
                case 22 -> {
                    p.setGameMode(GameMode.SURVIVAL);
                    p.sendMessage("§eSurvival.");
                }
                case 24 -> {
                    p.setHealth(20);
                    p.setFoodLevel(20);
                    p.sendMessage("§aGeheilt.");
                }
                case 30 -> openPlayers(p);
            }
            return;
        }

        if (title.equals(ECO)) {
            if (slot == 45) { open(p); return; }
            if (slot == 49) { p.closeInventory(); return; }
            switch (slot) {
                case 20 -> { plugin.data().addCoins(p, 100); p.sendMessage("§a+100 Coins"); }
                case 22 -> { plugin.data().addCoins(p, 1000); p.sendMessage("§a+1000 Coins"); }
                case 24 -> { plugin.data().addCoins(p, 10000); p.sendMessage("§a+10000 Coins"); }
                case 31 -> openPlayers(p);
            }
            return;
        }

        if (title.equals(ITEMS)) {
            if (slot == 45) { open(p); return; }
            if (slot == 49) { p.closeInventory(); return; }
            switch (slot) {
                case 20 -> p.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
                case 21 -> p.getInventory().addItem(new ItemStack(Material.NETHERITE_SWORD));
                case 22 -> p.getInventory().addItem(new ItemStack(Material.BOW));
                case 23 -> p.getInventory().addItem(new ItemStack(Material.ARROW, 64));
                case 24 -> p.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 8));
                case 29 -> p.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 32));
                case 30 -> p.getInventory().addItem(new ItemStack(Material.TORCH, 64));
                case 31 -> p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 16));
                case 32 -> p.getInventory().addItem(new ItemStack(Material.ELYTRA));
                case 33 -> p.getInventory().addItem(new ItemStack(Material.FIREWORK_ROCKET, 16));
            }
            p.sendMessage("§aItem erhalten.");
            return;
        }

        if (title.equals(WORLD)) {
            if (slot == 45) { open(p); return; }
            if (slot == 49) { p.closeInventory(); return; }
            switch (slot) {
                case 20 -> {
                    p.teleport(p.getWorld().getSpawnLocation());
                    p.sendMessage("§aSpawn.");
                }
                case 22 -> {
                    p.getWorld().setTime(1000);
                    p.sendMessage("§aTag.");
                }
                case 24 -> {
                    p.getWorld().setStorm(false);
                    p.getWorld().setThundering(false);
                    p.sendMessage("§aWetter klar.");
                }
                case 30 -> {
                    p.closeInventory();
                    plugin.harbor().buildAt(p);
                }
                case 32 -> p.sendMessage("§e/bayisland create <name>");
                case 34 -> plugin.broadcast("&eWillkommen bei &6NEXUS-BAY&e!");
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (isAdminInv(e.getView().getTitle())) e.setCancelled(true);
    }
}
