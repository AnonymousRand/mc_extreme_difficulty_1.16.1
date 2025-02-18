package AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util;

import net.minecraft.server.v1_16_R1.PathfinderGoalSelector;

/**
 * Allows removal of vanilla <code>PathfinderGoalHurtByTarget</code> for mobs that use
 * <code>super.initPathfinder()</code> and <code>PathfinderGoalNearestAttackableTarget</code> goals in order to replace them with custom ones.
 */
public interface IGoalRemovingMob {
    PathfinderGoalSelector getVanillaGoalSelector();
    PathfinderGoalSelector getVanillaTargetSelector();
}
