package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableLightningStorm;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomEntityLargeFireball extends EntityLargeFireball {

    private final JavaPlugin plugin;
    private boolean summonLightning = false;

    public CustomEntityLargeFireball(EntityTypes<? extends EntityLargeFireball> entitytypes, World world, int intYield, Vec3D vec, double x, double y, double z) {
        super(entitytypes, world);
        this.plugin = null;
        this.yield = intYield;
        this.setPosition(x, y, z);
        this.setMot(vec);
    }

    public CustomEntityLargeFireball(World world, EntityLiving entityliving, double d0, double d1, double d2, int intYield) {
        super(world, entityliving, d0, d1, d2);
        this.plugin = null;
        this.yield = intYield;
        this.setShooter(entityliving);
    }

    public CustomEntityLargeFireball(JavaPlugin plugin, World world, EntityLiving entityliving, double d0, double d1, double d2, int intYield, boolean summonLightning) {
        super(world, entityliving, d0, d1, d2);
        this.plugin = plugin;
        this.yield = intYield;
        this.summonLightning = summonLightning;
        this.setShooter(entityliving);
    }

    @Override
    protected void a(MovingObjectPosition movingobjectposition) { //in order for the new yield value to register
        super.a(movingobjectposition);

        if (!this.world.isClientSide && this.yield != 1) {
            boolean flag = this.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING);

            this.world.createExplosion((Entity)null, this.locX(), this.locY(), this.locZ(), (float)this.yield, flag, flag ? Explosion.Effect.DESTROY : Explosion.Effect.NONE);

            if (this.summonLightning && this.plugin != null) { //summon thor lightning
                new RunnableLightningStorm(this.getWorld(), new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ()), 10.0, this.random.nextInt(3) + 8, false).runTaskTimer(this.plugin, 0L, this.random.nextInt(3) + 2);
            }

            this.die();
        }
    }

    @Override
    protected void a(MovingObjectPositionEntity movingobjectpositionentity) {
        if (!this.world.isClientSide) {
            Entity entity = movingobjectpositionentity.getEntity();
            Entity entity1 = this.getShooter();

            entity.damageEntity(DamageSource.fireball(this, entity1), 1.0F); /**large fireballs only do 1 direct damage*/
            if (entity1 instanceof EntityLiving) {
                this.a((EntityLiving) entity1, entity);
            }

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
