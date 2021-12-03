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

    private CustomEntityLightning newLightning;
    private final net.minecraft.server.v1_16_R1.World nmsWorld;
    private final World bukkitWorld;
    private final Location loc;
    private Location loc2;
    private final double radius;
    private int cycles;
    private final int maxCycles;
    private final boolean customLightning;
    private static final Random random = new Random();

    public RunnableLightningStorm(net.minecraft.server.v1_16_R1.World nmsWorld, Location loc, int maxCycles) {
        this.nmsWorld = nmsWorld;
        this.bukkitWorld = nmsWorld.getWorld();
        this.loc = loc;
        this.cycles = 0;
        this.radius = 100.0;
        this.maxCycles = maxCycles;
        this.customLightning = true;
        ListenerLightningStrike.storm = true;
    }

    public RunnableLightningStorm(net.minecraft.server.v1_16_R1.World nmsWorld, Location loc, double radius, int maxCycles, boolean customLightning) {
        this.nmsWorld = nmsWorld;
        this.bukkitWorld = nmsWorld.getWorld();
        this.loc = loc;
        this.cycles = 0;
        this.radius = radius;
        this.maxCycles = maxCycles;
        this.customLightning = customLightning;
        ListenerLightningStrike.storm = true;
    }

    @Override
    public void run() {
        if (++this.cycles <= this.maxCycles) {
            this.loc2 = CustomMathHelper.coordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(this.loc.getX(), this.loc.getY(), this.loc.getZ()), random.nextDouble() * this.radius, this.bukkitWorld.getHighestBlockYAt(this.loc), 361.0);

            if (this.customLightning) {
                new SpawnEntity(this.nmsWorld, new CustomEntityLightning(this.nmsWorld), 1, null, this.loc2, false);
            } else {
                this.bukkitWorld.strikeLightning(this.loc2);
            }
        } else if (this.cycles - 5 >= this.maxCycles) {
            this.cancel();
            ListenerLightningStrike.storm = false;
        }
    }
}
