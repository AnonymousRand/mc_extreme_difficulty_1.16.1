package AnonymousRand.anonymousrand.plugin.nms.customgoals.attack;

import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.plugin.util.EntityFilter;
import net.minecraft.server.v1_16_R1.*;

/**
 * The base attack goal in my rewrite of vanilla's attack goals.
 * <p></p>
 *
 * Some (imo) unnecessary details have been simplified for the sake of consistency and clean code. Attack and attackmvmt
 * have also been split up into separate goals for separation of concerns & easier overriding.
 */
public abstract class CustomPathfinderGoalAttack<T extends EntityInsentient & ICustomHostile/* & IAttackLevelingMob*/>
        extends PathfinderGoal {

    protected final T goalOwner;
    protected int attackCooldown;
    protected int remainingCooldownAttack;

    protected CustomPathfinderGoalAttack(T goalOwner, int attackCooldown) {
        this.goalOwner = goalOwner;
        this.attackCooldown = attackCooldown;
    }

    @Override
    public boolean a() {
        return EntityFilter.BASE.test(this.goalOwner, this.goalOwner.getGoalTarget());
    }

    @Override
    public boolean b() {
        return this.a();
    }

    @Override
    public void c() {
        this.remainingCooldownAttack = this.attackCooldown;
        this.goalOwner.setAggressive(true);
    }

    @Override
    public void d() {
        this.goalOwner.setAggressive(false);
    }

    @Override
    public void e() {
        EntityLiving attackTarget = this.goalOwner.getGoalTarget();
        if (attackTarget == null) {
            return;
        }

        this.remainingCooldownAttack--;
        if (this.remainingCooldownAttack <= 0) {
            if (this.checkAttack(attackTarget)) {
                // only reset cooldown if attack was successful
                this.remainingCooldownAttack = this.attackCooldown;
                // this.goalOwner.increaseAttacks(1); // todo uncomment eventually
                this.attack(attackTarget);
            }
        }
    }

    protected boolean checkAttack(EntityLiving attackTarget) {
        return true;
    }

    protected abstract void attack(EntityLiving attackTarget);

    public void setAttackCooldown(int attackCooldown) {
        this.attackCooldown = attackCooldown;
    }
}
