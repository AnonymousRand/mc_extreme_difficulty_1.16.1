package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.EntityFilter;
import com.sun.istack.internal.NotNull;
import net.minecraft.server.v1_16_R1.*;

import java.util.EnumSet;

// Entire class rewritten instead of inherited from PathfinderGoalMeleeAttack in order to apply our own logic
// without being too hacky or needing too much reflection (everything's private :/)
// Attack and movement also split up more clearly; you should never have to override a(), b(), c(), d(), or e() directly
// (the only reason they are not separate goals is that they must execute together/meet the same conditions to execute)
// todo uncomment once all have been converted
public abstract class CustomPathfinderGoalAttack<T extends EntityInsentient & ICustomHostile /* & IAttackLevelingMob*/>
        extends PathfinderGoal {

    // attack
    protected final T goalOwner;
    protected int attackCooldown;
    protected int remainingAttackCooldown;
    // movement
    protected double moveSpeed;

    protected CustomPathfinderGoalAttack(T goalOwner, int attackCooldown, double moveSpeed) {
        this.goalOwner = goalOwner;
        this.attackCooldown = attackCooldown;
        this.moveSpeed = moveSpeed;
        this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
    }

    @Override
    public boolean a() {
        return this.shouldExecuteAttack() && this.shouldExecuteMovement();
    }

    protected boolean shouldExecuteAttack() {
        return EntityFilter.BASE.test(this.goalOwner, this.goalOwner.getGoalTarget());
    }

    protected boolean shouldExecuteMovement() {
        return this.shouldExecuteAttack();
    }

    @Override
    public boolean b() {
        return this.shouldContinueExecutingAttack() && this.shouldContinueExecutingMovement();
    }

    protected boolean shouldContinueExecutingAttack() {
        return this.shouldExecuteAttack();
    }

    protected boolean shouldContinueExecutingMovement() {
        return this.shouldExecuteMovement();
    }

    @Override
    public void c() {
        this.startExecutingAttack();
        this.startExecutingMovement();
    }

    protected void startExecutingAttack() {
        this.remainingAttackCooldown = this.attackCooldown;
        this.goalOwner.setAggressive(true);
    }

    protected void startExecutingMovement() {}

    @Override
    public void d() {
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
    public void e() {
        EntityLiving target = this.goalOwner.getGoalTarget();
        if (target == null) {
            return;
        }

        this.tickAttack(target);
        this.tickMovement(target);
    }

    protected void tickAttack(@NotNull EntityLiving target) {
        this.remainingAttackCooldown--;

        if (this.remainingAttackCooldown <= 0) {
            if (this.checkAttack(target)) {
                this.remainingAttackCooldown = this.attackCooldown;
                // this.goalOwner.increaseAttacks(1); // todo uncomment eventually
                this.attack(target);
            }
        }
    }

    protected void tickMovement(@NotNull EntityLiving target) {
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
