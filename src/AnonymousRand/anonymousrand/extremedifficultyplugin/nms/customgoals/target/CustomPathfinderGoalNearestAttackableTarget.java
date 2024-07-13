package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.EntityFilter;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.event.entity.EntityTargetEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Random;
import java.util.function.Predicate;

// All the line of sight/y-level checking vanillaly in the melee/ranged attack goals has been moved here
// so those goals only have to deal with, well, attack
public class CustomPathfinderGoalNearestAttackableTarget<S extends EntityLiving,
        T extends EntityInsentient & ICustomHostile> extends CustomPathfinderGoalTarget<T> {

    protected final Class<S> targetClass;
    protected final int targetChance;
    private static final Random random = new Random();

    public CustomPathfinderGoalNearestAttackableTarget(
            T goalOwner,
            Class<S> targetClass) {

        this(goalOwner, targetClass, goalOwner.ignoresLOS(), goalOwner.ignoresY(), 10, null);
    }

    public CustomPathfinderGoalNearestAttackableTarget(
            T goalOwner,
            Class<S> targetClass,
            boolean ignoreLOS,
            boolean ignoreY) {

        this(goalOwner, targetClass, ignoreLOS, ignoreY, 10, null);
    }

    public CustomPathfinderGoalNearestAttackableTarget(
            T goalOwner,
            Class<S> targetClass,
            boolean ignoreLOS,
            boolean ignoreY,
            @Nullable Predicate<EntityLiving> extraEntityPredicate) {

        this(goalOwner, targetClass, ignoreLOS, ignoreY, 10, extraEntityPredicate);
    }

    public CustomPathfinderGoalNearestAttackableTarget(
            T goalOwner,
            Class<S> targetClass,
            boolean ignoreLOS,
            boolean ignoreY,
            int targetChance,
            @Nullable Predicate<EntityLiving> extraEntityPredicate) {

        super(goalOwner, ignoreLOS, ignoreY, extraEntityPredicate);
        this.targetClass = targetClass;
        this.targetChance = targetChance;
        this.a(EnumSet.of(PathfinderGoal.Type.TARGET));
    }

    // todo test priority: make sure hurt by is not overriden by this goal
    @Override
    public boolean a() {
        if (this.goalOwner.getRandom().nextInt(this.targetChance) != 0) {
            return false;
        }
        return super.a();
    }

    // Overridden to periodically change target to the nearest one (including y) to prevent strats like
    // one player leading mobs away from another at build height, taking advantage of ignoreY = true
    // todo test with offline multimc 2 player? also test if this overrides hurtby/enderman look etc
    @Override
    public boolean b() {
        if (!super.b()) {
            return false;
        }

        EntityLiving target = this.goalOwner.getGoalTarget();
        if (random.nextDouble() < 0.025 && target != null) {
            S nearestPotentialTarget = this.findNearestPotentialTarget(false);
            if (nearestPotentialTarget != null
                    && nearestPotentialTarget.getUniqueID() != target.getUniqueID()) {
                this.goalOwner.setGoalTarget(nearestPotentialTarget);
            }
        }

        return true;
    }

    @Override
    protected S findNearestPotentialTarget(boolean allowIgnoreY) {
        EntityFilter targetCondition; // I'm sure there's a cleaner way but I can't think of it at the moment
        if (allowIgnoreY) {
            targetCondition = this.targetCondition;
        } else {
            targetCondition = this.targetConditionNoIgnoreY;
        }

        if (this.targetClass == EntityHuman.class || this.targetClass == EntityPlayer.class) {
            return (S) NMSUtil.getNearestEntityFromList(this.goalOwner.getWorld().getPlayers(), targetCondition,
                    this.goalOwner);
        } else {
            return NMSUtil.getNearestEntityWithinRange(this.targetClass, targetCondition, this.goalOwner,
                    this.getDetectionRange(), 4.0D, this.getDetectionRange());
        }
    }

    @Override
    protected EntityTargetEvent.TargetReason getTargetReason(EntityLiving target) {
        return target instanceof EntityPlayer
                ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY;
    }
}
