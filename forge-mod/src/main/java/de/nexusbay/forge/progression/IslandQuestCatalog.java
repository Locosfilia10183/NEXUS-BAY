package de.nexusbay.forge.progression;

import java.util.List;
import java.util.Optional;

/** Starter-island quest definitions. NPC dialogue can be attached to these IDs later. */
public final class IslandQuestCatalog {
    public record Quest(String id, String title, String description, String islandId,
                        int requiredKills, long beliReward, long fruitXpReward) {}
    private static final List<Quest> QUESTS = List.of(
            new Quest("foosha_bandits", "Banditen von Foosha", "Besiege 5 feindliche Monster.", "FooshaVillage", 5, 250, 100),
            new Quest("foosha_patrol", "Schutz der Küste", "Besiege 10 feindliche Monster.", "FooshaVillage", 10, 600, 250),
            new Quest("foosha_veteran", "Stärkster der Küste", "Besiege 20 feindliche Monster.", "FooshaVillage", 20, 1500, 600),
            new Quest("shells_patrol", "Patrouille von Shells Town", "Besiege 6 feindliche Monster rund um die Marinebasis.", "ShellsTown", 6, 500, 150),
            new Quest("shells_reinforcements", "Verstärkung der Marine", "Besiege 12 feindliche Monster in der Umgebung.", "ShellsTown", 12, 1100, 350),
            new Quest("shells_commander", "Bedrohung der Basis", "Besiege 20 feindliche Monster und sichere Shells Town.", "ShellsTown", 20, 2200, 700),
            new Quest("orange_ruffians", "Unruhe in Orange Town", "Besiege 8 feindliche Monster in den Straßen der Stadt.", "OrangeTown", 8, 800, 220),
            new Quest("orange_defenders", "Verteidiger der Stadt", "Besiege 15 feindliche Monster und schütze die Bewohner.", "OrangeTown", 15, 1600, 480),
            new Quest("orange_champion", "Champion von Orange Town", "Besiege 25 feindliche Monster und werde zum Beschützer der Stadt.", "OrangeTown", 25, 3000, 900),
            new Quest("syrup_watch", "Wache im Dorf", "Besiege 10 feindliche Monster nahe Syrup Village.", "SyrupVillage", 10, 1200, 300),
            new Quest("syrup_threat", "Bedrohung im Wald", "Besiege 18 feindliche Monster und sichere die Wege.", "SyrupVillage", 18, 2400, 650),
            new Quest("syrup_guardian", "Beschützer von Syrup", "Besiege 30 feindliche Monster und verteidige das Dorf.", "SyrupVillage", 30, 4200, 1200),
            new Quest("baratie_raiders", "Angriff auf das Baratie", "Besiege 12 feindliche Monster nahe dem schwimmenden Restaurant.", "Baratie", 12, 1800, 450),
            new Quest("baratie_defense", "Schutz der Köche", "Besiege 22 feindliche Monster und verteidige die Besatzung.", "Baratie", 22, 3600, 900),
            new Quest("baratie_vanguard", "Stärkste Bedrohung", "Besiege 35 feindliche Monster und sichere das Baratie.", "Baratie", 35, 6000, 1600),
            new Quest("arlong_scourge", "Terror im Arlong Park", "Besiege 15 feindliche Monster im Arlong Park.", "ArlongPark", 15, 2800, 700),
            new Quest("arlong_resistance", "Widerstand des Dorfes", "Besiege 28 feindliche Monster und unterstütze die Bewohner.", "ArlongPark", 28, 5200, 1300),
            new Quest("arlong_liberator", "Befreier von Arlong Park", "Besiege 45 feindliche Monster und beende den Terror.", "ArlongPark", 45, 8500, 2200),
            new Quest("loguetown_watch", "Wache am Anfang und Ende", "Besiege 18 feindliche Monster in Loguetown.", "Loguetown", 18, 4200, 1000),
            new Quest("loguetown_hunters", "Jagd auf Gesetzlose", "Besiege 32 feindliche Monster und halte die Straßen sicher.", "Loguetown", 32, 7800, 1800),
            new Quest("loguetown_execution", "Unruhe am Richtplatz", "Besiege 50 feindliche Monster und sichere den Richtplatz.", "Loguetown", 50, 12500, 3000),
            new Quest("reverse_current", "Gegen die Strömung", "Besiege 20 feindliche Monster am Fuße des Reverse Mountain.", "ReverseMountain", 20, 6500, 1400),
            new Quest("reverse_passage", "Sicherung der Passage", "Besiege 35 feindliche Monster entlang des Aufstiegs.", "ReverseMountain", 35, 10500, 2400),
            new Quest("reverse_guardian", "Wächter des Aufstiegs", "Besiege 55 feindliche Monster und sichere den Weg zur Grand Line.", "ReverseMountain", 55, 16500, 3800),
            new Quest("whiskey_bounty", "Kopfgeldjäger von Whiskey Peak", "Besiege 22 feindliche Monster auf der Insel.", "WhiskeyPeak", 22, 8000, 1800),
            new Quest("whiskey_trap", "Die Falle der Jäger", "Besiege 40 feindliche Monster und überstehe den Hinterhalt.", "WhiskeyPeak", 40, 13500, 3000),
            new Quest("whiskey_captain", "Kapitän der Kopfgeldjäger", "Besiege 65 feindliche Monster und zerschlage die Jagdtruppe.", "WhiskeyPeak", 65, 21000, 4800),
            new Quest("little_garden_hunt", "Jagd im Urwald", "Besiege 28 feindliche Monster im Dschungel von Little Garden.", "LittleGarden", 28, 12000, 2600),
            new Quest("little_garden_survival", "Überleben der Giganteninsel", "Besiege 48 feindliche Monster und halte den Dschungel sicher.", "LittleGarden", 48, 19500, 4200),
            new Quest("little_garden_champion", "Champion von Little Garden", "Besiege 75 feindliche Monster und beweise deine Stärke.", "LittleGarden", 75, 30000, 6500),
            new Quest("drum_snow_patrol", "Patrouille im Schnee", "Besiege 30 feindliche Monster auf Drum Island.", "DrumIsland", 30, 15000, 3200),
            new Quest("drum_medical_supply", "Sicherheit für die Versorgung", "Besiege 52 feindliche Monster und sichere die Wege.", "DrumIsland", 52, 24000, 5200),
            new Quest("drum_defender", "Beschützer von Drum", "Besiege 80 feindliche Monster und verteidige die Insel.", "DrumIsland", 80, 38000, 8200),
            new Quest("alabasta_rebels", "Unruhe im Wüstenreich", "Besiege 35 feindliche Monster in Alabasta.", "Alabasta", 35, 22000, 4500),
            new Quest("alabasta_caravan", "Schutz der Karawanen", "Besiege 60 feindliche Monster und sichere die Handelswege.", "Alabasta", 60, 36000, 7200),
            new Quest("alabasta_guardian", "Retter von Alabasta", "Besiege 90 feindliche Monster und verteidige das Königreich.", "Alabasta", 90, 55000, 11000),
            new Quest("jaya_raiders", "Räuber von Jaya", "Besiege 38 feindliche Monster in Mock Town und Umgebung.", "Jaya", 38, 26000, 5200),
            new Quest("jaya_rivals", "Kampf um Jaya", "Besiege 65 feindliche Monster und sichere die Insel.", "Jaya", 65, 42000, 8500),
            new Quest("jaya_champion", "Champion von Jaya", "Besiege 100 feindliche Monster und beweise deine Stärke.", "Jaya", 100, 65000, 13000)
    );
    private IslandQuestCatalog() {}
    public static List<Quest> all() { return QUESTS; }
    public static Optional<Quest> find(String id) { return QUESTS.stream().filter(q -> q.id().equalsIgnoreCase(id)).findFirst(); }
}
