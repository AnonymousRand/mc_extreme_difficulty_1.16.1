package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderTargetCondition;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;

public class CustomEntityMushroomCow extends EntityMushroomCow {

    public CustomEntityMushroomCow(World world) {
        super(EntityTypes.MOOSHROOM, world);
    }

    @Override
    public EnumInteractionResult b(EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.b(enumhand);

        if ((itemstack.getItem() == Items.BOWL && !this.isBaby()) || (itemstack.getItem() == Items.SHEARS && this.canShear())) {
            EntityPlayer player = this.getWorld().a(EntityPlayer.class, new CustomPathfinderTargetCondition(), this, this.locX(), this.locY(), this.locZ(), this.getBoundingBox().grow(6.0, 6.0, 6.0)); //get closest player within bounding box

            if (player != null) { /**mooshrooms inflict these effects when they are milked/sheared*/
                player.addEffect(new MobEffect(MobEffects.WEAKNESS, 1200, 255));
                player.addEffect(new MobEffect(MobEffects.SLOWER_DIG, 1200, 255));
            }

            this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 15.0f, false, Explosion.Effect.DESTROY); /**mooshrooms explode massively and die when they are milked/sheared*/
            this.die();

            Bukkit.broadcastMessage("You've gotten food...but at what cost?");
        }

        return super.b(entityhuman, enumhand);
    }
}
