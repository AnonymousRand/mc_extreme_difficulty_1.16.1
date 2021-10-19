package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;

public class CustomEntityHusk extends EntityZombieHusk {
    public CustomEntityHusk(World world) {
        super(EntityTypes.HUSK, world);
    }

    public void tick() {
        super.tick();
    }
}