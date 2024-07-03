package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityEndermite extends EntityEndermite implements ICustomHostile, IAttackLevelingMob, IGoalRemovingMob {

    private AttackController attackController;
    public PathfinderGoalSelector vanillaTargetSelector;

    public CustomEntityEndermite(World world) {
        super(EntityTypes.ENDERMITE, world);
        this.initCustom();
        this.initAttacks();
        this.initGoalRemoval();
    }

    ////////////////////////////  ICustomHostile  ////////////////////////////
    public void initCustom() {
        /** No longer avoids lava */
        this.a(PathType.LAVA, 0.0F);
        /** No longer avoids fire */
        this.a(PathType.DAMAGE_FIRE, 0.0F);

        /** No longer despawns or takes up the mob cab */
        this.getBukkitEntity().setCustomName("Insert name here");

        this.initAttributes();
    }

    private void initAttributes() {
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.4); /** endermites move 60% faster and have 12 health, but only do 1 damage */
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(1.0);
        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(12.0);
        this.setHealth(12.0F);
    }

    public double getFollowRange() { /** endmites have 20 block detection range (setting attribute doesn't work) */
        return 20.0;
    }

    //////////////////////////  IAttackLevelingMob  //////////////////////////
    public void initAttacks() {
        this.attackController = new AttackController(35, 60);
    }

    public int getAttacks() {
        return this.attackController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        for (int metThreshold : this.attackController.increaseAttacks(increase)) {
            int[] attackThresholds = this.attackController.getAttackThresholds();
            if (metThreshold == attackThresholds[0]) {
                /** After 35 attacks, endermites get more knockback */
                this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(1.5);
            } else if (metThreshold == attackThresholds[1]) {
                /** After 60 attacks, endermites get even more knockback, 14 max health and regen 2 */
                this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(2.5);
                ((LivingEntity)this.getBukkitEntity()).setMaxHealth(14);
                this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
            }
        }
    }

    ///////////////////////////  IGoalRemovingMob  ///////////////////////////
    public void initGoalRemoval() {
        this.vanillaTargetSelector = super.targetSelector;
        // remove vanilla HurtByTarget and NearestAttackableTarget goals to replace them with custom ones
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this);
    }

    public PathfinderGoalSelector getVanillaTargetSelector() {
        return this.vanillaTargetSelector;
    }

    /////////////////////  Overridden vanilla functions  /////////////////////
    @Override
    public void initPathfinder() {
        super.initPathfinder();
        /** Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this));
        /** Takes buffs from bats and piglins etc. */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 100, 1, 0, 1, 0, true)); /** custom goal that breaks blocks around the mob periodically except for diamond blocks, emerald blocks, nertherite blocks, and beacons */
        this.goalSelector.a(1, new NewPathfinderGoalTeleportToPlayerAdjustY(this, 1.0, random.nextDouble() * 3.0, 0.005)); /** custom goal that gives mob a chance every tick to teleport to a spot where its y level difference from its target is reduced if its y level difference is too large */
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /** uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (damagesource.getEntity() instanceof EntityPlayer && this.getHealth() - f > 0.0) { /** duplicates when hit by player and not killed */
            new SpawnEntity(this.getWorld(), new CustomEntityEndermite(this.getWorld()), 1, null, null, this, false, true);
        }

        return super.damageEntity(damagesource, f);
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityHuman = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (entityHuman != null) {
                double d0 = Math.pow(entityHuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityHuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); /** mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleepingdouble d0 = entityHuman.h(this); */
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (d0 > (double)j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 8; /** random despawn distance increased to 40 blocks */
                int l = k * k;

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && d0 > (double)l && this.isTypeNotPersistent(d0)) {
                    this.die();
                } else if (d0 < (double)l) {
                    this.ticksFarFromPlayer = 0;
                }
            }

        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    @Override
    public double g(double d0, double d1, double d2) {
        double d3 = this.locX() - d0; /** for determining distance to entities, y level does not matter, e.g. mob follow range, attacking (can hit player no matter the y level) */
        double d5 = this.locZ() - d2;

        return d3 * d3 + d5 * d5;
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /** for determining distance to entities, y level does not matter, e.g. mob follow range, attacking (can hit player no matter the y level) */
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }

    @Override
    public int bL() {
        return Integer.MAX_VALUE; /** mobs are willing to take any fall to reach the player as they don't take fall damage */
    }
}
