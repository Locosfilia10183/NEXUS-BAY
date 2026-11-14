package de.nexusbay.forge.ship;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;

/**
 * First server-authoritative sailing prototype.
 * Uses vanilla boat physics and passenger controls as a stable foundation;
 * custom hull, sails and steering upgrades are layered on in later milestones.
 */
public class NexusShipEntity extends Boat {
    public NexusShipEntity(EntityType<? extends Boat> type, Level level) {
        super(type, level);
        this.setVariant(Boat.Type.OAK);
    }

    @Override
    protected int getMaxPassengers() {
        return 6;
    }
}
