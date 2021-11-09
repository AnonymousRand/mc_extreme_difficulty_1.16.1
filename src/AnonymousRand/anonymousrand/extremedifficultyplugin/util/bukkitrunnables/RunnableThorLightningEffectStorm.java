package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityZombieThor;
import AnonymousRand.anonymousrand.extremedifficultyplugin.listeners.LightningStrikeListeners;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CoordsFromHypotenuse;
import net.minecraft.server.v1_16_R1.BlockPosition;
import net.minecraft.server.v1_16_R1.Entity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class RunnableThorLightningEffectStorm extends BukkitRunnable {

    private final CustomEntityZombieThor.PathfinderGoalThorSummonLightning thorGoal;
    private final Entity entity;
    private final World bukkitWorld;
    private Location loc;
    private Location loc2;
    private int cycles;
    private final int maxCycles;
    private final boolean effect;
    private final CoordsFromHypotenuse coordsFromHypotenuse = new CoordsFromHypotenuse();

    public RunnableThorLightningEffectStorm(CustomEntityZombieThor.PathfinderGoalThorSummonLightning thorGoal, int maxCycles) {
        this.thorGoal = thorGoal;
        this.entity = thorGoal.thor;
        this.bukkitWorld = thorGoal.bukkitWorld;
        this.cycles = 0;
        this.maxCycles = maxCycles;
        this.effect = true;
        this.thorGoal.storm = true;
        LightningStrikeListeners.storm = true;
    }

    public RunnableThorLightningEffectStorm(Entity entity, int maxCycles, boolean effect) {
        this.thorGoal = null;
        this.entity = entity;
        this.bukkitWorld = entity.getWorld().getWorld();
        this.cycles = 0;
        this.maxCycles = maxCycles;
        this.effect = effect;
        LightningStrikeListeners.storm = true;
    }

    @Override
    public void run() {
        if (++this.cycles <= this.maxCycles) {
            this.loc = new Location(this.bukkitWorld, this.entity.locX(), this.entity.locY(), this.entity.locZ());

            for (int i = 0; i < 5; i++) {
                this.loc2 = coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(this.loc.getX(), this.loc.getY(), this.loc.getZ()), 3.0, this.loc.getY(), this.cycles * 13.0 + i * 60.0);

                if (this.effect) {
                    this.bukkitWorld.strikeLightningEffect(this.loc2);
                } else {
                    this.bukkitWorld.strikeLightning(this.loc2);
                }
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
