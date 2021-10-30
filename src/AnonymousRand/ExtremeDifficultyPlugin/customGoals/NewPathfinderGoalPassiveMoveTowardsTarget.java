package AnonymousRand.ExtremeDifficultyPlugin.customGoals;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;

import java.util.EnumSet;

public class NewPathfinderGoalPassiveMoveTowardsTarget extends PathfinderGoal {

    protected final EntityInsentient entity;
    public EntityLiving targetEntity;
    private final float maxFollowDistance;

    public NewPathfinderGoalPassiveMoveTowardsTarget(EntityInsentient entitycreature, double d0, float maxFollowDistance) {
        this.entity = entitycreature;
        this.maxFollowDistance = maxFollowDistance;
        this.a(EnumSet.of(PathfinderGoal.Type.MOVE));
    }

    @Override
    public boolean a() {
        this.targetEntity = this.entity.getGoalTarget();

        if (this.targetEntity == null) {
            return false;
        } else if (this.targetEntity.h(this.entity) > (double)(this.maxFollowDistance * this.maxFollowDistance)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean b() {
        return !this.entity.getNavigation().m() && this.targetEntity.isAlive() && this.targetEntity.h((Entity)this.entity) < (double)(this.maxFollowDistance * this.maxFollowDistance);
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
