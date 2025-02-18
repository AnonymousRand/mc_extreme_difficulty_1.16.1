package AnonymousRand.anonymousrand.plugin.nms.customentities.projectiles;

import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.CustomEntityLlama;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.CustomEntityLlamaTrader;
import AnonymousRand.anonymousrand.plugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;

public class CustomEntityLlamaSpit extends EntityLlamaSpit {

    private double damage;

    public CustomEntityLlamaSpit(World world, EntityLlama entityLlama, double damage) {
        super(world, entityLlama);
        this.damage = damage;
    }

    @Override
    protected void a(MovingObjectPositionEntity movingObjectPositionEntity) {
        Entity entity = this.getShooter();

        if (entity instanceof EntityLiving) {
            movingObjectPositionEntity.getEntity().damageEntity(DamageSource.a(this, (EntityLiving) entity).c(), (float) this.damage); /* llama spit does at least 12 damage */
        }

        if (this.getShooter() instanceof CustomEntityLlama) {
            if (((CustomEntityLlama)this.getShooter()).getAttacks() >= 25) { /* after 25 attacks, llama spit explodes on hit */
                this.world.createExplosion(null, this.locX(), this.locY(), this.locZ(), 3.0F, true, Explosion.Effect.DESTROY);
            }
        } else if (this.getShooter() instanceof CustomEntityLlamaTrader) {
            if (((CustomEntityLlamaTrader)this.getShooter()).getAttacks() >= 25) { /* after 25 attacks, trader llama spit summons a wandering trader on hit */
                new SpawnEntity(this.world, new EntityVillagerTrader(EntityTypes.WANDERING_TRADER, this.world), 1, null, null, this, false, true);
            }
        }
    }
}
