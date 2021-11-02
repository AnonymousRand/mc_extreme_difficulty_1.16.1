package AnonymousRand.anonymousrand.extremedifficultyplugin.bukkitrunnables.entityrunnables;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityEvoker;
import net.minecraft.server.v1_16_R1.EntityLiving;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EvokerStopPlayer extends BukkitRunnable {

    private CustomEntityEvoker evoker;
    private EntityLiving target;
    private int cycles, maxCycles;

    public EvokerStopPlayer(CustomEntityEvoker evoker, EntityLiving target, int maxCycles) {
        this.evoker = evoker;
        this.target = target;
        this.cycles = 0;
        this.maxCycles = maxCycles;
    }

    public void run() {
        if (++this.cycles > this.maxCycles) {
            this.cancel();
        }

        LivingEntity bukkitEntity = (LivingEntity)target.getBukkitEntity();
        bukkitEntity.setVelocity(new Vector(0.0, 0.0, 0.0));
    }
}
