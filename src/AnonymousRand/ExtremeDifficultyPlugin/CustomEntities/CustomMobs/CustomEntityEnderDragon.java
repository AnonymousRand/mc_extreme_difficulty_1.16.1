package AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.CustomMobs;

import net.minecraft.server.v1_16_R1.EntityEnderDragon;
import net.minecraft.server.v1_16_R1.EntityTypes;
import net.minecraft.server.v1_16_R1.World;

public class CustomEntityEnderDragon extends EntityEnderDragon {
    public CustomEntityEnderDragon(World world) {
        super(EntityTypes.ENDER_DRAGON, world);
    }
}