package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util;

import net.minecraft.server.v1_16_R1.PathfinderGoalSelector;

/**
 * For mobs that use <code>super.initPathfinder()</code>: allows removal of vanilla <code>PathfinderGoalHurtByTarget</code>
 * and <code>PathfinderGoalNearestAttackableTarget</code> goals in order to replace them with custom ones.
 */
public interface IGoalRemovingMob {
    PathfinderGoalSelector getVanillaTargetSelector();
}
