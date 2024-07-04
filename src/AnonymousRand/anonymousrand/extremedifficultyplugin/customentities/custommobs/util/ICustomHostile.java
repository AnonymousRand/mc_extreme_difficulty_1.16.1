package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util;

import net.minecraft.server.v1_16_R1.Vec3D;

public interface ICustomHostile {
    //void initCustomHostile(); // todo uncomment once all have been converted
    double getFollowRange();
    void checkDespawn();                       // vanilla override
    double g(double d0, double d1, double d2); // vanilla override (get dist from a coord); not default since inherited > interface methods
    double d(Vec3D vec3d);                     // vanilla override (get dist from a Vec3d coord)
    int bL();                                  // vanilla override (getMaxFallHeight()) (gets max height mobs are willing to drop from)

    default double getNormalDistanceSq(Vec3D vec3d1, Vec3D vec3d2) { // this function takes into account y level
        double d0 = vec3d2.getX() - vec3d1.getX();
        double d1 = vec3d2.getY() - vec3d1.getY();
        double d2 = vec3d2.getZ() - vec3d1.getZ();

        return d0 * d0 + d1 * d1 + d2 * d2;
    }
}