package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.util;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.EntityLiving;

import javax.annotation.Nullable;
import java.util.function.Predicate;

// Entire class rewritten instead of inherited from PathfinderTargetCondition in order to apply our own NMSUtil.distSqIgnoreY()
// without being too hacky or needing too much reflection (everything's private :sob:)
public class EntityFilter {

    public static final EntityFilter BASE = new EntityFilter(-1.0); // negative detectionRange means don't check distance
    private double detectionRange;
    private final boolean ignoreY;
    private Predicate<EntityLiving> additionalPredicate;

    public EntityFilter(double detectionRange) {
        this(detectionRange, true, null);
    }

    public EntityFilter(double detectionRange, @Nullable Predicate<EntityLiving> additionalPredicate) {
        this(detectionRange, true, additionalPredicate);
    }

    public EntityFilter(double detectionRange, boolean ignoreY, @Nullable Predicate<EntityLiving> additionalPredicate) {
        this.detectionRange = detectionRange;
        this.ignoreY = ignoreY;
        this.additionalPredicate = additionalPredicate;
    }

    public void setDetectionRange(double detectionRange) {
        this.detectionRange = detectionRange;
    }

    // Logic heavily simplified to ignore y-level, line of sight, and invis/skulls
    public boolean test(@Nullable EntityLiving from, EntityLiving target) {
        boolean passedBasicChecks =
                from != target
                && !(target.isSpectator() || target.isInvulnerable())
                && target.isAlive();
        if (!passedBasicChecks) {
            return false;
        }

        if (this.additionalPredicate != null && !this.additionalPredicate.test(target)) {
            return false;
        } else {
            if (from != null) {
                if (!from.d(target) || !from.a(target.getEntityType()) || from.r(target)) {
                    return false;
                }

                if (this.detectionRange > 0.0) {
                    if (this.ignoreY) {
                        if (NMSUtil.distSqIgnoreY(from, target) > this.detectionRange * this.detectionRange) {
                            return false;
                        }
                    } else {
                        if (NMSUtil.distSq(from, target) > this.detectionRange * this.detectionRange) {
                            return false;
                        }
                    }
                }
            }

            return true;
        }
    }
}
