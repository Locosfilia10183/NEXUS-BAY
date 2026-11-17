package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Server-side selectable sword and unarmed styles with timed sword combos. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class CombatStyleEvents {
    public enum Style {
        AUTO("Automatisch"), SWORD("Schwertkampf"), FIST("Faustkampf");
        private final String display;
        Style(String display) { this.display = display; }
        public String display() { return display; }
        public Style next() { return values()[(ordinal() + 1) % values().length]; }
    }

    private static final String STYLE_KEY = "nexusbay_combat_style";
    private static final String COMBO_COUNT = "nexusbay_sword_combo_count";
    private static final String COMBO_TICK = "nexusbay_sword_combo_tick";
    private static final long COMBO_WINDOW_TICKS = 36L;

    private CombatStyleEvents() {}

    public static Style getStyle(ServerPlayer player) {
        int id = player.getPersistentData().getInt(STYLE_KEY);
        return Style.values()[Math.floorMod(id, Style.values().length)];
    }

    public static void cycle(ServerPlayer player) {
        Style next = getStyle(player).next();
        player.getPersistentData().putInt(STYLE_KEY, next.ordinal());
        player.getPersistentData().putInt(COMBO_COUNT, 0);
        player.displayClientMessage(Component.literal("§6Kampfstil: §f" + next.display()), true);
    }

    @SubscribeEvent
    public static void onPlayerDamage(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)
                || !(event.getEntity() instanceof LivingEntity target)
                || target == player || !target.isAlive()) return;

        Style style = getStyle(player);
        boolean swordEquipped = player.getMainHandItem().getItem() instanceof SwordItem;
        boolean emptyHand = player.getMainHandItem().isEmpty();
        boolean swordActive = style == Style.AUTO ? swordEquipped : style == Style.SWORD && swordEquipped;
        boolean fistActive = style == Style.AUTO ? emptyHand : style == Style.FIST && emptyHand;

        if (swordActive) {
            applySwordCombo(player, target, event);
        } else if (fistActive) {
            event.setAmount(event.getAmount() * 1.08F);
        } else if (style != Style.AUTO) {
            // A selected style only grants its bonus when the matching weapon state is used.
            player.getPersistentData().putInt(COMBO_COUNT, 0);
        }
    }

    private static void applySwordCombo(ServerPlayer player, LivingEntity target,
                                        LivingHurtEvent event) {
        long now = player.serverLevel().getGameTime();
        long previous = player.getPersistentData().getLong(COMBO_TICK);
        int combo = player.getPersistentData().getInt(COMBO_COUNT);
        if (now - previous > COMBO_WINDOW_TICKS || now < previous) combo = 0;
        combo = Math.min(3, combo + 1);
        player.getPersistentData().putInt(COMBO_COUNT, combo);
        player.getPersistentData().putLong(COMBO_TICK, now);

        event.setAmount(event.getAmount() * switch (combo) {
            case 2 -> 1.05F;
            case 3 -> 1.12F;
            default -> 1.0F;
        });

        if (combo == 3) {
            var level = player.serverLevel();
            level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    target.getX(), target.getY() + target.getBbHeight() * 0.55D, target.getZ(),
                    1, 0.25D, 0.25D, 0.25D, 0.0D);
            level.sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + target.getBbHeight() * 0.55D, target.getZ(),
                    8, 0.35D, 0.35D, 0.35D, 0.12D);
            level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT,
                    SoundSource.PLAYERS, 0.7F, 1.15F);
        }
    }
}
