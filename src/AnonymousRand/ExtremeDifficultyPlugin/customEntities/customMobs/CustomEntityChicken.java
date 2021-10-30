package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.NewPathfinderGoalCobweb;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.NewPathfinderGoalGetBuffedByMobs;
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
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobweb(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived == 10) { /**chickens move twice as fast*/
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.5);

            if (this.random.nextDouble() < 0.25) { /**25% chance to spawn in as an aggressive chicken instead*/
                CustomEntityChickenAggressive newChicken = new CustomEntityChickenAggressive(this.getWorld());
                newChicken.setPosition(this.locX(), this.locY(), this.locZ());
                this.getWorld().addEntity(newChicken, CreatureSpawnEvent.SpawnReason.NATURAL);
                this.die();
            }
        }
    }
}
