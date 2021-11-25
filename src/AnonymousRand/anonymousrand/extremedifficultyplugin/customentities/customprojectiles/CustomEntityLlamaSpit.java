package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityLlama;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityLlamaTrader;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;

public class CustomEntityLlamaSpit extends EntityLlamaSpit {

    private double damage;

    public CustomEntityLlamaSpit(World world, EntityLlama entityLlama, double damage) {
        super(world, entityLlama);
        this.damage = damage;
    }

    @Override
    protected void a(MovingObjectPositionEntity movingobjectpositionentity) {
        Entity entity = this.getShooter();

        if (entity instanceof EntityLiving) {
            movingobjectpositionentity.getEntity().damageEntity(DamageSource.a((Entity) this, (EntityLiving) entity).c(), (float)this.damage); /**llama spit does at least 16 damage*/
        }

        if (this.getShooter() instanceof CustomEntityLlama) {
            if (((CustomEntityLlama)this.getShooter()).attacks >= 25) { /**after 25 attacks, llama spit explodes on hit*/
                this.getWorld().createExplosion((Entity)null, this.locX(), this.locY(), this.locZ(), 3.0F, true, Explosion.Effect.DESTROY);
            }
        } else if (this.getShooter() instanceof CustomEntityLlamaTrader) {
            if (((CustomEntityLlamaTrader)this.getShooter()).attacks >= 25) { /**after 25 attacks, trader llama spit summons a wandering trader on hit*/
                new SpawnEntity(this.getWorld(), new EntityVillagerTrader(EntityTypes.WANDERING_TRADER, this.getWorld()), 1, null, null, this, false, true);
            }
        }
    }
}
