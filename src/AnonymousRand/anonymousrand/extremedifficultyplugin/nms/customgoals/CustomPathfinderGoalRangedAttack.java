package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.util.EntityFilter;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;

import java.util.EnumSet;

// Entire class rewritten instead of inherited from PathfinderGoalArrowAttack in order to apply our own logic
// without being too hacky or needing too much reflection (everything's private :/)
// todo uncomment once all have been converted
public class CustomPathfinderGoalRangedAttack<T extends EntityInsentient & IRangedEntity
        & ICustomHostile /* & IAttackLevelingMob*/> extends PathfinderGoal {

    protected final T goalOwner;
    protected final double speedTowardsTarget;
    protected int attackCooldown;
    protected int remainingAttackCooldown;
    protected int targetSeenTicks;

    public CustomPathfinderGoalRangedAttack(T goalOwner, int attackCooldown) {
        this(goalOwner, 1.0, attackCooldown);
    }

    public CustomPathfinderGoalRangedAttack(T goalOwner, double speedTowardsTarget, int attackCooldown) {
        this.goalOwner = goalOwner;
        this.speedTowardsTarget = speedTowardsTarget;
        this.attackCooldown = attackCooldown;
        this.remainingAttackCooldown = -1;
        this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
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
        this.goalOwner.setAggressive(true);
    }

    @Override
    public void d() {
        this.targetSeenTicks = 0;
        this.remainingAttackCooldown = -1;
        this.goalOwner.setAggressive(false);
    }

    @Override
    public void e() {
        this.remainingAttackCooldown--;

        EntityLiving goalTarget = this.goalOwner.getGoalTarget();
        if (goalTarget == null) {
            return;
        }

        this.goalOwner.getControllerLook().a(goalTarget, 30.0F, 30.0F);
        double distSqToGoalTarget = NMSUtil.distSq(this.goalOwner, goalTarget, false);

        // determine whether to repath or to stand still (repaths if can't see and ignoreLOS is false)
        if (this.goalOwner.getEntitySenses().a(goalTarget) || this.goalOwner.ignoresLOS()) {
            this.targetSeenTicks++;
        } else {
            this.targetSeenTicks = 0;
        }
        if (this.targetSeenTicks < 5) {
            this.goalOwner.getNavigation().a(goalTarget, this.speedTowardsTarget); // tryMoveTo()
        } else {
            this.goalOwner.getNavigation().o();                                    // clearPath() (stands still)
        }

        // attack
        if (this.remainingAttackCooldown <= 0) {
            this.remainingAttackCooldown = this.attackCooldown;
            // this.entity.increaseAttacks(1); // todo uncomment once all have been converted
            float distanceFactor = (float) MathHelper.a(
                    MathHelper.sqrt(distSqToGoalTarget) / this.goalOwner.getDetectionRange(), 0.1, 1.0);
            this.attack(goalTarget, distanceFactor);
        }
    }

    protected void attack(EntityLiving goalTarget, float distanceFactor) {
        this.goalOwner.a(goalTarget, distanceFactor); // shoot()
    }

    public void setAttackCooldown(int attackCooldown) {
        this.attackCooldown = attackCooldown;
    }
}
