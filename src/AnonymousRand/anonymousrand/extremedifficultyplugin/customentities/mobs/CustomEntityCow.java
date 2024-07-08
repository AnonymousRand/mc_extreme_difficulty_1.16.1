package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalMoveFasterInCobweb;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
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
        this.setHealth(20.0F);
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(20.0);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                               Overridden vanilla functions                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));    /* Takes buffs from bats, piglins, etc. */
    }
}