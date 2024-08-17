package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.EntityFilter;
import net.minecraft.server.v1_16_R1.*;

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
public abstract class CustomPathfinderGoalAttack<T extends EntityInsentient & ICustomHostile/* & IAttackLevelingMob*/>
        extends PathfinderGoal {

    protected final T goalOwner;
    /* Attack */
    protected int attackCooldown;
    protected int remainingAttackCooldown;
    /* Movement */
    protected double moveSpeed;
    protected int repathCooldown;
    protected int remainingRepathCooldown;

    protected CustomPathfinderGoalAttack(T goalOwner, int attackCooldown, double moveSpeed) {
        this.goalOwner = goalOwner;
        this.attackCooldown = attackCooldown;
        this.moveSpeed = moveSpeed;
        this.repathCooldown = 4 + this.goalOwner.getRandom().nextInt(7);
        this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
    }

    @Override
    public final boolean a() {
        return this.shouldExecuteAttack() && this.shouldExecuteMovement();
    }

    protected boolean shouldExecuteAttack() {
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

    protected void tickAttack(EntityLiving target) {
        if (this.remainingAttackCooldown <= 0) {
            if (this.checkAttack(target)) {
                // only reset cooldown if attack was successful
                this.remainingAttackCooldown = this.attackCooldown;
                // this.goalOwner.increaseAttacks(1); // todo uncomment eventually
                this.attack(target);
            }
        }
    }

    protected void tickMovement(EntityLiving target) {
        this.goalOwner.getControllerLook().a(target, 30.0F, 30.0F); // setLookPositionWithEntity(); faces target
    }

    protected boolean checkAttack(EntityLiving target) {
        return true;
    }

    protected abstract void attack(EntityLiving target);

    public void setAttackCooldown(int attackCooldown) {
        this.attackCooldown = attackCooldown;
    }

    public void setMoveSpeed(double moveSpeed) {
        this.moveSpeed = moveSpeed;
    }
}
