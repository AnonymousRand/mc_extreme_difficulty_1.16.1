package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.projectiles.CustomEntityLargeFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.projectiles.CustomEntitySmallFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.NewPathfinderGoalMoveFasterInCobweb;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.NewPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableRingOfFireballs;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

import java.util.EnumSet;

public class CustomEntityBlaze extends EntityBlaze implements ICustomHostile, IAttackLevelingMob, IGoalRemovingMob {

    /* Ignores y-level for initially finding a player target and maintaining it
       as the target, as well as for retaliating against players */
    private static final boolean IGNORE_LOS = false;
    private static final boolean IGNORE_Y = true;
    private boolean rapidFire;

    public CustomEntityBlaze(World world) {
        super(EntityTypes.BLAZE, world);
        initCustom();
        initAttackLevelingMob();
        initGoalRemovingMob();
    }

    private void initCustom() {
        this.initAttributes();

        /* No longer avoids fire, lava, and water */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.a(PathType.WATER, 0.0F);

        this.rapidFire = false;
    }

    private void initAttributes() {
        /* Blazes only have 12.5 health */
        this.setHealth(12.5F);
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(12.5);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                      ICustomHostile                                       //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /* Blazes have 40 block detection range */
    public double getDetectionRange() {
        return 40.0;
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.world.findNearbyPlayer(this, -1.0D);

            if (nearestPlayer != null) {
                /* Mobs only despawn along horizontal axes, so even at build height, mobs will spawn below you and prevent sleeping */
                double distSqToNearestPlayer = Math.pow(nearestPlayer.getPositionVector().getX()- this.getPositionVector().getX(), 2)
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

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && distSqToNearestPlayer
                        > (double) randomDespawnDistSq && this.isTypeNotPersistent(distSqToNearestPlayer)) {
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
    //                                    IAttackLevelingMob                                     //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private AttackLevelingController attackLevelingController = null;

    private void initAttackLevelingMob() {
        this.attackLevelingController = new AttackLevelingController(100, 200, 300);
    }

    public int getAttacks() {
        return this.attackLevelingController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        for (int metThreshold : this.attackLevelingController.increaseAttacks(increase)) {
            int[] attackThresholds = this.getAttacksThresholds();
            if (metThreshold == attackThresholds[0]) {
                /* After 100 attacks, blazes shoot an exploding fireball with power 1 */
                double d1 = this.getGoalTarget().locX() - this.locX();
                double d2 = this.getGoalTarget().e(0.5D) - this.e(0.5D);
                double d3 = this.getGoalTarget().locZ() - this.locZ();
                CustomEntityLargeFireball largeFireball = new CustomEntityLargeFireball(this.world,
                        this, d1, d2, d3, 1);
                largeFireball.setPosition(largeFireball.locX(), this.e(0.5D) + 0.5D, largeFireball.locZ());
                this.world.addEntity(largeFireball);
            } else if (metThreshold == attackThresholds[1]) {
                /* After 200 attacks, blazes shoot out a ring of fireballs */
                new RunnableRingOfFireballs(this, 0.5, 1).run();
            } else if (metThreshold == attackThresholds[2]) {
                /* After 300 attacks, blazes enter rapid fire state */
                this.rapidFire = true;
            }
        }
    }

    public int[] getAttacksThresholds() {
        return this.attackLevelingController.getAttacksThresholds();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                     IGoalRemovingMob                                      //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public PathfinderGoalSelector vanillaTargetSelector;

    private void initGoalRemovingMob() {
        this.vanillaTargetSelector = super.targetSelector;
        // remove vanilla HurtByTarget and NearestAttackableTarget goals to replace them with custom ones
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
    //                                  Other custom functions                                   //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public boolean isRapidFire() {
        return this.rapidFire;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                               Overridden vanilla functions                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this));                                                       /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));                                                          /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(3, new PathfinderGoalBlazeAttack(this));
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, IGNORE_LOS, IGNORE_Y)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets to the closest option */
    }

    @Override
    public boolean dN() {
        /* No longer damaged by water */
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                Mob-specific goals/classes                                 //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    static class PathfinderGoalBlazeAttack extends PathfinderGoal {
        private final CustomEntityBlaze blaze;
        private final World nmsWorld;
        private int rangedAttackRemainingCooldown;
        private int meleeAttackRemainingCooldown;

        public PathfinderGoalBlazeAttack(CustomEntityBlaze entityBlaze) {
            this.blaze = entityBlaze;
            this.nmsWorld = entityBlaze.getWorld();
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        }

        @Override // shouldExecute()
        public boolean a() {
            EntityLiving entityLiving = this.blaze.getGoalTarget();
            return entityLiving != null && entityLiving.isAlive() && this.blaze.d(entityLiving);
        }

        @Override // startExecuting()
        public void c() {
            this.rangedAttackRemainingCooldown = 0;
            this.meleeAttackRemainingCooldown = 0;
        }

        @Override // tick()
        public void e() {
            --this.rangedAttackRemainingCooldown;
            --this.meleeAttackRemainingCooldown;
            EntityLiving goalTarget = this.blaze.getGoalTarget();

            if (goalTarget != null) {
                double distSqToGoalTarget = NMSUtil.distSq(this.blaze, goalTarget, false);

                if (distSqToGoalTarget < 3.0D) { // melee attack
                    if (this.meleeAttackRemainingCooldown <= 0) {
                        this.meleeAttackRemainingCooldown = 20;

                        this.blaze.attackEntity(goalTarget);
                        /* Blaze melee attack creates a power 0.5 explosion on the player's location */
                        this.blaze.getWorld().createExplosion(this.blaze, goalTarget.locX(), goalTarget.locY(),
                                goalTarget.locZ(), 0.5F, false, Explosion.Effect.DESTROY);
                    }

                    this.blaze.getControllerMove().a(goalTarget.locX(), goalTarget.locY(), goalTarget.locZ(), 1.0D);
                } else {
                    double distToGoalTargetX = goalTarget.locX() - this.blaze.locX();
                    double distToGoalTargetY = goalTarget.e(0.5D) - this.blaze.e(0.5D);
                    double distToGoalTargetZ = goalTarget.locZ() - this.blaze.locZ();

                    if (this.rangedAttackRemainingCooldown <= 0) {
                        this.blaze.increaseAttacks(1);

                        if (!this.blaze.isSilent()) {
                            this.nmsWorld.a(null, 1018, this.blaze.getChunkCoordinates(), 0);
                        }

                        float defaultInaccuracy = MathHelper.c(MathHelper.sqrt(distSqToGoalTarget)) * 0.5F;

                        /* Blazes do not pause between each volley and instead shoots constantly */
                        if (this.blaze.isRapidFire()) {
                            this.rangedAttackRemainingCooldown = 4;

                            /* In rapid fire state, blazes shoot 2 fireballs at a time, with 250% of their vanilla inaccuracy */
                            CustomEntitySmallFireball smallFireball;
                            for (int i = 0; i < 2; i++) {
                                smallFireball = new CustomEntitySmallFireball(this.nmsWorld, this.blaze,
                                        distToGoalTargetX + this.blaze.getRandom().nextGaussian() * defaultInaccuracy * 2.5,
                                        distToGoalTargetY,
                                        distToGoalTargetZ + this.blaze.getRandom().nextGaussian() * defaultInaccuracy * 2.5);
                                smallFireball.setPosition(smallFireball.locX(), this.blaze.e(0.5D) + 0.5D, smallFireball.locZ());
                                this.nmsWorld.addEntity(smallFireball);
                            }
                        } else {
                            this.rangedAttackRemainingCooldown = 6;

                            /* In normal firing state, blazes have 20% of their vanilla inaccuracy */
                            CustomEntitySmallFireball smallFireball = new CustomEntitySmallFireball(this.nmsWorld, this.blaze,
                                    distToGoalTargetX + this.blaze.getRandom().nextGaussian() * defaultInaccuracy * 0.2,
                                    distToGoalTargetY,
                                    distToGoalTargetZ + this.blaze.getRandom().nextGaussian() * defaultInaccuracy * 0.2);
                            smallFireball.setPosition(smallFireball.locX(), this.blaze.e(0.5D) + 0.5D, smallFireball.locZ());
                            this.nmsWorld.addEntity(smallFireball);
                        }
                    }

                    this.blaze.getControllerLook().a(goalTarget, 10.0F, 10.0F);
                }

                super.e();
            }
        }
    }
}
