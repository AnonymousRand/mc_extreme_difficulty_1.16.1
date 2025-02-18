package AnonymousRand.anonymousrand.plugin.nms.customgoals.attack;

import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.plugin.util.NmsUtil;
import net.minecraft.server.v1_16_R1.*;

/**
 * The base melee attack goal in my rewrite of vanilla's attack goals, analogous to
 * <code>PathfinderGoalMeleeAttack</code>.
 * <p></p>
 *
 * Melee attacks don't ignore y-level even if the targeting goal does, so make sure those mobs can change
 * their y-level to get within melee range of their targets.
 */
public class CustomPathfinderGoalAttackMelee<T extends EntityInsentient & ICustomHostile/* & IAttackLevelingMob*/>
        extends CustomPathfinderGoalAttack<T> {

    protected double minAttackReach;

    public CustomPathfinderGoalAttackMelee(T goalOwner) {
        this(goalOwner, 20);
    }

    public CustomPathfinderGoalAttackMelee(T goalOwner, int attackCooldown) {
        // default minimum attack reach is 2.0 blocks to help baby zombies out
        this(goalOwner, attackCooldown, 2.0);
    }

    public CustomPathfinderGoalAttackMelee(T goalOwner, int attackCooldown, double minAttackReach) {
        super(goalOwner, attackCooldown);
        this.minAttackReach = minAttackReach;
    }

    @Override
    protected boolean checkAttack(EntityLiving attackTarget) {
        return NmsUtil.distSq(this.goalOwner, attackTarget, false) <= this.getAttackReachSq(attackTarget);
    }

    @Override
    protected void attack(EntityLiving attackTarget) {
        this.goalOwner.swingHand(EnumHand.MAIN_HAND);
        this.goalOwner.attackEntity(attackTarget);
    }

    protected double getAttackReachSq(EntityLiving target) {
        return Math.max(
                Math.pow(2.0F * this.goalOwner.getWidth(), 2) + target.getWidth(), this.minAttackReach);
    }

    public void setMinAttackReach(double minAttackReach) {
        this.minAttackReach = minAttackReach;
    }
}
