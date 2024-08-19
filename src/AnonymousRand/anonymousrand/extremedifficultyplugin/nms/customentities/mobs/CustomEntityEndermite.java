package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack.CustomPathfinderGoalMeleeAttack;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.movement.CustomPathfinderGoalMeleeMovement;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityEndermite extends EntityEndermite
        implements ICustomHostile, IAttackLevelingMob, IGoalRemovingMob {

    /* Ignores line of sight and y-level for initially finding a player target and maintaining it as the target,
     * as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;

    public CustomEntityEndermite(World world) {
        super(EntityTypes.ENDERMITE, world);
        this.initCustom();
        this.initAttackLevelingMob();
        this.initGoalRemovingMob();
    }

    private void initCustom() {
        this.initAttributes();

        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);

        /* No longer despawns or takes up the mob cab */
        this.getBukkitEntity().setCustomName("Insert name here");
    }

    private void initAttributes() {
        /* Endermites move 60% faster and have 12 health, but only do 1 damage */
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.4);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(1.0);
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(12.0);
        this.setHealth(12.0F);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `ICustomHostile`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public double getDetectionRange() {
        /* Endermites have 20 block detection range */
        return 20.0;
    }

    public boolean ignoresLOS() {
        return IGNORE_LOS;
    }

    public boolean ignoresY() {
        return IGNORE_Y;
    }

    @Override
    public int bL() {
        return Integer.MAX_VALUE;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `IAttackLevelingMob`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private AttackLevelingController attackLevelingController = null;

    private void initAttackLevelingMob() {
        this.attackLevelingController = new AttackLevelingController(35, 60);
    }

    public int getAttacks() {
        return this.attackLevelingController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        int[] threshsMet = this.attackLevelingController.increaseAttacks(increase);
        for (int threshMet : threshsMet) {
            int[] attackThreshs = this.getAttacksThreshs();
            if (threshMet == attackThreshs[0]) {
                /* After 35 attacks, endermites get more knockback */
                this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(1.5);
            } else if (threshMet == attackThreshs[1]) {
                /* After 60 attacks, endermites get even more knockback, 14 max health and regen 2 */
                this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(2.5);
                ((LivingEntity) this.getBukkitEntity()).setMaxHealth(14);
                this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
            }
        }
    }

    public int[] getAttacksThreshs() {
        return this.attackLevelingController.getAttacksThreshs();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `IGoalRemovingMob`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void initGoalRemovingMob() {
        // remove vanilla HurtByTarget and NearestAttackableTarget goals to replace them with custom ones
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
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this));                                              /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this));                                                 /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new CustomPathfinderGoalBreakBlocksAround(this, 100, 1, 0, 1, 0, true));                        /* Breaks most blocks around the mob periodically */
        this.goalSelector.a(1, new CustomPathfinderGoalTeleportNearTargetYLevel(this, 1.0, random.nextDouble() * 3.0, 0.005)); /* Occasionally teleports to a spot closer in y-level to its target */
        this.goalSelector.a(2, new CustomPathfinderGoalMeleeAttack<>(this));
        this.goalSelector.a(2, new CustomPathfinderGoalMeleeMovement<>(this));
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget<>(this));                                                /* Always retaliates against players and teleports to them if they are out of range/do not have line of sight, but doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */ // todo does the listener actually not work sometimes? also if this is no longer needed, don't remove old goal in igoalremovingmob
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class));                 /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the nearest option */
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damageAmount) {
        boolean damageSuccess = super.damageEntity(damageSource, damageAmount);
        /* Endermites duplicate when hit by player and not killed */
        if (damageSuccess && this.isAlive() && damageSource.getEntity() instanceof EntityPlayer) {
            new SpawnEntity(this.world, new CustomEntityEndermite(this.world), 1, null, null, this, false, true);
        }

        return damageSuccess;
    }
}
