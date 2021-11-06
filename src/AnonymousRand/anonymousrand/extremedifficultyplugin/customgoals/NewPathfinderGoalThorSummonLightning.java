package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityZombieThor;
import AnonymousRand.anonymousrand.extremedifficultyplugin.listeners.LightningStrikeListeners;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CoordsFromHypotenuse;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.ThorLightningEffectStorm;
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
}
