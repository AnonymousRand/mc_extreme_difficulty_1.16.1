package AnonymousRand.ExtremeDifficultyPlugin.util;
import net.minecraft.server.v1_16_R1.BlockPosition;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;

import java.math.*;
import java.util.Random;

public class CoordsFromHypotenuse {

    Random random = new Random();

    public BlockPosition RandomCoordsFromHypotenuseAndAngle(BlockPosition origin, double hypotenuse, double y, double angle) {
        if (angle == 361.0) { //random angle
            angle = Math.toRadians(random.nextDouble() * 360);
        }

        double x = Math.floor(Math.abs(hypotenuse * Math.cos(angle)));
        double z = Math.floor(Math.abs(hypotenuse * Math.sin(angle)));

        if (angle >= 0 && angle < 90) { //quadrant 1,  towards neg pos
            return new BlockPosition(origin.getX() - x, y, origin.getZ() + z);
        } else if (angle >= 90 && angle < 180) { //quadrant 2, neg neg
            return new BlockPosition(origin.getX() - x, y, origin.getZ() - z);
        } else if (angle >= 180 && angle < 270) { //quadrant 3, pos neg
            return new BlockPosition(origin.getX() + x, y, origin.getZ() - z);
        } else { //quadrant 4, pos pos
            return new BlockPosition(origin.getX() + x, y, origin.getZ() + z);
        }
    }
}
