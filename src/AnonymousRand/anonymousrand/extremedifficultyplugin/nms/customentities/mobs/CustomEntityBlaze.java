package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.projectiles.CustomEntityLargeFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.projectiles.CustomEntitySmallFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack.CustomPathfinderGoalAttackMelee;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack.CustomPathfinderGoalAttackRanged;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.CustomPathfinderGoalMoveFasterInCobweb;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.CustomPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NmsUtil;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableRingOfFireballs;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityBlaze extends EntityBlaze
        implements IRangedEntity, ICustomHostile, IAttackLevelingMob, IGoalRemovingMob {

    /* Ignores y-level for initially finding a player target and maintaining it as the target,
     * as well as for retaliating against players */
    private static final boolean IGNORE_LOS = false;
    private static final boolean IGNORE_Y = true;
    private CustomPathfinderGoalAttackRanged<CustomEntityBlaze> pathfinderGoalFireballAttack;
    private boolean rapidFire;

    public CustomEntityBlaze(World world) {
        super(EntityTypes.BLAZE, world);
        initCustom();
        initAttackLevelingMob();
        initGoalRemovingMob();
    }

    protected void initCustom() {
        this.initAttributes();

        /* No longer avoids fire, lava, and water */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.a(PathType.WATER, 0.0F);

        this.rapidFire = false;
    }

    protected void initAttributes() {
        /* Blazes only have 12.5 health */
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(12.5);
        this.setHealth(12.5F);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `ICustomHostile`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public double getDetectionRange() {
        /* Blazes have 40 block detection range */
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

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `IAttackLevelingMob`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private AttackLevelingController attackLevelingController = null;

    protected void initAttackLevelingMob() {
        this.attackLevelingController = new AttackLevelingController(75, 150, 250);
    }

    public int getAttacks() {
        return this.attackLevelingController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        int[] attackThreshs = this.getAttacksThreshs();
        int[] metThreshs = this.attackLevelingController.increaseAttacks(increase);

        for (int metThresh : metThreshs) {
            if (metThresh == attackThreshs[0]) {
                /* After 75 attacks, blazes shoot an exploding fireball with power 1 */
                double d1 = this.getGoalTarget().locX() - this.locX();
                double d2 = this.getGoalTarget().e(0.5) - this.e(0.5);
                double d3 = this.getGoalTarget().locZ() - this.locZ();
                CustomEntityLargeFireball largeFireball =
                        new CustomEntityLargeFireball(this.world, this, d1, d2, d3, 1);
                largeFireball.setPosition(largeFireball.locX(), this.e(0.5) + 0.5, largeFireball.locZ());
                this.world.addEntity(largeFireball);
            } else if (metThresh == attackThreshs[1]) {
                /* After 150 attacks, blazes shoot out a ring of fireballs */
                new RunnableRingOfFireballs(this, 0.5, 1).run();
            } else if (metThresh == attackThreshs[2]) {
                /* After 250 attacks, blazes enter rapid fire state, which reduces attack cooldown to 4 ticks */
                this.pathfinderGoalFireballAttack.setAttackCooldown(4);
                this.rapidFire = true;
            }
        }
    }

    public int[] getAttacksThreshs() {
        return this.attackLevelingController.getAttacksThreshs();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `IGoalRemovingMob`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected void initGoalRemovingMob() {
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
    // Overridden Vanilla Functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /* For IRangedEntity */
    @Override // `shoot()` 
    public void a(EntityLiving target, float distanceFactor) {
        if (!this.isSilent()) {
            this.world.a(null, 1018, this.getChunkCoordinates(), 0);
        }

        float vanillaInaccuracy = MathHelper.c(MathHelper.sqrt(NmsUtil.distSq(this, target, false))) * 0.5F;
        double distToTargetX = target.locX() - this.locX();
        double distToTargetY = target.e(0.5) - this.e(0.5); // e() is getEyeLevel()
        double distToTargetZ = target.locZ() - this.locZ();
        CustomEntitySmallFireball smallFireball;

        /* In rapid fire state, blazes shoot 2 fireballs at a time, with 250% of their vanilla inaccuracy */
        if (this.rapidFire) {
            for (int i = 0; i < 2; i++) {
                smallFireball = new CustomEntitySmallFireball(this.world, this,
                                distToTargetX + this.getRandom().nextGaussian() * vanillaInaccuracy * 2.5,
                                distToTargetY,
                                distToTargetZ + this.getRandom().nextGaussian() * vanillaInaccuracy * 2.5);
                smallFireball.setPosition(smallFireball.locX(), this.e(0.5) + 0.5, smallFireball.locZ());
                this.world.addEntity(smallFireball);
            }
        } else {
            smallFireball = new CustomEntitySmallFireball(this.world, this,
                            distToTargetX + this.random.nextGaussian() * vanillaInaccuracy * 0.2,
                            distToTargetY,
                            distToTargetZ + this.random.nextGaussian() * vanillaInaccuracy * 0.2);
            smallFireball.setPosition(smallFireball.locX(), this.e(0.5) + 0.5, smallFireball.locZ());
            this.world.addEntity(smallFireball);
        }
    }

    @Override
    protected void initPathfinder() {
        super.initPathfinder();
        this.pathfinderGoalFireballAttack = new CustomPathfinderGoalAttackRanged<>(this, 6);
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this));                              /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this));                                 /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(4, new PathfinderGoalAttackMelee(this));
        this.goalSelector.a(5, this.pathfinderGoalFireballAttack);                                             /* Blazes do not pause between each volley and instead shoots constantly every 6 ticks */
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget<>(this));                                /* Always retaliates against players and teleports to them if they are out of range/do not have line of sight, but doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */ // todo does the listener actually not work sometimes? also if this is no longer needed, don't remove old goal in igoalremovingmob
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets to the nearest option */
    }

    @Override
    public boolean dN() {
        /* No longer damaged by water */
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Nested Classes
    ///////////////////////////////////////////////////////////////////////////////////////////////

    static class PathfinderGoalAttackMelee extends CustomPathfinderGoalAttackMelee<CustomEntityBlaze> {

        public PathfinderGoalAttackMelee(CustomEntityBlaze blaze) {
            super(blaze);
        }

        @Override
        protected void attack(EntityLiving attackTarget) {
            super.attack(attackTarget);

            /* Blaze melee attack creates a power 0.5 explosion on the player's location */
            this.goalOwner.getWorld().createExplosion(
                    this.goalOwner,
                    attackTarget.locX(),
                    attackTarget.locY(),
                    attackTarget.locZ(),
                    0.5F,
                    false,
                    Explosion.Effect.DESTROY);
        }

        @Override
        protected double getAttackReachSq(EntityLiving target) {
            return 3.0;
        }
    }
}
