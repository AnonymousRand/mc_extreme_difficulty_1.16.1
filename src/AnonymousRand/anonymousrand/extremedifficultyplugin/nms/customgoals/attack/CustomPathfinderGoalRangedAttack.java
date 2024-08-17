package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;

/**
 * The base ranged attack goal in my rewrite of vanilla's attack goals, analogous to
 * <code>PathfinderGoalArrowAttack</code>.
 */
public class CustomPathfinderGoalRangedAttack<T extends EntityInsentient & IRangedEntity
        & ICustomHostile/* & IAttackLevelingMob*/> extends CustomPathfinderGoalAttack<T> {

    /* Movement */
    protected int targetSeenTicks;

    public CustomPathfinderGoalRangedAttack(T goalOwner, int attackCooldown) {
        this(goalOwner, attackCooldown, 1.0);
    }

    public CustomPathfinderGoalRangedAttack(T goalOwner, int attackCooldown, double moveSpeed) {
        super(goalOwner, attackCooldown, moveSpeed);
    }

    @Override
    protected void startExecutingMovement() {
        super.startExecutingMovement();
        this.targetSeenTicks = 0;
    }

    @Override
    protected void tickMovement(EntityLiving target) {
        super.tickMovement(target);

        // repath to target if it can't be seen
        // Note: checking if there is a way to get to the target doesn't seem to work
        if (this.remainingRepathCooldown <= 0 && !this.goalOwner.getEntitySenses().a(target)) {
            this.remainingRepathCooldown = this.repathCooldown;
            this.targetSeenTicks = 0;
        } else {
            this.targetSeenTicks++;
        }

        if (this.targetSeenTicks < 5) {
            this.goalOwner.getNavigation().a(target, this.moveSpeed); // tryMoveTo()
        } else {
            this.goalOwner.getNavigation().o();                       // clearPath() (stands still)
        }
    }

    @Override
    protected void attack(EntityLiving target) {
        float distanceFactor = (float) MathHelper.a(NMSUtil.dist(this.goalOwner, target, false)
                                       / this.goalOwner.getDetectionRange(), 0.1, 1.0);
        this.goalOwner.a(target, distanceFactor); // shoot()
    }
}
