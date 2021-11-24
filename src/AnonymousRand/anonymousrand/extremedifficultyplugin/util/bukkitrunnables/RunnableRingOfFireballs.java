package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityGhast;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntitySmallFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityWitherSkull;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.scheduler.BukkitRunnable;

public class RunnableRingOfFireballs extends BukkitRunnable {

    private final EntityInsentient entity;
    private final World nmsWorld;
    private final double spacing;
    private int cycles;
    private final int maxCycles;
    private CustomEntitySmallFireball entitySmallFireball;

    public RunnableRingOfFireballs(EntityInsentient entity, double spacing, int maxCycles) {
        this.entity = entity;
        this.nmsWorld = entity.getWorld();
        this.spacing = spacing;
        this.cycles = 0;
        this.maxCycles = maxCycles;
    }

    @Override
    public void run() {
        if (++this.cycles > this.maxCycles) {
            this.cancel();
            return;
        }

        for (double x = -1.0; x <= 1.0; x += this.spacing) {
            for (double y = -1.0; y <= 1.0; y += this.spacing) {
                for (double z = -1.0; z <= 1.0; z += this.spacing) {
                    this.entitySmallFireball = new CustomEntitySmallFireball(this.nmsWorld, this.entity, x, y, z);
                    this.entitySmallFireball.setPosition(entitySmallFireball.locX(), this.entity.e(0.5D) + 0.5D, this.entitySmallFireball.locZ());
                    this.nmsWorld.addEntity(entitySmallFireball);
                }
            }
        }
    }
}
