package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.EnumHand;
import net.minecraft.server.v1_16_R1.IRangedEntity;
import net.minecraft.server.v1_16_R1.Item;

/**
 * A subclass of <code>CustomPathfinderGoalRangedAttack</code> that makes sure the correct item is in the main hand to
 * attack, and that the animations of the hand holding the weapon are played correctly.
 */
// todo uncomment
public class CustomPathfinderGoalHandheldRangedAttack<T extends EntityInsentient & IRangedEntity
        & ICustomHostile /* & IAttackLevelingMob*/> extends CustomPathfinderGoalRangedAttack<T> {

    protected final Item attackingItemInMainHand;

    public CustomPathfinderGoalHandheldRangedAttack(T goalOwner, Item attackingItemInMainHand, int attackCooldown) {
        this(goalOwner, attackingItemInMainHand, 1.0, attackCooldown);
    }

    public CustomPathfinderGoalHandheldRangedAttack(
            T goalOwner,
            Item attackingItemInMainHand,
            double speedTowardsTarget,
            int attackCooldown) {
        super(goalOwner, speedTowardsTarget, attackCooldown);
        this.attackingItemInMainHand = attackingItemInMainHand;
    }

    @Override
    public boolean a() {
        return super.a() && this.correctItemInMainHand();
    }

    @Override
    public void c() {
        super.c();
        this.goalOwner.c(EnumHand.MAIN_HAND); // todo test this is from drowned; surely it works with skeletons?
    }

    @Override
    public void d() {
        super.d();
        this.goalOwner.clearActiveItem();
    }

    protected boolean correctItemInMainHand() {
        return this.goalOwner.getItemInMainHand().getItem() == this.attackingItemInMainHand;
    }
}
