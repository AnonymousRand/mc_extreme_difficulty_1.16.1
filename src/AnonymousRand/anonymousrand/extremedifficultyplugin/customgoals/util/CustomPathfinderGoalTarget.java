package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.util;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.ICustomHostile;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.PathfinderGoalTarget;

public abstract class CustomPathfinderGoalTarget extends PathfinderGoalTarget {

    public CustomPathfinderGoalTarget(EntityInsentient goalOwner, boolean needSightToMaintainTarget, boolean nearbyOnly) {
        super(goalOwner, needSightToMaintainTarget, nearbyOnly);
    }

    @Override // getDetectionRange(); overridden here for both subclasses CustomPathfinderGoalNearestAttackableTarget and CustomPathfinderGoalHurtByTarget to use
    protected double k() {
        return ((ICustomHostile) this.e).getDetectionRange();
    }
}