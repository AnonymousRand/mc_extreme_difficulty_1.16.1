package AnonymousRand.anonymousrand.plugin.nms.customgoals.attackmvmt;

import AnonymousRand.anonymousrand.plugin.util.NmsUtil;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.PathEntity;

public class CustomPathfinderGoalAttackMvmtMelee<T extends EntityInsentient> extends CustomPathfinderGoalAttackMvmt<T> {

    protected PathEntity path;
    protected double oldTargetX;
    protected double oldTargetY;
    protected double oldTargetZ;

    public CustomPathfinderGoalAttackMvmtMelee(T goalOwner) {
        this(goalOwner, 1.0);
    }

    public CustomPathfinderGoalAttackMvmtMelee(T goalOwner, double speedTowardsTarget) {
        super(goalOwner, speedTowardsTarget);
    }

    @Override
    public boolean a() {
        if (!super.a()) {
            return false;
        }

        this.path = this.goalOwner.getNavigation().a(this.goalOwner.getGoalTarget(), 0); // getPath()
        return this.path != null;
    }

    @Override
    public boolean b() {
        if (!super.b()) {
            return false;
        }

        return this.goalOwner.a(this.goalOwner.getGoalTarget().getChunkCoordinates());
    }

    @Override
    public void c() {
        super.c();

        EntityLiving attackTarget = this.goalOwner.getGoalTarget();
        if (attackTarget != null) {
            this.oldTargetX = Double.MAX_VALUE;
            this.oldTargetY = Double.MAX_VALUE;
            this.oldTargetZ = Double.MAX_VALUE;
        }

        this.goalOwner.getNavigation().a(this.path, this.speedTowardsTarget);
    }

    @Override
    public void e() {
        super.e();

        EntityLiving attackTarget = this.goalOwner.getGoalTarget();
        if (attackTarget == null) {
            return;
        }

        // repath to target once remainingCooldownRepath is up if it's more than 1 block away from the target (or 10% random chance if it is)
        boolean shouldRepathToTarget =
                this.remainingCooldownRepath <= 0
                        && (NmsUtil.distSq(attackTarget.locX(), attackTarget.locY(), attackTarget.locZ(),
                        this.oldTargetX, this.oldTargetY, this.oldTargetZ, false) >= 1.0
                        || this.goalOwner.getRandom().nextFloat() < 0.1F); // increased from vanilla 0.05 to 0.1
        if (shouldRepathToTarget) {
            this.oldTargetX = attackTarget.locX();
            this.oldTargetY = attackTarget.locY();
            this.oldTargetZ = attackTarget.locZ();
            this.remainingCooldownRepath = this.repathCooldown;

            double distSqToTarget = NmsUtil.distSq(this.goalOwner, attackTarget, false);
            if (distSqToTarget > 1024.0) {
                this.remainingCooldownRepath += 10;
            } else if (distSqToTarget > 256.0) {
                this.remainingCooldownRepath += 5;
            }

            if (!this.goalOwner.getNavigation().a(attackTarget, this.speedTowardsTarget)) { // tryMoveTo()
                this.remainingCooldownRepath += 15;
            }
        }
    }
}
