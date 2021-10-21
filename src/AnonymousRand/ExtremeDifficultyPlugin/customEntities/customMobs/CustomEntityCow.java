package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CustomEntityCow extends EntityCow {

    public CustomEntityCow(World world) {
        super(EntityTypes.COW, world);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived == 10) { /**cows move twice as fast and have 20 health*/
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.4);
            this.setHealth(20.0f);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(20.0);

            //todo: check
            if (random.nextDouble() < 0.03333333333) { /**cows have a 1 in 30 chance to spawn as a mooshroom*/
                CustomEntityMushroomCow newMooshroom = new CustomEntityMushroomCow(this.getWorld());
                newMooshroom.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
                this.getWorld().addEntity(newMooshroom, CreatureSpawnEvent.SpawnReason.NATURAL);
                this.die();
            }
        }

        Location thisLoc = new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ());
        if (thisLoc.getBlock().getType() == org.bukkit.Material.COBWEB) { /**non-player mobs gain Speed 11 while in a cobweb (approx original speed)*/
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, 2, 10));
        }
    }
}
