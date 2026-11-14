package de.nexusbay.core;

import java.util.Locale;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class IslandService {
    private final NexusBay plugin;
    public IslandService(NexusBay plugin) { this.plugin=plugin; }

    public boolean create(String name) {
        ConfigurationSection all=plugin.getConfig().getConfigurationSection("islands");
        if (all == null) all=plugin.getConfig().createSection("islands");
        String key=name.toLowerCase(Locale.ROOT);
        if (all.contains(key) || !IslandProgression.isKnown(name)) return false;
        World world=Bukkit.getWorld("world");
        if (world==null) return false;
        int spacing=plugin.getConfig().getInt("settings.island-spacing",320);
        int index=all.getKeys(false).size();
        int x=(index%8)*spacing, z=(index/8)*spacing, y=100;
        buildIsland(world,x,y,z,name);
        all.set(key+".name",name); all.set(key+".world",world.getName());
        all.set(key+".x",x+0.5); all.set(key+".y",y+3); all.set(key+".z",z+0.5);
        all.set(key+".required-level",IslandProgression.requiredLevel(name));
        plugin.saveConfig();
        return true;
    }

    public boolean visit(Player player,String name) {
        String key=name.toLowerCase(Locale.ROOT);
        if (!plugin.getConfig().contains("islands."+key)) return false;
        int required=plugin.getConfig().getInt("islands."+key+".required-level",IslandProgression.requiredLevel(name));
        int current=plugin.data().level(player);
        if (current < required) {
            player.sendMessage("§cLevel zu niedrig! §7" + name + " benötigt §eLevel " + required + "§7. Dein Level: §e" + current);
            return true;
        }
        String worldName=plugin.getConfig().getString("islands."+key+".world","world");
        World world=Bukkit.getWorld(worldName);
        if (world==null) return false;
        double x=plugin.getConfig().getDouble("islands."+key+".x");
        double y=plugin.getConfig().getDouble("islands."+key+".y");
        double z=plugin.getConfig().getDouble("islands."+key+".z");
        player.teleport(new Location(world,x,y,z));
        player.sendMessage("§bKurs gesetzt: §f" + name);
        return true;
    }

    public String describeIslands() {
        ConfigurationSection section=plugin.getConfig().getConfigurationSection("islands");
        if(section==null || section.getKeys(false).isEmpty()) return "Noch keine Inseln erstellt";
        StringBuilder out=new StringBuilder();
        for(String key:section.getKeys(false)) {
            if(out.length()>0) out.append(" §8| ");
            String name=section.getString(key+".name",key);
            int level=section.getInt(key+".required-level",IslandProgression.requiredLevel(name));
            out.append("§f").append(name).append(" §7(Level ").append(level).append(")");
        }
        return out.toString();
    }

    private void buildIsland(World w,int cx,int cy,int cz,String name) {
        int radius=plugin.getConfig().getInt("settings.island-radius",42);
        for(int x=-radius;x<=radius;x++) for(int z=-radius;z<=radius;z++) {
            double dist=Math.sqrt(x*x+z*z);
            double edge=radius*(0.82 + 0.10*Math.sin((x+cz)*0.19) + 0.08*Math.cos((z+cx)*0.17));
            if(dist>edge) continue;
            int top=cy+(int)(Math.sin(x*0.16)*2+Math.cos(z*0.13)*2+Math.sin((x+z)*0.08)*2);
            int depth=Math.max(3,(int)((1-dist/radius)*9)+3);
            for(int d=0;d<=depth;d++) {
                Material mat=d==0 ? Material.GRASS_BLOCK : d<depth-1 ? Material.DIRT : Material.STONE;
                Block b=w.getBlockAt(cx+x,top-d,cz+z); b.setType(mat,false);
            }
            if(dist<radius*0.76 && Math.floorMod(x*31+z*17,47)==0) w.getBlockAt(cx+x,top+1,cz+z).setType(Material.SHORT_GRASS,false);
            if(dist<radius*0.7 && Math.floorMod(x*13-z*29,173)==0) w.getBlockAt(cx+x,top+1,cz+z).setType(Material.OAK_LEAVES,false);
        }
        int sea=cy-10;
        for(int x=-(radius+24);x<=radius+24;x++) for(int z=-(radius+24);z<=radius+24;z++) {
            double d=Math.sqrt(x*x+z*z);
            if(d>radius-2 && d<=radius+24) for(int wy=sea-2;wy<=sea;wy++) w.getBlockAt(cx+x,wy,cz+z).setType(Material.WATER,false);
        }
        int dockZ=cz+radius/2;
        for(int x=-5;x<=5;x++) for(int z=0;z<12;z++) w.getBlockAt(cx+x,cy-1,dockZ+z).setType(Material.OAK_PLANKS,false);
        for(int x=-5;x<=5;x+=5) for(int z=0;z<12;z+=3) w.getBlockAt(cx+x,cy,dockZ+z).setType(Material.OAK_FENCE,false);
        int lx=cx-radius/3, lz=cz-radius/3;
        for(int y=cy;y<cy+13;y++) {
            Material m=(y==cy+12)?Material.SEA_LANTERN:Material.COBBLESTONE;
            w.getBlockAt(lx,y,lz).setType(m,false);
            if(y<cy+11) { w.getBlockAt(lx+1,y,lz).setType(m,false); w.getBlockAt(lx-1,y,lz).setType(m,false); w.getBlockAt(lx,y,lz+1).setType(m,false); w.getBlockAt(lx,y,lz-1).setType(m,false); }
        }
        w.getBlockAt(cx,cy+1,cz).setType(Material.CHEST,false);
        w.getBlockAt(cx+1,cy+1,cz).setType(Material.BARREL,false);
        w.getBlockAt(cx,cy+2,cz).setType(Material.OAK_SIGN,false);
    }
}