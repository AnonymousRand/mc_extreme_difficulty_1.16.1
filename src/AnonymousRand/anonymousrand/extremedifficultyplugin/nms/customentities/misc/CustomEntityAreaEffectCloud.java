package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.misc;

import net.minecraft.server.v1_16_R1.EntityAreaEffectCloud;
import net.minecraft.server.v1_16_R1.EntityTypes;
import net.minecraft.server.v1_16_R1.World;

import java.lang.reflect.Field;

public class CustomEntityAreaEffectCloud extends EntityAreaEffectCloud {

    public Field potionRegistry;

    public CustomEntityAreaEffectCloud(World world, float radius, int duration, int waitTime) {
        super(EntityTypes.AREA_EFFECT_CLOUD, world);
        this.setRadius(radius);
        this.setDuration(duration);
        this.setWaitTime(waitTime);

        try {
            this.potionRegistry = EntityAreaEffectCloud.class.getDeclaredField("potionRegistry");
            this.potionRegistry.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}