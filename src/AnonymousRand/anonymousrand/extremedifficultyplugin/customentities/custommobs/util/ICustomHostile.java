package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util;

import net.minecraft.server.v1_16_R1.Vec3D;

public interface ICustomHostile {
    //void initCustomHostile(); // todo uncomment once all have been converted
    double getFollowRange();
    void checkDespawn();                    // vanilla override
    /** For determining distance to entities, y level does not matter, e.g. mob follow range, attacking (can hit player no matter the y level) */
    double g(double x, double y, double z); // vanilla override (get dist from a coord); not default since inherited > interface methods
    double d(Vec3D vec3d);                  // vanilla override (get dist from a Vec3d coord)
    /** Mobs are willing to take any fall to reach the player as they don't take fall damage */
    int bL();                               // vanilla override (getMaxFallHeight()) (gets max height mobs are willing to drop from)

    default double getNormalDistanceSq(Vec3D vec3d1, Vec3D vec3d2) { // this function takes into account y level
        double d0 = vec3d2.getX() - vec3d1.getX();
        double d1 = vec3d2.getY() - vec3d1.getY();
        double d2 = vec3d2.getZ() - vec3d1.getZ();

        return d0 * d0 + d1 * d1 + d2 * d2;
    }
}