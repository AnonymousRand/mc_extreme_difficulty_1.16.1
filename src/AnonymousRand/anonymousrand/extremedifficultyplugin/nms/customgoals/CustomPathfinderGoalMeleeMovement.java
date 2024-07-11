package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.util.EntityFilter;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;

import java.util.EnumSet;

// originally from PathfinderGoalMeleeAttack; separated to provide separation of concerns
public class CustomPathfinderGoalMeleeMovement<T extends EntityInsentient & ICustomHostile> extends PathfinderGoal {

    protected final T goalOwner;
    protected final double speedTowardsTarget;
    protected PathEntity path;
    protected double oldTargetX;
    protected double oldTargetY;
    protected double oldTargetZ;
    protected int repathCooldown;

    public CustomPathfinderGoalMeleeMovement(T goalOwner) {
        this(goalOwner, 1.0);
    }

    public CustomPathfinderGoalMeleeMovement(T goalOwner, double speedTowardsTarget) {
        this.goalOwner = goalOwner;
        this.speedTowardsTarget = speedTowardsTarget;
        this.a(EnumSet.of(Type.MOVE, Type.LOOK));
    }

    @Override
    public boolean a() {
        EntityLiving goalTarget = this.goalOwner.getGoalTarget();
        if (!EntityFilter.BASE.test(goalTarget)) {
            return false;
        }

        this.path = this.goalOwner.getNavigation().a(goalTarget, 0);
        // melee attacks will never ignore y-level, even if the target goal does
        // so hope/make sure that those mobs have some way to get closer to the target!
        return this.path != null;
    }

    @Override
    public boolean b() {
        EntityLiving goalTarget = this.goalOwner.getGoalTarget();
        if (!EntityFilter.BASE.test(goalTarget)) {
            return false;
        }

        if (!this.goalOwner.a(goalTarget.getChunkCoordinates())) {
            return false;
        }

        return true;
    }

    @Override
    public void c() {
        this.repathCooldown = 0;
        EntityLiving goalTarget = this.goalOwner.getGoalTarget();
        if (goalTarget != null) {
            this.oldTargetX = Double.MAX_VALUE;
            this.oldTargetY = Double.MAX_VALUE;
            this.oldTargetZ = Double.MAX_VALUE;
        }

        this.goalOwner.getNavigation().a(this.path, this.speedTowardsTarget);
    }

    @Override
    public void d() {
        this.goalOwner.getNavigation().o(); // clearPath()
    }

    @Override
    public void e() {
        EntityLiving goalTarget = this.goalOwner.getGoalTarget();
        if (goalTarget == null) {
            return;
        }

        this.goalOwner.getControllerLook().a(goalTarget, 30.0F, 30.0F);
        double distSqToGoalTarget = NMSUtil.distSq(this.goalOwner, goalTarget, false);

        // repath to target once repathCooldown is up if it's more than 1 block away from the target (or 10% random chance if it is)
        this.repathCooldown = Math.max(this.repathCooldown - 1, 0);
        boolean shouldRepathToTarget =
                this.repathCooldown <= 0
                && (NMSUtil.distSq(goalTarget.locX(), goalTarget.locY(), goalTarget.locZ(),
                    this.oldTargetX, this.oldTargetY, this.oldTargetZ, false) >= 1.0
                    || this.goalOwner.getRandom().nextFloat() < 0.1F); // increased from vanilla 0.05 to 0.1
        if (shouldRepathToTarget) {
            this.oldTargetX = goalTarget.locX();
            this.oldTargetY = goalTarget.locY();
            this.oldTargetZ = goalTarget.locZ();
            this.repathCooldown = 4 + this.goalOwner.getRandom().nextInt(7);
            if (distSqToGoalTarget > 1024.0) {
                this.repathCooldown += 10;
            } else if (distSqToGoalTarget > 256.0) {
                this.repathCooldown += 5;
            }

            if (!this.goalOwner.getNavigation().a(goalTarget, this.speedTowardsTarget)) { // tryMoveTo()
                this.repathCooldown += 15;
            }
        }
    }
}
