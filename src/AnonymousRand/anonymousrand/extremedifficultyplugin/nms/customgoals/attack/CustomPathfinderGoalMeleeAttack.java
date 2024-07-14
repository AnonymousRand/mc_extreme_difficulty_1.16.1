package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;

/**
 * The base melee attack goal in my rewrite of vanilla's attack goals, analogous to
 * <code>PathfinderGoalMeleeAttack</code>. Note that melee attacks don't ignore y-level even if the targeting goal
 * does, so make sure those mobs can change their y-level to get within melee range of their targets.
 */
public class CustomPathfinderGoalMeleeAttack<T extends EntityInsentient & ICustomHostile/* & IAttackLevelingMob*/>
        extends CustomPathfinderGoalAttack<T> {

    // attack
    protected double minAttackReach;
    // movement
    protected PathEntity path;
    protected double oldTargetX;
    protected double oldTargetY;
    protected double oldTargetZ;

    public CustomPathfinderGoalMeleeAttack(T goalOwner) {
        /* Default minimum attack reach is 2.0 blocks to help baby zombies out */
        this(goalOwner, 20, 1.0, 2.0);
    }

    public CustomPathfinderGoalMeleeAttack(T goalOwner, int attackCooldown) {
        this(goalOwner, attackCooldown, 1.0, 2.0);
    }

    public CustomPathfinderGoalMeleeAttack(T goalOwner, double moveSpeed) {
        this(goalOwner, 20, moveSpeed, 2.0);
    }

    public CustomPathfinderGoalMeleeAttack(T goalOwner, int attackCooldown, double moveSpeed, double minAttackReach) {
        super(goalOwner, attackCooldown, moveSpeed);
        this.minAttackReach = minAttackReach;
    }

    @Override
    protected boolean shouldExecuteMovement() {
        this.path = this.goalOwner.getNavigation().a(this.goalOwner.getGoalTarget(), 0); // getPath()
        return this.path != null;
    }

    @Override
    protected boolean shouldContinueExecutingMovement() {
        return this.goalOwner.a(this.goalOwner.getGoalTarget().getChunkCoordinates());
    }

    @Override
    protected void startExecutingMovement() {
        super.startExecutingMovement();

        EntityLiving target = this.goalOwner.getGoalTarget();
        if (target != null) {
            this.oldTargetX = Double.MAX_VALUE;
            this.oldTargetY = Double.MAX_VALUE;
            this.oldTargetZ = Double.MAX_VALUE;
        }

        this.goalOwner.getNavigation().a(this.path, this.moveSpeed);
    }

    @Override
    protected void tickMovement(EntityLiving target) {
        super.tickMovement(target);

        // repath to target once remainingRepathCooldown is up if it's more than 1 block away from the target (or 10% random chance if it is)
        boolean shouldRepathToTarget =
                this.remainingRepathCooldown <= 0
                        && (NMSUtil.distSq(target.locX(), target.locY(), target.locZ(),
                        this.oldTargetX, this.oldTargetY, this.oldTargetZ, false) >= 1.0
                        || this.goalOwner.getRandom().nextFloat() < 0.1F); // increased from vanilla 0.05 to 0.1
        if (shouldRepathToTarget) {
            this.oldTargetX = target.locX();
            this.oldTargetY = target.locY();
            this.oldTargetZ = target.locZ();
            this.remainingRepathCooldown = this.repathCooldown;

            double distSqToTarget = NMSUtil.distSq(this.goalOwner, target, false);
            if (distSqToTarget > 1024.0) {
                this.remainingRepathCooldown += 10;
            } else if (distSqToTarget > 256.0) {
                this.remainingRepathCooldown += 5;
            }

            if (!this.goalOwner.getNavigation().a(target, this.moveSpeed)) { // tryMoveTo()
                this.remainingRepathCooldown += 15;
            }
        }
    }

    @Override
    protected boolean checkAttack(EntityLiving target) {
        return NMSUtil.distSq(this.goalOwner, target, false) <= this.getAttackReachSq(target);
    }

    @Override
    protected void attack(EntityLiving target) {
        this.goalOwner.swingHand(EnumHand.MAIN_HAND);
        this.goalOwner.attackEntity(target);
    }

    protected double getAttackReachSq(EntityLiving target) {
        return Math.max(Math.pow(2.0F * this.goalOwner.getWidth(), 2) + target.getWidth(),
                this.minAttackReach);
    }

    public void setMinAttackReach(double minAttackReach) {
        this.minAttackReach = minAttackReach;
    }
}
