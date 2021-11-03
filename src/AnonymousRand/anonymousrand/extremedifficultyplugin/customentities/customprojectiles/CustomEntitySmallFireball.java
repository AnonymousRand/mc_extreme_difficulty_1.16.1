package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles;

import net.minecraft.server.v1_16_R1.*;

public class CustomEntitySmallFireball extends EntitySmallFireball {

    public CustomEntitySmallFireball(World world, EntityLiving entityliving, double d0, double d1, double d2) {
        super(world, entityliving, d0, d1, d2);
        this.setShooter(entityliving);
        this.isIncendiary = true; //make sure it lights the player on fire
    }

    @Override
    protected void a(MovingObjectPositionEntity movingobjectpositionentity) {
        if (!this.world.isClientSide) {
            Entity entity = movingobjectpositionentity.getEntity();

            if (!entity.isFireProof()) {
                Entity entity1 = this.getShooter();
                int i = entity.getFireTicks();

                entity.setOnFire(4);
                boolean flag = entity.damageEntity(DamageSource.fireball(this, entity1), 1.0F); /**small fireballs only do 1 direct damage and do 1 less fire tick damage*/

                if (!flag) {
                    entity.setFireTicks(i);
                } else if (entity1 instanceof EntityLiving) {
                    this.a((EntityLiving) entity1, entity);
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived > 70) { /**small fireballs despawn after 3.5 seconds to reduce lag*/
            this.die();
        }
    }
}
