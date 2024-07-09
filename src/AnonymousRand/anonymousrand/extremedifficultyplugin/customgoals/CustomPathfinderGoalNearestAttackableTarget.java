package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.util.CustomPathfinderGoalTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.util.CustomPathfinderTargetCondition;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.EntityUtils;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.event.entity.EntityTargetEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class CustomPathfinderGoalNearestAttackableTarget<S extends EntityInsentient & ICustomHostile, T extends EntityLiving>
        extends CustomPathfinderGoalTarget {

    protected final Class<T> targetClass;
    protected final int targetChance;
    protected T potentialTarget;
    protected CustomPathfinderTargetCondition targetCondition;

    public CustomPathfinderGoalNearestAttackableTarget(S goalOwner, Class<T> targetClass) {
        this(goalOwner, targetClass, 10, null);
    }

    public CustomPathfinderGoalNearestAttackableTarget(S goalOwner, Class<T> targetClass, int targetChance,
                                                       @Nullable Predicate<EntityLiving> targetPredicate) {
        // needSightToMaintainTarget (checkSight) is always false, meaning we never need sight to continue tracking a target as the goalTarget (checked in shouldContinueExecuting())
        super(goalOwner, false, false);
        this.targetClass = targetClass;
        this.targetChance = targetChance;
        this.targetCondition = new CustomPathfinderTargetCondition(this.k(), targetPredicate); // we use getDetectionRange() as changing FOLLOW_RANGE attribute doesn't work: this k() call happens in initPathfinder() in the super() constructor, before we are able to change FOLLOW_RANGE
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

        this.targetCondition.a(this.k()); // setDetectionRange(); automatically make sure target range is updated for predicate for those mobs that change their detection range
        super.c();
    }

    protected T findPotentialValidTarget() {
        if (this.targetClass == EntityPlayer.class) {
            return (T) this.findNearestPlayerIgnoreLosAndY(this.e.getWorld().getPlayers(), this.e);
        } else {
            return this.e.getWorld().b(this.targetClass, this.targetCondition, this.e, this.e.locX(),
                    this.e.getHeadY(), this.e.locZ(), this.getTargetableArea(this.k()));
        }
    }

    protected EntityHuman findNearestPlayerIgnoreLosAndY(List<? extends EntityHuman> entities, EntityLiving theOneWhoSeeks) {
        double minDistSq = Double.MAX_VALUE;
        EntityHuman target = null;

        for (EntityHuman entity : entities) {
            if (this.targetCondition.a(theOneWhoSeeks, entity)) {                 // ignores line of sight, invis, and skulls to initially find a target (because CustomPathfinderTargetCondition) // todo test invis/skulls
                double distSq = EntityUtils.getDistSqNoY(theOneWhoSeeks, entity); // ignores y-level

                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    target = entity;
                }
            }
        }

        return target;
    }

    protected AxisAlignedBB getTargetableArea(double detectionRange) {
        return this.e.getBoundingBox().grow(detectionRange, 4.0D, detectionRange);
    }
}