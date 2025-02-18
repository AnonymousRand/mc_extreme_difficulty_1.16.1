package AnonymousRand.anonymousrand.plugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.IGoalRemovingMob;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.VanillaPathfinderGoalsRemove;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.attack.CustomPathfinderGoalAttackMelee;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.attackmvmt.CustomPathfinderGoalAttackMvmtMelee;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.target.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityZombiePig extends EntityPigZombie implements ICustomHostile, IAttackLevelingMob, IGoalRemovingMob {

    /* Ignores line of sight and y-level for initially finding a player target and maintaining it as the target,
     * as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;
    private int attacks;
    private boolean a5, a15, a25, a35;


    public CustomEntityZombiePig(World world) {
        super(EntityTypes.ZOMBIFIED_PIGLIN, world);
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD)); // makes sure that it has a sword
        this.attacks = 0;
        this.a5 = false;
        this.a15 = false;
        this.a25 = false;
        this.a35 = false;
        VanillaPathfinderGoalsRemove.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new CustomPathfinderGoalTeleportNearTargetYLevel(this, 5.0, random.nextDouble() * 5 + 10.0, 0.0005));  /* Occasionally teleports to a spot closer in y-level to its target */
        this.goalSelector.a(0, new CustomPathfinderGoalTeleportNearTarget(this, this.getDetectionRange(), 300.0, 0.0005)); /* Occasionally teleports to a spot near its target */
        this.goalSelector.a(0, new CustomPathfinderGoalUpgradeArmor(this)); /* custom goal that allows this mob to upgrade its armor gradually as part of the attacks system */
        this.goalSelector.a(1, new CustomPathfinderGoalAttackMelee<>(this)); /* uses the custom melee attack goal that attacks regardless of the y-level */
        this.goalSelector.a(1, new CustomPathfinderGoalAttackMvmtMelee<>(this));
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget<>(this));                                /* Always retaliates against players and teleports to them if they are out of range/do not have line of sight, but doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */ // todo does the listener actually not work sometimes? also if this is no longer needed, don't remove old goal in igoalremovingmob
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* always aggro; uses the custom goal which doesn't need line of sight to start attack (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
    }

    public double getDetectionRange() { /* zombie piglins have 20 block detection range */
        return 20.0;
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
            EntityHuman nearestPlayer = this.world.findNearbyPlayer(this, -1.0);

            if (nearestPlayer != null) {
                /* Mobs only despawn along horizontal axes, so even at build height,
                   mobs will spawn below you and prevent sleeping */
                double distSqToNearestPlayer =
                        Math.pow(nearestPlayer.getPositionVector().getX() - this.getPositionVector().getX(), 2)
                        + Math.pow(nearestPlayer.getPositionVector().getZ() - this.getPositionVector().getZ(), 2);
                int forceDespawnDist = this.getEntityType().e().f();
                int forceDespawnDistSq = forceDespawnDist * forceDespawnDist;

                if (distSqToNearestPlayer > (double) forceDespawnDistSq
                        && this.isTypeNotPersistent(distSqToNearestPlayer)) {
                    this.die();
                }

                /* Random despawn distance increased to 40 blocks */
                int randomDespawnDist = this.getEntityType().e().g() + 8;
                int randomDespawnDistSq = randomDespawnDist * randomDespawnDist;

                if (this.ticksFarFromPlayer > 600
                        && random.nextInt(800) == 0
                        && distSqToNearestPlayer > (double) randomDespawnDistSq
                        && this.isTypeNotPersistent(distSqToNearestPlayer)) {
                    this.die();
                } else if (distSqToNearestPlayer < (double) randomDespawnDistSq) {
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

    public int getAttacks() {
        return this.attacks;
    }

    public void increaseAttacks(int increase) {
        this.attacks += increase;
    }

//    public int[] getAttacksThreshs() {
//        return this.attackLevelingController.getAttacksThreshs();
//    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 5 && !this.a5) { /* after 5 attacks, zombie piglins can aggro other zombie piglins up to 25 blocks away and get regen 2 */
            this.a5 = true;
            this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(25.0);
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
        }

        if (this.attacks == 15 && !this.a15) { /* after 15 attacks, zombie piglins can aggro other zombie piglins up to 32 blocks away and get 30 max health */
            this.a15 = true;
            this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(32.0);
            ((LivingEntity) this.getBukkitEntity()).setMaxHealth(30.0);
        }

        if (this.attacks == 25 && !this.a25) { /* after 25 attacks, zombie piglins can aggro other zombie piglins up to 40 blocks away and get regen 3 */
            this.a25 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));
            this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(40.0);
        }

        if (this.attacks == 35 && !this.a35) { /* after 35 attacks, zombie piglins can aggro other zombie piglins up to 50 blocks away */
            this.a35 = true;
            this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(50.0);
        }
    }

    @Override
    public PathfinderGoalSelector getVanillaGoalSelector() {
        return super.goalSelector;
    }

    public PathfinderGoalSelector getVanillaTargetSelector() {
        return super.targetSelector;
    }
}
