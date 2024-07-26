package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.NewPathfinderGoalMoveFasterInCobweb;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.NewPathfinderGoalGetBuffedByMobs;
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
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this));
        /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));
    }

    @Override
    public EnumInteractionResult b(EntityHuman human, EnumHand enumhand) {
        ItemStack heldItem = human.b(enumhand);

        if ((heldItem.getItem() == Items.BOWL && !this.isBaby()) || (heldItem.getItem() == Items.SHEARS && this.canShear())) {
            /* mooshrooms inflict these effects when they are milked/sheared */
            human.addEffect(new MobEffect(MobEffects.WEAKNESS, 1200, 255));
            human.addEffect(new MobEffect(MobEffects.SLOWER_DIG, 1200, 255));

            this.world.createExplosion(this, this.locX(), this.locY(), this.locZ(), 20.0F, false, Explosion.Effect.DESTROY); /* mooshrooms explode massively and die when they are milked/sheared */
            this.die();
        }

        return super.b(human, enumhand);
    }
}
