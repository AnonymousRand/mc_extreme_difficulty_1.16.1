package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.movement;

import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.Item;

public class CustomPathfinderGoalRangedHandheldMovement<T extends EntityInsentient>
        extends CustomPathfinderGoalRangedMovement<T> {

    protected final Item weapon;

    public CustomPathfinderGoalRangedHandheldMovement(T goalOwner, Item weapon) {
        this(goalOwner, weapon, 1.0);
    }

    public CustomPathfinderGoalRangedHandheldMovement(T goalOwner, Item weapon, double speedTowardsTarget) {
        super(goalOwner, speedTowardsTarget);
        this.weapon = weapon;
    }

    @Override
    public boolean a() {
        return super.a() && this.correctItemInMainHand();
    }

    protected boolean correctItemInMainHand() {
        return this.goalOwner.getItemInMainHand().getItem() == this.weapon;
    }
}
