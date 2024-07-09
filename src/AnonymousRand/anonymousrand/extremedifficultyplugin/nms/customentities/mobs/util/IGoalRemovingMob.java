package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util;

import net.minecraft.server.v1_16_R1.PathfinderGoalSelector;

public interface IGoalRemovingMob {
    PathfinderGoalSelector getVanillaTargetSelector();
}
