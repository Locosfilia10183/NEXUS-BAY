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
            new Quest("jaya_champion", "Champion von Jaya", "Besiege 100 feindliche Monster und beweise deine Stärke.", "Jaya", 100, 65000, 13000),
            new Quest("skypiea_raiders", "Gefahr auf den Wolken", "Besiege 42 feindliche Monster auf Skypiea.", "Skypiea", 42, 34000, 6800),
            new Quest("skypiea_sky_patrol", "Himmels-Patrouille", "Besiege 72 feindliche Monster und sichere die Himmelswege.", "Skypiea", 72, 55000, 11000),
            new Quest("skypiea_guardian", "Beschützer des Himmels", "Besiege 110 feindliche Monster und verteidige Skypiea.", "Skypiea", 110, 85000, 17000),
            new Quest("water7_dock_threat", "Gefahr an den Docks", "Besiege 45 feindliche Monster in Water 7.", "Water7", 45, 38000, 7600),
            new Quest("water7_shipwrights", "Schutz der Schiffsbauer", "Besiege 78 feindliche Monster und sichere die Werften.", "Water7", 78, 62000, 12500),
            new Quest("water7_defender", "Verteidiger von Water 7", "Besiege 120 feindliche Monster und schütze die Stadt.", "Water7", 120, 95000, 19000),
            new Quest("enies_lobby_invasion", "Angriff auf Enies Lobby", "Besiege 55 feindliche Monster auf der Gerichtsinsel.", "EniesLobby", 55, 52000, 10500),
            new Quest("enies_lobby_rescue", "Rettung unter Beschuss", "Besiege 90 feindliche Monster und sichere den Weg durch die Festung.", "EniesLobby", 90, 85000, 17000),
            new Quest("enies_lobby_judgment", "Urteil über Enies Lobby", "Besiege 140 feindliche Monster und bezwinge die Verteidigung.", "EniesLobby", 140, 130000, 26000),
            new Quest("thriller_bark_shadows", "Schatten im Nebel", "Besiege 60 feindliche Monster auf Thriller Bark.", "ThrillerBark", 60, 60000, 12000),
            new Quest("thriller_bark_survivors", "Überleben auf dem Geisterschiff", "Besiege 100 feindliche Monster und sichere den Friedhof.", "ThrillerBark", 100, 98000, 19500),
            new Quest("thriller_bark_nightmare", "Albtraum von Thriller Bark", "Besiege 155 feindliche Monster und beende den Spuk.", "ThrillerBark", 155, 150000, 30000),
            new Quest("sabaody_bounty", "Kopfgeldjagd auf Sabaody", "Besiege 70 feindliche Monster im Archipel.", "Sabaody", 70, 78000, 15500),
            new Quest("sabaody_chaos", "Chaos im Archipel", "Besiege 115 feindliche Monster und sichere die Groves.", "Sabaody", 115, 125000, 25000),
            new Quest("sabaody_supernova", "Prüfung der Supernova", "Besiege 175 feindliche Monster und werde zur Legende des Archipels.", "Sabaody", 175, 190000, 38000),
            new Quest("amazon_lily_intruders", "Eindringlinge auf Amazon Lily", "Besiege 75 feindliche Monster auf der Insel.", "AmazonLily", 75, 90000, 18000),
            new Quest("amazon_lily_defense", "Schutz der Kriegerinnen", "Besiege 125 feindliche Monster und verteidige die Insel.", "AmazonLily", 125, 145000, 29000),
            new Quest("amazon_lily_champion", "Champion von Amazon Lily", "Besiege 190 feindliche Monster und beweise deine Stärke.", "AmazonLily", 190, 220000, 44000),
            new Quest("impel_down_escape", "Unruhe im Gefängnis", "Besiege 85 feindliche Monster in Impel Down.", "ImpelDown", 85, 110000, 22000),
            new Quest("impel_down_blocks", "Sicherung der Gefängnisblöcke", "Besiege 140 feindliche Monster und sichere die Gefängnisgänge.", "ImpelDown", 140, 180000, 36000),
            new Quest("impel_down_warden", "Herausforderung des Wärters", "Besiege 210 feindliche Monster und bezwinge die Gefängnisbedrohung.", "ImpelDown", 210, 270000, 54000),
            new Quest("marineford_front", "Frontlinie von Marineford", "Besiege 95 feindliche Monster auf dem Schlachtfeld.", "Marineford", 95, 135000, 27000),
            new Quest("marineford_defense", "Verteidigung des Hauptquartiers", "Besiege 155 feindliche Monster und halte die Stellung.", "Marineford", 155, 220000, 44000),
            new Quest("marineford_war", "Legende der großen Schlacht", "Besiege 230 feindliche Monster und entscheide die Schlacht.", "Marineford", 230, 330000, 66000)
    );
    private IslandQuestCatalog() {}
    public static List<Quest> all() { return QUESTS; }
    public static Optional<Quest> find(String id) { return QUESTS.stream().filter(q -> q.id().equalsIgnoreCase(id)).findFirst(); }
}
