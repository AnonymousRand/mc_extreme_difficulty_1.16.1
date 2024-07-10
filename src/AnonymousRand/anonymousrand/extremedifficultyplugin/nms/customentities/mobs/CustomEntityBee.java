package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IGoalRemovingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.CustomPathfinderGoalMeleeAttack;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.NewPathfinderGoalMoveFasterInCobweb;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.NewPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityBee extends EntityBee implements ICustomHostile, IGoalRemovingMob {

    /* Ignores y-level and line of sight for initially finding a player target and maintaining it
       as the target, as well as for retaliating against players */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;
    private boolean firstSting;

    public CustomEntityBee(World world) {
        super(EntityTypes.BEE, world);
        initCustom();
        initGoalRemovingMob();
    }

    private void initCustom() {
        this.initAttributes();

        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);

        this.firstSting = true;
    }

    private void initAttributes() {
        /* Bees do 420 damage but only have 5 health */
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(420.0);
        this.setHealth(5.0F);
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(5.0);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                      ICustomHostile                                       //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /* Bees have 16 block detection range */
    public double getDetectionRange() {
        return 16.0;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                     IGoalRemovingMob                                      //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public PathfinderGoalSelector vanillaTargetSelector;

    private void initGoalRemovingMob() {
        this.vanillaTargetSelector = super.targetSelector;
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this);
    }

    public PathfinderGoalSelector getVanillaTargetSelector() {
        return this.vanillaTargetSelector;
    }
    
    public boolean getIgnoreLOS() {
        return IGNORE_LOS;
    }
    
    public boolean getIgnoreY() {
        return IGNORE_Y;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                               Overridden vanilla functions                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this));                                                       /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));                                                          /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack(this, 1.4));                                                      /* Continues attacking regardless of y-level and line of sight (the old goal stopped the mob from attacking even if it still has a target) */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, IGNORE_LOS, IGNORE_Y)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the closest option */
    }

    @Override
    public void tick() {
        super.tick();

        if (this.hasStung()) {
            /* Bees don't die from stinging */
            this.setHasStung(false);

            if (this.firstSting) {
                this.firstSting = false;

                /* Bees have a 50% chance to duplicate after the first sting */
                if (random.nextDouble() < 0.5) {
                    new SpawnEntity(this.world, new CustomEntityBee(this.world), 1, null,
                            null, this, false, true);
                }
            }
        }
    }
}
