package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customProjectiles;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;

public class CustomEntityLargeFireball extends EntityLargeFireball {

    public CustomEntityLargeFireball(EntityTypes<? extends EntityLargeFireball> entitytypes, World world, int intYield, Vec3D vec, double x, double y, double z) {
        super(entitytypes, world);
        this.yield = intYield;
        this.setPosition(x, y, z);
        this.setMot(vec);
    }

    public CustomEntityLargeFireball(World world, EntityLiving entityliving, double d0, double d1, double d2, int intYield) {
        super(world, entityliving, d0, d1, d2);
        this.yield = intYield;
        this.setShooter(entityliving);
    }

    @Override
    protected void a(MovingObjectPosition movingobjectposition) { //in order for the new yield value to register
        super.a(movingobjectposition);

        if (!this.world.isClientSide) {
            boolean flag = this.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING);

            this.world.createExplosion((Entity)null, this.locX(), this.locY(), this.locZ(), (float)this.yield, flag, flag ? Explosion.Effect.DESTROY : Explosion.Effect.NONE);
            this.die();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived > 100) { /**large fireballs despawn after 5 seconds to reduce lag*/
            this.die();
        }
    }
}
