package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntitySheep extends EntitySheep {

    public CustomEntitySheep(World world) {
        super(EntityTypes.SHEEP, world);
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.46); /* sheep move 2x faster and have 20 health */
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(20.0);
        this.setHealth(20.0F);
    }

    @Override
    public void tick() {
        super.tick();

        /* Sheep explode when they are less than 2.5 blocks away from nearest player */
        if (this.random.nextDouble() < 0.2) {
            EntityHuman nearestPlayer = NMSUtil.getNearestEntityFromList(this.getWorld().getPlayers(), null, this);
            if (nearestPlayer != null && NMSUtil.distSq(this, nearestPlayer, false) <= 6.25) {
                this.world.createExplosion(this, this.locX(), this.locY(), this.locZ(), 2.0F, true, Explosion.Effect.DESTROY);
                this.die();
            }
        }
    }
}
