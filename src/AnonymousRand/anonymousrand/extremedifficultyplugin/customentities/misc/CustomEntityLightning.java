package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc;

import net.minecraft.server.v1_16_R1.EntityLightning;
import net.minecraft.server.v1_16_R1.EntityTypes;
import net.minecraft.server.v1_16_R1.World;

public class CustomEntityLightning extends EntityLightning {

    private int lifeTicks;
    private int remainingBoltAnimationDuration;
    private int maxLifespan;
    private final float power;

    public CustomEntityLightning(World world) {
        super(EntityTypes.LIGHTNING_BOLT, world);
        this.lifeTicks = 2;
        this.remainingBoltAnimationDuration = random.nextInt(3) + 1;
        this.maxLifespan = this.remainingBoltAnimationDuration;
        this.power = 1.25F;
    }

    public CustomEntityLightning(World world, float power) {
        super(EntityTypes.LIGHTNING_BOLT, world);
        this.lifeTicks = 2;
        this.remainingBoltAnimationDuration = random.nextInt(3) + 1;
        this.maxLifespan = this.remainingBoltAnimationDuration;
        this.power = power;
    }

    @Override
    public void tick() {
        --this.lifeTicks;

        if (this.lifeTicks < 0) {
            if (this.remainingBoltAnimationDuration == this.maxLifespan) {
                this.getWorld().getWorld().createExplosion(this.locX(), this.locY(), this.locZ(), this.power, true); /** creates explosion power 1.25 on impact */
                --this.remainingBoltAnimationDuration;
            } else if (this.remainingBoltAnimationDuration == 0) {
                this.die();
            } else if (this.lifeTicks < -random.nextInt(10)) {
                --this.remainingBoltAnimationDuration;
                this.lifeTicks = 1;
                this.b = random.nextLong();
                this.a(0);
            }
        }

        super.tick();
    }
}
