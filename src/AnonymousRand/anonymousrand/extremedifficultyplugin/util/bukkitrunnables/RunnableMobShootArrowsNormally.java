package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class RunnableMobShootArrowsNormally extends BukkitRunnable {

    private final EntityInsentient entity;
    private final EntityLiving target;
    private final int numOfArrows, arrowType, pierce;
    private final double inaccuracy;
    private final boolean onFire, noGravity;
    private final World nmsWorld;
    private int cycles;
    private final int maxCycles;
    private CustomEntityArrow entityArrow;
    private static final Random random = new Random();

    public RunnableMobShootArrowsNormally(EntityInsentient entity, EntityLiving target, int numOfArrows, int arrowType, double inaccuracy, int pierce, boolean onFire, boolean noGravity) {
        this.entity = entity;
        this.target = target;
        this.numOfArrows = numOfArrows;
        this.arrowType = arrowType;
        this.inaccuracy = inaccuracy;
        this.pierce = pierce;
        this.onFire = onFire;
        this.noGravity = noGravity;
        this.nmsWorld = entity.getWorld();
        this.cycles = 0;
        this.maxCycles = 1;
    }

    public RunnableMobShootArrowsNormally(EntityInsentient entity, EntityLiving target, int numOfArrows, int arrowType, double inaccuracy, int pierce, boolean onFire, boolean noGravity, int maxCycles) {
        this.entity = entity;
        this.target = target;
        this.numOfArrows = numOfArrows;
        this.arrowType = arrowType;
        this.inaccuracy = inaccuracy;
        this.pierce = pierce;
        this.onFire = onFire;
        this.noGravity = noGravity;
        this.nmsWorld = entity.getWorld();
        this.cycles = 0;
        this.maxCycles = maxCycles;
    }

    @Override
    public void run() {
        if (++this.cycles > this.maxCycles) {
            this.cancel();
            return;
        }

        for (int i = 0; i < this.numOfArrows; i++) {
            this.entityArrow = new CustomEntityArrow(this.nmsWorld);
            double rand = random.nextDouble();

            switch (this.arrowType) {
                case 2 -> this.entityArrow = new CustomEntityArrowExploding(this.nmsWorld); // exploding arrows
                case 3 -> { // stray spawn mob arrows
                    this.entityArrow = new CustomEntityArrowSpawnMob(this.nmsWorld, rand < 0.25 ? new CustomEntityCreeper(this.nmsWorld, 30) : rand < 0.5 ? new CustomEntityVex(this.nmsWorld) : rand < 0.75 ? new CustomEntityRabbit(this.nmsWorld) : new CustomEntitySilverfish(this.nmsWorld));
                }
                case 4 -> { // piglin spawn piglin arrows
                    if (rand < 0.0125) {
                        this.entityArrow = new CustomEntityArrowSpawnMob(this.nmsWorld, new CustomEntityPiglin(this.nmsWorld));
                    }
                }
                case 5 -> this.entityArrow = new CustomEntityArrowBadEffects(this.nmsWorld); // piglin bad status effects arrow
                case 6 -> this.entityArrow = new CustomEntityArrowKnockback(this.nmsWorld); // extreme knockback arrows
            }

            this.entityArrow.setShooter(this.entity);
            this.entityArrow.setPosition(this.entity.locX(), this.entity.locY() + 1.5, this.entity.locZ());
            this.entityArrow.setPierceLevel((byte)this.pierce);
            double d0 = this.target.locX() - this.entity.locX();
            double d1 = this.target.e(0.3333333333333333D) - this.entityArrow.locY();
            double d2 = this.target.locZ() - this.entity.locZ();
            double d3 = this.noGravity ? 0.0 : (double)MathHelper.sqrt(d0 * d0 + d2 * d2); // this adjusts arrow height for distance

            if (this.onFire) {
                this.entityArrow.setOnFire(50);
            }

            if (this.noGravity) {
                this.entityArrow.setNoGravity(true);
            }

            this.entityArrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (this.arrowType == 3 || this.inaccuracy == 0.0) ? 0.0F : (float)(this.inaccuracy - this.nmsWorld.getDifficulty().a() * 4)); /** mob-spawning arrows have no inaccuracy */

            if (this.entity instanceof CustomEntityIllusioner || this.entity instanceof CustomEntityPiglin || this.entity instanceof CustomEntityPillager || this.entity instanceof CustomEntitySkeleton || this.entity instanceof CustomEntitySkeletonStray) {
                this.entityArrow.setDamage(1.5); /** illusioners, piglins, pillagers, skeletons and strays always do 3 damage with arrows and distance does not play a factor in determining damage */
            }

            this.entity.playSound(SoundEffects.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
            this.nmsWorld.addEntity(this.entityArrow);
        }
    }
}
