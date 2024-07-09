package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityTargetEvent;

import java.lang.reflect.Field;

public abstract class CustomPathfinderGoalTarget extends PathfinderGoalTarget {

    protected final boolean ignoreY;
    private static Field a;

    public CustomPathfinderGoalTarget(
            EntityInsentient goalOwner,
            boolean needSightToMaintainTarget,
            boolean nearbyOnly,
            boolean ignoreY) {
        
        super(goalOwner, needSightToMaintainTarget, nearbyOnly);
        this.ignoreY = ignoreY;
    }

    static {
        try {
            a = PathfinderGoalTarget.class.getDeclaredField("d");
            a.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override // shouldContinueExecuting(); overridden here to apply NMSUtil.distSqIgnoreY()
    public boolean b() {
        EntityLiving goalTarget = this.e.getGoalTarget();
        if (goalTarget == null) {
            goalTarget = this.g;
        }

        if (goalTarget == null) {
            return false;
        } else if (!goalTarget.isAlive()) {
            return false;
        } else {
            ScoreboardTeamBase goalOwnerTeam = this.e.getScoreboardTeam();
            ScoreboardTeamBase goalTargetTeam = goalTarget.getScoreboardTeam();
            if (goalOwnerTeam != null && goalTargetTeam == goalOwnerTeam) {
                return false;
            } else {
                double detectionRange = this.k();
                double distSq;
                if (this.ignoreY) {
                    distSq = NMSUtil.distSqIgnoreY(this.e, goalTarget);
                } else {
                    distSq = NMSUtil.distSq(this.e, goalTarget);
                }

                if (distSq > detectionRange * detectionRange) {
                    return false;
                } else {
                    if (this.f) {
                        if (this.e.getEntitySenses().a(goalTarget)) {
                            try {
                                a.setInt(this, 0);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                a.setInt(this, a.getInt(this) + 1);
                                if (a.getInt(this) > this.h) {
                                    return false;
                                }
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (goalTarget instanceof EntityHuman && ((EntityHuman) goalTarget).abilities.isInvulnerable) {
                        return false;
                    } else {
                        this.e.setGoalTarget(goalTarget, EntityTargetEvent.TargetReason.CLOSEST_ENTITY, true);
                        return true;
                    }
                }
            }
        }
    }

    @Override // getDetectionRange(); overridden here for both subclasses CustomPathfinderGoalNearestAttackableTarget and CustomPathfinderGoalHurtByTarget to use
    protected double k() {
        return ((ICustomHostile) this.e).getDetectionRange();
    }
}