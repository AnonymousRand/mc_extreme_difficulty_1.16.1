package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;

import java.util.EnumSet;

// not extending PathfinderGoalArrowAttack as I normally would due to unused private fields like the goal target (obfuscated as this.c)
// todo uncomment once all have been converted
public class CustomPathfinderGoalRangedAttack<T extends EntityInsentient & IRangedEntity/* & IAttackLevelingMob*/> extends PathfinderGoal {

    protected final T entity;
    protected final double speedTowardsTarget;
    protected int attackCooldown;
    protected float maxAttackDistance;
    protected int attackRemainingCooldown;
    protected int seeTime;

    public CustomPathfinderGoalRangedAttack(T entity, double speedTowardsTarget, int attackCooldown, float maxAttackDistance) {
        this.entity = entity;
        this.speedTowardsTarget = speedTowardsTarget;
        this.attackCooldown = attackCooldown;
        this.maxAttackDistance = maxAttackDistance;
        this.attackRemainingCooldown = -1;
        this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
    }

    @Override
    public boolean a() {
        return this.entity.getGoalTarget() != null;
    }

    @Override
    public boolean b() {
        return this.a() || !this.entity.getNavigation().m();
    }

    @Override
    public void d() {
        this.seeTime = 0;
        this.attackRemainingCooldown = -1;
    }

    @Override
    public void e() {
        EntityLiving goalTarget = this.entity.getGoalTarget();
        if (goalTarget == null) {
            return;
        }

        this.attackRemainingCooldown--;
        double distSqToGoalTarget = NMSUtil.distSq(this.entity, goalTarget, true);
        /* Breaking line of sight does not stop the mob from attacking */
        ++this.seeTime; // todo what this for

        if (distSqToGoalTarget <= (double) (this.maxAttackDistance * this.maxAttackDistance) && this.seeTime >= 5) {
            this.entity.getNavigation().o(); // todo what function is this
        } else {
            this.entity.getNavigation().a(goalTarget, this.speedTowardsTarget);
        }

        this.entity.getControllerLook().a(goalTarget, 30.0F, 30.0F);

        if (this.attackRemainingCooldown <= 0) {
            this.attackRemainingCooldown = this.attackCooldown;
            // this.entity.increaseAttacks(1); // todo uncomment once all have been converted

            float f1 = MathHelper.a(MathHelper.sqrt(distSqToGoalTarget) / this.maxAttackDistance, 0.1F, 1.0F);
            this.entity.a(goalTarget, f1); // shoot()
        }
    }
}