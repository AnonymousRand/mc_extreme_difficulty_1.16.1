package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.projectiles.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class RunnableMobShootArrows extends BukkitRunnable {

    private final EntityInsentient entity;
    private final EntityLiving target;
    private final int numOfArrows, arrowType, pierce;
    private final double inaccuracy;
    private final boolean onFire, noGravity;
    private final World nmsWorld;
    private int cycleCount;
    private final int cycleCountMax;
    private static final Random random = new Random();

    public RunnableMobShootArrows(EntityInsentient entity, EntityLiving target, int numOfArrows, int arrowType, double inaccuracy, int pierce, boolean onFire, boolean noGravity) {
        this.entity = entity;
        this.target = target;
        this.numOfArrows = numOfArrows;
        this.arrowType = arrowType;
        this.inaccuracy = inaccuracy;
        this.pierce = pierce;
        this.onFire = onFire;
        this.noGravity = noGravity;
        this.nmsWorld = entity.getWorld();
        this.cycleCount = 0;
        this.cycleCountMax = 1;
    }

    public RunnableMobShootArrows(EntityInsentient entity, EntityLiving target, int numOfArrows, int arrowType, double inaccuracy, int pierce, boolean onFire, boolean noGravity, int cycleCountMax) {
        this.entity = entity;
        this.target = target;
        this.numOfArrows = numOfArrows;
        this.arrowType = arrowType;
        this.inaccuracy = inaccuracy;
        this.pierce = pierce;
        this.onFire = onFire;
        this.noGravity = noGravity;
        this.nmsWorld = entity.getWorld();
        this.cycleCount = 0;
        this.cycleCountMax = cycleCountMax;
    }

    @Override
    public void run() {
        this.cycleCount++;
        if (this.cycleCount > this.cycleCountMax) {
            this.cancel();
            return;
        }

        for (int i = 0; i < this.numOfArrows; i++) {
            CustomEntityArrow entityArrow = new CustomEntityArrow(this.nmsWorld);
            double rand = random.nextDouble();

            switch (this.arrowType) {
                case 2:
                    entityArrow = new CustomEntityArrowExploding(this.nmsWorld); // exploding arrows
                    break;
                case 3:
                    entityArrow = new CustomEntityArrowSpawnMob(this.nmsWorld, rand < 0.25 ? new CustomEntityCreeper(this.nmsWorld) : rand < 0.5 ? new CustomEntityVex(this.nmsWorld) : rand < 0.75 ? new CustomEntityRabbit(this.nmsWorld) : new CustomEntitySilverfish(this.nmsWorld)); // stray spawn mob arrows
                    break;
                case 4:
                    entityArrow = new CustomEntityArrowBadEffects(this.nmsWorld); // piglin bad status effects arrow
                    break;
                case 5:
                    entityArrow = new CustomEntityArrowKnockback(this.nmsWorld); // extreme knockback arrows
                    break;
            }

            entityArrow.setShooter(this.entity);
            entityArrow.setPosition(this.entity.locX(), this.entity.locY() + 1.5, this.entity.locZ());
            entityArrow.setPierceLevel((byte)this.pierce);
            double d0 = this.target.locX() - this.entity.locX();
            double d1 = this.target.e(0.3333333333333333D) - entityArrow.locY();
            double d2 = this.target.locZ() - this.entity.locZ();
            double d3 = this.noGravity ? 0.0 : MathHelper.sqrt(d0 * d0 + d2 * d2); // this adjusts arrow height for distance

            if (this.onFire) {
                entityArrow.setOnFire(50);
            }

            if (this.noGravity) {
                entityArrow.setNoGravity(true);
            }

            entityArrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (this.arrowType == 3 || this.inaccuracy == 0.0) ? 0.0F : (float) (this.inaccuracy - this.nmsWorld.getDifficulty().a() * 4)); /* mob-spawning arrows have no inaccuracy */

            if (this.entity instanceof CustomEntityIllusioner || this.entity instanceof CustomEntityPiglin || this.entity instanceof CustomEntityPillager || this.entity instanceof CustomEntitySkeleton || this.entity instanceof CustomEntitySkeletonStray) {
                entityArrow.setDamage(1.5); /* illusioners, piglins, pillagers, skeletons and strays always do 3 damage with arrows and distance does not play a factor in determining damage */
            }

            this.entity.playSound(SoundEffects.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
            this.nmsWorld.addEntity(entityArrow);
        }
    }
}
