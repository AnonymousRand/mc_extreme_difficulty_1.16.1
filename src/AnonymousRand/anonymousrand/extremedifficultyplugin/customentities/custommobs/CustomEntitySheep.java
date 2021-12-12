package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalPassiveMoveTowardsTarget;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntitySheep extends EntitySheep implements ICustomMob {

    public CustomEntitySheep(World world) {
        super(EntityTypes.SHEEP, world);
        this.a(PathType.LAVA, 0.0F); /**no longer avoids lava*/
        this.a(PathType.DAMAGE_FIRE, 0.0F); /**no longer avoids fire*/
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.46); /**sheep move 2x faster and have 20 health*/
        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(20.0);
        this.setHealth(20.0F);
        this.goalSelector.a(2, new NewPathfinderGoalPassiveMoveTowardsTarget(this, 1.0, (float)this.getFollowRange())); /**uses the custom goal that makes this mob actually move towards the player within 32 blocks; lower priority than panicking goal*/
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, false)); /**this mob now seeks out players; uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement); this custom goal also allows the spider to continue attacking regardless of light level*/
    }

    public double getFollowRange() { /**sheep have 32 block detection range (setting attribute doesn't work)*/
        return 32.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getGoalTarget() != null) {
            if (this.d(this.getGoalTarget().getPositionVector()) <= 4.0) { /**sheep explode instantly when it is less than 2 blocks away from player*/
                this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 2.0F, true, Explosion.Effect.DESTROY);
                this.die();
            }
        }
    }

    @Override
    public int bL() { //getMaxFallHeight
        return Integer.MAX_VALUE; /**mobs are willing to take any fall to reach the player as they don't take fall damage*/
    }
}
