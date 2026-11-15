package de.nexusbay.forge.progression;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Weighted server-side fruit case pool.
 *
 * Weights are basis points out of 10,000 (100%). A fruit is selected first by
 * rarity tier and then uniformly from that tier. Additions remain data-driven
 * through the immutable entry list.
 */
public final class DevilFruitCaseTable {
    public enum Rarity {
        COMMON(5_000),
        UNCOMMON(2_500),
        RARE(1_500),
        EPIC(700),
        LEGENDARY(250),
        MYTHICAL(50);

        private final int weight;
        Rarity(int weight) { this.weight = weight; }
        public int weight() { return weight; }
    }

    public record Fruit(String id, String displayName, Rarity rarity) {}

    private static final List<Fruit> FRUITS = List.of(
            new Fruit("supa_supa", "Supa Supa no Mi", Rarity.COMMON),
            new Fruit("bomu_bomu", "Bomu Bomu no Mi", Rarity.COMMON),
            new Fruit("kilo_kilo", "Kilo Kilo no Mi", Rarity.COMMON),
            new Fruit("bane_bane", "Bane Bane no Mi", Rarity.COMMON),
            new Fruit("doru_doru", "Doru Doru no Mi", Rarity.UNCOMMON),
            new Fruit("sube_sube", "Sube Sube no Mi", Rarity.UNCOMMON),
            new Fruit("ori_ori", "Ori Ori no Mi", Rarity.UNCOMMON),
            new Fruit("bara_bara", "Bara Bara no Mi", Rarity.UNCOMMON),
            new Fruit("gomu_gomu", "Gomu Gomu no Mi", Rarity.RARE),
            new Fruit("mera_mera", "Mera Mera no Mi", Rarity.RARE),
            new Fruit("goro_goro", "Goro Goro no Mi", Rarity.RARE),
            new Fruit("yomi_yomi", "Yomi Yomi no Mi", Rarity.RARE),
            new Fruit("ito_ito", "Ito Ito no Mi", Rarity.EPIC),
            new Fruit("ope_ope", "Ope Ope no Mi", Rarity.EPIC),
            new Fruit("magu_magu", "Magu Magu no Mi", Rarity.EPIC),
            new Fruit("hie_hie", "Hie Hie no Mi", Rarity.LEGENDARY),
            new Fruit("pika_pika", "Pika Pika no Mi", Rarity.LEGENDARY),
            new Fruit("soru_soru", "Soru Soru no Mi", Rarity.LEGENDARY),
            new Fruit("uo_uo_seiryu", "Uo Uo no Mi, Model: Seiryu", Rarity.MYTHICAL)
    );

    public static List<Fruit> all() {
        return FRUITS;
    }

    public static Fruit open(RandomGenerator random) {
        int roll = random.nextInt(10_000);
        int cursor = 0;
        Rarity selected = Rarity.MYTHICAL;
        for (Rarity rarity : Rarity.values()) {
            cursor += rarity.weight();
            if (roll < cursor) {
                selected = rarity;
                break;
            }
        }

        List<Fruit> candidates = FRUITS.stream()
                .filter(fruit -> fruit.rarity() == selected)
                .toList();

        // Keep every rarity tier usable even if its pool is temporarily empty.
        if (candidates.isEmpty()) {
            candidates = FRUITS.stream()
                    .filter(fruit -> fruit.rarity().ordinal() < selected.ordinal())
                    .toList();
        }
        if (candidates.isEmpty()) throw new IllegalStateException("Devil fruit case pool is empty");
        return candidates.get(random.nextInt(candidates.size()));
    }

    private DevilFruitCaseTable() {}
}
