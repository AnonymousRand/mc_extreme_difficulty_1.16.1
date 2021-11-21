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
    private int type;
    private CustomEntitySmallFireball entitySmallFireball;
    private CustomEntityWitherSkull entityWitherSkull;

    public RunnableRingOfFireballs(EntityInsentient entity, double spacing, int maxCycles) {
        this.entity = entity;
        this.nmsWorld = entity.getWorld();
        this.spacing = spacing;
        this.cycles = 0;
        this.maxCycles = maxCycles;
        this.type = 0;
    }

    public RunnableRingOfFireballs(EntityInsentient entity, double spacing, int type, int maxCycles) {
        this.entity = entity;
        this.nmsWorld = entity.getWorld();
        this.spacing = spacing;
        this.cycles = 0;
        this.maxCycles = maxCycles;
        this.type = type;
    }

    @Override
    public void run() {
        if (++this.cycles > this.maxCycles) {
            this.cancel();
            return;
        }

        if (this.entity instanceof CustomEntityGhast) {
            if (((CustomEntityGhast)this.entity).attacks >= 100) {
                this.type = 2;
            }
        }

        switch (type) {
            case 0 -> { //small fireballs, default
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
            case 1 -> { //black wither skulls
                for (double x = -1.0; x <= 1.0; x += this.spacing) {
                    for (double y = -1.0; y <= 1.0; y += this.spacing) {
                        for (double z = -1.0; z <= 1.0; z += this.spacing) {
                            this.entityWitherSkull = new CustomEntityWitherSkull(this.nmsWorld, this.entity, x, y, z);
                            this.entityWitherSkull.setPosition(entityWitherSkull.locX(), this.entity.e(0.5D) + 0.5D, this.entityWitherSkull.locZ());
                            this.nmsWorld.addEntity(entityWitherSkull);
                        }
                    }
                }
            }
            case 2 -> { //blue wither skulls
                for (double x = -1.0; x <= 1.0; x += this.spacing) {
                    for (double y = -1.0; y <= 1.0; y += this.spacing) {
                        for (double z = -1.0; z <= 1.0; z += this.spacing) {
                            this.entityWitherSkull = new CustomEntityWitherSkull(this.nmsWorld, this.entity, x, y, z);
                            this.entityWitherSkull.setCharged(true);
                            this.entityWitherSkull.setDeathFireballs(true);
                            this.entityWitherSkull.setPosition(entityWitherSkull.locX(), this.entity.e(0.5D) + 0.5D, this.entityWitherSkull.locZ());
                            this.nmsWorld.addEntity(entityWitherSkull);
                        }
                    }
                }
            }
        }
    }
}
