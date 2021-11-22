package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalCobwebMoveFaster;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;

public class CustomEntityChicken extends EntityChicken {
    
    public CustomEntityChicken(World world) {
        super(EntityTypes.CHICKEN, world);
        this.a(PathType.LAVA, 0.0F); /**no longer avoids lava*/
        this.a(PathType.DAMAGE_FIRE, 0.0F); /**no longer avoids fire*/
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.5); /**chickens move twice as fast*/
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived == 5) {
            if (random.nextDouble() < 0.25) { /**25% chance to spawn in as an aggressive chicken instead*/
                new SpawnEntity(this.getWorld(), new CustomEntityChickenAggressive(this.getWorld()), 1, null, null, this, true, true);
            }
        }
    }
}
