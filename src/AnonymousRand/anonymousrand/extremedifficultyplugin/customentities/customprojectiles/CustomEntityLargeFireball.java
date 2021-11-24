package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableLightningStorm;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class CustomEntityLargeFireball extends EntityLargeFireball {

    private boolean summonLightning = false;

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

    public CustomEntityLargeFireball(World world, EntityLiving entityliving, double d0, double d1, double d2, int intYield, boolean summonLightning) {
        super(world, entityliving, d0, d1, d2);
        this.yield = intYield;
        this.summonLightning = summonLightning;
        this.setShooter(entityliving);
    }

    @Override
    protected void a(MovingObjectPosition movingobjectposition) { //in order for the new yield value to register
        if (movingobjectposition instanceof MovingObjectPositionEntity) {
            if (((MovingObjectPositionEntity)movingobjectposition).getEntity() instanceof EntityPlayer) { /**large fireballs can only impact players*/
                this.a((MovingObjectPositionEntity)movingobjectposition);
            }

            return;
        }

        //otherwise if hit block
        super.a(movingobjectposition);
        if (!this.world.isClientSide && this.yield > 1) {
            boolean flag = this.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING);
            this.world.createExplosion((Entity)null, this.locX(), this.locY(), this.locZ(), (float)this.yield, flag, flag ? Explosion.Effect.DESTROY : Explosion.Effect.NONE);

            if (this.summonLightning && StaticPlugin.plugin != null) { //summon thor lightning
                new RunnableLightningStorm(this.getWorld(), new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ()), 10.0, random.nextInt(3) + 8, false).runTaskTimer(StaticPlugin.plugin, 0L, random.nextInt(3) + 2);
            }
        }
    }

    @Override
    protected void a(MovingObjectPositionEntity movingobjectpositionentity) {
        if (!this.world.isClientSide) {
            Entity entity = movingobjectpositionentity.getEntity();

            if (!(entity instanceof EntityPlayer)) { /**large fireballs can only impact players*/
                return;
            }

            Entity entity1 = this.getShooter();

            entity.damageEntity(DamageSource.fireball(this, entity1), 1.333333333F); /**large fireballs only do 1.333333333 direct damage*/
            if (entity1 instanceof EntityLiving) {
                this.a((EntityLiving) entity1, entity);
            }

            boolean flag = this.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING);
            this.world.createExplosion((Entity)null, this.locX(), this.locY(), this.locZ(), (float)this.yield, flag, flag ? Explosion.Effect.DESTROY : Explosion.Effect.NONE);

            if (this.summonLightning && StaticPlugin.plugin != null) { //summon thor lightning
                new RunnableLightningStorm(this.getWorld(), new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ()), 10.0, random.nextInt(3) + 8, false).runTaskTimer(StaticPlugin.plugin, 0L, random.nextInt(3) + 2);
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
