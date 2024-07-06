package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import net.minecraft.server.v1_16_R1.*;

public class CustomEntityPig extends EntityPig {

    public CustomEntityPig(World world) {
        super(EntityTypes.PIG, world);
        /* No longer avoids lava and fire */
        this.a(PathType.LAVA, 0.0F);
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.75); /* pigs move three times as fast */
    }
}
