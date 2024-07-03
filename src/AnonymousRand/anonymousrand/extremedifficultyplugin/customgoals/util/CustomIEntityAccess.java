package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.util;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderTargetCondition;
import net.minecraft.server.v1_16_R1.Entity;
import net.minecraft.server.v1_16_R1.EntityHuman;
import net.minecraft.server.v1_16_R1.EntityLiving;

import javax.annotation.Nullable;
import java.util.List;

public interface CustomIEntityAccess {

    default EntityHuman customFindPlayers(CustomPathfinderTargetCondition pathfindertargetcondition, EntityLiving entityLiving, double d0, double d1, double d2) { // passes to customFindEntities()
        return (EntityHuman)this.customFindEntities(entityLiving.world.getPlayers(), pathfindertargetcondition, entityLiving, d0, d1, d2);
    }

    default <T extends Entity> EntityLiving customFindEntities(List<? extends T> list, CustomPathfinderTargetCondition pathfindertargetcondition, @Nullable EntityLiving entityLiving, double d0, double d1, double d2) {
        double d3 = -1.0D;
        EntityLiving target = null;

        for (T entity : list) {
            if (entity instanceof EntityLiving) {
                EntityLiving potentialTarget = (EntityLiving)entity;

                if (pathfindertargetcondition.a(entityLiving, potentialTarget)) { // uses overriden a() method in CustomPathfinderTargetCondition which turns off line of sight requirement to initially find a target player
                    double d4 = potentialTarget.g(d0, d1, d2);

                    if (d3 == -1.0D || d4 < d3) {
                        d3 = d4;
                        target = potentialTarget;
                    }
                }
            }
        }

        return target;
    }
}
