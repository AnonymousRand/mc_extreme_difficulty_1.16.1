package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles;

import net.minecraft.server.v1_16_R1.*;

public class CustomEntitySmallFireball extends EntitySmallFireball {

    public CustomEntitySmallFireball(World world, EntityLiving entityLiving, double d0, double d1, double d2) {
        super(world, entityLiving, d0, d1, d2);
        this.setShooter(entityLiving);
        this.isIncendiary = true; // make sure it lights the player on fire
    }

    @Override
    protected void a(MovingObjectPositionEntity movingObjectPositionEntity) {
        if (!this.world.isClientSide) {
            Entity entity = movingObjectPositionEntity.getEntity();

            if (!(entity instanceof EntityPlayer)) { /** small fireballs can only impact players */
                return;
            }

            if (!entity.isFireProof()) {
                Entity entity1 = this.getShooter();
                int i = entity.getFireTicks();

                entity.setOnFire(5);
                boolean flag = entity.damageEntity(DamageSource.fireball(this, entity1), 1.5F); /** small fireballs only do 1.5 direct damage */

                if (!flag) {
                    entity.setFireTicks(i);
                } else if (entity1 instanceof EntityLiving) {
                    this.a((EntityLiving)entity1, entity);
                }
            }
        }
    }

    @Override
    protected void a(MovingObjectPosition movingObjectPosition) { // most entity collisions are actually registered here
        if (movingObjectPosition instanceof MovingObjectPositionEntity) {
            this.a((MovingObjectPositionEntity)movingObjectPosition);
        } else {
            super.a(movingObjectPosition);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived > 70) { /** small fireballs despawn after 3.5 seconds to reduce lag */
            this.die();
        }
    }
}
