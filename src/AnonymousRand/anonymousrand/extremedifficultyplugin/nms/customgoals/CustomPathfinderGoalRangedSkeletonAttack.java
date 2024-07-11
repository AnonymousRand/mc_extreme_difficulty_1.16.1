package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;

import java.util.EnumSet;

// originally PathfinderGoalBowShoot
public class CustomPathfinderGoalRangedSkeletonAttack<T extends EntityInsentient & IRangedEntity
        & ICustomHostile /* & IAttackLevelingMob*/> extends CustomPathfinderGoalHandheldRangedAttack<T> {

    protected boolean strafingClockwise;
    protected boolean strafingBackwards;
    protected int strafingTime = -1;
    
    public CustomPathfinderGoalRangedSkeletonAttack(T goalOwner, int attackCooldown) {
        this(goalOwner, 1.0, attackCooldown);
    }

    public CustomPathfinderGoalRangedSkeletonAttack(T goalOwner, double speedTowardsTarget, int attackCooldown) {
        super(goalOwner, Items.BOW, speedTowardsTarget, attackCooldown);
        this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
    }

    @Override
    public void e() {
        EntityLiving goalTarget = this.goalOwner.getGoalTarget();

        if (goalTarget == null) {
            return;
        }
        double distanceToSquared = NMSUtil.distSq(this.goalOwner, goalTarget, true);
        /* breaking line of sight does not stop the mob from attacking */
        ++this.targetSeenTicks;

        if (distanceToSquared <= this.getDetectionRangeSq() && this.targetSeenTicks >= 20) {
            this.goalOwner.getNavigation().o();
            ++this.strafingTime;
        } else {
            this.goalOwner.getNavigation().a(goalTarget, this.speedTowardsTarget);
            this.strafingTime = -1;
        }

        // every 20 ticks of strafing, have a 30% chance to switch the direction and rotation of strafing
        if (this.strafingTime >= 20) {
            if (this.goalOwner.getRandom().nextDouble() < 0.3) {
                this.strafingClockwise = !this.strafingClockwise;
            }

            if (this.goalOwner.getRandom().nextDouble() < 0.3) {
                this.strafingBackwards = !this.strafingBackwards;
            }

            this.strafingTime = 0;
        }

        // strafe
        if (this.strafingTime > -1) {
            // if too close to target, strafe backwards
            if (distanceToSquared > this.getDetectionRangeSq() * 0.75) {
                this.strafingBackwards = false;
            } else if (distanceToSquared < this.getDetectionRangeSq() * 0.25) {
                this.strafingBackwards = true;
            }

            this.goalOwner.getControllerMove().a(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
            this.goalOwner.a(goalTarget, 30.0F, 30.0F);
        } else {
            this.goalOwner.getControllerLook().a(goalTarget, 30.0F, 30.0F);
        }

        if (this.goalOwner.isHandRaised()) {
            this.goalOwner.clearActiveItem();
            this.goalOwner.a(goalTarget, ItemBow.a(20)); // shoot(); ItemBow.a() gets the attack power for a corresponding charge of the bow in ticks (manually setting it to the normal 20 here to allow rapid fire, because normally this only runs if mob has charged bow for 20 ticks)
            this.remainingAttackCooldown = this.attackCooldown;
        } else if (--this.remainingAttackCooldown <= 0 && this.targetSeenTicks >= -60) {
            this.goalOwner.c(ProjectileHelper.a(this.goalOwner, Items.BOW)); // startUsingItem()
        }
    }

    protected double getDetectionRangeSq() {
        return this.goalOwner.getDetectionRange() * this.goalOwner.getDetectionRange();
    }
}
