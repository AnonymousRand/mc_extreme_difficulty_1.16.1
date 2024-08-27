package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.AttackLevelingController;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack.CustomPathfinderGoalAttackMelee;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attackmvmt.CustomPathfinderGoalAttackMvmtMelee;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityIronGolem extends EntityIronGolem implements ICustomHostile, IAttackLevelingMob {

    /* Ignores line of sight and y-level for initially finding a player target and maintaining it as the target,
     * as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;

    private AttackLevelingController attackLevelingController = null;
    private double detectionRangeMultipler;

    public CustomEntityIronGolem(World world) {
        super(EntityTypes.IRON_GOLEM, world);
        this.initCustom();
        this.initAttackLevelingMob();
    }

    protected void initCustom() {
        this.initAttributes();

        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);

        this.detectionRangeMultipler = 1.0;
    }

    protected void initAttributes() {
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.5); /* iron golems move twice as fast but do half as much damage */
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(7.5);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `ICustomHostile`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public double getDetectionRange() { /* iron golems have 24 block detection range */
        return 24.0 * this.detectionRangeMultipler;
    }

    public boolean ignoresLOS() {
        return IGNORE_LOS;
    }

    public boolean ignoresY() {
        return IGNORE_Y;
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityHuman = this.world.findNearbyPlayer(this, -1.0);

            if (entityHuman != null) {
                /* Mobs only despawn along horizontal axes, so even at build height,
                   mobs will spawn below you and prevent sleeping */
                double distToNearestPlayer = Math.pow(entityHuman.getPositionVector().getX() - this.getPositionVector().getX(), 2)
                        + Math.pow(entityHuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2);
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (distToNearestPlayer > (double) j && this.isTypeNotPersistent(distToNearestPlayer)) {
                    this.die();
                }

                /* Random despawn distance increased to 40 blocks */
                int k = this.getEntityType().e().g() + 8;
                int l = k * k;

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && distToNearestPlayer > (double) l
                        && this.isTypeNotPersistent(distToNearestPlayer)) {
                    this.die();
                } else if (distToNearestPlayer < (double) l) {
                    this.ticksFarFromPlayer = 0;
                }
            }
        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    @Override
    public int bL() {
        return Integer.MAX_VALUE;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `IAttackLevelingMob`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected void initAttackLevelingMob() {
        this.attackLevelingController = new AttackLevelingController();
    }

    public int getAttacks() {
        return this.attackLevelingController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        this.attackLevelingController.increaseAttacksAndCheckThreshs(increase);
        /* Every attack, iron golems increase their stats by a bit */
        this.increaseStatsAdd(2.0, 0.5, 0.025);
    }

    public int[] getAttacksThreshs() {
        return this.attackLevelingController.getAttacksThreshs();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Other Custom Functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public void increaseStatsAdd(double healthIncrease, double damageIncrease, double speedIncrease) {
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue() + damageIncrease);

        if (this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() <= (0.65 - speedIncrease)) {
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() + speedIncrease);
        }

        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(((LivingEntity) this.getBukkitEntity()).getMaxHealth() + healthIncrease);
        this.setHealth((float) (this.getHealth() + healthIncrease));
    }

    public void increaseStatsMultiply(double multiplier) {
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue() * multiplier);

        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(((LivingEntity) this.getBukkitEntity()).getMaxHealth() * multiplier);
        this.setHealth((float) (this.getHealth() * multiplier));

        this.detectionRangeMultipler *= multiplier;
        if (this.detectionRangeMultipler >= 3.0) { // cap to prevent lag with too many blocks being searched
            this.detectionRangeMultipler = 3.0;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Overridden Vanilla Functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void initPathfinder() { /* no longer targets monsters or defends villages */
        /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this));
        /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this));
        this.goalSelector.a(0, new CustomPathfinderGoalBreakBlocksAround(this, 40, 2, 1, 2, 1, true)); /* Breaks most blocks around the mob periodically */
        this.goalSelector.a(1, new CustomPathfinderGoalAttackMelee<>(this)); /* uses the custom melee attack goal that attacks regardless of the y-level */
        this.goalSelector.a(1, new CustomPathfinderGoalAttackMvmtMelee<>(this));
        this.goalSelector.a(2, new PathfinderGoalMoveTowardsTarget(this, 0.9, 32.0F));
        this.goalSelector.a(2, new PathfinderGoalStrollVillage(this, 0.6, false));
        this.goalSelector.a(4, new PathfinderGoalStrollVillageGolem(this, 0.6));
        this.goalSelector.a(5, new PathfinderGoalOfferFlower(this));
        this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 6.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget<>(this));
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* always hostile to players; uses the custom goal which doesn't need line of sight to start attack (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
        this.targetSelector.a(2, new PathfinderGoalUniversalAngerReset<>(this, false));
    }
}
