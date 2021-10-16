package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalHurtByTarget;
import net.minecraft.server.v1_16_R1.*;

public class CustomEntityBlaze extends EntityBlaze {

    public CustomEntityBlaze(World world) {
        super(EntityTypes.BLAZE, world);
        this.a(PathType.WATER, 0.0F); /**no longer avoid water*/
        this.a(PathType.LAVA, 8.0F);
        this.a(PathType.DANGER_FIRE, 0.0F);
        this.a(PathType.DAMAGE_FIRE, 0.0F);
    }

    @Override
    public boolean dN() {
        return false;
    } /**no longer damaged by water*/
}
