package AnonymousRand.anonymousrand.extremedifficultyplugin.util;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.util.EntityFilter;
import net.minecraft.server.v1_16_R1.*;

import javax.annotation.Nullable;
import java.util.List;

public class NMSUtil {

    /**
     * Calculate distance between <code>entityFrom</code> and <code>entityTo</code>, including y-level.
     */
    public static double distSq(Entity entityFrom, Entity entityTo) {
        return NMSUtil.distSq(entityFrom.getPositionVector(), entityTo.getPositionVector());
    }

    /**
     * Calculate distance between <code>vec3DFrom</code> and <code>vec3DTo</code>, including y-level.
     */
    public static double distSq(Vec3D vec3DFrom, Vec3D vec3DTo) {
        return distSq(vec3DFrom.getX(), vec3DFrom.getY(), vec3DFrom.getZ(), vec3DTo.getX(), vec3DTo.getY(), vec3DTo.getZ());
    }

    /**
     * Calculate distance between the first set of three coordinates and the second set, including y-level.
     */
    public static double distSq(double x1, double y1, double z1, double x2, double y2, double z2) {
        double distX = x1 - x2;
        double distY = y1 - y2;
        double distZ = z1 - z2;
        return distX * distX + distY * distY + distZ * distZ;
    }

    /**
     * Calculate distance between <code>entityFrom</code> and <code>entityTo</code>, excluding y-level.
     */
    public static double distSqExcludeY(Entity entityFrom, Entity entityTo) {
        return NMSUtil.distSqExcludeY(entityFrom.getPositionVector(), entityTo.getPositionVector());
    }

    /**
     * Calculate distance between <code>vec3DFrom</code> and <code>vec3DTo</code>, excluding y-level.
     */
    public static double distSqExcludeY(Vec3D vec3DFrom, Vec3D vec3DTo) {
        return distSqExcludeY(vec3DFrom.getX(), vec3DFrom.getZ(), vec3DTo.getX(), vec3DTo.getZ());
    }

    /**
     * Calculate distance between the first set of three coordinates and the second set, excluding y-level.
     */
    public static double distSqExcludeY(double x1, double z1, double x2, double z2) {
        double distX = x1 - x2;
        double distZ = z1 - z2;
        return distX * distX + distZ * distZ;
    }

    /**
     * Find the closest entity within an AABB (rectangular) area (considers y-level).
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
    public static <T extends EntityLiving> T getClosestEntityWithinRange(
            Class<? extends T> targetClass,
            @Nullable EntityFilter entityFilter,
            EntityLiving fromEntity,
            double rangeX,
            double rangeY,
            double rangeZ) {

        List<T> candidates = fromEntity.getWorld().a(targetClass, fromEntity.getBoundingBox().grow(rangeX, rangeY, rangeZ));
        return getClosestEntityFromList(candidates, entityFilter, fromEntity);
    }

    /**
     * Find the closest entity within an AABB (rectangular) area (considers y-level).
     * Essentially the same as the ones defined in World.java, but using <code>EntityFilter</code> as a parameter instead of <code>PathfinderTargetCondition</code>.
     *
     * @param candidates   the candidate entities
     * @param entityFilter conditions that the target entity must satisfy (use <code>null</code> for no conditions)
     * @param fromEntity   the entity that is performing the search (used for <code>entityFilter.test()</code>)
     */
    @Nullable
    public static <T extends EntityLiving> T getClosestEntityFromList(
            List<T> candidates,
            @Nullable EntityFilter entityFilter,
            EntityLiving fromEntity) {

        double minDistSq = Double.MAX_VALUE;
        T closestCandidate = null;

        for (T candidate : candidates) {
            if (entityFilter == null || entityFilter.test(fromEntity, candidate)) {
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
