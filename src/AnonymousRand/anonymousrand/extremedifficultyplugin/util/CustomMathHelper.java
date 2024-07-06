package AnonymousRand.anonymousrand.extremedifficultyplugin.util;

import net.minecraft.server.v1_16_R1.BlockPosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Random;

import static java.lang.Math.PI;

public class CustomMathHelper {

    private static final double[] trigTableSin = new double[720]; // approximate values of trig functions to avoid lag when large amounts of Math.sin/cos is called while running
    private static final double[] trigTableCos = new double[720];
    private static final Random random = new Random();

    public static void initTrigTables() {
        for (int angle = 0; angle < 720; angle++) { // each index = +1/360 pi radians or +0.5 degrees
            trigTableSin[angle] = Math.abs(Math.sin(angle * PI / 360.0)); // absval because Minecraft does not follow the same quadrants (i.e. Minecraft is not pos pos for quadrant 1, neg pos for quadrant 2 etc. as the sin/cos functions would return)
            trigTableCos[angle] = Math.abs(Math.cos(angle * PI / 360.0));
        }
    }

    public static BlockPosition coordsFromHypotenuseAndAngle(BlockPosition origin, double hypotenuse, double y, double angle) {
        if (angle > 360) { // random angle
            angle = random.nextInt(720);
        } else {
            angle *= 2;
            angle %= 720;
        }

        double x = hypotenuse * trigTableSin[(int) angle]; // in Minecraft, x direction/coordinate is y-axis, but negative
        double z = hypotenuse * trigTableCos[(int) angle];

        if (angle >= 0 && angle < 180.0) { // quadrant 1, towards neg pos
            return new BlockPosition(origin.getX() - x, y, origin.getZ() + z);
        } else if (angle >= 180.0 && angle < 360.0) { // quadrant 2, neg neg
            return new BlockPosition(origin.getX() - x, y, origin.getZ() - z);
        } else if (angle >= 360.0 && angle < 540.0) { // quadrant 3, pos neg
            return new BlockPosition(origin.getX() + x, y, origin.getZ() - z);
        } else { // quadrant 4, pos pos
            return new BlockPosition(origin.getX() + x, y, origin.getZ() + z);
        }
    }

    public static Location coordsFromHypotenuseAndAngle(World bukkitWorld, BlockPosition origin, double hypotenuse, double y, double angle) {
        if (angle > 360) { // random angle
            angle = random.nextInt(720);
        } else {
            angle *= 2;
            angle %= 720;
        }

        double x = hypotenuse * trigTableSin[(int) angle];
        double z = hypotenuse * trigTableCos[(int) angle];

        if (angle >= 0 && angle < 180.0) {
            return new Location(bukkitWorld, origin.getX() - x, y, origin.getZ() + z);
        } else if (angle >= 180.0 && angle < 360.0) {
            return new Location(bukkitWorld, origin.getX() - x, y, origin.getZ() - z);
        } else if (angle >= 360.0 && angle < 540.0) {
            return new Location(bukkitWorld, origin.getX() + x, y, origin.getZ() - z);
        } else {
            return new Location(bukkitWorld, origin.getX() + x, y, origin.getZ() + z);
        }
    }

    public static Vector spiralVector(double radiusOfSpiral, double xIntervalNumber, double yChangePerInterval) { // clockwise starting quadrant 2
        double angle = xIntervalNumber / (8.5 + random.nextDouble());
        angle %= 4.0;
        angle *= 180.0;
        double x = radiusOfSpiral * trigTableSin[(int) angle];
        double z = radiusOfSpiral * trigTableCos[(int) angle];

        if (angle >= 0 && angle < 180.0) {
            return new Vector(-x, yChangePerInterval, z);
        } else if (angle >= 180.0 && angle < 360.0) {
            return new Vector(-x, yChangePerInterval, -z);
        } else if (angle >= 360.0 && angle < 540.0) {
            return new Vector(x, yChangePerInterval, -z);
        } else {
            return new Vector(x, yChangePerInterval, z);
        }
    }
}