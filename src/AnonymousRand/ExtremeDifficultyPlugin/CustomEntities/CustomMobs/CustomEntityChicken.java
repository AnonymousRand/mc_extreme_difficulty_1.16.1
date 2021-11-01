package AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.CustomMobs;

import AnonymousRand.ExtremeDifficultyPlugin.CustomGoals.NewPathfinderGoalCobweb;
import AnonymousRand.ExtremeDifficultyPlugin.CustomGoals.NewPathfinderGoalGetBuffedByMobs;
import AnonymousRand.ExtremeDifficultyPlugin.util.SpawnLivingEntity;
import net.minecraft.server.v1_16_R1.*;

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
                new SpawnLivingEntity(this.getWorld(), new CustomEntityChickenAggressive(this.getWorld()), 1, null, null, this, true, true).run();
            }
        }
    }
}
