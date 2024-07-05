package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import net.minecraft.server.v1_16_R1.*;

public class CustomEntityPig extends EntityPig {

    public CustomEntityPig(World world) {
        super(EntityTypes.PIG, world);
        this.a(PathType.LAVA, 0.0F); /* no longer avoids lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F); /* no longer avoids fire */
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.75); /* pigs move three times as fast */
    }
}
