package AnonymousRand.anonymousrand.plugin.nms.customgoals.attackmvmt;

import AnonymousRand.anonymousrand.plugin.util.EntityFilter;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.PathfinderGoal;

import java.util.EnumSet;

/**
 * 
 *
 * @param <T>
 */
public abstract class CustomPathfinderGoalAttackMvmt<T extends EntityInsentient> extends PathfinderGoal {

    protected final T goalOwner;
    protected double speedTowardsTarget;
    protected int repathCooldown;
    protected int remainingCooldownRepath;

    protected CustomPathfinderGoalAttackMvmt(T goalOwner, double speedTowardsTarget) {
        this.goalOwner = goalOwner;
        this.speedTowardsTarget = speedTowardsTarget;
        this.repathCooldown = 4 + this.goalOwner.getRandom().nextInt(7);
        this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK)); // `setMutexFlags()`
    }

    @Override
    public boolean a() {
        return EntityFilter.BASE.test(this.goalOwner.getGoalTarget());
    }

    @Override
    public boolean b() {
        return this.a();
    }

    @Override
    public void c() {
        this.remainingCooldownRepath = 0;
    }

    @Override
    public void d() {
        this.goalOwner.getNavigation().o(); // clearPath()
    }

    @Override
    public void e() {
        EntityLiving attackTarget = this.goalOwner.getGoalTarget();
        if (attackTarget == null) {
            return;
        }

        this.remainingCooldownRepath--;

        this.goalOwner.getControllerLook().a(attackTarget, 30.0F, 30.0F); // setLookPositionWithEntity(); faces target
    }

    public void setSpeedTowardsTarget(double speedTowardsTarget) {
        this.speedTowardsTarget = speedTowardsTarget;
    }
}
