package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityAreaEffectCloud;
import net.minecraft.server.v1_16_R1.*;

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

        for (int i = -3; i < 3; i++) { /**area effect clouds are 6 blocks high, 9 blocks wide and take 5 ticks less to start doing damage*/
            entities = this.world.a(EntityPlayer.class, this.getBoundingBox().grow(17.0, 128.0, 17.0));
            entityareaeffectcloud = new CustomEntityAreaEffectCloud(this.world, 4.0F, 600, 15);

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

        if (this.spawnMobs) {

        }
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /**for determining distance to entities, y level does not matter, eg. mob follow range, attacking (can hit player no matter the y level)*/
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }
}
