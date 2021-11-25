package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityAreaEffectCloud;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
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
    protected void a(MovingObjectPosition movingobjectposition) {
        if (movingobjectposition.getType() == MovingObjectPosition.EnumMovingObjectType.ENTITY) {
            if (!(((MovingObjectPositionEntity)movingobjectposition).getEntity() instanceof EntityPlayer)) { /**area effect clouds can't collide with non-player mobs/entities*/
                return;
            }
        } else if (movingobjectposition.getType() != MovingObjectPosition.EnumMovingObjectType.BLOCK) {
            return;
        }

        Entity shooter = this.getShooter();
        List<EntityLiving> entities;
        CustomEntityAreaEffectCloud entityareaeffectcloud;

        for (int i = -3; i < 3; i++) { /**area effect clouds are 6 blocks high, 9 blocks wide and take 5 ticks less to start doing damage, but only last 10 seconds*/
            entities = this.world.a(EntityPlayer.class, this.getBoundingBox().grow(17.0, 128.0, 17.0));
            entityareaeffectcloud = new CustomEntityAreaEffectCloud(this.world, 4.0F, 200, 15);

            if (shooter instanceof EntityLiving) {
                entityareaeffectcloud.setSource((EntityLiving)shooter);
            }

            entityareaeffectcloud.setPosition(this.locX(), this.locY() + i, this.locZ());
            entityareaeffectcloud.setRadiusPerTick((7.0F - entityareaeffectcloud.getRadius()) / (float) entityareaeffectcloud.getDuration());
            entityareaeffectcloud.addEffect(new MobEffect(MobEffects.HARM, 1, 3)); /**super fireball area effect clouds do twice as much damage*/

            for (EntityLiving entity : entities) {
                if (this.d(entity.getPositionVector()) < 144.0D) { /**super fireball area effect clouds snap on to location of closest player within 12 blocks horizontally*/
                    entityareaeffectcloud.setPosition(entity.locX(), entity.locY() + i, entity.locZ());
                    break;
                }
            }

            this.getWorld().triggerEffect(2006, this.getChunkCoordinates(), this.isSilent() ? -1 : 3);
            this.getWorld().addEntity(entityareaeffectcloud);
        }

        this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 3.0F, false, Explosion.Effect.DESTROY); /**super fireballs explode on hit*/
        this.die();
    }

    @Override
    public void die() {
        super.die();

        if (this.spawnMobs) { /**most super fireballs summon different mobs/effects on impact*/
            double rand = random.nextDouble();

            if (rand < 0.125) {
                new RunnableLightningStorm(this.getWorld(), new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ()), random.nextInt(11) + 35).runTaskTimer(StaticPlugin.plugin, 0L, random.nextInt(4) + 2);
            } else if (rand < 0.225) {
                new RunnableMeteorRain(this, 1, 40.0, 10).runTaskTimer(StaticPlugin.plugin, 0L, 1L);
                new RunnableMeteorRain(this, 2, 40.0, 7).runTaskTimer(StaticPlugin.plugin, 0L, 1L);
                new RunnableMeteorRain(this, 3, 40.0, 6).runTaskTimer(StaticPlugin.plugin, 0L, 1L);
            } else if (rand < 0.325) {
                new SpawnEntity(this.getWorld(), new CustomEntityGuardianElder(this.getWorld()), 1, null, null, this, false, true);
            } else if (rand < 0.425) {
                new SpawnEntity(this.getWorld(), new CustomEntityZombieThor(this.getWorld()), 1, null, null, this, false, true);
            } else if (rand < 0.525) {
                new SpawnEntity(this.getWorld(), new CustomEntityBee(this.getWorld()), 6, null, null, this, false, true);
            } else if (rand < 0.625) {
                new SpawnEntity(this.getWorld(), new CustomEntitySlimeMagmaCube(this.getWorld(), 16), 1, null, null, this, false, true);
            } else if (rand < 0.725) {
                new SpawnEntity(this.getWorld(), new CustomEntityBlaze(this.getWorld()), 4, null, null, this, false, false);
            } else if (rand < 0.8) {
                new SpawnEntity(this.getWorld(), new CustomEntityIllusioner(this.getWorld()), 2, null, null, this, false, true);
            } else if (rand < 0.85) {
                new SpawnEntity(this.getWorld(), new CustomEntityDrowned(this.getWorld()), 1, null, null, this, false, true);
                new SpawnEntity(this.getWorld(), new CustomEntityWitch(this.getWorld()), 1, null, null, this, false, true);
            } else if (rand < 0.9) {
                new SpawnEntity(this.getWorld(), new CustomEntityEvoker(this.getWorld()), 1, null, null, this, false, true);
                new SpawnEntity(this.getWorld(), new CustomEntityRavager(this.getWorld()), 1, null, null, this, false, true);
            } else if (rand < 0.95) {
                new SpawnEntity(this.getWorld(), new CustomEntitySheepAggressive(this.getWorld()), 1, null, null, this, false, true);
            } else {
                new SpawnEntity(this.getWorld(), new EntityEnderCrystal(EntityTypes.END_CRYSTAL, this.getWorld()), 1, null, null, this, false, true);
            }
        }
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /**for determining distance to entities, y level does not matter, eg. mob follow range, attacking (can hit player no matter the y level)*/
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }
}
