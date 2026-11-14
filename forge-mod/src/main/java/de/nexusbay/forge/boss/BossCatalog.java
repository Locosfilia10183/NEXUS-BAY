package de.nexusbay.forge.boss;

import java.util.Map;

public final class BossCatalog {
    private static final Map<String, BossDefinition> BOSSES = Map.of(
            "kaido", new BossDefinition(
                    "kaido",
                    "KAIDO – KÖNIG DER BESTIEN",
                    250,
                    60_000_000D,
                    3.5D,
                    BossRank.WORLD_BOSS,
                    "onigashima",
                    "kanabo_and_dragon",
                    java.util.List.of(
                            "kanabo_crushing_blow",
                            "conquerors_haki_burst",
                            "flame_breath",
                            "dragon_transformation",
                            "flaming_dragon_finisher"
                    ),
                    java.util.List.of(
                            "kaido_dragon_scale",
                            "kanabo_fragment",
                            "world_boss_key",
                            "azure_dragon_fruit_very_rare"
                    )
            )
    );

    private BossCatalog() {}

    public static BossDefinition get(String id) {
        return BOSSES.get(id);
    }

    public static Map<String, BossDefinition> all() {
        return BOSSES;
    }
}
