package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import net.minecraft.server.v1_16_R1.*;

import java.util.EnumSet;

// not extending PathfinderGoalArrowAttack as I normally would due to unused private fields like the target (obfuscated as this.c)
public class CustomPathfinderGoalRangedAttack<T extends EntityInsentient & IRangedEntity> extends PathfinderGoal {

    protected final T entity;
    protected final double speedTowardsTarget;
    protected int attackInterval;
    protected float maxDistance;
    protected int attackRemainingCooldown;
    protected int seeTime;

    public CustomPathfinderGoalRangedAttack(T entity, double speedTowardsTarget, int attackInterval, float maxDistance) {
        this.entity = entity;
        this.speedTowardsTarget = speedTowardsTarget;
        this.attackInterval = attackInterval;
        this.maxDistance = maxDistance;
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
        EntityLiving attackTarget = this.entity.getGoalTarget();
        if (attackTarget == null) {
            return;
        }

        double distanceToSquared = this.entity.d(attackTarget.getPositionVector());
        /* breaking line of sight does not stop the mob from attacking */
        ++this.seeTime;

        if (distanceToSquared <= (double) (this.maxDistance * this.maxDistance) && this.seeTime >= 5) {
            this.entity.getNavigation().o();
        } else {
            this.entity.getNavigation().a(attackTarget, this.speedTowardsTarget);
        }

        this.entity.getControllerLook().a(attackTarget, 30.0F, 30.0F);

        if (--this.attackRemainingCooldown == 0) {
            float f1 = MathHelper.a(MathHelper.sqrt(distanceToSquared) / this.maxDistance, 0.1F, 1.0F);

            this.entity.a(attackTarget, f1); // shoot()
            this.attackRemainingCooldown = this.attackInterval;
        } else if (this.attackRemainingCooldown < 0) {
            this.attackRemainingCooldown = this.attackInterval;
        }
    }
}
