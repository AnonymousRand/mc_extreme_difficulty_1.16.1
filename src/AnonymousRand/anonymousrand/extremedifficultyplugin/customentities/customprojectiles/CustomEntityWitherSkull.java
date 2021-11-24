package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;

public class CustomEntityWitherSkull extends EntityWitherSkull {

    public CustomEntityWitherSkull(World world, EntityLiving entityliving, double d0, double d1, double d2) {
        super(world, entityliving, d0, d1, d2);
    }

    @Override
    protected void a(MovingObjectPositionEntity movingobjectpositionentity) {
        if (!this.world.isClientSide) {
            Entity entity = movingobjectpositionentity.getEntity();

            if (!(entity instanceof EntityPlayer)) { /**wither skulls can only impact players*/
                return;
            }

            Entity entity1 = this.getShooter();
            boolean flag;

            if (entity1 instanceof EntityLiving) {
                EntityLiving entityliving = (EntityLiving) entity1;

                flag = entity.damageEntity(DamageSource.a(this, (Entity)entityliving), 0.5F); /**wither skulls only do 0.5 direct damage*/
                if (flag) {
                    if (entity.isAlive()) {
                        this.a(entityliving, entity);
                    } else {
                        entityliving.heal(20.0F); /**withers heal 20 health per entity killed instead of 5 since they only attack players*/
                    }
                }
            } else {
                flag = entity.damageEntity(DamageSource.MAGIC, 5.0F);
            }

            if (flag) {
                byte b0;

                if (this.world.getDifficulty() == EnumDifficulty.NORMAL) { /**wither skulls also inflict 10 seconds of wither 2 in easy mode and 20 seconds instead in hard mode*/
                    b0 = 20;
                } else if (this.world.getDifficulty() == EnumDifficulty.HARD) {
                    b0 = 40;
                } else {
                    b0 = 10;
                }

                ((EntityLiving)entity).addEffect(new MobEffect(MobEffects.WITHER, 20 * b0, 1));

                Explosion.Effect explosion_effect = this.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING) ? Explosion.Effect.DESTROY : Explosion.Effect.NONE;
                this.world.createExplosion(this, this.locX(), this.locY(), this.locZ(), this.isCharged() ? 2.0F : 1.0F, false, explosion_effect); /**blue skulls explode power 2*/

            }
        }
    }

    @Override
    protected void a(MovingObjectPosition movingobjectposition) {
        if (movingobjectposition instanceof MovingObjectPositionEntity) {
            if (((MovingObjectPositionEntity)movingobjectposition).getEntity() instanceof EntityPlayer) { /**wither skulls can only impact players*/
                this.a((MovingObjectPositionEntity)movingobjectposition);
            }

            return;
        }

        //otherwise if hit block
        super.a(movingobjectposition);
        if (!this.world.isClientSide && this.isCharged()) {
            Explosion.Effect explosion_effect = this.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING) ? Explosion.Effect.DESTROY : Explosion.Effect.NONE;
            this.world.createExplosion(this, this.locX(), this.locY(), this.locZ(), 2.0F, false, explosion_effect); /**blue skulls explode power 2*/
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived >= 200) { /**wither skulls despawn after 10 seconds to reduce lag*/
            this.die();
        }
    }
}
