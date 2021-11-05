package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc;

import net.minecraft.server.v1_16_R1.EntityAreaEffectCloud;
import net.minecraft.server.v1_16_R1.EntityTypes;
import net.minecraft.server.v1_16_R1.World;

public class CustomEntityAreaEffectCloud extends EntityAreaEffectCloud {

    public double damage;

    public CustomEntityAreaEffectCloud(World world, float radius, int duration, int waitTime, double damage) {
        super(EntityTypes.AREA_EFFECT_CLOUD, world);
        this.setRadius(radius);
        this.setDuration(duration);
        this.setWaitTime(waitTime);
        this.damage = damage;
    }
}
