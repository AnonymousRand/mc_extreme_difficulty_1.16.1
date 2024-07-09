package AnonymousRand.anonymousrand.extremedifficultyplugin.util;

import net.minecraft.server.v1_16_R1.Entity;
import net.minecraft.server.v1_16_R1.Vec3D;

public class EntityUtils {

    public static double getDistSqNoY(Entity entityFrom, Entity entityTo) {
        return EntityUtils.getDistSqNoY(entityFrom.getPositionVector(), entityTo.getPositionVector());
    }

    public static double getDistSqNoY(Vec3D vec3DFrom, Vec3D vec3DTo) {
        return getDistSqNoY(vec3DFrom.getX(), vec3DFrom.getZ(), vec3DTo.getX(), vec3DTo.getZ());
    }

    public static double getDistSqNoY(double x1, double z1, double x2, double z2) {
        double distX = x1 - x2;
        double distZ = z1 - z2;
        return distX * distX + distZ * distZ;
    }
}
