package AnonymousRand.anonymousrand.plugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.plugin.nms.customgoals.CustomPathfinderGoalMoveFasterInCobweb;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.CustomPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.plugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.EntityChicken;
import net.minecraft.server.v1_16_R1.EntityTypes;
import net.minecraft.server.v1_16_R1.GenericAttributes;
import net.minecraft.server.v1_16_R1.World;

public class CustomEntityChicken extends EntityChicken {
    
    public CustomEntityChicken(World world) {
        super(EntityTypes.CHICKEN, world);
        this.initCustom();
    }

    protected void initCustom() {
        this.initAttributes();
    }

    protected void initAttributes() {
        /* Chickens move twice as fast */
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.5);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Overridden Vanilla Functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this));    /* Takes buffs from bats, piglins, etc. */
    }

    @Override
    public void tick() {
        super.tick();

        /* 25% chance to spawn as an aggressive chicken instead */ // todo why not by listener? also todo doesnt always work?
        if (this.ticksLived == 5) {
            if (random.nextDouble() < 0.25) {
                new SpawnEntity(this.world, new CustomEntityChickenAggressive(this.world), 1,
                        null, null, this, true, true);
            }
        }
    }
}
