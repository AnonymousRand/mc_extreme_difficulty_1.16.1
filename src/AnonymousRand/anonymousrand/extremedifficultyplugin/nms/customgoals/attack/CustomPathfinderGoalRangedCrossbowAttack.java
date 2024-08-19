package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import net.minecraft.server.v1_16_R1.*;

/**
 * A subclass of <code>CustomPathfinderGoalRangedHandheldAttack</code> that animates crossbow charging, analogous to
 * <code>PathfinderGoalRangedCrossbowAttack</code>.
 * <p></p>
 *
 * Movement behavior is not copied from its vanilla analog, but rather left unchanged from
 * <code>CustomPathfinderGoalRangedAttack</code>: move closer if there's no line of sight, and stand still otherwise.
 */
public class CustomPathfinderGoalRangedCrossbowAttack<T extends EntityInsentient & IRangedEntity & ICrossbow
        & ICustomHostile /* & IAttackLevelingMob*/> extends CustomPathfinderGoalRangedHandheldAttack<T> {

    public CustomPathfinderGoalRangedCrossbowAttack(T goalOwner, int attackCooldown) {
        this(goalOwner, attackCooldown, 1.0);
    }

    public CustomPathfinderGoalRangedCrossbowAttack(T goalOwner, int attackCooldown, double speedTowardsTarget) {
        super(goalOwner, Items.CROSSBOW, attackCooldown, speedTowardsTarget);
    }

    @Override
    protected void stopExecutingAttack() {
        super.stopExecutingAttack();

        // uncharge crossbow
        this.goalOwner.b(false);                               // setCharging()
        ItemCrossbow.a(this.goalOwner.getActiveItem(), false); // setCrossbowCharged()
    }

    @Override
    protected void tickAttack(EntityLiving goalTarget) {
        // animate crossbow: uncharged -> charging, charging -> charged, and charged -> uncharged states respectively
        if (this.remainingAttackCooldown == this.attackCooldown - 1) {
            this.goalOwner.c(ProjectileHelper.a(this.goalOwner, Items.CROSSBOW)); // setActiveHand()
            this.goalOwner.b(true);                                               // setCharging()
        } else if (this.remainingAttackCooldown == 1) {
            this.goalOwner.releaseActiveItem();                                   // stopActiveHand(); doesn't seem to do anything, but it's from vanilla so I'll keep it in case
            this.goalOwner.b(false);                                              // setCharging()
        } else if (this.remainingAttackCooldown == 0) {
            // getActiveItem() still seems to work despite releaseActiveItem() and literally setting it to
            // ItemStack.EMPTY; vanilla does this too in d() though so not that worried
            ItemCrossbow.a(this.goalOwner.getActiveItem(), false);                // setCrossbowCharged()
        }

        super.tickAttack(goalTarget);
    }

    @Override
    protected void attack(EntityLiving goalTarget) {
        this.goalOwner.a(goalTarget, 1.0F); // shoot()
    }
}
