package de.nexusbay.forge.progression;

import java.util.Map;

/** Canonical enemy faction identity for each island region. */
public final class IslandFactionCatalog {
    public record Faction(String id,String displayName,int recommendedLevel,String territory) {}
    private static final Map<String,Faction> FACTIONS=Map.ofEntries(
        Map.entry("FooshaVillage",new Faction("foosha_bandits","Bergbanditen",1,"Foosha-Küste")),
        Map.entry("ShellsTown",new Faction("shells_marine","Abtrünnige Marine",5,"Marinebasis")),
        Map.entry("OrangeTown",new Faction("orange_pirates","Buggy-Piraten",8,"Orange Town")),
        Map.entry("SyrupVillage",new Faction("syrup_blackcat","Black-Cat-Piraten",12,"Küstenwald")),
        Map.entry("Baratie",new Faction("baratie_raiders","Küstenräuber",16,"Baratie-Gewässer")),
        Map.entry("ArlongPark",new Faction("arlong_fishmen","Arlong-Piraten",20,"Arlong Park")),
        Map.entry("Loguetown",new Faction("loguetown_outlaws","Gesetzlose",25,"Loguetown")),
        Map.entry("ReverseMountain",new Faction("reverse_raiders","Bergplünderer",30,"Reverse Mountain")),
        Map.entry("WhiskeyPeak",new Faction("whiskey_hunters","Kopfgeldjäger",34,"Whiskey Peak")),
        Map.entry("LittleGarden",new Faction("little_giants","Urwald-Räuber",38,"Little Garden")),
        Map.entry("DrumIsland",new Faction("drum_raiders","Schneepiraten",42,"Drum Island")),
        Map.entry("Alabasta",new Faction("alabasta_rebels","Wüstenrebellen",48,"Alabasta")),
        Map.entry("Jaya",new Faction("jaya_bellamy","Jaya-Piraten",55,"Mock Town")),
        Map.entry("Skypiea",new Faction("skypiea_enforcers","Himmelskrieger",62,"Skypiea")),
        Map.entry("Water7",new Faction("water7_underworld","Unterwelt-Agenten",68,"Water 7")),
        Map.entry("EniesLobby",new Faction("enies_cp9","Weltregierungstruppen",72,"Enies Lobby")),
        Map.entry("ThrillerBark",new Faction("thriller_zombies","Schatten-Zombies",78,"Thriller Bark")),
        Map.entry("Sabaody",new Faction("sabaody_slavers","Menschenhändler",82,"Sabaody")),
        Map.entry("AmazonLily",new Faction("amazon_intruders","Eindringlinge",85,"Amazon Lily")),
        Map.entry("ImpelDown",new Faction("impel_prisoners","Gefangenenbanden",88,"Impel Down")),
        Map.entry("Marineford",new Faction("marineford_raiders","Kriegsgegner",92,"Marineford")),
        Map.entry("FishManIsland",new Faction("fishman_raiders","Tiefsee-Räuber",95,"Fish-Man Island")),
        Map.entry("PunkHazard",new Faction("punk_hazards","Labor-Kreaturen",98,"Punk Hazard")),
        Map.entry("Dressrosa",new Faction("dressrosa_dons","Donquixote-Truppen",100,"Dressrosa")),
        Map.entry("Zou",new Faction("zou_invaders","Minksöldner",105,"Zou")),
        Map.entry("WholeCakeIsland",new Faction("wholecake_homies","Big-Mom-Piraten",110,"Whole Cake Island")),
        Map.entry("WanoKuni",new Faction("wano_beasts","Bestien-Piraten",120,"Wano")),
        Map.entry("Onigashima",new Faction("onigashima_beasts","Kaidos Elite",125,"Onigashima"))
    );
    public static Faction find(String islandId){return FACTIONS.get(islandId);}
    private IslandFactionCatalog(){}
}
