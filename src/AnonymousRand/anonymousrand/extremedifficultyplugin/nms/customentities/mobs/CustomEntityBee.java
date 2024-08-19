package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IGoalRemovingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.VanillaPathfinderGoalsRemove;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack.CustomPathfinderGoalMeleeAttack;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityBee extends EntityBee implements ICustomHostile, IGoalRemovingMob {

    /* Ignores line of sight and y-level for initially finding a player target and maintaining it as the target,
     * as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
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
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(5.0);
        this.setHealth(5.0F);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `ICustomHostile`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public double getDetectionRange() {
        /* Bees have 16 block detection range */
        return 16.0;
    }

    public boolean ignoresLOS() {
        return IGNORE_LOS;
    }

    public boolean ignoresY() {
        return IGNORE_Y;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `IGoalRemovingMob`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void initGoalRemovingMob() {
        VanillaPathfinderGoalsRemove.removePathfinderGoals(this);
    }

    public PathfinderGoalSelector getVanillaGoalSelector() {
        return super.goalSelector;
    }

    public PathfinderGoalSelector getVanillaTargetSelector() {
        return super.targetSelector;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Overridden vanilla functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this));                              /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this));                                 /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack<>(this, 1.5));                              /* Bees move speed multiplier when angry 1.4 -> 1.5 */
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget<>(this));                                /* Always retaliates against players and teleports to them if they are out of range/do not have line of sight, but doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */ // todo does the listener actually not work sometimes? also if this is no longer needed, don't remove old goal in igoalremovingmob
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the nearest option */
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
