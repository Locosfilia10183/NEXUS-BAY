package de.nexusbay.forge.boss;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Canonical design roster for NEXUS-BAY boss encounters.
 * Entries are encounter data; an entry does not become a spawnable entity
 * until its dedicated model, renderer, AI and entity registration are implemented.
 */
public final class BossCatalog {
    private static final Map<String, BossDefinition> BOSSES = createBosses();

    private static Map<String, BossDefinition> createBosses() {
        Map<String, BossDefinition> bosses = new LinkedHashMap<>();
        add(bosses, "kaido", "KAIDO – KÖNIG DER BESTIEN", 250, 60_000_000D, 3.5D,
                BossRank.WORLD_BOSS, "onigashima", "kanabo_and_dragon",
                List.of("kanabo_crushing_blow", "conquerors_haki_burst", "flame_breath",
                        "dragon_transformation", "flaming_dragon_finisher"),
                List.of("kaido_dragon_scale", "kanabo_fragment", "world_boss_key",
                        "azure_dragon_fruit_very_rare"));
        add(bosses, "crocodile", "SIR CROCODILE", 90, 180_000D, 1.15D,
                BossRank.LEGENDARY, "alabasta", "sand_and_hook",
                List.of("desert_spada", "sables", "ground_secco", "desert_spada_finisher"),
                List.of("hook_fragment", "suna_essence", "baroque_key"));
        add(bosses, "rob_lucci", "ROB LUCCI – CP0", 105, 260_000D, 1.2D,
                BossRank.LEGENDARY, "egghead", "rokushiki_and_leopard",
                List.of("shigan", "rankyaku", "rokuogan", "leopard_form"),
                List.of("cp0_badge", "rokushiki_manual", "leopard_core"));
        add(bosses, "enel", "ENEL – GOTT VON SKYPIEA", 110, 320_000D, 1.25D,
                BossRank.LEGENDARY, "skypiea", "lightning_and_goro",
                List.of("el_thor", "raigo", "mantra", "lightning_travel"),
                List.of("golden_dial", "goro_essence", "maxim_fragment"));
        add(bosses, "gecko_moria", "GECKO MORIA", 100, 290_000D, 1.35D,
                BossRank.LEGENDARY, "thriller_bark", "shadow_and_zombie",
                List.of("shadow_asgard", "brick_bat", "shadow_boxing", "zombie_call"),
                List.of("shadow_fragment", "salt_bundle", "kage_essence"));
        add(bosses, "donquixote_doflamingo", "DONQUIXOTE DOFLAMINGO", 125, 520_000D, 1.2D,
                BossRank.EMPEROR_ADMIRAL, "dressrosa", "strings_and_awakening",
                List.of("parasite", "overheat", "birdcage", "god_thread"),
                List.of("string_core", "ito_essence", "corazon_keepsake"));
        add(bosses, "charlotte_katakuri", "CHARLOTTE KATAKURI", 140, 850_000D, 1.3D,
                BossRank.EMPEROR_ADMIRAL, "whole_cake", "mochi_and_future_sight",
                List.of("zangiri_mochi", "power_mochi", "peerless_donut", "future_sight"),
                List.of("mochi_core", "scarf_fragment", "observation_manual"));
        add(bosses, "big_mom", "CHARLOTTE LINLIN – BIG MOM", 190, 8_000_000D, 2.8D,
                BossRank.WORLD_BOSS, "whole_cake", "soul_and_homies",
                List.of("ikoku_sovereignty", "maser_cannon", "prometheus_burst", "zeus_storm"),
                List.of("soul_fragment", "homie_core", "emperor_treasure"));
        add(bosses, "blackbeard", "MARSHALL D. TEACH – BLACKBEARD", 200, 12_000_000D, 1.5D,
                BossRank.WORLD_BOSS, "hachinosu", "darkness_and_quake",
                List.of("black_hole", "liberation", "tremor_crush", "dark_vortex"),
                List.of("darkness_core", "quake_fragment", "yami_relic"));
        add(bosses, "akainu", "SAKAZUKI – AKAINU", 175, 6_000_000D, 1.35D,
                BossRank.EMPEROR_ADMIRAL, "marineford", "magma_and_marine",
                List.of("meigo", "ryusei_kazan", "dai_funka", "magma_fist"),
                List.of("magma_core", "fleet_admiral_insignia", "marine_medal"));
        return Map.copyOf(bosses);
    }

    private static void add(Map<String, BossDefinition> target, String id, String name,
                            int level, double health, double scale, BossRank rank,
                            String island, String style, List<String> abilities, List<String> drops) {
        target.put(id, new BossDefinition(id, name, level, health, scale, rank,
                island, style, abilities, drops));
    }

    private BossCatalog() {}

    public static BossDefinition get(String id) {
        return BOSSES.get(id);
    }

    public static Map<String, BossDefinition> all() {
        return BOSSES;
    }
}
