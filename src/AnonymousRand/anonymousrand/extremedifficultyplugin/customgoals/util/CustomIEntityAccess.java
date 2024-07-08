package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.util;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderTargetCondition;
import net.minecraft.server.v1_16_R1.Entity;
import net.minecraft.server.v1_16_R1.EntityHuman;
import net.minecraft.server.v1_16_R1.EntityLiving;

import javax.annotation.Nullable;
import java.util.List;

public interface CustomIEntityAccess {

    default EntityHuman customFindPlayers(CustomPathfinderTargetCondition pathfindertargetcondition, EntityLiving entityLiving, double d0, double d1, double d2) { // passes to customFindEntities()
        return (EntityHuman) this.customFindEntities(entityLiving.getWorld().getPlayers(), pathfindertargetcondition, entityLiving, d0, d1, d2);
    }

    default <T extends EntityLiving> T customFindEntities(List<? extends T> entities, CustomPathfinderTargetCondition pathfinderTargetCondition, EntityLiving theOneWhoSeeks, double fromX, double fromY, double fromZ) {
        double minDistSq = Double.MAX_VALUE;
        T target = null;

        for (T entity : entities) {
            if (pathfinderTargetCondition.a(theOneWhoSeeks, entity)) { // uses overridden a() method in CustomPathfinderTargetCondition which turns off line of sight requirement to initially find a target player
                double distSq = entity.g(fromX, fromY, fromZ); // uses overridden g() which ignores y-level // todo instead of having every mob provide a copy of this function just find all uses of g() and make custom method in here or soemthing?

                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    target = entity;
                }
            }
        }

        return target;
    }
}