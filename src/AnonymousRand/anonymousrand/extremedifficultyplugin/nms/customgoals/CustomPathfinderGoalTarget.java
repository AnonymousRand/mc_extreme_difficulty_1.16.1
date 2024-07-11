package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.util.EntityFilter;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityTargetEvent;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public abstract class CustomPathfinderGoalTarget<T extends EntityInsentient & ICustomHostile> extends PathfinderGoal {

    protected final T goalOwner;
    protected final EntityFilter targetCondition;
    protected final EntityFilter targetConditionIgnoreLOS;
    protected final EntityFilter targetConditionNoIgnoreY;
    protected final boolean ignoreLOS;
    protected final boolean ignoreY;
    protected final int unseenTicksToForget;
    protected EntityLiving potentialTarget;
    protected int targetUnseenTicks;

    public CustomPathfinderGoalTarget(
            T goalOwner,
            boolean ignoreLOS,
            boolean ignoreY) {
        
        this(goalOwner, ignoreLOS, ignoreY, null);
    }
    
    public CustomPathfinderGoalTarget(
            T goalOwner,
            boolean ignoreLOS,
            boolean ignoreY,
            @Nullable Predicate<EntityLiving> extraEntityPredicate) {

        this.goalOwner = goalOwner;
        // EntityFilter with ignoreLOS and ignoreY affects INITIALLY finding a player target
        // also note that this constructor is called in initPathfinder() in the super() constructor of a mob
        // before we are able to change FOLLOW_RANGE, thus we have to use getDetectionRange() instead
        this.targetCondition = new EntityFilter(this.getDetectionRange(), ignoreLOS, ignoreY,
                extraEntityPredicate);
        this.targetConditionIgnoreLOS = new EntityFilter(this.getDetectionRange(), true, ignoreY, // todo cleaner?
                extraEntityPredicate);
        this.targetConditionNoIgnoreY = new EntityFilter(this.getDetectionRange(), ignoreLOS, false,
                extraEntityPredicate);
        // ignoreLOS and ignoreY affects CONTINUING to track a target in b()/shouldContinueExecuting()
        this.ignoreLOS = ignoreLOS;
        this.ignoreY = ignoreY;

        /* Mobs not ignoring line of sight now require 100 instead of 60 ticks of no sight to stop targeting */
        this.unseenTicksToForget = 100;
    }

    @Override // shouldExecute()
    public boolean a() {
        this.potentialTarget = this.findNearestPotentialTarget(true);
        return this.targetCondition.test(this.goalOwner, this.potentialTarget);
    }

    @Override // shouldContinueExecuting()
    public boolean b() {
        EntityLiving goalTarget = this.goalOwner.getGoalTarget();

        if (this.targetCondition.test(this.goalOwner, goalTarget)) {
            if (!this.ignoreLOS) {
                this.targetUnseenTicks = 0;
            }
            return true;
        }

        // if breaking line of sight is the ONLY crime the target has committed (hence the
        // this.targetConditionIgnoreLOS.test()), attempt to give leeway
        if (!this.ignoreLOS && this.targetConditionIgnoreLOS.test(this.goalOwner, goalTarget)) {
            this.targetUnseenTicks++;
            return this.targetUnseenTicks < this.unseenTicksToForget;
        }

        return false;
    }

    @Override // startExecuting()
    public void c() {
        this.targetUnseenTicks = 0;
        // automatically make sure target range is updated for predicate for those mobs that change their detection range // todo test eventually
        this.targetCondition.setDetectionRange(this.getDetectionRange());
    }

    @Override // resetTask()
    public void d() {
        this.goalOwner.setGoalTarget(null, EntityTargetEvent.TargetReason.FORGOT_TARGET, true);
    }
    
    protected double getDetectionRange() {
        return this.goalOwner.getDetectionRange();
    }

    protected abstract EntityLiving findNearestPotentialTarget(boolean allowIgnoreY);
}
