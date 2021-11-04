package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityLightning;
import AnonymousRand.anonymousrand.extremedifficultyplugin.listeners.LightningStrikeListeners;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CoordsFromHypotenuse;
import net.minecraft.server.v1_16_R1.BlockPosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class LightningStorm extends BukkitRunnable {

    private CustomEntityLightning lightning;
    private final net.minecraft.server.v1_16_R1.World nmsWorld;
    private final World bukkitWorld;
    private final Location loc;
    private Location loc2;
    private final double radius;
    private int cycles;
    private final int maxCycles;
    private final boolean customLightning;
    private final CoordsFromHypotenuse coordsFromHypotenuse = new CoordsFromHypotenuse();
    private final Random random = new Random();

    public LightningStorm(net.minecraft.server.v1_16_R1.World nmsWorld, Location loc, int maxCycles) {
        this.nmsWorld = nmsWorld;
        this.bukkitWorld = nmsWorld.getWorld();
        this.loc = loc;
        this.cycles = 0;
        this.radius = 100.0;
        this.maxCycles = maxCycles;
        this.customLightning = true;
    }

    public LightningStorm(net.minecraft.server.v1_16_R1.World nmsWorld, Location loc, double radius, int maxCycles, boolean customLightning) {
        this.nmsWorld = nmsWorld;
        this.bukkitWorld = nmsWorld.getWorld();
        this.loc = loc;
        this.cycles = 0;
        this.radius = radius;
        this.maxCycles = maxCycles;
        this.customLightning = customLightning;
    }

    public void run()
    {
        if (this.cycles == 0) {
            LightningStrikeListeners.storm = true;
        }

        if (++this.cycles <= this.maxCycles) {
            this.loc2 = coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(this.loc.getX(), this.loc.getY(), this.loc.getZ()), random.nextDouble() * this.radius, this.bukkitWorld.getHighestBlockYAt(this.loc), 361.0);

            if (this.customLightning) {
                this.lightning = new CustomEntityLightning(this.nmsWorld);
                this.lightning.setPosition(this.loc2.getX(), this.loc2.getY(), this.loc2.getZ());
                this.nmsWorld.addEntity(this.lightning);
            } else {
                this.bukkitWorld.strikeLightning(this.loc2);
            }
        } else if (this.cycles - 10 >= this.maxCycles) {
            this.cancel();
            LightningStrikeListeners.storm = false;
        }
    }
}
