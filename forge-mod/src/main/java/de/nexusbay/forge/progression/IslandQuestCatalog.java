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
            new Quest("shells_commander", "Bedrohung der Basis", "Besiege 20 feindliche Monster und sichere Shells Town.", "ShellsTown", 20, 2200, 700)
    );
    private IslandQuestCatalog() {}
    public static List<Quest> all() { return QUESTS; }
    public static Optional<Quest> find(String id) { return QUESTS.stream().filter(q -> q.id().equalsIgnoreCase(id)).findFirst(); }
}
