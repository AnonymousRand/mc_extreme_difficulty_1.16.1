package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util;

import net.minecraft.server.v1_16_R1.Vec3D;

public interface ICustomHostile {
    double getDetectionRange();
    void checkDespawn();                    // vanilla override
    /* Mobs are willing to take any fall to reach the player as they don't take fall damage */
    int bL();                               // vanilla override (getMaxFallHeight()) (gets max height mobs are willing to drop from)

    default double getDistSq(Vec3D vec3d1, Vec3D vec3d2) { // this function takes into account y-level
        double d0 = vec3d2.getX() - vec3d1.getX();
        double d1 = vec3d2.getY() - vec3d1.getY();
        double d2 = vec3d2.getZ() - vec3d1.getZ();

        return d0 * d0 + d1 * d1 + d2 * d2;
    }
}