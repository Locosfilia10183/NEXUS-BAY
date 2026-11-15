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
            ),
            "crocodile", new BossDefinition("crocodile", "CROCODILE – WÜSTENKÖNIG", 120, 180000D, 2.4D,
                    BossRank.LEGENDARY, "alabasta", "suna_suna_no_mi",
                    java.util.List.of("sand_blade", "desert_spada", "ground_dehydration", "sandstorm"),
                    java.util.List.of("desert_hook", "dance_powder", "砂砂_core")),
            "enel", new BossDefinition("enel", "ENEL – GOTT VON SKYPIEA", 150, 260000D, 2.7D,
                    BossRank.LEGENDARY, "skypiea", "goro_goro_no_mi",
                    java.util.List.of("lightning_spear", "el_thor", "mantra", "raigo"),
                    java.util.List.of("golden_dial", "lightning_core", "golden_staff")),
            "rob_lucci", new BossDefinition("rob_lucci", "ROB LUCCI – CP0-MEISTER", 135, 210000D, 2.2D,
                    BossRank.LEGENDARY, "enieslobby", "rokushiki",
                    java.util.List.of("shigan", "rankyaku", " soru", "rokuogan"),
                    java.util.List.of("cp0_mask", "rokushiki_manual")),
            "doflamingo", new BossDefinition("doflamingo", "DONQUIXOTE DOFLAMINGO", 190, 420000D, 2.8D,
                    BossRank.EMPEROR_ADMIRAL, "dressrosa", "ito_ito_no_mi",
                    java.util.List.of("parasite", "overheat", "birdcage", "god_thread"),
                    java.util.List.of("string_core", "flamingo_coat", "underworld_contract")),
            "katakuri", new BossDefinition("katakuri", "CHARLOTTE KATAKURI – SÜSSER KOMMANDANT", 210, 520000D, 2.6D,
                    BossRank.EMPEROR_ADMIRAL, "wholecakeisland", "mochi_mochi_no_mi",
                    java.util.List.of("buzz_cut_mochi", "power_mochi", "future_sight", "zan_giri_mochi"),
                    java.util.List.of("mochi_core", "scarf_of_future_sight")),
            "big_mom", new BossDefinition("big_mom", "CHARLOTTE LINLIN – BIG MOM", 235, 900000D, 3.3D,
                    BossRank.WORLD_BOSS, "wholecakeisland", "soru_soru_no_mi",
                    java.util.List.of("ikoku", "maser_cannon", "prometheus_burst", "hera_storm"),
                    java.util.List.of("soul_fragment", "homie_core", "napoleon_fragment")),
            "akainu", new BossDefinition("akainu", "SAKAZUKI – FLEETADMIRAL AKAINU", 225, 760000D, 2.9D,
                    BossRank.WORLD_BOSS, "marineford", "magu_magu_no_mi",
                    java.util.List.of("dai_funka", "meigo", "meteor_volcano", "magma_rain"),
                    java.util.List.of("magma_core", "fleet_admiral_insignia"))
    );

    private BossCatalog() {}

    public static BossDefinition get(String id) {
        return BOSSES.get(id);
    }

    public static Map<String, BossDefinition> all() {
        return BOSSES;
    }
}
