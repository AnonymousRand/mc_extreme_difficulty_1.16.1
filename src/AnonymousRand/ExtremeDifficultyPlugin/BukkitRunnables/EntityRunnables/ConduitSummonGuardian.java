package AnonymousRand.ExtremeDifficultyPlugin.BukkitRunnables.EntityRunnables;

import AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.CustomMobs.CustomEntityChickenAggressive;
import AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.CustomMobs.CustomEntityGuardian;
import AnonymousRand.ExtremeDifficultyPlugin.util.SpawnLivingEntity;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class ConduitSummonGuardian extends BukkitRunnable {

    private CustomEntityGuardian newGuardian;
    private final World nmsWorld;
    private final Location loc;
    private int cycles, maxCycles;

    public ConduitSummonGuardian(World nmsWorld, Location loc, int maxCycles) {
        this.nmsWorld = nmsWorld;
        this.loc = loc;
        this.cycles = 0;
        this.maxCycles = maxCycles;
    }

    @Override
    public void run() {
        if (++this.cycles >= this.maxCycles) {
            this.cancel();
        }

        new SpawnLivingEntity(this.nmsWorld, new CustomEntityGuardian(this.nmsWorld), 1, null, this.loc, true).run();
    }
}
