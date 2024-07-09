package AnonymousRand.anonymousrand.extremedifficultyplugin.util;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.util.EntityFilter;
import net.minecraft.server.v1_16_R1.*;

import javax.annotation.Nullable;
import java.util.List;

public class NMSUtil {

    public static double distSq(Entity entityFrom, Entity entityTo) {
        return NMSUtil.distSq(entityFrom.getPositionVector(), entityTo.getPositionVector());
    }

    public static double distSq(Vec3D vec3DFrom, Vec3D vec3DTo) {
        return distSq(vec3DFrom.getX(), vec3DFrom.getY(), vec3DFrom.getZ(), vec3DTo.getX(), vec3DTo.getY(), vec3DTo.getZ());
    }

    public static double distSq(double x1, double y1, double z1, double x2, double y2, double z2) {
        double distX = x1 - x2;
        double distY = y1 - y2;
        double distZ = z1 - z2;
        return distX * distX + distY * distY + distZ * distZ;
    }

    public static double distSqIgnoreY(Entity entityFrom, Entity entityTo) {
        return NMSUtil.distSqIgnoreY(entityFrom.getPositionVector(), entityTo.getPositionVector());
    }

    public static double distSqIgnoreY(Vec3D vec3DFrom, Vec3D vec3DTo) {
        return distSqIgnoreY(vec3DFrom.getX(), vec3DFrom.getZ(), vec3DTo.getX(), vec3DTo.getZ());
    }

    public static double distSqIgnoreY(double x1, double z1, double x2, double z2) {
        double distX = x1 - x2;
        double distZ = z1 - z2;
        return distX * distX + distZ * distZ;
    }

    // Essentially the same as the ones defined in World.java, but using EntityFilter as a parameter instead of PathfinderTargetCondition
    @Nullable
    public static <T extends EntityLiving> T getClosestEntityWithinRange(
            Class<? extends T> targetClass,
            EntityFilter entityFilter,
            EntityLiving fromEntity,
            double rangeX,
            double rangeY,
            double rangeZ) {

        List<T> candidates = fromEntity.getWorld().a(targetClass, fromEntity.getBoundingBox().grow(rangeX, rangeY, rangeZ));
        return getClosestEntityFromList(candidates, entityFilter, fromEntity);
    }

    @Nullable
    public static <T extends EntityLiving> T getClosestEntityFromList(
            List<T> candidates,
            EntityFilter entityFilter,
            EntityLiving fromEntity) {

        double minDistSq = Double.MAX_VALUE;
        T closestCandidate = null;

        for (T candidate : candidates) {
            if (entityFilter.test(fromEntity, candidate)) {
                double distSq;
                distSq = NMSUtil.distSq(fromEntity, candidate); // doesn't matter here if we're not ignoring Y; that's entityFilter's job

                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    closestCandidate = candidate;
                }
            }
        }

        return closestCandidate;
    }
}
