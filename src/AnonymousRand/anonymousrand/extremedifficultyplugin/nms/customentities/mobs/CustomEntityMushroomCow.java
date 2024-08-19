package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.CustomPathfinderGoalMoveFasterInCobweb;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.CustomPathfinderGoalGetBuffedByMobs;
import net.minecraft.server.v1_16_R1.*;

public class CustomEntityMushroomCow extends EntityMushroomCow {

    public CustomEntityMushroomCow(World world) {
        super(EntityTypes.MOOSHROOM, world);
        this.initCustom();
    }

    private void initCustom() {
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Overridden vanilla functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this));
        /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this));
    }

    @Override
    public EnumInteractionResult b(EntityHuman human, EnumHand enumhand) {
        ItemStack heldItem = human.b(enumhand);

        if ((heldItem.getItem() == Items.BOWL && !this.isBaby()) || (heldItem.getItem() == Items.SHEARS && this.canShear())) {
            /* mooshrooms explode when they are milked/sheared */
            this.world.createExplosion(this, this.locX(), this.locY(), this.locZ(), 20.0F, false, Explosion.Effect.DESTROY);
            this.die();
        }

        return super.b(human, enumhand);
    }
}
