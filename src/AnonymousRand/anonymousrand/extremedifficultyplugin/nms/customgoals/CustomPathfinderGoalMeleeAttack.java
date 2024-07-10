package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;

import java.util.EnumSet;
import java.util.Random;

// Entire class rewritten instead of inherited from PathfinderGoalMeleeAttack in order to apply our own logic
// without being too hacky or needing too much reflection (everything's private :/)
public class CustomPathfinderGoalMeleeAttack<T extends EntityInsentient & ICustomHostile> extends PathfinderGoal {

    protected final T goalOwner;
    protected final double speedTowardsTarget;
    protected final boolean continuePathingIfNoLOS;
    protected PathEntity path;
    protected double oldTargetX;
    protected double oldTargetY;
    protected double oldTargetZ;
    protected int repathCooldown;
    protected int attackCooldown;
    protected int remainingAttackCooldown;
    protected double minAttackReach;

    public CustomPathfinderGoalMeleeAttack(T goalOwner, double speedTowardsTarget) {

        // default minimum attack reach is 2.0 blocks to help baby zombies out
        this(goalOwner, speedTowardsTarget, goalOwner.ignoresLOS(), 20, 2.0);
    }

    public CustomPathfinderGoalMeleeAttack(T goalOwner, double speedTowardsTarget, boolean continuePathingIfNoLOS) {

        this(goalOwner, speedTowardsTarget, continuePathingIfNoLOS, 20, 2.0);
    }

    public CustomPathfinderGoalMeleeAttack(
            T goalOwner,
            double speedTowardsTarget,
            boolean continuePathingIfNoLOS,
            int attackCooldown,
            double minAttackReach) {

        this.goalOwner = goalOwner;
        this.speedTowardsTarget = speedTowardsTarget;
        this.continuePathingIfNoLOS = continuePathingIfNoLOS;
        this.attackCooldown = attackCooldown;
        this.remainingAttackCooldown = attackCooldown;
        this.minAttackReach = minAttackReach;
        this.a(EnumSet.of(Type.MOVE, Type.LOOK));
    }

    @Override
    public boolean a() {
        EntityLiving goalTarget = this.goalOwner.getGoalTarget();
        if (goalTarget == null || goalTarget.isSpectator() || goalTarget.isInvulnerable() || !goalTarget.isAlive()) {
            return false;
        }

        this.path = this.goalOwner.getNavigation().a(goalTarget, 0);
        return this.path != null || this.getAttackReachSq(goalTarget) >= NMSUtil.distSq(this.goalOwner, goalTarget, true);
    }
    
    @Override
    public boolean b() {
        EntityLiving goalTarget = this.goalOwner.getGoalTarget();
        if (goalTarget == null || goalTarget.isSpectator() || goalTarget.isInvulnerable() || !goalTarget.isAlive()) {
            return false;
        }

        if (!this.continuePathingIfNoLOS) {
            return !this.goalOwner.getNavigation().m();
        }

        if (!this.goalOwner.a(goalTarget.getChunkCoordinates())) {
            return false;
        }

        return true;
    }

    @Override
    public void c() {
        this.goalOwner.getNavigation().a(this.path, this.speedTowardsTarget);
        this.goalOwner.setAggressive(true);
        this.repathCooldown = 0;

        EntityLiving goalTarget = this.goalOwner.getGoalTarget();
        if (goalTarget != null) {
            this.oldTargetX = Double.MAX_VALUE;
            this.oldTargetY = Double.MAX_VALUE;
            this.oldTargetZ = Double.MAX_VALUE;
        }
    }
    
    @Override
    public void d() {
        EntityLiving goalTarget = this.goalOwner.getGoalTarget();
        if (goalTarget != null && (goalTarget.isSpectator() || goalTarget.isInvulnerable())) {
            this.goalOwner.setGoalTarget(null);
        }

        this.goalOwner.setAggressive(false);
        this.goalOwner.getNavigation().o(); // clearPath()
    }

    @Override
    public void e() {
        this.remainingAttackCooldown--;

        EntityLiving goalTarget = this.goalOwner.getGoalTarget();
        if (goalTarget == null) {
            return;
        }

        this.goalOwner.getControllerLook().a(goalTarget, 30.0F, 30.0F);
        double distSqToGoalTarget = NMSUtil.distSq(this.goalOwner, goalTarget, false); // todo make sure no ignore y is fine; also comment on it?

        // repath to target
        this.repathCooldown = Math.max(this.repathCooldown - 1, 0);
        boolean shouldRepathToTarget =
                this.repathCooldown <= 0
                && (this.continuePathingIfNoLOS ? true : this.goalOwner.getEntitySenses().a(goalTarget))
                && (NMSUtil.distSq(goalTarget.locX(), goalTarget.locY(), goalTarget.locZ(),
                            this.oldTargetX, this.oldTargetY, this.oldTargetZ, false) >= 1.0
                    || this.goalOwner.getRandom().nextFloat() < 0.075F); // chance to repath if stationary target increased from 0.05 to 0.075
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

        // attack
        if (this.remainingAttackCooldown <= 0 && distSqToGoalTarget <= this.getAttackReachSq(goalTarget)) {
            this.remainingAttackCooldown = this.attackCooldown;
            this.goalOwner.swingHand(EnumHand.MAIN_HAND);
            this.goalOwner.attackEntity(goalTarget);
        }
    }

    protected double getAttackReachSq(EntityLiving goalTarget) {
        return Math.max(this.goalOwner.getWidth() * 2.0F * this.goalOwner.getWidth() * 2.0F + goalTarget.getWidth(),
                this.minAttackReach);
    }

    public void setAttackCooldown(int attackCooldown) {
        this.attackCooldown = attackCooldown;
    }

    public void setMinAttackReach(double minAttackReach) {
        this.minAttackReach = minAttackReach;
    }
}
