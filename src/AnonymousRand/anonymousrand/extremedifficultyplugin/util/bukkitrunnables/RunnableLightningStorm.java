package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityLightning;
import AnonymousRand.anonymousrand.extremedifficultyplugin.listeners.ListenerLightningStrike;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CustomMathHelper;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.BlockPosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class RunnableLightningStorm extends BukkitRunnable {

    private final net.minecraft.server.v1_16_R1.World nmsWorld;
    private final World bukkitWorld;
    private final Location bukkitLoc;
    private final double radius;
    private int cycles;
    private final int maxCycles;
    private final boolean customLightning;
    private static final Random random = new Random();

    public RunnableLightningStorm(net.minecraft.server.v1_16_R1.World nmsWorld, Location bukkitLoc, int maxCycles) {
        this.nmsWorld = nmsWorld;
        this.bukkitWorld = nmsWorld.getWorld();
        this.bukkitLoc = bukkitLoc;
        this.cycles = 0;
        this.radius = 100.0;
        this.maxCycles = maxCycles;
        this.customLightning = true;
        ListenerLightningStrike.storm = true;
    }

    public RunnableLightningStorm(net.minecraft.server.v1_16_R1.World nmsWorld, Location bukkitLoc, double radius, int maxCycles, boolean customLightning) {
        this.nmsWorld = nmsWorld;
        this.bukkitWorld = nmsWorld.getWorld();
        this.bukkitLoc = bukkitLoc;
        this.cycles = 0;
        this.radius = radius;
        this.maxCycles = maxCycles;
        this.customLightning = customLightning;
        ListenerLightningStrike.storm = true;
    }

    @Override
    public void run() {
        if (++this.cycles <= this.maxCycles) {
            Location bukkitLoc2 = CustomMathHelper.coordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(this.bukkitLoc.getX(), this.bukkitLoc.getY(), this.bukkitLoc.getZ()), random.nextDouble() * this.radius, this.bukkitWorld.getHighestBlockYAt(this.bukkitLoc), 361.0);

            if (this.customLightning) {
                new SpawnEntity(this.nmsWorld, new CustomEntityLightning(this.nmsWorld), 1, null, bukkitLoc2, false);
            } else {
                this.bukkitWorld.strikeLightning(bukkitLoc2);
            }
        } else if (this.cycles - 5 >= this.maxCycles) {
            this.cancel();
            ListenerLightningStrike.storm = false;
        }
    }
}
