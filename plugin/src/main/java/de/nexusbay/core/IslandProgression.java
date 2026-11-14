package de.nexusbay.core;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Island level rules shared by island creation, travel and the player-facing atlas. */
public final class IslandProgression {
    private static final Map<String, Integer> LEVELS = new LinkedHashMap<>();
    static {
        String[] names = {"Foosha Village","Shells Town","Orange Town","Syrup Village","Baratie","Arlong Park","Loguetown","Reverse Mountain","Whiskey Peak","Little Garden","Drum Island","Alabasta","Jaya","Skypiea","Water 7","Enies Lobby","Thriller Bark","Sabaody Archipelago","Amazon Lily","Impel Down","Marineford","Fish-Man Island","Punk Hazard","Dressrosa","Zou","Whole Cake Island","Wano kuni","Onigashima"};
        int[] levels = {1,5,8,12,16,20,25,30,34,38,42,48,55,62,68,72,78,82,85,88,92,95,98,100,105,110,120,125};
        for (int i = 0; i < names.length; i++) LEVELS.put(key(names[i]), levels[i]);
    }
    private IslandProgression() {}
    private static String key(String name) { return name.toLowerCase(Locale.ROOT).replace('_',' ').replace('-',' ').trim(); }
    public static int requiredLevel(String name) { return LEVELS.getOrDefault(key(name), 1); }
    public static boolean isKnown(String name) { return LEVELS.containsKey(key(name)); }
    public static Map<String, Integer> all() { return Map.copyOf(LEVELS); }
}