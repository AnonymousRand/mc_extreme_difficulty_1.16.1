package AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.CustomMobs;

import net.minecraft.server.v1_16_R1.World;

public class CustomEntityChickenAggressiveExploding extends CustomEntityChickenAggressive {

    public CustomEntityChickenAggressiveExploding(World world) {
        super(world);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived == 400) {
            this.die();
        }
    }
}
