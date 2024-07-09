package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.projectiles;

import AnonymousRand.anonymousrand.extremedifficultyplugin.ExtremeDifficultyPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableLightningStorm;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;

public class CustomEntityLargeFireball extends EntityLargeFireball {

    private boolean summonLightning = false;

    public CustomEntityLargeFireball(EntityTypes<? extends EntityLargeFireball> entityTypes, World world, int intYield, Vec3D vec, double x, double y, double z) {
        super(entityTypes, world);
        this.yield = intYield;
        this.setPosition(x, y, z);
        this.setMot(vec);
    }

    public CustomEntityLargeFireball(World world, EntityLiving entityLiving, double d0, double d1, double d2, int intYield) {
        super(world, entityLiving, d0, d1, d2);
        this.yield = intYield;
        this.setShooter(entityLiving);
    }

    public CustomEntityLargeFireball(World world, EntityLiving entityLiving, double d0, double d1, double d2, int intYield, boolean summonLightning) {
        super(world, entityLiving, d0, d1, d2);
        this.yield = intYield;
        this.summonLightning = summonLightning;
        this.setShooter(entityLiving);
    }

    @Override
    protected void a(MovingObjectPosition movingObjectPosition) { // in order for the new yield value to register
        if (movingObjectPosition instanceof MovingObjectPositionEntity) {
            if (((MovingObjectPositionEntity)movingObjectPosition).getEntity() instanceof EntityPlayer) { /* large fireballs can only impact players */
                this.a((MovingObjectPositionEntity)movingObjectPosition);
            }

            return;
        }

        // otherwise if hit block
        super.a(movingObjectPosition);
        if (!this.world.isClientSide && this.yield > 1) {
            boolean flag = this.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING);
            this.world.createExplosion(null, this.locX(), this.locY(), this.locZ(), (float) this.yield, flag, flag ? Explosion.Effect.DESTROY : Explosion.Effect.NONE);

            if (this.summonLightning && ExtremeDifficultyPlugin.plugin != null) { // summon thor lightning
                new RunnableLightningStorm(this.world, new Location(this.world.getWorld(), this.locX(), this.locY(), this.locZ()), 10.0, random.nextInt(3) + 8, false).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, random.nextInt(3) + 2);
            }
        }
    }

    @Override
    protected void a(MovingObjectPositionEntity movingObjectPositionEntity) {
        if (!this.world.isClientSide) {
            Entity entity = movingObjectPositionEntity.getEntity();

            if (!(entity instanceof EntityPlayer)) { /* large fireballs can only impact players */
                return;
            }

            Entity entity1 = this.getShooter();

            entity.damageEntity(DamageSource.fireball(this, entity1), 1.0F); /* large fireballs only do 1 direct damage */
            if (entity1 instanceof EntityLiving) {
                this.a((EntityLiving) entity1, entity);
            }

            boolean flag = this.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING);
            this.world.createExplosion(null, this.locX(), this.locY(), this.locZ(), (float) this.yield, flag, flag ? Explosion.Effect.DESTROY : Explosion.Effect.NONE);

            if (this.summonLightning && ExtremeDifficultyPlugin.plugin != null) { // summon thor lightning
                new RunnableLightningStorm(this.world, new Location(this.world.getWorld(), this.locX(), this.locY(), this.locZ()), 10.0, random.nextInt(3) + 8, false).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, random.nextInt(3) + 2);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived > 100) { /* large fireballs despawn after 5 seconds to reduce lag */
            this.die();
        }
    }
}
