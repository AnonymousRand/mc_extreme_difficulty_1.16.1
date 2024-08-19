package AnonymousRand.anonymousrand.extremedifficultyplugin.util;

import net.minecraft.server.v1_16_R1.EntityHuman;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.EntityLiving;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * My rewrite of vanilla's <code>PathfinderTargetCondition</code>.
 * <p></p>
 *
 * Some attribute values are hardcoded compared to vanilla:
 * <ul>
 *     <li><code>allowInvulnerable = false</code></li>
 *     <li><code>friendlyFire = false</code></li>
 *     <li><code>skipAttackChecks = false</code></li>
 *     <li><code>useVisibilityModifier = false</code> (ignores invis/skulls)</li>
 * </ul>
 * <p></p>
 *
 * Use <code>EntityFilter.BASE</code> to test for the basic conditions: not null, not spectator/creative, and alive.
 */
public class EntityFilter {

    public static final EntityFilter BASE = new EntityFilter(-1.0);
    private double detectionRange;
    private final boolean ignoreLOS;
    private final boolean ignoreY;
    private final Predicate<EntityLiving> extraEntityPredicate;

    public EntityFilter(double detectionRange) {
        this(detectionRange, true, true, null);
    }

    public EntityFilter(
            double detectionRange,
            boolean ignoreLOS,
            boolean ignoreY,
            @Nullable Predicate<EntityLiving> extraEntityPredicate) {
        this.detectionRange = detectionRange;
        this.ignoreLOS = ignoreLOS;
        this.ignoreY = ignoreY;
        this.extraEntityPredicate = extraEntityPredicate;
    }

    /**
     * Applies filter to <code>target</code>.
     * <p></p>
     *
     * Ignores invis/skulls, and supports ignoring y-level and line of sight.
     */
    public boolean test(@Nullable EntityLiving target) {
        return this.test(null, target);
    }

    /**
     * Applies filter to <code>target</code> with respect to <code>from</code>.
     * <p></p>
     *
     * Ignores invis/skulls, and supports ignoring y-level and line of sight.
     */
    public boolean test(@Nullable EntityLiving from, @Nullable EntityLiving target) {
        // `BASE` checks
        if (target == null) {
            return false;
        }

        boolean passedBaseChecks =
                from != target
                && !target.isSpectator()
                && !target.isInvulnerable()
                && !(target instanceof EntityHuman && ((EntityHuman) target).abilities.isInvulnerable)
                && target.isAlive();
        if (!passedBaseChecks) {
            return false;
        }

        // extraEntityPredicate check
        if (this.extraEntityPredicate != null && !this.extraEntityPredicate.test(target)) {
            return false;
        }

        if (from != null) {
            // `canAttack()` and friendly fire check
            if (!from.d(target) || !from.a(target.getEntityType()) || from.r(target)) {
                return false;
            }

            // distance check
            if (this.detectionRange > 0.0
                    && NmsUtil.distSq(from, target, this.ignoreY) > this.detectionRange * this.detectionRange) {
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

    public void setDetectionRange(double detectionRange) {
        this.detectionRange = detectionRange;
    }
}
