package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityZombieThor;
import AnonymousRand.anonymousrand.extremedifficultyplugin.listeners.LightningStrikeListeners;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CoordsFromHypotenuse;
import net.minecraft.server.v1_16_R1.BlockPosition;
import net.minecraft.server.v1_16_R1.PathfinderGoal;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class NewPathfinderGoalThorSummonLightning extends PathfinderGoal {

    private final JavaPlugin plugin;
    public final CustomEntityZombieThor thor;
    public final World bukkitWorld;
    private final Location loc;
    private Location loc2;
    public boolean storm;
    private final CoordsFromHypotenuse coordsFromHypotenuse = new CoordsFromHypotenuse();
    private final Random random = new Random();

    public NewPathfinderGoalThorSummonLightning(JavaPlugin plugin, CustomEntityZombieThor thor) {
        this.plugin = plugin;
        this.thor = thor;
        this.bukkitWorld = thor.getWorld().getWorld();
        this.loc = new Location(this.bukkitWorld, thor.locX(), thor.locY(), thor.locZ());
        this.storm = false;
    }

    @Override
    public boolean a() {
        return this.thor.getGoalTarget() != null;
    }

    @Override
    public boolean b() {
        return this.a();
    }

    @Override
    public void e() {
        if (this.random.nextDouble() < 0.04) {
            this.loc2 = coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(this.loc.getX(), this.loc.getY(), this.loc.getZ()), 20.0, this.bukkitWorld.getHighestBlockYAt(this.loc), 361.0);
            if (this.random.nextDouble() < 0.25) {
                this.bukkitWorld.strikeLightning(this.loc2);
            } else {
                this.bukkitWorld.strikeLightningEffect(this.loc2);
            }
        }

        if (this.random.nextDouble() < 0.006 && !this.storm) {
            new ThorLightningEffectStorm(this, this.random.nextInt(11) + 30).runTaskTimer(this.plugin, 0L, 2L);
        }
    }

    static class ThorLightningEffectStorm extends BukkitRunnable {

        private final NewPathfinderGoalThorSummonLightning thorGoal;
        private final CustomEntityZombieThor thor;
        private final World bukkitWorld;
        private Location loc;
        private Location loc2;
        private int cycles;
        private final int maxCycles;
        private final CoordsFromHypotenuse coordsFromHypotenuse = new CoordsFromHypotenuse();

        public ThorLightningEffectStorm(NewPathfinderGoalThorSummonLightning thorGoal, int maxCycles) {
            this.thorGoal = thorGoal;
            this.thor = thorGoal.thor;
            this.bukkitWorld = thorGoal.bukkitWorld;
            this.cycles = 0;
            this.maxCycles = maxCycles;
            this.thorGoal.storm = true;
            LightningStrikeListeners.storm = true;
        }

        @Override
        public void run() {
            if (++this.cycles <= this.maxCycles) {
                this.loc = new Location(this.bukkitWorld, this.thor.locX(), this.thor.locY(), this.thor.locZ());

                for (int i = 0; i < 5; i++) {
                    this.loc2 = coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(this.loc.getX(), this.loc.getY(), this.loc.getZ()), 3.0, this.loc.getY(), this.cycles * 13.0 + i * 60.0);
                    this.bukkitWorld.strikeLightningEffect(this.loc2);
                }
            } else if (this.cycles - 5 >= this.maxCycles) {
                this.cancel();
                this.thorGoal.storm = false;
                LightningStrikeListeners.storm = false;
            }
        }
    }
}
