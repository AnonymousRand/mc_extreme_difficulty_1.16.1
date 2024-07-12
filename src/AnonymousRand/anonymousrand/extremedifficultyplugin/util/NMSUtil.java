package AnonymousRand.anonymousrand.extremedifficultyplugin.util;

import net.minecraft.server.v1_16_R1.*;

import javax.annotation.Nullable;
import java.util.List;

public class NMSUtil {

    /**
     * Calculate distance between <code>entityFrom</code> and <code>entityTo</code>.
     */
    public static double dist(Entity entityFrom, Entity entityTo, boolean ignoreY) {
        return dist(entityFrom.getPositionVector(), entityTo.getPositionVector(), ignoreY);
    }

    /**
     * Calculate distance between <code>vec3DFrom</code> and <code>vec3DTo</code>.
     */
    public static double dist(Vec3D vec3DFrom, Vec3D vec3DTo, boolean ignoreY) {
        return dist(vec3DFrom.getX(), vec3DFrom.getY(), vec3DFrom.getZ(),
                vec3DTo.getX(), vec3DTo.getY(), vec3DTo.getZ(), ignoreY);
    }

    /**
     * Calculate distance between the first set of three coordinates and the second set.
     */
    public static double dist(double x1, double y1, double z1, double x2, double y2, double z2, boolean ignoreY) {
        return Math.sqrt(distSq(x1, y1, z1, x2, y2, z2, ignoreY));
    }

    /**
     * Calculate distance squared between <code>entityFrom</code> and <code>entityTo</code>.
     */
    public static double distSq(Entity entityFrom, Entity entityTo, boolean ignoreY) {
        return distSq(entityFrom.getPositionVector(), entityTo.getPositionVector(), ignoreY);
    }

    /**
     * Calculate distance squared between <code>vec3DFrom</code> and <code>vec3DTo</code>.
     */
    public static double distSq(Vec3D vec3DFrom, Vec3D vec3DTo, boolean ignoreY) {
        return distSq(vec3DFrom.getX(), vec3DFrom.getY(), vec3DFrom.getZ(),
                vec3DTo.getX(), vec3DTo.getY(), vec3DTo.getZ(), ignoreY);
    }

    /**
     * Calculate distance squared between the first set of three coordinates and the second set.
     */
    public static double distSq(double x1, double y1, double z1, double x2, double y2, double z2, boolean ignoreY) {
        if (ignoreY) {
            double distX = x1 - x2;
            double distZ = z1 - z2;
            return distX * distX + distZ * distZ;
        } else {
            double distX = x1 - x2;
            double distY = y1 - y2;
            double distZ = z1 - z2;
            return distX * distX + distY * distY + distZ * distZ;
        }
    }

    /**
     * Find the nearest entity within an AABB (rectangular) area (considers y-level).
     * Essentially the same as the ones defined in World.java, but using <code>EntityFilter</code> as a parameter instead of <code>PathfinderTargetCondition</code>.
     *
     * @param targetClass  the entity class to search for (racism)
     * @param entityFilter conditions that the target entity must satisfy (use <code>null</code> for no conditions)
     * @param fromEntity   the entity that is in the center of the search area
     * @param rangeX       the x-distance from <code>fromEntity</code> to the area boundary
     * @param rangeY       the y-distance from <code>fromEntity</code> to the area boundary
     * @param rangeZ       the z-distance from <code>fromEntity</code> to the area boundary
     */
    @Nullable
    public static <T extends EntityLiving> T getNearestEntityWithinRange(
            Class<? extends T> targetClass,
            @Nullable EntityFilter entityFilter,
            EntityLiving fromEntity,
            double rangeX,
            double rangeY,
            double rangeZ) {

        List<T> candidates = fromEntity.getWorld().a(targetClass, fromEntity.getBoundingBox().grow(rangeX, rangeY, rangeZ));
        return getNearestEntityFromList(candidates, entityFilter, fromEntity);
    }

    /**
     * Find the nearest entity within an AABB (rectangular) area (considers y-level).
     * Essentially the same as the ones defined in World.java, but using <code>EntityFilter</code> as a parameter instead of <code>PathfinderTargetCondition</code>.
     *
     * @param candidates   the candidate entities
     * @param entityFilter conditions that the target entity must satisfy (use <code>null</code> for no conditions)
     * @param fromEntity   the entity that is performing the search (used for <code>entityFilter.test()</code>)
     */
    @Nullable
    public static <T extends EntityLiving> T getNearestEntityFromList(
            List<T> candidates,
            @Nullable EntityFilter entityFilter,
            EntityLiving fromEntity) {

        double minDistSq = Double.MAX_VALUE;
        T nearestCandidate = null;

        for (T candidate : candidates) {
            if (entityFilter == null || entityFilter.test(fromEntity, candidate)) {
                double distSq;
                distSq = NMSUtil.distSq(fromEntity, candidate, false); // doesn't matter here if we're not ignoring Y; that's entityFilter's job

                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    nearestCandidate = candidate;
                }
            }
        }

        return nearestCandidate;
    }
}
