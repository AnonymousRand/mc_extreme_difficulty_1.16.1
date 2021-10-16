package AnonymousRand.ExtremeDifficultyPlugin.util;
import net.minecraft.server.v1_16_R1.BlockPosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.Random;

import static java.lang.Math.PI;

public class CoordsFromHypotenuse {

    Random rand = new Random();

    public BlockPosition CoordsFromHypotenuseAndAngle(BlockPosition origin, double hypotenuse, double y, double angle) {
        if (angle == 361.0) { //random angle
            angle = Math.toRadians(rand.nextDouble() * 360);
        }

        double x = Math.floor(Math.abs(hypotenuse * Math.cos(angle)));
        double z = Math.floor(Math.abs(hypotenuse * Math.sin(angle)));

        if (angle >= 0 && angle < PI / 2) { //quadrant 1, towards neg pos
            return new BlockPosition(origin.getX() - x, y, origin.getZ() + z);
        } else if (angle >= PI / 2 && angle < PI) { //quadrant 2, neg neg
            return new BlockPosition(origin.getX() - x, y, origin.getZ() - z);
        } else if (angle >= PI && angle < 3 * PI / 2) { //quadrant 3, pos neg
            return new BlockPosition(origin.getX() + x, y, origin.getZ() - z);
        } else { //quadrant 4, pos pos
            return new BlockPosition(origin.getX() + x, y, origin.getZ() + z);
        }
    }

    public Location CoordsFromHypotenuseAndAngle(World world, BlockPosition origin, double hypotenuse, double y, double angle) {
        if (angle == 361.0) { //random angle
            angle = Math.toRadians(rand.nextDouble() * 360);
        } else {
            angle = Math.toRadians(angle);
        }

        double x = Math.abs(hypotenuse * Math.cos(angle));
        double z = Math.abs(hypotenuse * Math.sin(angle));

        if (angle >= 0 && angle < PI / 2) {
            return new Location(world, origin.getX() - x, y, origin.getZ() + z);
        } else if (angle >= PI / 2 && angle < PI) {
            return new Location(world, origin.getX() - x, y, origin.getZ() - z);
        } else if (angle >= PI && angle < 3 * PI / 2) {
            return new Location(world, origin.getX() + x, y, origin.getZ() - z);
        } else {
            return new Location(world, origin.getX() + x, y, origin.getZ() + z);
        }
    }
}