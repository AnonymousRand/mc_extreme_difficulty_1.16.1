package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import net.minecraft.server.v1_16_R1.EntityPig;
import net.minecraft.server.v1_16_R1.EntityTypes;
import net.minecraft.server.v1_16_R1.GenericAttributes;
import net.minecraft.server.v1_16_R1.World;

public class CustomEntityPig extends EntityPig {

    public CustomEntityPig(World world) {
        super(EntityTypes.PIG, world);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived == 10) { /**pigs move three times as fast*/
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.75);
        }
    }
}
