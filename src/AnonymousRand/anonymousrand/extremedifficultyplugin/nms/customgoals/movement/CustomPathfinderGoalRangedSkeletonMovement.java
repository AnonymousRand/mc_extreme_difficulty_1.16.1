package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.movement;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NmsUtil;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.Items;

public class CustomPathfinderGoalRangedSkeletonMovement<T extends EntityInsentient & ICustomHostile>
        extends CustomPathfinderGoalRangedHandheldMovement<T> {

    protected boolean strafingBackwards;
    protected boolean strafingClockwise;
    protected int strafingTime;

    public CustomPathfinderGoalRangedSkeletonMovement(T goalOwner) {
        this(goalOwner, 1.0);
    }

    public CustomPathfinderGoalRangedSkeletonMovement(T goalOwner, double speedTowardsTarget) {
        super(goalOwner, Items.BOW, speedTowardsTarget);
    }

    @Override
    public void c() {
        super.c();

        this.strafingBackwards = false;
        this.strafingClockwise = false;
        /* Skeletons and illusioners start strafing immediately, and don't need line of sight to do so */
        this.strafingTime = 0;
    }

    @Override
    public void e() {
        super.e();

        EntityLiving goalTarget = this.goalOwner.getGoalTarget();
        if (goalTarget == null) {
            return;
        }

        this.strafingTime++;

        /* Skeletons and illusioners try to maintain a distance from their targets equal to 50% of their detection
           range */
        this.strafingBackwards = !(NmsUtil.distSq(this.goalOwner, goalTarget, true) > this.getDetectionRangeSq() * 0.5);
        /* Skeletons and illusioners have an increased frequency of switching strafe rotation: every 10 ticks of
           strafing, they have a 50% chance to switch the rotation direction */
        if (this.strafingTime % 10 == 0) {
            if (this.goalOwner.getRandom().nextDouble() < 0.5) {
                this.strafingClockwise = !this.strafingClockwise;
            }
        }

        // apply strafe
        /* Skeletons and illusioners strafe in circles much faster than they do forward/backward */
        this.goalOwner.getControllerMove().a(this.strafingBackwards ? -0.2F : 0.2F, // strafe(); largely idempotent
                this.strafingClockwise ? 200F : -200F);
        this.goalOwner.a(goalTarget, 30.0F, 30.0F); // faceEntity(); this.goalOwner.getControllerLook().a() doesn't work
    }

    protected double getDetectionRangeSq() {
        double detectionRange = this.goalOwner.getDetectionRange();
        return detectionRange * detectionRange;
    }
}
