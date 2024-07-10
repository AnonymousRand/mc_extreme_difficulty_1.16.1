package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.util;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.EntityLiving;

import javax.annotation.Nullable;
import java.util.function.Predicate;

// Entire class rewritten instead of inherited from PathfinderTargetCondition in order to apply our own NMSUtil.distSqIgnoreY()
// without being too hacky or needing too much reflection (everything's private :sob:)
public class EntityFilter {

    private double detectionRange;
    private final boolean ignoreLOS;
    private final boolean ignoreY;
    private Predicate<EntityLiving> extraPredicate;

    public EntityFilter(double detectionRange, boolean ignoreLOS, boolean ignoreY, @Nullable Predicate<EntityLiving> extraPredicate) {
        this.detectionRange = detectionRange;
        this.ignoreLOS = ignoreLOS;
        this.ignoreY = ignoreY;
        this.extraPredicate = extraPredicate;
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

        if (this.extraPredicate != null && !this.extraPredicate.test(target)) {
            return false;
        } else {
            if (from != null) {
                if (!from.d(target) || !from.a(target.getEntityType()) || from.r(target)) {
                    return false;
                }

                // distance check
                if (this.detectionRange > 0.0
                        && NMSUtil.distSq(from, target, this.ignoreY) > this.detectionRange * this.detectionRange) {
                        return false;
                }

                // line of sight check
                if (!this.ignoreLOS && from instanceof EntityInsentient
                        && !((EntityInsentient) from).getEntitySenses().a(target)) {
                    return false;
                }
            }

            return true;
        }
    }
}