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
            new Quest("syrup_guardian", "Beschützer von Syrup", "Besiege 30 feindliche Monster und verteidige das Dorf.", "SyrupVillage", 30, 4200, 1200)
    );
    private IslandQuestCatalog() {}
    public static List<Quest> all() { return QUESTS; }
    public static Optional<Quest> find(String id) { return QUESTS.stream().filter(q -> q.id().equalsIgnoreCase(id)).findFirst(); }
}
