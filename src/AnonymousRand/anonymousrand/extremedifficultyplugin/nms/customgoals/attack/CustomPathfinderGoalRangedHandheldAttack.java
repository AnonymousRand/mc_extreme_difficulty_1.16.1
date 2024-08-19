package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.EnumHand;
import net.minecraft.server.v1_16_R1.IRangedEntity;
import net.minecraft.server.v1_16_R1.Item;

/**
 * A subclass of <code>CustomPathfinderGoalRangedAttack</code> that makes sure the correct item is in the main hand to
 * attack, and that the animations of the hand holding the weapon are played correctly.
 * <p></p>
 *
 * This class has no vanilla analog, but its logic is based off of drowned's trident attack goal (class <code>f</code>).
 */
public class CustomPathfinderGoalRangedHandheldAttack<T extends EntityInsentient & IRangedEntity
        & ICustomHostile /* & IAttackLevelingMob*/> extends CustomPathfinderGoalRangedAttack<T> {

    protected final Item weapon;

    public CustomPathfinderGoalRangedHandheldAttack(T goalOwner, Item weapon, int attackCooldown) {
        super(goalOwner, attackCooldown);
        this.weapon = weapon;
    }

    @Override
    public boolean a() {
        return super.a() && this.correctItemInMainHand();
    }

    @Override
    public void c() {
        super.c();
        this.goalOwner.c(EnumHand.MAIN_HAND); // setActiveHand()
    }

    @Override
    public void d() {
        super.d();
        this.goalOwner.clearActiveItem();
    }

    protected boolean correctItemInMainHand() {
        return this.goalOwner.getItemInMainHand().getItem() == this.weapon;
    }
}
