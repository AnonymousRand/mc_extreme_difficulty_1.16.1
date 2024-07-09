package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.util.EntityFilter;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityTargetEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

public class CustomPathfinderGoalNearestAttackableTarget<S extends EntityInsentient & ICustomHostile, T extends EntityLiving>
        extends CustomPathfinderGoalTarget {

    protected final Class<T> targetClass;
    protected final int targetChance;
    protected T potentialTarget;
    protected EntityFilter targetCondition;

    public CustomPathfinderGoalNearestAttackableTarget(S goalOwner, Class<T> targetClass) {
        this(goalOwner, targetClass, 10, null);
    }

    public CustomPathfinderGoalNearestAttackableTarget(
            S goalOwner,
            Class<T> targetClass,
            int targetChance,
            @Nullable Predicate<EntityLiving> additionalPredicate) {

        // needSightToMaintainTarget (checkSight) is always false, meaning we never need sight to continue tracking a target as the goalTarget (checked in shouldContinueExecuting())
        super(goalOwner, false, false);
        this.targetClass = targetClass;
        this.targetChance = targetChance;
        // EntityFilter means we ignore y-level, line of sight, invis, and skulls to initially find a player target // todo test invis/skulls
        this.targetCondition = new EntityFilter(this.k(), // this k() call happens in initPathfinder() in the super() constructor before we are able to change FOLLOW_RANGE, thus we use getDetectionRange() instead
                targetClass == EntityHuman.class || targetClass == EntityPlayer.class, additionalPredicate);
        this.a(EnumSet.of(PathfinderGoal.Type.TARGET));
    }

    @Override
    public boolean a() {
        if (this.targetChance > 0 && this.e.getRandom().nextInt(this.targetChance) != 0) {
            return false;
        } else {
            this.potentialTarget = this.findPotentialValidTarget();
            return this.potentialTarget != null;
        }
    }

    @Override
    public void c() {
        if (this.potentialTarget != null) {
            this.e.setGoalTarget(this.potentialTarget, this.potentialTarget instanceof EntityPlayer
                    ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY, true);
        }

        this.targetCondition.setDetectionRange(this.k()); // automatically make sure target range is updated for predicate for those mobs that change their detection range // todo test eventually
        super.c();
    }

    protected T findPotentialValidTarget() {
        if (this.targetClass == EntityHuman.class || this.targetClass == EntityPlayer.class) {
            return (T) NMSUtil.getClosestEntityFromList(this.e.getWorld().getPlayers(), this.targetCondition, this.e);
        } else {
            return NMSUtil.getClosestEntityWithinRange(this.targetClass, this.targetCondition,
                    this.e, this.k(), 4.0D, this.k());
        }
    }
}
