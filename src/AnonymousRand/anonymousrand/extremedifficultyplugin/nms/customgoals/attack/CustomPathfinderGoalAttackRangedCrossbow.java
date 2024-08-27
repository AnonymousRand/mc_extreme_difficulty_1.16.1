package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import net.minecraft.server.v1_16_R1.*;

/**
 * A subclass of <code>CustomPathfinderGoalAttackRangedHandheld</code> that animates crossbow charging, analogous to
 * <code>PathfinderGoalRangedCrossbowAttack</code>.
 * <p></p>
 *
 * Movement behavior is not copied from its vanilla analog, but rather left unchanged from
 * <code>CustomPathfinderGoalAttackRanged</code>: move closer if there's no line of sight, and stand still otherwise.
 */
public class CustomPathfinderGoalAttackRangedCrossbow<T extends EntityInsentient & IRangedEntity & ICrossbow
        & ICustomHostile /* & IAttackLevelingMob*/> extends CustomPathfinderGoalAttackRangedHandheld<T> {

    public CustomPathfinderGoalAttackRangedCrossbow(T goalOwner, int attackCooldown) {
        super(goalOwner, Items.CROSSBOW, attackCooldown);
    }

    @Override
    public void d() {
        super.d();

        // uncharge crossbow
        this.goalOwner.b(false);                               // setCharging()
        ItemCrossbow.a(this.goalOwner.getActiveItem(), false); // setCrossbowCharged()
    }

    @Override
    public void e() {
        EntityLiving attackTarget = this.goalOwner.getGoalTarget();
        if (attackTarget == null) {
            return;
        }

        // animate crossbow: uncharged -> charging, charging -> charged, and charged -> uncharged states respectively
        if (this.remainingCooldownAttack == this.attackCooldown - 1) {
            this.goalOwner.c(ProjectileHelper.a(this.goalOwner, Items.CROSSBOW)); // setActiveHand()
            this.goalOwner.b(true);                                               // setCharging()
        } else if (this.remainingCooldownAttack == 1) {
            this.goalOwner.releaseActiveItem();                                   // stopActiveHand(); doesn't seem to do anything, but it's from vanilla so I'll keep it in case
            this.goalOwner.b(false);                                              // setCharging()
        } else if (this.remainingCooldownAttack == 0) {
            // getActiveItem() still seems to work despite releaseActiveItem() and literally setting it to
            // ItemStack.EMPTY; vanilla does this too in d() though so not that worried
            ItemCrossbow.a(this.goalOwner.getActiveItem(), false);                // setCrossbowCharged()
        }

        super.e();
    }

    @Override
    protected void attack(EntityLiving attackTarget) {
        this.goalOwner.a(attackTarget, 1.0F); // shoot()
    }
}
