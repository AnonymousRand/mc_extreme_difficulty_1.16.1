package AnonymousRand.ExtremeDifficultyPlugin.customGoals;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;

import java.util.EnumSet;

public class CustomPathfinderGoalPassiveMoveTowardsTarget extends PathfinderGoal {

    private final EntityInsentient entity;
    private EntityLiving targetEntity;
    private double c;
    private double d;
    private double e;
    private final double f;
    private final float maxFollowDistance;

    public CustomPathfinderGoalPassiveMoveTowardsTarget(EntityInsentient entitycreature, double d0, float maxFollowDistance) {
        this.entity = entitycreature;
        this.f = d0;
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
        if (this.entity.getNavigation().m()) {
            this.entity.getNavigation().a(targetEntity, this.entity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue());
        }
    }
}
