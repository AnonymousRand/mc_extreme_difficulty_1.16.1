package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.EntityFilter;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.event.entity.EntityTargetEvent;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public abstract class CustomPathfinderGoalTarget<T extends EntityInsentient & ICustomHostile> extends PathfinderGoal {

    protected static final EntityTargetEvent.TargetReason TARGET_REASON = EntityTargetEvent.TargetReason.UNKNOWN;
    protected final T goalOwner;
    protected final EntityFilter targetCondition;
    protected final EntityFilter targetConditionIgnoreLOS;
    protected final EntityFilter targetConditionNoIgnoreY;
    protected final boolean ignoreLOS;
    protected final boolean ignoreY;
    protected EntityLiving candidateTarget;

    protected CustomPathfinderGoalTarget(T goalOwner, boolean ignoreLOS, boolean ignoreY) {
        this(goalOwner, ignoreLOS, ignoreY, null);
    }

    protected CustomPathfinderGoalTarget(
            T goalOwner, boolean ignoreLOS, boolean ignoreY, @Nullable Predicate<EntityLiving> extraEntityPredicate) {
        this.goalOwner = goalOwner;
        // EntityFilter with ignoreLOS and ignoreY affects INITIALLY finding a player target
        // (also note that this constructor is called in initPathfinder() in the super() constructor of a mob
        // before we are able to change FOLLOW_RANGE, thus we have to use getDetectionRange() instead)
        this.targetCondition =
                new EntityFilter(this.getDetectionRange(), ignoreLOS, ignoreY, extraEntityPredicate);
        this.targetConditionIgnoreLOS =
                new EntityFilter(this.getDetectionRange(), true, ignoreY, extraEntityPredicate);
        this.targetConditionNoIgnoreY =
                new EntityFilter(this.getDetectionRange(), ignoreLOS, false, extraEntityPredicate);
        // ignoreLOS and ignoreY affects CONTINUING to track a target in b()/shouldContinueExecuting()
        this.ignoreLOS = ignoreLOS;
        this.ignoreY = ignoreY;
    }

    @Override // `shouldExecute()` 
    public boolean a() {
        this.candidateTarget = this.findNearestCandidateTarget(true);
        return this.targetCondition.test(this.goalOwner, this.candidateTarget);
    }

    @Override // `shouldContinueExecuting()` 
    public boolean b() {
        /* Mobs, even those not ignoring line of sight, will no longer lose targets by breaking line of sight. Thus, the
           only ways to shake off a mob without dying is to leave its range or have it retarget to a nearer option. */
        return this.targetConditionIgnoreLOS.test(this.goalOwner, this.goalOwner.getGoalTarget());
    }

    @Override // `startExecuting()` 
    public void c() {
        this.goalOwner.setGoalTarget(this.candidateTarget, this.getTargetReason(this.candidateTarget), true);
        // automatically make sure target range is updated for predicate for those mobs that change their detection range // todo test eventually
        this.targetCondition.setDetectionRange(this.getDetectionRange());
    }

    @Override // `resetTask()` 
    public void d() {
        this.goalOwner.setGoalTarget(null, EntityTargetEvent.TargetReason.FORGOT_TARGET, true);
    }

    @Nullable
    protected abstract EntityLiving findNearestCandidateTarget(boolean allowIgnoreY);

    protected abstract EntityTargetEvent.TargetReason getTargetReason(EntityLiving target);
    
    protected double getDetectionRange() {
        return this.goalOwner.getDetectionRange();
    }
}
