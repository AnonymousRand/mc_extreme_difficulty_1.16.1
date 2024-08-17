package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.CustomPathfinderGoalMoveFasterInCobweb;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.CustomPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityCow extends EntityCow {

    public CustomEntityCow(World world) {
        super(EntityTypes.COW, world);
        this.initCustom();
    }

    private void initCustom() {
        this.initAttributes();

        /* Cows have a 5% chance to spawn as a mooshroom instead */ // todo why not listener?
        if (random.nextDouble() < 0.05) {
            new SpawnEntity(this.world, new CustomEntityMushroomCow(this.world), 1, null,
                    null, this, true, true);
        }

        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
    }

    private void initAttributes() {
        /* Cows move twice as fast and have 20 health */
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.4);
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(20.0);
        this.setHealth(20.0F);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Overridden vanilla functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this));    /* Takes buffs from bats, piglins, etc. */
    }
}
