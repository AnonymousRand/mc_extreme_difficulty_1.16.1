package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.NewPathfinderGoalCobweb;
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
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobweb(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived == 10) { /**cows move twice as fast and have 20 health*/
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.4);
            this.setHealth(20.0f);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(20.0);

            if (this.random.nextDouble() < 0.04) { /**cows have a 1 in 25 chance to spawn as a mooshroom*/
                CustomEntityMushroomCow newMooshroom = new CustomEntityMushroomCow(this.getWorld());
                newMooshroom.setPosition(this.locX(), this.locY(), this.locZ());
                this.getWorld().addEntity(newMooshroom, CreatureSpawnEvent.SpawnReason.NATURAL);
                this.die();
            }
        }
    }
}
