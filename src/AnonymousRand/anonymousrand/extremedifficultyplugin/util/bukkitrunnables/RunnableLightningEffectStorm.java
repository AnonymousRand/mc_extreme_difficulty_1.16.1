package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.CustomEntityZombieThor;
import AnonymousRand.anonymousrand.extremedifficultyplugin.listeners.ListenerLightningStrike;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CustomMathHelper;
import net.minecraft.server.v1_16_R1.BlockPosition;
import net.minecraft.server.v1_16_R1.Entity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class RunnableLightningEffectStorm extends BukkitRunnable {

    private final CustomEntityZombieThor.PathfinderGoalThorSummonLightning thorGoal;
    private final Entity entity;
    private final World bukkitWorld;
    private int cycles;
    private final int maxCycles;
    private final boolean effect;

    public RunnableLightningEffectStorm(CustomEntityZombieThor.PathfinderGoalThorSummonLightning thorGoal, int maxCycles) {
        this.thorGoal = thorGoal;
        this.entity = thorGoal.thor;
        this.bukkitWorld = thorGoal.bukkitWorld;
        this.cycles = 0;
        this.maxCycles = maxCycles;
        this.effect = true;
        this.thorGoal.storm = true;
        ListenerLightningStrike.storm = true;
    }

    public RunnableLightningEffectStorm(Entity entity, int maxCycles, boolean effect) {
        this.thorGoal = null;
        this.entity = entity;
        this.bukkitWorld = entity.getWorld().getWorld();
        this.cycles = 0;
        this.maxCycles = maxCycles;
        this.effect = effect;
        ListenerLightningStrike.storm = true;
    }

    @Override
    public void run() {
        this.cycles++;
        if (this.cycles > this.maxCycles) {
            Location bukkitLoc = new Location(this.bukkitWorld, this.entity.locX(), this.entity.locY(), this.entity.locZ());

            for (int i = 0; i < 4; i++) {
                Location bukkitLoc2 = CustomMathHelper.coordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(bukkitLoc.getX(), bukkitLoc.getY(), bukkitLoc.getZ()), 3.0, bukkitLoc.getY(), this.cycles * 13.0 + i * 60.0);

                if (this.effect) {
                    this.bukkitWorld.strikeLightningEffect(bukkitLoc2);
                } else {
                    this.bukkitWorld.strikeLightning(bukkitLoc2);
                }
            }
        } else if (this.cycles - 5 >= this.maxCycles) {
            this.cancel();

            if (this.thorGoal != null) {
                this.thorGoal.storm = false;
            }

            ListenerLightningStrike.storm = false;
        }
    }
}
