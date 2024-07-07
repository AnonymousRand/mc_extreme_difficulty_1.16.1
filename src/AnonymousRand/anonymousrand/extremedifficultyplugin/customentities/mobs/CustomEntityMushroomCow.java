package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderTargetCondition;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalMoveFasterInCobweb;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import net.minecraft.server.v1_16_R1.*;

public class CustomEntityMushroomCow extends EntityMushroomCow {

    public CustomEntityMushroomCow(World world) {
        super(EntityTypes.MOOSHROOM, world);
        this.initCustom();
    }

    private void initCustom() {
        /* No longer avoids lava and fire */
        this.a(PathType.LAVA, 0.0F);
        this.a(PathType.DAMAGE_FIRE, 0.0F);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                               Overridden vanilla functions                                //
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
    public EnumInteractionResult b(EntityHuman entityHuman, EnumHand enumhand) {
        ItemStack itemstack = entityHuman.b(enumhand);

        if ((itemstack.getItem() == Items.BOWL && !this.isBaby()) || (itemstack.getItem() == Items.SHEARS && this.canShear())) {
            EntityPlayer player = this.getWorld().a(EntityPlayer.class, new CustomPathfinderTargetCondition(), this, this.locX(), this.locY(), this.locZ(), this.getBoundingBox().grow(6.0, 6.0, 6.0)); // get closest player within bounding box

            if (player != null) { /* mooshrooms inflict these effects when they are milked/sheared */
                player.addEffect(new MobEffect(MobEffects.WEAKNESS, 1200, 255));
                player.addEffect(new MobEffect(MobEffects.SLOWER_DIG, 1200, 255));
            }

            this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 20.0F, false, Explosion.Effect.DESTROY); /* mooshrooms explode massively and die when they are milked/sheared */
            this.die();
        }

        return super.b(entityHuman, enumhand);
    }
}
