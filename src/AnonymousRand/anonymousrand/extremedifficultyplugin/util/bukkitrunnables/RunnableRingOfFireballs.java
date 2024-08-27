package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.projectiles.CustomEntityDragonFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.projectiles.CustomEntitySmallFireball;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.scheduler.BukkitRunnable;

public class RunnableRingOfFireballs extends BukkitRunnable {

    private final EntityInsentient entity;
    private final World nmsWorld;
    private final double spacing;
    private int cycleCount;
    private final int maxCycleCount, type;

    public RunnableRingOfFireballs(EntityInsentient entity, double spacing, int maxCycleCount) {
        this.entity = entity;
        this.nmsWorld = entity.getWorld();
        this.spacing = spacing;
        this.type = 1;
        this.cycleCount = 0;
        this.maxCycleCount = maxCycleCount;
    }

    public RunnableRingOfFireballs(EntityInsentient entity, double spacing, int type, int maxCycleCount) {
        this.entity = entity;
        this.nmsWorld = entity.getWorld();
        this.spacing = spacing;
        this.type = type;
        this.cycleCount = 0;
        this.maxCycleCount = maxCycleCount;
    }

    @Override
    public void run() {
        this.cycleCount++;
        if (this.cycleCount > this.maxCycleCount) {
            this.cancel();
            return;
        }

        switch (type) {
            case 1:
                for (double x = -1.0; x <= 1.0; x += this.spacing) {
                    for (double y = -1.0; y <= 1.0; y += this.spacing) {
                        for (double z = -1.0; z <= 1.0; z += this.spacing) {
                            CustomEntitySmallFireball smallFireball = new CustomEntitySmallFireball(this.nmsWorld, this.entity, x, y, z);
                            smallFireball.setPosition(smallFireball.locX(), this.entity.e(0.5) + 0.5, smallFireball.locZ());
                            this.nmsWorld.addEntity(smallFireball);
                        }
                    }
                }
                break;
            case 2:
                for (double x = -1.0; x <= 1.0; x += this.spacing) {
                    for (double y = -1.0; y <= 1.0; y += this.spacing) {
                        for (double z = -1.0; z <= 1.0; z += this.spacing) {
                            CustomEntityDragonFireball entityDragonFireball = new CustomEntityDragonFireball(this.nmsWorld, this.entity, x, y, z, false);
                            entityDragonFireball.setPosition(entityDragonFireball.locX(), this.entity.e(0.5) + 0.5, entityDragonFireball.locZ());
                            this.nmsWorld.addEntity(entityDragonFireball);
                        }
                    }
                }
                break;
        }
    }
}
