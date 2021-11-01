package AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.CustomProjectiles;

import net.minecraft.server.v1_16_R1.*;

public class CustomEntitySmallFireball extends EntitySmallFireball {

    public CustomEntitySmallFireball(World world, EntityLiving entityliving, double d0, double d1, double d2) {
        super(world, entityliving, d0, d1, d2);
        this.setShooter(entityliving);
        this.isIncendiary = true; //make sure it lights the player on fire
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived > 70) { /**small fireballs despawn after 3.5 seconds to reduce lag*/
            this.die();
        }
    }
}
