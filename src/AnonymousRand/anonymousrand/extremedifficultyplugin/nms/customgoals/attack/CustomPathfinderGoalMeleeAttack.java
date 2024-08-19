package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NmsUtil;
import net.minecraft.server.v1_16_R1.*;

/**
 * The base melee attack goal in my rewrite of vanilla's attack goals, analogous to
 * <code>PathfinderGoalMeleeAttack</code>.
 * <p></p>
 *
 * Melee attacks don't ignore y-level even if the targeting goal does, so make sure those mobs can change
 * their y-level to get within melee range of their targets.
 */
public class CustomPathfinderGoalMeleeAttack<T extends EntityInsentient & ICustomHostile/* & IAttackLevelingMob*/>
        extends CustomPathfinderGoalAttack<T> {

    protected double minAttackReach;

    public CustomPathfinderGoalMeleeAttack(T goalOwner) {
        this(goalOwner, 20);
    }

    public CustomPathfinderGoalMeleeAttack(T goalOwner, int attackCooldown) {
        /* Default minimum attack reach is 2.0 blocks to help baby zombies out */
        this(goalOwner, attackCooldown, 2.0);
    }

    public CustomPathfinderGoalMeleeAttack(T goalOwner, int attackCooldown, double minAttackReach) {
        super(goalOwner, attackCooldown);
        this.minAttackReach = minAttackReach;
    }

    @Override
    protected boolean checkAttack(EntityLiving goalTarget) {
        return NmsUtil.distSq(this.goalOwner, goalTarget, false) <= this.getAttackReachSq(goalTarget);
    }

    @Override
    protected void attack(EntityLiving goalTarget) {
        this.goalOwner.swingHand(EnumHand.MAIN_HAND);
        this.goalOwner.attackEntity(goalTarget);
    }

    protected double getAttackReachSq(EntityLiving target) {
        return Math.max(
                Math.pow(2.0F * this.goalOwner.getWidth(), 2) + target.getWidth(), this.minAttackReach);
    }

    public void setMinAttackReach(double minAttackReach) {
        this.minAttackReach = minAttackReach;
    }
}
