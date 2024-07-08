package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.util.CustomIEntityAccess;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityTargetEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

public class CustomPathfinderGoalNearestAttackableTarget<T extends EntityLiving> extends CustomPathfinderGoalTarget implements CustomIEntityAccess {
    protected final Class<T> targetClass;
    protected final int targetChance;
    protected EntityLiving potentialTarget;
    protected CustomPathfinderTargetCondition targetCondition;

    public CustomPathfinderGoalNearestAttackableTarget(EntityInsentient goalOwner, Class<T> targetClass) {
        this(goalOwner, targetClass, 10, null);
    }

    public CustomPathfinderGoalNearestAttackableTarget(EntityInsentient goalOwner, Class<T> targetClass, Predicate<EntityLiving> targetPredicate) {
        this(goalOwner, targetClass, 10, targetPredicate);
    }

    public CustomPathfinderGoalNearestAttackableTarget(EntityInsentient goalOwner, Class<T> targetClass, CustomPathfinderTargetCondition targetCondition) {
        this(goalOwner, targetClass, 10, null);
        this.targetCondition = targetCondition;
    }

    public CustomPathfinderGoalNearestAttackableTarget(EntityInsentient goalOwner, Class<T> targetClass, int targetChance, @Nullable Predicate<EntityLiving> targetPredicate) {
        super(goalOwner, false, false); // checkSight/needSightToMaintainTarget is always false, meaning we never need sight to continue tracking a target as the goalTarget (checked in shouldContinueExecuting())
        this.targetClass = targetClass;
        this.targetChance = targetChance;
        this.targetCondition = (new CustomPathfinderTargetCondition()).a(this.k()).a(targetPredicate); // todo replace this.k() with just this.entityinsenitnet.getfollowrange() after mandating entityinsentient be an icustomhosile.
        this.a(EnumSet.of(PathfinderGoal.Type.TARGET));
}

    public void updateDetectionRange() {
        this.targetCondition.a(this.k());
    }

    @Override
    public boolean a() {
        if (this.targetChance > 0 && this.e.getRandom().nextInt(this.targetChance) != 0) {
            return false;
        } else {
            this.findPotentialTarget();
            return this.potentialTarget != null;
        }
    }

    @Override
    public void c() {
        if (this.potentialTarget != null) {
            this.e.setGoalTarget(this.potentialTarget, this.potentialTarget instanceof EntityPlayer
                    ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY, true);
        }
        super.c();
    }

    // overrides a() (getTargetableArea()); name change because not used elsewhere
    protected AxisAlignedBB getTargetableArea(double detectionRange) {
        return this.e.getBoundingBox().grow(detectionRange, 4.0D, detectionRange);
    }

    // overrides a(); name change because not used elsewhere
    protected void setPotentialTarget(@Nullable EntityLiving entityLiving) {
        this.potentialTarget = entityLiving;
    }

    // overrides g() (findNearestTarget()); name change because not used meaningfully elsewhere
    protected void findPotentialTarget() {
        if (this.targetClass == EntityHuman.class || this.targetClass == EntityPlayer.class) {
            // passes to CustomPathfinderGoalNearestAttackableTarget.g()
            // which passes to CustomIEntityAccess.customFindPlayer()
            // which passes to CustomIEntityAccess.customFindEntity()
            // which passes to CustomPathfinderTargetConditions.a()
            // which removes line of sight requirement for initially finding targets
            this.potentialTarget = this.customFindPlayers(this.targetCondition, this.e, this.e.locX(), this.e.getHeadY(), this.e.locZ());
        } else {
            this.potentialTarget = this.e.getWorld().b(this.targetClass, this.targetCondition, this.e, this.e.locX(), this.e.getHeadY(), this.e.locZ(), this.getTargetableArea(this.k()));
        }
    }
}