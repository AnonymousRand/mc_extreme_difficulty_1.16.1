package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import net.minecraft.server.v1_16_R1.Vec3D;

public interface ICommonCustomMethods {

    default double getNormalDistanceSq(Vec3D vec3d1, Vec3D vec3d2) {
        double d0 = vec3d2.getX() - vec3d1.getX(); //this function still takes into account y level
        double d1 = vec3d2.getY() - vec3d1.getY();
        double d2 = vec3d2.getZ() - vec3d1.getZ();

        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    double getFollowRange();

    void checkDespawn();

    double g(double d0, double d1, double d2);

    double d(Vec3D vec3d);

    int bL();
}
