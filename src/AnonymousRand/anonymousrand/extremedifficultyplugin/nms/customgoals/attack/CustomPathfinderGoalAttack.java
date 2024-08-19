package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.EntityFilter;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;

import java.util.EnumSet;

// todo uncomment once all have been converted
/**
 * The base attack goal in my rewrite of vanilla's attack goals.
 * <p></p>
 *
 * Some (imo) unnecessary details have been simplified for the sake of consistency and clean code. Attack and movement
 * have also been split up more clearly; the only reason they are not separate goals is that they must meet the same
 * conditions to execute.
 */
// todo split up attack and movement with mutexes going to movement; and test primilarily?
public abstract class CustomPathfinderGoalAttack<T extends EntityInsentient & ICustomHostile/* & IAttackLevelingMob*/>
        extends PathfinderGoal {

    protected final T goalOwner;
    /* Attack */
    protected int attackCooldown;
    protected int remainingAttackCooldown;
    /* Movement */
    protected double speedTowardsTarget;
    protected int repathCooldown;
    protected int remainingRepathCooldown;

    protected CustomPathfinderGoalAttack(T goalOwner, int attackCooldown, double speedTowardsTarget) {
        this.goalOwner = goalOwner;
        this.attackCooldown = attackCooldown;
        this.speedTowardsTarget = speedTowardsTarget;
        this.repathCooldown = 4 + this.goalOwner.getRandom().nextInt(7);
        this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
    }

    @Override
    public final boolean a() {
        return this.shouldExecuteAttack() && this.shouldExecuteMovement();
    }

    protected boolean shouldExecuteAttack() {
        if (EntityFilter.BASE.test(this.goalOwner, this.goalOwner.getGoalTarget())) {
            Bukkit.broadcastMessage("should execute attack");
        }
        return EntityFilter.BASE.test(this.goalOwner, this.goalOwner.getGoalTarget());
    }

    protected boolean shouldExecuteMovement() {
        return this.shouldExecuteAttack();
    }

    @Override
    public final boolean b() {
        return this.shouldContinueExecutingAttack() && this.shouldContinueExecutingMovement();
    }

    protected boolean shouldContinueExecutingAttack() {
        return this.shouldExecuteAttack();
    }

    protected boolean shouldContinueExecutingMovement() {
        return this.shouldExecuteMovement();
    }

    @Override
    public final void c() {
        this.startExecutingAttack();
        this.startExecutingMovement();
    }

    protected void startExecutingAttack() {
        this.remainingAttackCooldown = this.attackCooldown;
        this.goalOwner.setAggressive(true);
    }

    protected void startExecutingMovement() {
        this.remainingRepathCooldown = 0;
    }

    @Override
    public final void d() {
        this.stopExecutingAttack();
        this.stopExecutingMovement();
    }

    protected void stopExecutingAttack() {
        this.goalOwner.setAggressive(false);
    }

    protected void stopExecutingMovement() {
        this.goalOwner.getNavigation().o(); // clearPath()
    }

    @Override
    public final void e() {
        EntityLiving goalTarget = this.goalOwner.getGoalTarget();
        if (goalTarget == null) {
            return;
        }

        this.remainingAttackCooldown--;
        this.remainingRepathCooldown--;
        this.tickAttack(goalTarget);
        this.tickMovement(goalTarget);
    }

    protected void tickAttack(EntityLiving goalTarget) {
        if (this.remainingAttackCooldown <= 0) {
            if (this.checkAttack(goalTarget)) {
                // only reset cooldown if attack was successful
                this.remainingAttackCooldown = this.attackCooldown;
                // this.goalOwner.increaseAttacks(1); // todo uncomment eventually
                this.attack(goalTarget);
            }
        }
    }

    protected void tickMovement(EntityLiving goalTarget) {
        this.goalOwner.getControllerLook().a(goalTarget, 30.0F, 30.0F); // setLookPositionWithEntity(); faces target
    }

    protected boolean checkAttack(EntityLiving goalTarget) {
        return true;
    }

    protected abstract void attack(EntityLiving goalTarget);

    public void setAttackCooldown(int attackCooldown) {
        this.attackCooldown = attackCooldown;
    }

    public void setSpeedTowardsTarget(double speedTowardsTarget) {
        this.speedTowardsTarget = speedTowardsTarget;
    }
}
