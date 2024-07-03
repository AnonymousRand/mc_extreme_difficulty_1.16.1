package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util;

import net.minecraft.server.v1_16_R1.PathfinderGoalSelector;

public interface IGoalRemovingMob {
    void initGoalRemoval();

    PathfinderGoalSelector getVanillaTargetSelector();
}
