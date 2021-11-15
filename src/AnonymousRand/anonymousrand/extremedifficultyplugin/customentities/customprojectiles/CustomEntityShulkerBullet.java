package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityShulker;
import net.minecraft.server.v1_16_R1.*;

public class CustomEntityShulkerBullet extends EntityShulkerBullet {

    public CustomEntityShulkerBullet(World world, EntityLiving owner, Entity target, EnumDirection.EnumAxis enumdirection_enumaxis) {
        super(world, owner, target, enumdirection_enumaxis);
    }

    public CustomEntityShulkerBullet(World world, double x, double y, double z, EntityLiving owner, Entity target) { //custom constructor for shulker death bullets
        super(EntityTypes.SHULKER_BULLET, world);
        this.setShooter(owner);
        this.setPosition(x, y, z);
        this.setTarget(target);
    }

    @Override
    protected void a(MovingObjectPositionEntity movingobjectpositionentity) {
        Entity entity = movingobjectpositionentity.getEntity();
        if (!(entity instanceof EntityHuman)) { /**shulker bullets can only hit players*/
            return;
        }

        super.a(movingobjectpositionentity);

        EntityHuman entityHuman = (EntityHuman)entity;
        boolean shield = entityHuman.isBlocking();

        if (entityHuman.hasEffect(MobEffects.LEVITATION) || shield) { /**levitation is only applied for 8 seconds*/
            entityHuman.removeEffect(MobEffects.LEVITATION);

            if (this.getShooter() instanceof CustomEntityShulker) {
                if (((CustomEntityShulker)this.getShooter()).attacks >= 70) { /**after 70 attacks, shulkers' bullets inflict levitation for 50% longer and also inflict blindness for 2.5 seconds*/
                    entityHuman.addEffect(new MobEffect(MobEffects.LEVITATION, shield ? 180 : 240, shield ? 1 : 0));
                    entityHuman.addEffect(new MobEffect(MobEffects.BLINDNESS, 50));
                    return;
                }
            }

            entityHuman.addEffect(new MobEffect(MobEffects.LEVITATION, shield ? 120 : 160, shield ? 1 : 0)); /**shulker bullets give levitation 2 when hitting a shield for 6 seconds*/
        }
    }

    @Override
    protected void a(MovingObjectPositionBlock movingobjectpositionblock) { /**shulker bullets explode when hitting a block*/
        super.a(movingobjectpositionblock);
        this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 3.0F, false, Explosion.Effect.DESTROY);
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) { /**shulker bullets do not do anything when hit*/
        return false;
    }
}
