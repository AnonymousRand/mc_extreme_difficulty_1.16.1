package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IAttackLevelingMob;
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

public class CustomEntitySkeletonWither extends EntitySkeletonWither implements ICustomHostile, IAttackLevelingMob, IGoalRemovingMob {

    /* Ignores line of sight and y-level for initially finding a player target and maintaining it as the target,
       as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;
    private int attacks;
    private boolean a12, a50;

    public CustomEntitySkeletonWither(World world) {
        super(EntityTypes.WITHER_SKELETON, world);
        this.a(PathType.DAMAGE_FIRE, 0.0F); /* no longer avoids fire */
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.STONE_SWORD)); // makes sure that it has a sword
        this.attacks = 0;
        this.a12 = false;
        this.a50 = false;
        this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(4.0); /* wither skeletons twice as fast and have extra knockback */
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.5);
        VanillaPathfinderGoalsRemove.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 20, 1, 1, 1, 1, true)); /* Breaks most blocks around the mob periodically */
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(1, new NewPathfinderGoalSpawnBlocksEntitiesOnMob(this, org.bukkit.Material.SOUL_SOIL, 1, 1, 0, 1, -1.0, false)); /* custom goal that allows wither skeleton to summon soul sand in a 3 by 3 beneath itself constantly */
        this.goalSelector.a(1, new NewPathfinderGoalSpawnBlocksEntitiesOnMob(this, org.bukkit.Material.WITHER_ROSE, 1, 0, 0, 0, 0, false)); /* custom goal that allows wither skeleton to summon wither roses on itself constantly */
        this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack<>(this)); /* uses the custom melee attack goal that attacks regardless of the y-level */
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget<>(this));                                /* Always retaliates against players and teleports to them if they are out of range/do not have line of sight, but doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */ // todo does the listener actually not work sometimes? also if this is no longer needed, don't remove old goal in igoalremovingmob
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the nearest option */
    }

    @Override
    public boolean attackEntity(Entity entity) {
        if (!super.attackEntity(entity)) {
            return false;
        } else {
            if (entity instanceof EntityHuman) {
                ((EntityHuman) entity).addEffect(new MobEffect(MobEffects.WITHER, this.attacks < 8 ? 400 : this.attacks < 25 ? 600 : 900, 1)); /* wither skeletons apply wither 2 instead of wither 1 for 20 seconds instead of 10 (30 seconds after 8 attacks, 45 seconds after 25 attacks0 */
            }

            return true;
        }
    }

    public double getDetectionRange() { /* wither skeletons have 40 blocks detection range */
        return 40.0;
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
    public double g(double x, double y, double z) {
        double d3 = this.locX() - x; /* for determining distance to entities, y-level does not matter for wither skeletons, e.g. mob follow range, attack (can hit player no matter the y-level) */
        double d5 = this.locZ() - z;

        return d3 * d3 + d5 * d5;
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /* for determining distance to entities, y-level does not matter for wither skeletons, e.g. mob follow range, attack (can hit player no matter the y-level) */
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
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

//    public int[] getAttacksThresholds() {
//        return this.attackLevelingController.getAttacksThresholds();
//    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 12 && !this.a12) { /* after 12 attacks, wither skeletons get regen 2 and 30 max health */
            this.a12 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
            ((LivingEntity) this.getBukkitEntity()).setMaxHealth(30.0);
        }

        if (this.attacks == 50 && !this.a50) { /* after 50 attacks, wither skeletons summon a mini wither */
            this.a50 = true;
            new SpawnEntity(this.world, new CustomEntityWitherMini(this.world), 1, null, null, this, false, false);
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
