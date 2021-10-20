package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Random;

public class CustomEntityChicken extends EntityChicken {
    
    public CustomEntityChicken(World world) {
        super(EntityTypes.CHICKEN, world);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived == 10) { /**chickens move twice as fast*/
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.5);
            this.spawnAggressive();
        }

        Location thisLoc = new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ());
        if (thisLoc.getBlock().getType() == org.bukkit.Material.COBWEB) { /**non-player mobs gain Speed 11 while in a cobweb (approx original speed)*/
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, 2, 10));
        }
    }

    private void spawnAggressive() {
        if (random.nextDouble() < 0.25) { /**25% chance to spawn in as an aggressive chicken instead*/
            CustomEntityChickenAggressive newChicken = new CustomEntityChickenAggressive(this.getWorld());
            newChicken.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
            this.getWorld().addEntity(newChicken, CreatureSpawnEvent.SpawnReason.NATURAL);
            this.die();
        }
    }
}
