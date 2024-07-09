package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.projectiles;

import AnonymousRand.anonymousrand.extremedifficultyplugin.ExtremeDifficultyPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityAreaEffectCloud;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableLightningStorm;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableMeteorRain;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;

import java.util.List;

public class CustomEntityDragonFireballSuper extends EntityDragonFireball {

    private final boolean spawnMobs;

    public CustomEntityDragonFireballSuper(World world, EntityLiving entityLiving, double d0, double d1, double d2, boolean spawnMobs) {
        super(world, entityLiving, d0, d1, d2);
        this.spawnMobs = spawnMobs;
    }

    @Override
    protected void a(MovingObjectPosition movingObjectPosition) {
        if (movingObjectPosition.getType() == MovingObjectPosition.EnumMovingObjectType.ENTITY) {
            if (!(((MovingObjectPositionEntity)movingObjectPosition).getEntity() instanceof EntityPlayer)) { /* area effect clouds can't collide with non-player mobs/entities */
                return;
            }
        } else if (movingObjectPosition.getType() != MovingObjectPosition.EnumMovingObjectType.BLOCK) {
            return;
        }

        Entity shooter = this.getShooter();
        List<EntityLiving> entities;
        CustomEntityAreaEffectCloud entityAreaEffectCloud;

        for (int i = -3; i < 3; i++) { /* area effect clouds are 6 blocks high, 9 blocks wide and take 5 ticks less to start doing damage, but only last 10 seconds */
            entities = this.world.a(EntityPlayer.class, this.getBoundingBox().grow(17.0, 128.0, 17.0));
            entityAreaEffectCloud = new CustomEntityAreaEffectCloud(this.world, 4.0F, 200, 15);

            if (shooter instanceof EntityLiving) {
                entityAreaEffectCloud.setSource((EntityLiving) shooter);
            }

            entityAreaEffectCloud.setPosition(this.locX(), this.locY() + i, this.locZ());
            entityAreaEffectCloud.setRadiusPerTick((7.0F - entityAreaEffectCloud.getRadius()) / (float) entityAreaEffectCloud.getDuration());
            entityAreaEffectCloud.addEffect(new MobEffect(MobEffects.HARM, 1, 3)); /* super fireball area effect clouds do twice as much damage */

            for (EntityLiving entity : entities) {
                if (this.d(entity.getPositionVector()) < 144.0D) { /* super fireball area effect clouds snap on to location of closest player within 12 blocks horizontally */
                    entityAreaEffectCloud.setPosition(entity.locX(), entity.locY() + i, entity.locZ());
                    break;
                }
            }

            this.world.triggerEffect(2006, this.getChunkCoordinates(), this.isSilent() ? -1 : 3);
            this.world.addEntity(entityAreaEffectCloud);
        }

        this.world.createExplosion(this, this.locX(), this.locY(), this.locZ(), 3.0F, false, Explosion.Effect.DESTROY); /* super fireballs explode on hit */
        this.die();
    }

    @Override
    public void die() {
        super.die();

        if (this.spawnMobs) { /* most super fireballs summon different mobs/effects on impact */
            double rand = random.nextDouble();

            if (rand < 0.125) {
                new RunnableLightningStorm(this.world, new Location(this.world.getWorld(), this.locX(), this.locY(), this.locZ()), random.nextInt(11) + 35).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, random.nextInt(4) + 2);
            } else if (rand < 0.225) {
                new RunnableMeteorRain(this, 1, 40.0, 10).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 1L);
                new RunnableMeteorRain(this, 2, 40.0, 7).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 1L);
                new RunnableMeteorRain(this, 3, 40.0, 6).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 1L);
            } else if (rand < 0.325) {
                new SpawnEntity(this.world, new CustomEntityGuardianElder(this.world), 1, null, null, this, false, true);
            } else if (rand < 0.425) {
                new SpawnEntity(this.world, new CustomEntityZombieThor(this.world), 1, null, null, this, false, true);
            } else if (rand < 0.525) {
                new SpawnEntity(this.world, new CustomEntityBee(this.world), 6, null, null, this, false, true);
            } else if (rand < 0.625) {
                new SpawnEntity(this.world, new CustomEntitySlimeMagmaCube(this.world, 16), 1, null, null, this, false, true);
            } else if (rand < 0.725) {
                new SpawnEntity(this.world, new CustomEntityBlaze(this.world), 4, null, null, this, false, false);
            } else if (rand < 0.8) {
                new SpawnEntity(this.world, new CustomEntityIllusioner(this.world), 2, null, null, this, false, true);
            } else if (rand < 0.85) {
                new SpawnEntity(this.world, new CustomEntityDrowned(this.world), 1, null, null, this, false, true);
                new SpawnEntity(this.world, new CustomEntityWitch(this.world), 1, null, null, this, false, true);
            } else if (rand < 0.9) {
                new SpawnEntity(this.world, new CustomEntityEvoker(this.world), 1, null, null, this, false, true);
                new SpawnEntity(this.world, new CustomEntityRavager(this.world), 1, null, null, this, false, true);
            } else if (rand < 0.95) {
                new SpawnEntity(this.world, new CustomEntitySheepAggressive(this.world), 1, null, null, this, false, true);
            } else {
                new SpawnEntity(this.world, new EntityEnderCrystal(EntityTypes.END_CRYSTAL, this.world), 1, null, null, this, false, true);
            }
        }
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /* for determining distance to entities, y-level does not matter, e.g. mob follow range, attacking (can hit player no matter the y-level) */
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }
}
