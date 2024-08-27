package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attackmvmt;

import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.EntityLiving;

public class CustomPathfinderGoalAttackMvmtRanged<T extends EntityInsentient> extends CustomPathfinderGoalAttackMvmt<T> {

    protected int targetSeenTicks;

    public CustomPathfinderGoalAttackMvmtRanged(T goalOwner) {
        this(goalOwner, 1.0);
    }

    public CustomPathfinderGoalAttackMvmtRanged(T goalOwner, double speedTowardsTarget) {
        super(goalOwner, speedTowardsTarget);
    }

    @Override
    public void c() {
        super.c();
        this.targetSeenTicks = 0;
    }

    @Override
    public void e() {
        super.e();

        EntityLiving attackTarget = this.goalOwner.getGoalTarget();
        if (attackTarget == null) {
            return;
        }

        // repath to target if it can't be seen
        // note: checking if there is a way to get to the target doesn't seem to work
        if (this.remainingCooldownRepath <= 0 && !this.goalOwner.getEntitySenses().a(attackTarget)) {
            this.remainingCooldownRepath = this.repathCooldown;
            this.targetSeenTicks = 0;
        } else {
            this.targetSeenTicks++;
        }

        if (this.targetSeenTicks < 5) {
            this.goalOwner.getNavigation().a(attackTarget, this.speedTowardsTarget); // tryMoveTo()
        } else {
            this.goalOwner.getNavigation().o();                       // clearPath() (stands still)
        }
    }
}
