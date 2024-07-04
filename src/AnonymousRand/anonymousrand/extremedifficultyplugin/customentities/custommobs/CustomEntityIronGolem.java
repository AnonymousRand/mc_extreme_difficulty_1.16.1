package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.AttackController;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityIronGolem extends EntityIronGolem implements ICustomHostile, IAttackLevelingMob {

    private AttackController attackController;
    private double followRangeMultipler;

    public CustomEntityIronGolem(World world) {
        super(EntityTypes.IRON_GOLEM, world);
        this.initCustomHostile();
        this.initAttackLevelingMob();
    }

    //////////////////////////////////////  ICustomHostile  ///////////////////////////////////////

    public void initCustomHostile() {
        /** No longer avoids lava */
        this.a(PathType.LAVA, 0.0F);
        /** No longer avoids fire */
        this.a(PathType.DAMAGE_FIRE, 0.0F);

        this.followRangeMultipler = 1.0;

        this.initAttributes();
    }

    private void initAttributes() {
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.5); /** iron golems move twice as fast but do half as much damage */
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(7.5);
    }

    public double getFollowRange() { /** iron golems have 24 block detection range (setting attribute doesn't work) */
        return 24.0 * this.followRangeMultipler;
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityHuman = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (entityHuman != null) {
                /** Mobs only despawn along horizontal axes, so if you are at y=256, mobs will still spawn below you and prevent sleeping */
                double distToNearestPlayer = Math.pow(entityHuman.getPositionVector().getX() - this.getPositionVector().getX(), 2)
                        + Math.pow(entityHuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2);
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (distToNearestPlayer > (double)j && this.isTypeNotPersistent(distToNearestPlayer)) {
                    this.die();
                }

                /** Random despawn distance increased to 40 blocks */
                int k = this.getEntityType().e().g() + 8;
                int l = k * k;

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && distToNearestPlayer > (double)l
                        && this.isTypeNotPersistent(distToNearestPlayer)) {
                    this.die();
                } else if (distToNearestPlayer < (double)l) {
                    this.ticksFarFromPlayer = 0;
                }
            }
        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    @Override
    public double g(double x, double y, double z) {
        double dist_x = this.locX() - x;
        double dist_z = this.locZ() - z;

        return dist_x * dist_x + dist_z * dist_z;
    }

    @Override
    public double d(Vec3D vec3d) {
        double dist_x = this.locX() - vec3d.x;
        double dist_z = this.locZ() - vec3d.z;

        return dist_x * dist_x + dist_z * dist_z;
    }

    @Override
    public int bL() {
        return Integer.MAX_VALUE;
    }

    ////////////////////////////////////  IAttackLevelingMob  /////////////////////////////////////

    private void initAttackLevelingMob() {
        this.attackController = new AttackController();
    }

    public int getAttacks() {
        return this.attackController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        this.attackController.increaseAttacks(increase);
        /** Every attack, iron golems increase their stats by a bit */
        this.increaseStatsAdd(2.0, 0.5, 0.025);
    }

    //////////////////////////////////  Other custom functions  ///////////////////////////////////

    public void increaseStatsAdd(double healthIncrease, double damageIncrease, double speedIncrease) {
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue() + damageIncrease);

        if (this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() <= (0.65 - speedIncrease)) {
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() + speedIncrease);
        }

        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(((LivingEntity)this.getBukkitEntity()).getMaxHealth() + healthIncrease);
        this.setHealth((float)(this.getHealth() + healthIncrease));
    }

    public void increaseStatsMultiply(double multiplier) {
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue() * multiplier);

        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(((LivingEntity)this.getBukkitEntity()).getMaxHealth() * multiplier);
        this.setHealth((float)(this.getHealth() * multiplier));

        this.followRangeMultipler *= multiplier;
        if (this.followRangeMultipler >= 3.0) { // cap to prevent lag with too many blocks being searched
            this.followRangeMultipler = 3.0;
        }

        VanillaPathfinderGoalsAccess.updateMobFollowRange(this);
    }

    ///////////////////////////////  Overridden vanilla functions  ////////////////////////////////

    @Override
    protected void initPathfinder() { /** no longer targets monsters or defends villages */
        /** Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this));
        /** Takes buffs from bats and piglins etc. */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 40, 2, 1, 2, 1, true)); /** custom goal that breaks blocks around the mob periodically except for diamond blocks, emerald blocks, nertherite blocks, and beacons */
        this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack(this, 1.0D)); /** uses the custom melee attack goal that attacks regardless of the y level */
        this.goalSelector.a(2, new PathfinderGoalMoveTowardsTarget(this, 0.9D, 32.0F));
        this.goalSelector.a(2, new PathfinderGoalStrollVillage(this, 0.6D, false));
        this.goalSelector.a(4, new PathfinderGoalStrollVillageGolem(this, 0.6D));
        this.goalSelector.a(5, new PathfinderGoalOfferFlower(this));
        this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 6.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new CustomPathfinderGoalHurtByTarget(this, new Class[0]));
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /** always hostile to players; uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
        this.targetSelector.a(4, new PathfinderGoalUniversalAngerReset<>(this, false));
    }
}