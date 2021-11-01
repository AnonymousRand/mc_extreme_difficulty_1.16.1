package AnonymousRand.ExtremeDifficultyPlugin.BukkitRunnables;

import AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.misc.CustomEntityLightning;
import AnonymousRand.ExtremeDifficultyPlugin.Listeners.LightningStrikeListeners;
import AnonymousRand.ExtremeDifficultyPlugin.util.CoordsFromHypotenuse;
import net.minecraft.server.v1_16_R1.BlockPosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class UtilLightningStorm extends BukkitRunnable {

    private CustomEntityLightning lightning;
    private final net.minecraft.server.v1_16_R1.World nmsWorld;
    private final World bukkitWorld;
    private final Location loc;
    private Location loc2;
    private int cycles, maxCycles;
    private final CoordsFromHypotenuse coordsFromHypotenuse = new CoordsFromHypotenuse();
    private final Random random = new Random();

    public UtilLightningStorm(net.minecraft.server.v1_16_R1.World nmsWorld, World bukkitWorld, Location loc, int maxCycles) {
        this.nmsWorld = nmsWorld;
        this.bukkitWorld = bukkitWorld;
        this.loc = loc;
        this.cycles = 0;
        this.maxCycles = maxCycles;
    }

    public void run()
    {
        if (++this.cycles < this.maxCycles) {
            this.loc2 = coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(this.loc.getX(), this.loc.getY(), this.loc.getZ()), random.nextInt(101), this.bukkitWorld.getHighestBlockYAt(this.loc), 361.0);
            this.lightning = new CustomEntityLightning(this.nmsWorld);
            this.lightning.setPosition(this.loc2.getX(), this.loc2.getY(), this.loc2.getZ());
            this.nmsWorld.addEntity(this.lightning);
        } else if (this.cycles - 20 >= this.maxCycles) {
            this.cancel();
            LightningStrikeListeners.storm = false;
        }
    }
}
