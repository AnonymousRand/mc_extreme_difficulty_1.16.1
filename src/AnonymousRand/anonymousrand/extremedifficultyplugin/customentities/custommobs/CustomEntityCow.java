package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomPeaceful;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalCobwebMoveFaster;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityCow extends EntityCow implements ICustomPeaceful {

    public CustomEntityCow(World world) {
        super(EntityTypes.COW, world);
        this.initCustomPeaceful();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                      ICustomPeaceful                                      //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public void initCustomPeaceful() {
        /* Cows have a 5% chance to spawn as a mooshroom instead */
        if (random.nextDouble() < 0.05) {
            new SpawnEntity(this.getWorld(), new CustomEntityMushroomCow(this.getWorld()), 1, null, null, this, true, true);
        }

        /* No longer avoids lava */
        this.a(PathType.LAVA, 0.0F);
        /* No longer avoids fire */
        this.a(PathType.DAMAGE_FIRE, 0.0F);

        this.initAttributes();
    }

    private void initAttributes() {
        /* Cows move twice as fast and have 20 health */
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.4);
        this.setHealth(20.0F);
        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(20.0);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                               Overridden vanilla functions                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this));
        /* Takes buffs from bats and piglins etc. */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));
    }
}