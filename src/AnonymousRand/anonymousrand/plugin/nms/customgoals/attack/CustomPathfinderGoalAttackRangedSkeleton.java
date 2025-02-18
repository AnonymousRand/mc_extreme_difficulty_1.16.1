package AnonymousRand.anonymousrand.plugin.nms.customgoals.attack;

import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.ICustomHostile;
import net.minecraft.server.v1_16_R1.*;

// originally PathfinderGoalBowShoot
/**
 * A subclass of <code>CustomPathfinderGoalAttackRangedHandheld</code> that handle bow mechanics, analogous to
 * <code>PathfinderGoalBowShoot</code>.
 */
public class CustomPathfinderGoalAttackRangedSkeleton<T extends EntityInsentient & IRangedEntity
        & ICustomHostile /* & IAttackLevelingMob*/> extends CustomPathfinderGoalAttackRangedHandheld<T> {

    public CustomPathfinderGoalAttackRangedSkeleton(T goalOwner, int attackCooldown) {
        super(goalOwner, Items.BOW, attackCooldown);
    }

    @Override
    protected boolean checkAttack(EntityLiving attackTarget) {
        return this.goalOwner.isHandRaised();
    }

    @Override
    protected void attack(EntityLiving attackTarget) {
        // ItemBow.a() gets the attack power for a corresponding charge of the bow in ticks (we are manually setting it
        // to the normal 20 here to allow rapid fire, to mimic having charged the bow for the full 20 ticks already)
        this.goalOwner.a(attackTarget, ItemBow.a(20)); // shoot()
    }
}
