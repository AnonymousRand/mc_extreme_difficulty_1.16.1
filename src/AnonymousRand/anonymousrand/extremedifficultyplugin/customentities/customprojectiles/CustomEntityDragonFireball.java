package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityAreaEffectCloud;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;

import java.util.Iterator;
import java.util.List;

public class CustomEntityDragonFireball extends EntityDragonFireball {

    public CustomEntityDragonFireball(World world, EntityLiving entityLiving, double d0, double d1, double d2) {
        super(world, entityLiving, d0, d1, d2);
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

        for (int i = -3; i < 3; i++) { /**area effect clouds are 6 blocks high but only last 15 seconds and take 5 ticks less to start doing damage*/
            entities = this.world.a(EntityPlayer.class, this.getBoundingBox().grow(11.5, 128.0, 11.5));
            entityareaeffectcloud = new CustomEntityAreaEffectCloud(this.world, 3.0F, 300, 15);

            if (shooter instanceof EntityLiving) {
                entityareaeffectcloud.setSource((EntityLiving)shooter);
            }

            entityareaeffectcloud.setPosition(this.locX(), this.locY() + i, this.locZ());
            entityareaeffectcloud.setParticle(Particles.DRAGON_BREATH);
            entityareaeffectcloud.setRadiusPerTick((7.0F - entityareaeffectcloud.getRadius()) / (float) entityareaeffectcloud.getDuration());
            entityareaeffectcloud.addEffect(new MobEffect(MobEffects.HARM, 1, 2));

            for (EntityLiving entity : entities) {
                if (this.d(entity.getPositionVector()) < 64.0D) { /**area effect clouds snap on to location of closest player within 8 blocks horizontally*/
                    entityareaeffectcloud.setPosition(entity.locX(), entity.locY() + i, entity.locZ());
                    break;
                }
            }

            this.getWorld().triggerEffect(2006, this.getChunkCoordinates(), this.isSilent() ? -1 : 1);
            this.getWorld().addEntity(entityareaeffectcloud);
        }

        this.die();
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /**for determining distance to entities, y level does not matter, eg. mob follow range, attacking (can hit player no matter the y level)*/
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }
}
