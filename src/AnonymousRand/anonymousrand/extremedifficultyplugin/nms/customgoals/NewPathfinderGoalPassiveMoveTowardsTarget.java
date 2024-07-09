package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import net.minecraft.server.v1_16_R1.*;

import java.util.EnumSet;

public class NewPathfinderGoalPassiveMoveTowardsTarget extends PathfinderGoal {

    private final EntityInsentient entity;
    private EntityLiving targetEntity;
    private final float maxFollowDistance;

    public NewPathfinderGoalPassiveMoveTowardsTarget(EntityInsentient entityCreature, float maxFollowDistance) {
        this.entity = entityCreature;
        this.maxFollowDistance = maxFollowDistance;
        this.a(EnumSet.of(PathfinderGoal.Type.MOVE));
    }

    @Override
    public boolean a() {
        this.targetEntity = this.entity.getGoalTarget();

        if (this.targetEntity == null) {
            return false;
        } else if (this.targetEntity.h(this.entity) > (double) (this.maxFollowDistance * this.maxFollowDistance)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean b() {
        return !this.entity.getNavigation().m() && this.targetEntity.isAlive() && this.targetEntity.h(this.entity) < (double) (this.maxFollowDistance * this.maxFollowDistance);
    }

    @Override
    public void d() {
        this.entity.getNavigation().n();
    }

    @Override
    public void e() {
        this.entity.getNavigation().a(targetEntity, this.entity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() * 2.0);
    }
}
