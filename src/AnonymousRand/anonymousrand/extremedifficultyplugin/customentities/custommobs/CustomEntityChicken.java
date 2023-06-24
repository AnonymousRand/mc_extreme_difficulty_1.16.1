package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalCobwebMoveFaster;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.EntityChicken;
import net.minecraft.server.v1_16_R1.EntityTypes;
import net.minecraft.server.v1_16_R1.GenericAttributes;
import net.minecraft.server.v1_16_R1.World;

public class CustomEntityChicken extends EntityChicken {
    
    public CustomEntityChicken(World world) {
        super(EntityTypes.CHICKEN, world);
        this.initAttributes();
    }

    public void initAttributes() {
        /** Chickens move twice as fast */
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.5);
    }

    //////////////////////  Other or vanilla functions  //////////////////////
    @Override
    public void initPathfinder() {
        super.initPathfinder();
        /** Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this));
        /** Takes buffs from bats and piglins etc. */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));
    }

    @Override
    public void tick() {
        super.tick();

        /** 25% chance to spawn in as an aggressive chicken instead */
        if (this.ticksLived == 5) {
            if (random.nextDouble() < 0.25) {
                new SpawnEntity(this.getWorld(), new CustomEntityChickenAggressive(this.getWorld()), 1, null, null, this, true, true);
            }
        }
    }
}
