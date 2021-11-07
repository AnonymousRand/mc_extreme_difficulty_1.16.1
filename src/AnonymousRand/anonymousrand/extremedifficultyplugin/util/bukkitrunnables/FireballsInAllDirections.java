package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityGhast;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityLargeFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntitySmallFireball;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import org.bukkit.scheduler.BukkitRunnable;

public class FireballsInAllDirections extends BukkitRunnable {

    private final EntityInsentient entity;
    private final double spacing;
    private int cycles;
    private final int maxCycles;
    private CustomEntitySmallFireball entitySmallFireball;

    public FireballsInAllDirections(EntityInsentient entity, double spacing, int maxCycles) {
        this.entity = entity;
        this.spacing = spacing;
        this.cycles = 0;
        this.maxCycles = maxCycles;
    }

    @Override
    public void run() {
        if (++this.cycles > this.maxCycles) {
            this.cancel();
        }

        if (this.entity instanceof CustomEntityGhast) {
            if (((CustomEntityGhast)this.entity).attacks < 100) {
                for (double x = -1.0; x <= 1.0; x += this.spacing) {
                    for (double y = -1.0; y <= 1.0; y += this.spacing) {
                        for (double z = -1.0; z <= 1.0; z += this.spacing) {
                            entitySmallFireball = new CustomEntitySmallFireball(this.entity.getWorld(), this.entity, x, y, z);
                            entitySmallFireball.setPosition(entitySmallFireball.locX(), this.entity.e(0.5D) + 0.5D, entitySmallFireball.locZ());
                            this.entity.getWorld().addEntity(entitySmallFireball);
                        }
                    }
                }
            } else {
                //todo: summon wither skulls instead
            }
        } else {
            for (double x = -1.0; x <= 1.0; x += this.spacing) {
                for (double y = -1.0; y <= 1.0; y += this.spacing) {
                    for (double z = -1.0; z <= 1.0; z += this.spacing) {
                        entitySmallFireball = new CustomEntitySmallFireball(this.entity.getWorld(), this.entity, x, y, z);
                        entitySmallFireball.setPosition(entitySmallFireball.locX(), this.entity.e(0.5D) + 0.5D, entitySmallFireball.locZ());
                        this.entity.getWorld().addEntity(entitySmallFireball);
                    }
                }
            }
        }
    }
}
