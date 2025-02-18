package AnonymousRand.anonymousrand.plugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.CustomEntityZombieThor;
import AnonymousRand.anonymousrand.plugin.listeners.ListenerLightningStrike;
import AnonymousRand.anonymousrand.plugin.util.CustomMathHelper;
import net.minecraft.server.v1_16_R1.BlockPosition;
import net.minecraft.server.v1_16_R1.Entity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class RunnableLightningEffectStorm extends BukkitRunnable {

    private final CustomEntityZombieThor.PathfinderGoalThorSummonLightning thorGoal;
    private final Entity runnableOwner;
    private final World bukkitWorld;
    private int cycleCount;
    private final int maxCycleCount;
    private final boolean effect;

    public RunnableLightningEffectStorm(CustomEntityZombieThor.PathfinderGoalThorSummonLightning thorGoal, int maxCycleCount) {
        this.thorGoal = thorGoal;
        this.runnableOwner = thorGoal.thor;
        this.bukkitWorld = thorGoal.bukkitWorld;
        this.cycleCount = 0;
        this.maxCycleCount = maxCycleCount;
        this.effect = true;
        this.thorGoal.storm = true;
        ListenerLightningStrike.storm = true;
    }

    public RunnableLightningEffectStorm(Entity runnableOwner, int maxCycleCount, boolean effect) {
        this.thorGoal = null;
        this.runnableOwner = runnableOwner;
        this.bukkitWorld = runnableOwner.getWorld().getWorld();
        this.cycleCount = 0;
        this.maxCycleCount = maxCycleCount;
        this.effect = effect;
        ListenerLightningStrike.storm = true;
    }

    @Override
    public void run() {
        this.cycleCount++;
        if (this.cycleCount > this.maxCycleCount) {
            Location bukkitLoc = new Location(this.bukkitWorld, this.runnableOwner.locX(), this.runnableOwner.locY(), this.runnableOwner.locZ());

            for (int i = 0; i < 4; i++) {
                Location bukkitLoc2 = CustomMathHelper.coordsFromHypotAndAngle(this.bukkitWorld, new BlockPosition(bukkitLoc.getX(), bukkitLoc.getY(), bukkitLoc.getZ()), 3.0, bukkitLoc.getY(), this.cycleCount * 13.0 + i * 60.0);

                if (this.effect) {
                    this.bukkitWorld.strikeLightningEffect(bukkitLoc2);
                } else {
                    this.bukkitWorld.strikeLightning(bukkitLoc2);
                }
            }
        } else if (this.cycleCount - 5 >= this.maxCycleCount) {
            this.cancel();

            if (this.thorGoal != null) {
                this.thorGoal.storm = false;
            }

            ListenerLightningStrike.storm = false;
        }
    }
}
