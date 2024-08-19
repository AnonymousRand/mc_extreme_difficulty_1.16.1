package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NmsUtil;
import net.minecraft.server.v1_16_R1.*;

/**
 * The base ranged attack goal in my rewrite of vanilla's attack goals, analogous to
 * <code>PathfinderGoalArrowAttack</code>.
 */
public class CustomPathfinderGoalAttackRanged<T extends EntityInsentient & IRangedEntity
        & ICustomHostile/* & IAttackLevelingMob*/> extends CustomPathfinderGoalAttack<T> {

    public CustomPathfinderGoalAttackRanged(T goalOwner, int attackCooldown) {
        super(goalOwner, attackCooldown);
    }

    @Override
    protected void attack(EntityLiving goalTarget) {
        float distFactor = (float) MathHelper.a(NmsUtil.dist(this.goalOwner, goalTarget, false)
                / this.goalOwner.getDetectionRange(), 0.1, 1.0);
        this.goalOwner.a(goalTarget, distFactor); // `shoot()`
    }
}
