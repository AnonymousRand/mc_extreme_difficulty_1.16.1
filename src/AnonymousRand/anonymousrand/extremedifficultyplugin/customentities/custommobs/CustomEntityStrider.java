package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import net.minecraft.server.v1_16_R1.EntityStrider;
import net.minecraft.server.v1_16_R1.EntityTypes;
import net.minecraft.server.v1_16_R1.World;

public class CustomEntityStrider extends EntityStrider {

    public CustomEntityStrider(World world) {
        super(EntityTypes.STRIDER, world);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.hasSinglePlayerPassenger() && random.nextDouble() < 0.001) { /** if they have a rider, striders have a 0.1% chance per tick to disappear */
            this.die();
        }
    }
}
