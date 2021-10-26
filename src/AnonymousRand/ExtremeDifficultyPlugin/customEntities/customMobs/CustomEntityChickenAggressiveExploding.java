package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import net.minecraft.server.v1_16_R1.Explosion;
import net.minecraft.server.v1_16_R1.World;

public class CustomEntityChickenAggressiveExploding extends CustomEntityChickenAggressive {

    public CustomEntityChickenAggressiveExploding(World world) {
        super(world);
    }

    @Override
    public void die() {
        super.die();

        this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 1.0f, false, Explosion.Effect.DESTROY); /**explodes with power 1 after 20 seconds or when killed*/
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived == 400) {
            this.die();
        }
    }
}
