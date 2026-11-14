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
        if (!IslandProgression.isKnown(name)) return false;
        World world=Bukkit.getWorld("world");
        if (world==null) return false;
        int spacing=plugin.getConfig().getInt("settings.island-spacing",320);
        int index=IslandProgression.slot(name);
        if (index < 0) return false;
        int x=512+(index%8)*spacing, z=512+(index/8)*spacing, y=100;
        buildIsland(world,x,y,z,name);
        all.set(key+".name",name); all.set(key+".world",world.getName());
        all.set(key+".x",x+0.5); all.set(key+".y",y+3); all.set(key+".z",z+0.5);
        all.set(key+".required-level",IslandProgression.requiredLevel(name));
        all.set(key+".slot",index);
        all.set(key+".generated-version",2);
        plugin.saveConfig();
        return true;
    }

    /** Generates the complete canonical island chain automatically, without admin commands. */
    public void generateWorld() {
        org.bukkit.scheduler.BukkitRunnable task = new org.bukkit.scheduler.BukkitRunnable() {
            private int slot = 0;
            @Override public void run() {
                if (slot >= IslandProgression.all().size()) {
                    cancel();
                    plugin.getLogger().info("Grand Line generation complete: " + slot + " islands.");
                    return;
                }
                String islandName = IslandProgression.all().keySet().stream().skip(slot).findFirst().orElse(null);
                if (islandName == null) { cancel(); return; }
                String key = islandName.toLowerCase(Locale.ROOT);
                ConfigurationSection section = plugin.getConfig().getConfigurationSection("islands");
                boolean current = section != null
                        && section.getInt(key + ".generated-version", 0) >= 2
                        && section.getInt(key + ".slot", -1) == slot;
                if (!current) {
                    if (create(islandName)) {
                        plugin.getLogger().info("Generated island " + (slot + 1) + "/" + IslandProgression.all().size() + ": " + islandName);
                    } else {
                        plugin.getLogger().warning("Could not generate island: " + islandName + " (world 'world' may not be loaded)");
                        return;
                    }
                }
                slot++;
            }
        };
        task.runTaskTimer(plugin, 20L, 10L);
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
        if(section==null || section.getKeys(false).isEmpty()) return "Inselwelt wird automatisch generiert";
        StringBuilder out=new StringBuilder();
        for(String key:section.getKeys(false)) {
            if(out.length()>0) out.append(" §8| ");
            String name=section.getString(key+".name",key);
            int level=section.getInt(key+".required-level",IslandProgression.requiredLevel(name));
            out.append("§f").append(name).append(" §7(Level ").append(level).append(")");
        }
        return out.toString();
    }

    private record IslandTheme(Material surface, Material filler, Material accent, Material trunk, Material leaves,
                               boolean desert, boolean snowy, boolean jungle) {}

    private IslandTheme themeFor(String name) {
        String n=name.toLowerCase(Locale.ROOT);
        if (n.contains("alabasta") || n.contains("whiskey") || n.contains("arabasta"))
            return new IslandTheme(Material.SAND,Material.SANDSTONE,Material.CUT_SANDSTONE,Material.DEAD_BUSH,Material.DEAD_BUSH,true,false,false);
        if (n.contains("drum") || n.contains("punk hazard"))
            return new IslandTheme(Material.SNOW_BLOCK,Material.PACKED_ICE,Material.BLUE_ICE,Material.SPRUCE_LOG,Material.SPRUCE_LEAVES,false,true,false);
        if (n.contains("little garden") || n.contains("amazon") || n.contains("zou") || n.contains("foosha"))
            return new IslandTheme(Material.GRASS_BLOCK,Material.DIRT,Material.MOSS_BLOCK,Material.JUNGLE_LOG,Material.JUNGLE_LEAVES,false,false,true);
        if (n.contains("wano") || n.contains("onigashima"))
            return new IslandTheme(Material.GRASS_BLOCK,Material.DIRT,Material.POLISHED_DEEPSLATE,Material.DARK_OAK_LOG,Material.DARK_OAK_LEAVES,false,false,true);
        if (n.contains("marineford") || n.contains("enies lobby") || n.contains("impel down"))
            return new IslandTheme(Material.STONE_BRICKS,Material.STONE,Material.QUARTZ_BLOCK,Material.STONE_BRICKS,Material.IRON_BARS,false,false,false);
        return new IslandTheme(Material.GRASS_BLOCK,Material.DIRT,Material.COBBLESTONE,Material.OAK_LOG,Material.OAK_LEAVES,false,false,false);
    }

    private void buildIsland(World w,int cx,int cy,int cz,String name) {
        int radius=plugin.getConfig().getInt("settings.island-radius",42);
        IslandTheme theme=themeFor(name);
        for(int x=-radius;x<=radius;x++) for(int z=-radius;z<=radius;z++) {
            double dist=Math.sqrt(x*x+z*z);
            double edge=radius*(0.82 + 0.10*Math.sin((x+cz)*0.19) + 0.08*Math.cos((z+cx)*0.17));
            if(dist>edge) continue;
            int top=cy+(int)(Math.sin(x*0.16)*2+Math.cos(z*0.13)*2+Math.sin((x+z)*0.08)*2);
            int depth=Math.max(3,(int)((1-dist/radius)*9)+3);
            for(int d=0;d<=depth;d++) {
                Material mat=d==0 ? theme.surface() : d<depth-1 ? theme.filler() : Material.STONE;
                Block b=w.getBlockAt(cx+x,top-d,cz+z); b.setType(mat,false);
            }
            if (!theme.desert() && !theme.snowy() && dist<radius*0.76 && Math.floorMod(x*31+z*17,47)==0)
                w.getBlockAt(cx+x,top+1,cz+z).setType(Material.SHORT_GRASS,false);
            if (theme.snowy() && dist<radius*0.76 && Math.floorMod(x*31+z*17,31)==0)
                w.getBlockAt(cx+x,top+1,cz+z).setType(Material.SNOW_BLOCK,false);
            if (theme.desert() && dist<radius*0.76 && Math.floorMod(x*13-z*29,173)==0)
                w.getBlockAt(cx+x,top+1,cz+z).setType(Material.CACTUS,false);
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
            Material m=(y==cy+12)?Material.SEA_LANTERN:(y%4==0?theme.accent():Material.COBBLESTONE);
            w.getBlockAt(lx,y,lz).setType(m,false);
            if(y<cy+11) { w.getBlockAt(lx+1,y,lz).setType(m,false); w.getBlockAt(lx-1,y,lz).setType(m,false); w.getBlockAt(lx,y,lz+1).setType(m,false); w.getBlockAt(lx,y,lz-1).setType(m,false); }
        }
        w.getBlockAt(cx,cy+1,cz).setType(Material.CHEST,false);
        w.getBlockAt(cx+1,cy+1,cz).setType(Material.BARREL,false);
        w.getBlockAt(cx,cy+2,cz).setType(Material.OAK_SIGN,false);
        // Each island receives a small, deterministic grove so regeneration stays consistent.
        if (!theme.desert() && theme.leaves()!=Material.IRON_BARS) {
            for (int i=0;i<6;i++) {
                double angle=(Math.PI*2*i)/6.0;
                int tx=cx+(int)(Math.cos(angle)*radius*0.48);
                int tz=cz+(int)(Math.sin(angle)*radius*0.48);
                int ground=cy+(int)(Math.sin((tx-cx)*0.16)*2+Math.cos((tz-cz)*0.13)*2
                        +Math.sin(((tx-cx)+(tz-cz))*0.08)*2);
                buildTree(w,tx,ground+1,tz,theme.trunk(),theme.leaves(),theme.snowy());
            }
        }
        // A visible island landmark at the center helps players recognize the destination.
        for (int y=cy+1;y<cy+7;y++) {
            w.getBlockAt(cx+radius/5,y,cz-radius/5).setType(theme.accent(),false);
        }
        w.getBlockAt(cx+radius/5,cy+7,cz-radius/5).setType(Material.SEA_LANTERN,false);
        buildIslandBuildings(w,cx,cy,cz,name,theme,radius);
    }

    /** Distinct settlement silhouettes plus a dedicated raid arena for Onigashima. */
    private void buildIslandBuildings(World world,int cx,int cy,int cz,String name,IslandTheme theme,int radius) {
        String n=name.toLowerCase(Locale.ROOT);
        if (n.contains("onigashima")) {
            buildOnigashimaArena(world,cx,cy,cz,radius);
            return;
        }
        if (n.contains("marineford") || n.contains("enies lobby") || n.contains("impel down")) {
            buildFortress(world,cx,cy,cz,theme.accent());
            return;
        }
        Material wall = theme.desert()?Material.SANDSTONE:theme.snowy()?Material.SPRUCE_PLANKS:
                n.contains("wano")?Material.DARK_OAK_PLANKS:Material.OAK_PLANKS;
        Material roof = theme.desert()?Material.CUT_SANDSTONE:theme.snowy()?Material.SPRUCE_STAIRS:
                n.contains("wano")?Material.DARK_OAK_STAIRS:Material.OAK_STAIRS;
        int[][] homes={{-13,-10},{13,-8},{-12,13},{13,12}};
        for (int[] offset:homes) buildHouse(world,cx+offset[0],cy+1,cz+offset[1],wall,roof);
        if (n.contains("baratie")) buildRestaurantDeck(world,cx,cy,cz);
        if (n.contains("loguetown") || n.contains("shells town")) buildWatchTower(world,cx+19,cy+1,cz-18,theme.accent());
        if (n.contains("alabasta")) buildPalace(world,cx,cy+1,cz-18,Material.SANDSTONE,Material.CUT_SANDSTONE);
        if (n.contains("wano")) buildTorii(world,cx,cy+1,cz-22);
    }

    private void buildHouse(World w,int x,int y,int z,Material wall,Material roof) {
        int width=9, depth=8, height=5;
        for(int dx=0;dx<width;dx++) for(int dz=0;dz<depth;dz++) {
            w.getBlockAt(x+dx,y-1,z+dz).setType(Material.OAK_PLANKS,false);
            for(int dy=0;dy<height;dy++) {
                boolean edge=dx==0||dx==width-1||dz==0||dz==depth-1;
                if(edge) w.getBlockAt(x+dx,y+dy,z+dz).setType(dy==0?wall:wall,false);
            }
        }
        for(int dx=-1;dx<=width;dx++) for(int dz=-1;dz<=depth;dz++) {
            int edge=Math.min(Math.min(dx+1,width-dx),Math.min(dz+1,depth-dz));
            if(edge>=0) w.getBlockAt(x+dx,y+height+(Math.min(edge,2)),z+dz).setType(roof,false);
        }
        w.getBlockAt(x+width/2,y,z).setType(Material.AIR,false);
        w.getBlockAt(x+width/2,y+1,z).setType(Material.AIR,false);
        w.getBlockAt(x+2,y+2,z).setType(Material.GLASS_PANE,false);
        w.getBlockAt(x+width-3,y+2,z+depth-1).setType(Material.GLASS_PANE,false);
        w.getBlockAt(x+width/2,y+1,z+depth/2).setType(Material.LANTERN,false);
    }

    private void buildFortress(World w,int cx,int cy,int cz,Material accent) {
        int half=19;
        for(int y=cy;y<cy+12;y++) for(int i=-half;i<=half;i++) {
            w.getBlockAt(cx+i,y,cz-half).setType(Material.STONE_BRICKS,false);
            w.getBlockAt(cx+i,y,cz+half).setType(Material.STONE_BRICKS,false);
            w.getBlockAt(cx-half,y,cz+i).setType(Material.STONE_BRICKS,false);
            w.getBlockAt(cx+half,y,cz+i).setType(Material.STONE_BRICKS,false);
        }
        for(int x=-half+2;x<half-1;x+=4) for(int z=-half+2;z<half-1;z+=4)
            w.getBlockAt(cx+x,cy+1,cz+z).setType(accent,false);
        for(int y=cy+1;y<cy+8;y++) for(int x=-2;x<=2;x++) {
            w.getBlockAt(cx+x,y,cz-half).setType(Material.AIR,false);
        }
        buildWatchTower(w,cx,cy+1,cz,accent);
    }

    private void buildOnigashimaArena(World w,int cx,int cy,int cz,int radius) {
        int outer=23, inner=17;
        for(int y=cy+1;y<cy+5;y++) for(int x=-outer;x<=outer;x++) for(int z=-outer;z<=outer;z++) {
            double d=Math.sqrt(x*x+z*z);
            if(d>=inner && d<=outer) w.getBlockAt(cx+x,y,cz+z).setType(Material.POLISHED_DEEPSLATE_BRICKS,false);
        }
        for(int x=-inner;x<=inner;x++) for(int z=-inner;z<=inner;z++) {
            if(x*x+z*z<=inner*inner) w.getBlockAt(cx+x,cy,cz+z).setType(Material.BLACKSTONE,false);
        }
        for(int i=0;i<8;i++) {
            double a=Math.PI*2*i/8.0;
            int px=cx+(int)(Math.cos(a)*outer), pz=cz+(int)(Math.sin(a)*outer);
            for(int y=cy+5;y<cy+17;y++) w.getBlockAt(px,y,pz).setType(Material.POLISHED_BLACKSTONE_BRICKS,false);
            w.getBlockAt(px,cy+17,pz).setType(Material.SOUL_LANTERN,false);
        }
        // Four monumental gates mark the arena entrances.
        for(int side:new int[]{-1,1}) for(int x=-4;x<=4;x+=8) {
            int gx=cx+x, gz=cz+side*outer;
            for(int y=cy+1;y<cy+11;y++) w.getBlockAt(gx,y,gz).setType(Material.CRIMSON_STEM,false);
            for(int dx=-4;dx<=4;dx++) w.getBlockAt(cx+dx,cy+11,gz).setType(Material.CRIMSON_PLANKS,false);
        }
        buildWatchTower(w,cx,cy+1,cz-outer-8,Material.REDSTONE_BLOCK);
        w.getBlockAt(cx,cy+1,cz).setType(Material.BEACON,false);
    }

    private void buildWatchTower(World w,int x,int y,int z,Material accent) {
        for(int dy=0;dy<15;dy++) {
            w.getBlockAt(x,y+dy,z).setType(dy%5==0?accent:Material.STONE_BRICKS,false);
            if(dy<12) {
                w.getBlockAt(x+1,y+dy,z).setType(Material.STONE_BRICKS,false);
                w.getBlockAt(x-1,y+dy,z).setType(Material.STONE_BRICKS,false);
                w.getBlockAt(x,y+dy,z+1).setType(Material.STONE_BRICKS,false);
                w.getBlockAt(x,y+dy,z-1).setType(Material.STONE_BRICKS,false);
            }
        }
        w.getBlockAt(x,y+15,z).setType(Material.SEA_LANTERN,false);
    }

    private void buildPalace(World w,int cx,int y,int cz,Material wall,Material roof) {
        for(int dx=-10;dx<=10;dx++) for(int dz=-8;dz<=8;dz++) {
            boolean edge=Math.abs(dx)==10||Math.abs(dz)==8;
            if(edge) for(int dy=0;dy<9;dy++) w.getBlockAt(cx+dx,y+dy,cz+dz).setType(wall,false);
            if(!edge) w.getBlockAt(cx+dx,y-1,cz+dz).setType(Material.SMOOTH_SANDSTONE,false);
        }
        for(int dx=-11;dx<=11;dx++) for(int dz=-9;dz<=9;dz++)
            if(Math.abs(dx)==11||Math.abs(dz)==9) w.getBlockAt(cx+dx,y+9,cz+dz).setType(roof,false);
    }

    private void buildRestaurantDeck(World w,int cx,int y,int cz) {
        for(int x=-14;x<=14;x++) for(int z=-10;z<=10;z++)
            if(Math.abs(x)>11||Math.abs(z)>7) w.getBlockAt(cx+x,y,cz+z).setType(Material.OAK_PLANKS,false);
        for(int x=-12;x<=12;x+=4) w.getBlockAt(cx+x,y+1,cz-8).setType(Material.OAK_FENCE,false);
    }

    private void buildTorii(World w,int cx,int y,int cz) {
        for(int x=-6;x<=6;x+=12) for(int dy=0;dy<10;dy++) w.getBlockAt(cx+x,y+dy,cz).setType(Material.RED_CONCRETE,false);
        for(int x=-8;x<=8;x++) w.getBlockAt(cx+x,y+10,cz).setType(Material.RED_CONCRETE,false);
        for(int x=-7;x<=7;x++) w.getBlockAt(cx+x,y+8,cz).setType(Material.BLACKSTONE,false);
    }

    private void buildTree(World world,int x,int y,int z,Material trunk,Material leaves,boolean snowy) {
        if (trunk==Material.DEAD_BUSH || leaves==Material.DEAD_BUSH) return;
        int height=snowy?5:6;
        for(int dy=0;dy<height;dy++) world.getBlockAt(x,y+dy,z).setType(trunk,false);
        for(int dx=-2;dx<=2;dx++) for(int dy=height-3;dy<=height;dy++)
            for(int dz=-2;dz<=2;dz++) {
                if(Math.abs(dx)+Math.abs(dz)+(dy==height?1:0)>4) continue;
                Block block=world.getBlockAt(x+dx,y+dy,z+dz);
                if(block.getType().isAir()) block.setType(leaves,false);
            }
        if(snowy) world.getBlockAt(x,y+height,z).setType(Material.SNOW_BLOCK,false);
    }
}