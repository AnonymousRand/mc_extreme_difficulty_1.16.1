package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs;

import net.minecraft.server.v1_16_R1.World;

public class CustomEntityChickenAggressiveExploding extends CustomEntityChickenAggressive {

    public CustomEntityChickenAggressiveExploding(World world) {
        super(world);
    }

    @Override
    public void tick() {
        super.tick();

        /* Aggressive exploding chickens die after 20 seconds */
        if (this.ticksLived == 400) {
            this.die();
        }
    }
}