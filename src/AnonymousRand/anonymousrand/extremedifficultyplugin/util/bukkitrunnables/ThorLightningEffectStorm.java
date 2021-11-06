package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalThorSummonLightning;
import AnonymousRand.anonymousrand.extremedifficultyplugin.listeners.LightningStrikeListeners;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CoordsFromHypotenuse;
import net.minecraft.server.v1_16_R1.BlockPosition;
import net.minecraft.server.v1_16_R1.Entity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class ThorLightningEffectStorm extends BukkitRunnable {

    private final NewPathfinderGoalThorSummonLightning thorGoal;
    private final Entity entity;
    private final World bukkitWorld;
    private Location loc;
    private Location loc2;
    private int cycles;
    private final int maxCycles;
    private final CoordsFromHypotenuse coordsFromHypotenuse = new CoordsFromHypotenuse();

    public ThorLightningEffectStorm(NewPathfinderGoalThorSummonLightning thorGoal, int maxCycles) {
        this.thorGoal = thorGoal;
        this.entity = thorGoal.thor;
        this.bukkitWorld = thorGoal.bukkitWorld;
        this.cycles = 0;
        this.maxCycles = maxCycles;
        this.thorGoal.storm = true;
        LightningStrikeListeners.storm = true;
    }

    public ThorLightningEffectStorm(Entity entity, int maxCycles) {
        this.thorGoal = null;
        this.entity = entity;
        this.bukkitWorld = entity.getWorld().getWorld();
        this.cycles = 0;
        this.maxCycles = maxCycles;
        LightningStrikeListeners.storm = true;
    }

    @Override
    public void run() {
        if (++this.cycles <= this.maxCycles) {
            this.loc = new Location(this.bukkitWorld, this.entity.locX(), this.entity.locY(), this.entity.locZ());

            for (int i = 0; i < 5; i++) {
                this.loc2 = coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(this.loc.getX(), this.loc.getY(), this.loc.getZ()), 3.0, this.loc.getY(), this.cycles * 13.0 + i * 60.0);
                this.bukkitWorld.strikeLightningEffect(this.loc2);
            }
        } else if (this.cycles - 5 >= this.maxCycles) {
            this.cancel();

            if (this.thorGoal != null) {
                this.thorGoal.storm = false;
            }

            LightningStrikeListeners.storm = false;
        }
    }
}
