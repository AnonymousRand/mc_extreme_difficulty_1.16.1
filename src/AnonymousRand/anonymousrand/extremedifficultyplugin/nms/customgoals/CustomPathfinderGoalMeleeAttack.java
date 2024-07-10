package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;

import java.util.EnumSet;
import java.util.Random;

// Entire class rewritten instead of inherited from PathfinderGoalMeleeAttack in order to apply our own logic
// without being too hacky or needing too much reflection (everything's private :/)
public class CustomPathfinderGoalMeleeAttack extends PathfinderGoal {

    protected final EntityInsentient goalOwner;
    protected final double speedTowardsTarget;
    protected final boolean continuePathingIfNoLOS;
    protected PathEntity path;
    protected double oldTargetX;
    protected double oldTargetY;
    protected double oldTargetZ;
    protected int repathCooldown;
    protected int attackCooldown;
    protected int remainingAttackCooldown;

    public CustomPathfinderGoalMeleeAttack(
            EntityInsentient goalOwner,
            double speedTowardsTarget,
            boolean continuePathingIfNoLOS) {

        this(goalOwner, speedTowardsTarget, continuePathingIfNoLOS, 20);
    }

    public CustomPathfinderGoalMeleeAttack(
            EntityInsentient goalOwner,
            double speedTowardsTarget,
            boolean continuePathingIfNoLOS,
            int attackCooldown) {

        this.goalOwner = goalOwner;
        this.speedTowardsTarget = speedTowardsTarget;
        this.continuePathingIfNoLOS = continuePathingIfNoLOS;
        this.attackCooldown = attackCooldown;
        this.remainingAttackCooldown = attackCooldown;
        this.a(EnumSet.of(Type.MOVE, Type.LOOK));
    }

    @Override
    public boolean a() { // todo make sure this is called every tick! for remainingattackcooldown--
        this.remainingAttackCooldown--;
        if (this.remainingAttackCooldown > 0) {
            return false;
        }

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
        this.remainingAttackCooldown = this.attackCooldown;

        EntityLiving goalTarget = this.goalOwner.getGoalTarget();
        if (goalTarget != null) {
            this.oldTargetX = goalTarget.locX();
            this.oldTargetY = goalTarget.locY();
            this.oldTargetZ = goalTarget.locZ();
        }
    }
    
    @Override
    public void d() {
        EntityLiving goalTarget = this.goalOwner.getGoalTarget();
        if (goalTarget != null && (goalTarget.isSpectator() || goalTarget.isInvulnerable())) {
            this.goalOwner.setGoalTarget(null);
        }

        this.goalOwner.setAggressive(false);
        this.goalOwner.getNavigation().o();
    }

    @Override
    public void e() {
        EntityLiving goalTarget = this.goalOwner.getGoalTarget();
        if (goalTarget == null) {
            return;
        }

        this.goalOwner.getControllerLook().a(goalTarget, 30.0F, 30.0F);
        double distSqToGoalTarget = NMSUtil.distSq(this.goalOwner, goalTarget, true);

        this.repathCooldown = Math.max(this.repathCooldown - 1, 0);
        boolean shouldRepathToTarget =
                (this.continuePathingIfNoLOS || this.goalOwner.getEntitySenses().a(goalTarget))
                && this.repathCooldown <= 0
                && (NMSUtil.distSq(goalTarget.locX(), goalTarget.locY(), goalTarget.locZ(),
                            this.oldTargetX, this.oldTargetY, this.oldTargetZ, true) >= 1.0
                    || this.goalOwner.getRandom().nextFloat() < 0.05F);
        if (shouldRepathToTarget) { /* no longer requires line of sight to continue attacking */
            this.oldTargetX = goalTarget.locX();
            this.oldTargetY = goalTarget.locY();
            this.oldTargetZ = goalTarget.locZ();
            this.repathCooldown = 4 + this.goalOwner.getRandom().nextInt(7);
            if (distSqToGoalTarget > 1024.0) {
                this.repathCooldown += 10;
            } else if (distSqToGoalTarget > 256.0) {
                this.repathCooldown += 5;
            }

            if (!this.goalOwner.getNavigation().a(goalTarget, this.speedTowardsTarget)) {
                this.repathCooldown += 15;
            }
        }

        if (distSqToGoalTarget <= this.getAttackReachSq(goalTarget)) {
            this.goalOwner.swingHand(EnumHand.MAIN_HAND);
            this.goalOwner.attackEntity(goalTarget);
        }
    }

    protected void setAttackCooldown(int attackCooldown) {
        this.attackCooldown = attackCooldown;
    }

    protected double getAttackReachSq(EntityLiving goalTarget) {
        return this.goalOwner.getWidth() * 2.0F * this.goalOwner.getWidth() * 2.0F + goalTarget.getWidth();
    }
}
