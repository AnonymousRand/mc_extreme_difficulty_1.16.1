package AnonymousRand.anonymousrand.plugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.plugin.nms.customentities.misc.CustomEntityLightning;
import AnonymousRand.anonymousrand.plugin.listeners.ListenerLightningStrike;
import AnonymousRand.anonymousrand.plugin.util.CustomMathHelper;
import AnonymousRand.anonymousrand.plugin.util.SpawnEntity;
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
    private int cycleCount;
    private final int maxCycleCount;
    private final boolean customLightning;
    private static final Random random = new Random();

    public RunnableLightningStorm(net.minecraft.server.v1_16_R1.World nmsWorld, Location bukkitLoc, int maxCycleCount) {
        this.nmsWorld = nmsWorld;
        this.bukkitWorld = nmsWorld.getWorld();
        this.bukkitLoc = bukkitLoc;
        this.cycleCount = 0;
        this.radius = 100.0;
        this.maxCycleCount = maxCycleCount;
        this.customLightning = true;
        ListenerLightningStrike.storm = true;
    }

    public RunnableLightningStorm(net.minecraft.server.v1_16_R1.World nmsWorld, Location bukkitLoc, double radius, int maxCycleCount, boolean customLightning) {
        this.nmsWorld = nmsWorld;
        this.bukkitWorld = nmsWorld.getWorld();
        this.bukkitLoc = bukkitLoc;
        this.cycleCount = 0;
        this.radius = radius;
        this.maxCycleCount = maxCycleCount;
        this.customLightning = customLightning;
        ListenerLightningStrike.storm = true;
    }

    @Override
    public void run() {
        this.cycleCount++;
        if (this.cycleCount - 5 >= this.maxCycleCount) { // todo what is letting cycleCount go 5 past for
            this.cancel();
            ListenerLightningStrike.storm = false;
            return;
        }

        if (this.cycleCount <= this.maxCycleCount) {
            Location bukkitLoc2 = CustomMathHelper.coordsFromHypotAndAngle(this.bukkitWorld, new BlockPosition(this.bukkitLoc.getX(), this.bukkitLoc.getY(), this.bukkitLoc.getZ()), random.nextDouble() * this.radius, this.bukkitWorld.getHighestBlockYAt(this.bukkitLoc), 361.0);

            if (this.customLightning) {
                new SpawnEntity(this.nmsWorld, new CustomEntityLightning(this.nmsWorld), 1, null, bukkitLoc2, false);
            } else {
                this.bukkitWorld.strikeLightning(bukkitLoc2);
            }
        }
    }
}
