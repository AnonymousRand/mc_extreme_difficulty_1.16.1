package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.util.EntityFilter;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.event.entity.EntityTargetEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Random;
import java.util.function.Predicate;

public class CustomPathfinderGoalNearestAttackableTarget<S extends EntityInsentient & ICustomHostile, T extends EntityLiving>
        extends CustomPathfinderGoalTarget {

    protected final Class<T> targetClass;
    protected final int targetChance;
    protected T potentialTarget;
    protected Predicate<EntityLiving> extraPredicate;
    protected EntityFilter targetCondition;
    private static final Random random = new Random();

    public CustomPathfinderGoalNearestAttackableTarget(
            S goalOwner,
            Class<T> targetClass,
            boolean ignoreLOS,
            boolean ignoreY) {

        this(goalOwner, targetClass, 10, ignoreLOS, ignoreY, null);
    }

    public CustomPathfinderGoalNearestAttackableTarget(
            S goalOwner,
            Class<T> targetClass,
            int targetChance,
            boolean ignoreLOS,
            boolean ignoreY,
            @Nullable Predicate<EntityLiving> extraPredicate) {

        // passing in ignoreLOS and ignoreY affects CONTINUING to track a target (in b()/shouldContinueExecuting())
        super(goalOwner, false, ignoreLOS, ignoreY);
        this.targetClass = targetClass;
        this.targetChance = targetChance;
        this.extraPredicate = extraPredicate;
        // EntityFilter with ignoreLOS and ignoreY true affects INITIALLY finding a player target,
        // and always ignores invis/skulls (which is the main reason we don't just use vanilla
        // PathfinderGoalNearestAttackableTarget if ignoreYAndLOS is false) // todo verify difference
        // also note that this k() call happens in initPathfinder() in the super() constructor
        // before we are able to change FOLLOW_RANGE, thus we have to use getDetectionRange() instead
        this.targetCondition = new EntityFilter(this.k(), ignoreLOS, ignoreY, extraPredicate);
        this.a(EnumSet.of(PathfinderGoal.Type.TARGET));
    }

    @Override
    public boolean a() {
        if (this.targetChance > 0 && this.e.getRandom().nextInt(this.targetChance) != 0) {
            return false;
        } else {
            this.potentialTarget = this.findClosestPotentialTarget();
            return this.potentialTarget != null;
        }
    }

    // Overridden to periodically change target to the closest one (including y) to prevent strats like
    // one player leading mobs away from another at build height, taking advantage of ignoreY = true
    // todo test with offline multimc 2 player?
    @Override
    public boolean b() {
        boolean superRet = super.b();

        if (superRet && random.nextDouble() < 0.025 && this.potentialTarget != null) {
            T closestPotentialTarget = this.findClosestPotentialTargetNoIgnoreY();
            if (closestPotentialTarget != null
                    && closestPotentialTarget.getUniqueID() != this.potentialTarget.getUniqueID()) {
                this.potentialTarget = closestPotentialTarget;
            }
        }

        return superRet;
    }

    @Override
    public void c() {
        if (this.potentialTarget != null) {
            this.e.setGoalTarget(this.potentialTarget, this.potentialTarget instanceof EntityPlayer
                    ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY, true);
        }

        // automatically make sure target range is updated for predicate for those mobs that change their detection range // todo test eventually
        this.targetCondition.setDetectionRange(this.k());
        super.c();
    }

    protected T findClosestPotentialTarget() {
        if (this.targetClass == EntityHuman.class || this.targetClass == EntityPlayer.class) {
            return (T) NMSUtil.getClosestEntityFromList(this.e.getWorld().getPlayers(), this.targetCondition, this.e);
        } else {
            return NMSUtil.getClosestEntityWithinRange(this.targetClass, this.targetCondition,
                    this.e, this.k(), 4.0D, this.k());
        }
    }

    protected T findClosestPotentialTargetNoIgnoreY() {
        if (this.targetClass == EntityHuman.class || this.targetClass == EntityPlayer.class) {
            return (T) NMSUtil.getClosestEntityFromList(this.e.getWorld().getPlayers(),
                    new EntityFilter(this.k(), this.ignoreLOS, false, extraPredicate), this.e);
        } else {
            return NMSUtil.getClosestEntityWithinRange(this.targetClass,
                    new EntityFilter(this.k(), this.ignoreLOS, false, extraPredicate), this.e, this.k(), 4.0D, this.k());
        }
    }
}