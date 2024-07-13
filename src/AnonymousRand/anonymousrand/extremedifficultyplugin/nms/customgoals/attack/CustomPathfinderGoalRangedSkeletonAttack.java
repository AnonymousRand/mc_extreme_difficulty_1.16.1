package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;

// originally PathfinderGoalBowShoot
public class CustomPathfinderGoalRangedSkeletonAttack<T extends EntityInsentient & IRangedEntity
        & ICustomHostile /* & IAttackLevelingMob*/> extends CustomPathfinderGoalRangedHandheldAttack<T> {

    protected boolean strafingBackwards;
    protected boolean strafingClockwise;
    protected int strafingTime;

    public CustomPathfinderGoalRangedSkeletonAttack(T goalOwner, int attackCooldown) {
        this(goalOwner, attackCooldown, 1.0);
    }

    public CustomPathfinderGoalRangedSkeletonAttack(T goalOwner, int attackCooldown, double moveSpeed) {
        super(goalOwner, Items.BOW, attackCooldown, moveSpeed);
    }

    @Override
    public void startExecutingMovement() {
        super.startExecutingMovement();

        this.strafingBackwards = false;
        this.strafingClockwise = false;
        /* Skeletons and illusioners start strafing immediately, and don't need line of sight to do so */
        this.strafingTime = 0;
    }

    @Override
    public void tickMovement(EntityLiving target) {
        super.tickMovement(target);
        this.strafingTime++;

        // if kinda far from the target, stop strafing backwards, and if too close to target, strafe backwards
        this.strafingBackwards = !(NMSUtil.distSq(this.goalOwner, target, true) > this.getDetectionRangeSq() * 0.5);
        // every 10 ticks of strafing, have a 50% chance to switch the rotation of strafing
        if (this.strafingTime % 10 == 0) {
            if (this.goalOwner.getRandom().nextDouble() < 0.5) {
                this.strafingClockwise = !this.strafingClockwise;
            }
        }

        // apply strafe
        /* Skeletons and illusioners strafe in circles much faster than they do forward/backward */
        this.goalOwner.getControllerMove().a(this.strafingBackwards ? -0.2F : 0.2F, // strafe(); largely idempotent
                this.strafingClockwise ? 200F : -200F);
        this.goalOwner.a(target, 30.0F, 30.0F); // faceEntity(); this.goalOwner.getControllerLook().a() doesn't work
    }

    @Override
    protected boolean checkAttack(EntityLiving target) {
        return this.goalOwner.isHandRaised();
    }

    @Override
    protected void attack(EntityLiving target) {
        // ItemBow.a() gets the attack power for a corresponding charge of the bow in ticks (we are manually setting it
        // to the normal 20 here to allow rapid fire, to mimic having charged the bow for the full 20 ticks already)
        this.goalOwner.a(target, ItemBow.a(20)); // shoot()
    }

    protected double getDetectionRangeSq() {
        return this.goalOwner.getDetectionRange() * this.goalOwner.getDetectionRange();
    }
}
