package AnonymousRand.anonymousrand.extremedifficultyplugin.bukkitrunnables.entityrunnables;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntitySkeleton;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class SkeletonRapidFire extends BukkitRunnable {

    private final CustomEntitySkeleton skeleton;
    private final EntityLiving target;
    private int cycles;
    private final int maxCycles;
    private final float f;
    private final Random random = new Random();

    public SkeletonRapidFire(CustomEntitySkeleton skeleton, EntityLiving target, int maxCycles, float f) {
        this.skeleton = skeleton;
        this.target = target;
        this.cycles = 0;
        this.maxCycles = maxCycles;
        this.f = f;
    }

    public void run() {
        if (++this.cycles > this.maxCycles) {
            this.cancel();
        }

        for (int i = 0; i < (skeleton.attacks < 35 ? 10 : 1); i++) {
            ItemStack itemstack = skeleton.f(skeleton.b(ProjectileHelper.a(skeleton, Items.BOW)));
            EntityArrow entityarrow = skeleton.b(itemstack, f);
            double d0 = target.locX() - skeleton.locX();
            double d1 = target.locY() - skeleton.locY();
            double d2 = target.locZ() - skeleton.locZ();

            if (this.random.nextDouble() <= 0.02) { /**2% of arrows shot are piercing 1*/
                entityarrow.setPierceLevel((byte)1);
            }

            if (this.skeleton.attacks >= 35) { /**starting from the 35th attack, arrows do not lose y level*/
                entityarrow.setNoGravity(true);
            }

            entityarrow.setOnFire(50);

            entityarrow.shoot(d0, d1, d2, 1.6F, skeleton.attacks < 35 ? (float)(30 - skeleton.world.getDifficulty().a() * 4) : 0.0F); /**no inaccuracy after 35 attacks*/
            skeleton.playSound(SoundEffects.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (skeleton.getRandom().nextFloat() * 0.4F + 0.8F));
            skeleton.world.addEntity(entityarrow);
        }
    }
}
