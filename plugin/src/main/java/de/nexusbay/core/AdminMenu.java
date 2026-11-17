package de.nexusbay.core;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;

public final class AdminMenu implements Listener {
    private final NexusBay plugin;
    private static final String TITLE="§0NEXUS-BAY §8| §6Admin Control";
    private static final String INSPECT_PREFIX="§0NEXUS-BAY §8| §cInventory: ";
    public AdminMenu(NexusBay plugin) { this.plugin=plugin; }
    private ItemStack item(Material material,String name,String... lore) {
        ItemStack stack=new ItemStack(material); ItemMeta meta=stack.getItemMeta();
        meta.setDisplayName(name); meta.setLore(List.of(lore)); stack.setItemMeta(meta); return stack;
    }
    public void open(Player p) {
        Inventory inv=Bukkit.createInventory(null,54,TITLE);
        inv.setItem(10,item(Material.COMPASS,"§eTeleport: Weltspawn","§7Zum Hauptspawn teleportieren"));
        inv.setItem(12,item(Material.GRASS_BLOCK,"§aNeue Insel erstellen","§7Erstellt eine prozedurale Pirateninsel","§7Befehl: /bayisland create <name>"));
        inv.setItem(14,item(Material.DIAMOND_SWORD,"§cAdmin-Modus","§7Kreativmodus und Heilung"));
        inv.setItem(16,item(Material.CLOCK,"§bTag einstellen","§7Setzt die Weltzeit auf Tag"));
        inv.setItem(28,item(Material.BLAZE_ROD,"§6Wetter: Klar","§7Entfernt Regen und Gewitter"));
        inv.setItem(30,item(Material.PAPER,"§fServerstatus","§7Online: §a"+Bukkit.getOnlinePlayers().size(),"§7Welten: §a"+Bukkit.getWorlds().size()));
        inv.setItem(32,item(Material.GOLD_INGOT,"§6Bay-Coins geben","§7Gibt dir 1.000 Coins"));
        inv.setItem(34,item(Material.BELL,"§dServerankündigung","§7Sendet eine Testankündigung"));
        inv.setItem(49,item(Material.BARRIER,"§cSchließen"));
        p.openInventory(inv);
    }
    public void inspect(Player admin,Player target) {
        Inventory inv=Bukkit.createInventory(null,54,INSPECT_PREFIX+target.getName());
        ItemStack[] contents=target.getInventory().getStorageContents();
        for (int i=0;i<Math.min(contents.length,36);i++) if (contents[i]!=null) inv.setItem(i,contents[i].clone());
        admin.openInventory(inv);
    }
    @EventHandler public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().startsWith(INSPECT_PREFIX)) { e.setCancelled(true); return; }
        if (!TITLE.equals(e.getView().getTitle())) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p) || e.getCurrentItem()==null) return;
        switch(e.getSlot()) {
            case 10 -> { World w=p.getWorld(); p.teleport(w.getSpawnLocation()); p.sendMessage("§aZum Weltspawn teleportiert."); }
            case 12 -> p.sendMessage("§eErstelle Insel mit: §f/bayisland create <name>");
            case 14 -> { p.setGameMode(GameMode.CREATIVE); p.setHealth(20); p.setFoodLevel(20); p.sendMessage("§aAdmin-Modus aktiviert."); }
            case 16 -> { p.getWorld().setTime(1000); p.sendMessage("§aWeltzeit auf Tag gesetzt."); }
            case 28 -> { p.getWorld().setStorm(false); p.getWorld().setThundering(false); p.sendMessage("§aWetter bereinigt."); }
            case 30 -> p.sendMessage("§6Online: §f"+Bukkit.getOnlinePlayers().size()+" §8| §6Welten: §f"+Bukkit.getWorlds().size());
            case 32 -> { plugin.data().addCoins(p,1000); p.sendMessage("§a1.000 Bay-Coins gutgeschrieben."); }
            case 34 -> plugin.broadcast("&eDie Crew von NEXUS-BAY heißt euch willkommen!");
            case 49 -> p.closeInventory();
        }
    }
    @EventHandler public void onDrag(InventoryDragEvent e) { if (TITLE.equals(e.getView().getTitle()) || e.getView().getTitle().startsWith(INSPECT_PREFIX)) e.setCancelled(true); }
}