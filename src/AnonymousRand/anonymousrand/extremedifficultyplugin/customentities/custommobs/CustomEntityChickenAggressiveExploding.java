package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import net.minecraft.server.v1_16_R1.World;

public class CustomEntityChickenAggressiveExploding extends CustomEntityChickenAggressive {

    public CustomEntityChickenAggressiveExploding(World world) {
        super(world);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived == 400) { /**aggressive exploding chickens die after 20 sec*/
            this.die();
        }
    }
}
