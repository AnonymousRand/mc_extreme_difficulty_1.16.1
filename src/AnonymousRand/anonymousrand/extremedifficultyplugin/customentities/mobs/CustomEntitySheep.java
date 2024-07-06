package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalPassiveMoveTowardsTarget;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntitySheep extends EntitySheep implements ICustomHostile {

    public CustomEntitySheep(World world) {
        super(EntityTypes.SHEEP, world);
        /* No longer avoids lava and fire */
        this.a(PathType.LAVA, 0.0F);
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.46); /* sheep move 2x faster and have 20 health */
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(20.0);
        this.setHealth(20.0F);
        this.goalSelector.a(2, new NewPathfinderGoalPassiveMoveTowardsTarget(this, (float) this.getFollowRange())); /* uses the custom goal that makes this mob actually move towards the player within 32 blocks; lower priority than panicking goal */
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* this mob now seeks out players; uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
    }

    public double getFollowRange() { /* sheep have 32 block detection range */
        return 32.0;
    }

    @Override
    public int bL() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getGoalTarget() != null) {
            if (this.d(this.getGoalTarget().getPositionVector()) <= 4.0) { /* sheep explode instantly when it is less than 2 blocks away from player */
                this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 1.0F, true, Explosion.Effect.DESTROY);
                this.die();
            }
        }
    }
}
