package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
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
    protected boolean checkAttack(EntityLiving goalTarget) {
        return this.goalOwner.isHandRaised();
    }

    @Override
    protected void attack(EntityLiving goalTarget) {
        // ItemBow.a() gets the attack power for a corresponding charge of the bow in ticks (we are manually setting it
        // to the normal 20 here to allow rapid fire, to mimic having charged the bow for the full 20 ticks already)
        this.goalOwner.a(goalTarget, ItemBow.a(20)); // shoot()
    }
}
