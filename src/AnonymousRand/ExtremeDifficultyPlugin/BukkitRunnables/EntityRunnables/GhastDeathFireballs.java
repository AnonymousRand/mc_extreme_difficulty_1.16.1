package AnonymousRand.ExtremeDifficultyPlugin.BukkitRunnables.EntityRunnables;

import AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.CustomMobs.CustomEntityGhast;
import AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.CustomProjectiles.CustomEntitySmallFireball;
import org.bukkit.scheduler.BukkitRunnable;

public class GhastDeathFireballs extends BukkitRunnable {

    private CustomEntityGhast ghast;
    private int cycles, maxCycles;
    private double x, y, z;
    private CustomEntitySmallFireball entitySmallFireball;

    public GhastDeathFireballs(CustomEntityGhast ghast, int maxCycles) {
        this.ghast = ghast;
        this.cycles = 0;
        this.maxCycles = maxCycles;
    }

    public void run() {
        if (++this.cycles > this.maxCycles) {
            this.cancel();
        }

        if (this.ghast.attacks < 100) {
            for (double x = -1.0; x <= 1.0; x += 0.4) {
                for (double y = -1.0; y <= 1.0; y += 0.4) {
                    for (double z = -1.0; z <= 1.0; z += 0.4) {
                        entitySmallFireball = new CustomEntitySmallFireball(this.ghast.getWorld(), this.ghast, x, y, z);
                        entitySmallFireball.setPosition(entitySmallFireball.locX(), this.ghast.e(0.5D) + 0.5D, entitySmallFireball.locZ());
                        this.ghast.getWorld().addEntity(entitySmallFireball);
                    }
                }
            }
        } else {
            //todo: summon wither skulls instead
        }
    }
}
