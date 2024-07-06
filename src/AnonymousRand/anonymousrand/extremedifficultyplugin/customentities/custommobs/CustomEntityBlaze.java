package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityLargeFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntitySmallFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalCobwebMoveFaster;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableRingOfFireballs;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import java.util.EnumSet;

public class CustomEntityBlaze extends EntityBlaze implements ICustomHostile, IAttackLevelingMob, IGoalRemovingMob {

    private AttackLevelingController attackLevelingController;
    public PathfinderGoalSelector vanillaTargetSelector;
    private boolean rapidFire;

    public CustomEntityBlaze(World world) {
        super(EntityTypes.BLAZE, world);
        initCustom();
        initAttackLevelingMob();
        initGoalRemovingMob();
    }

    private void initCustom() {
        this.initAttributes();

        /* No longer avoids lava, fire, and water */
        this.a(PathType.LAVA, 0.0F);
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.WATER, 0.0F);

        this.rapidFire = false;
    }

    private void initAttributes() {
        /* Blazes only have 12.5 health */
        this.setHealth(12.5F);
        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(12.5);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                      ICustomHostile                                       //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public double getFollowRange() {
        /* Blazes have 40 block detection range (setting attribute doesn't work) */
        return 40.0;
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (nearestPlayer != null) {
                /* Mobs only despawn along horizontal axes, so even if you are at y=256, mobs will still spawn below you and prevent sleeping */
                double distSquaredToNearestPlayer = Math.pow(nearestPlayer.getPositionVector().getX() - this.getPositionVector().getX(), 2)
                        + Math.pow(nearestPlayer.getPositionVector().getZ() - this.getPositionVector().getZ(), 2);
                int forceDespawnDist = this.getEntityType().e().f();
                int forceDespawnDistSquared = forceDespawnDist * forceDespawnDist;

                if (distSquaredToNearestPlayer > (double) forceDespawnDistSquared && this.isTypeNotPersistent(distSquaredToNearestPlayer)) {
                    this.die();
                }

                /* Random despawn distance increased to 40 blocks */
                int randomDespawnDist = this.getEntityType().e().g() + 8;
                int randomDespawnDistSquared = randomDespawnDist * randomDespawnDist;

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && distSquaredToNearestPlayer > (double)randomDespawnDistSquared
                        && this.isTypeNotPersistent(distSquaredToNearestPlayer)) {
                    this.die();
                } else if (distSquaredToNearestPlayer < (double) randomDespawnDistSquared) {
                    this.ticksFarFromPlayer = 0;
                }
            }

        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    @Override
    public double g(double x, double y, double z) {
        double distX = this.locX() - x;
        double distZ = this.locZ() - z;

        return distX * distX + distZ * distZ;
    }

    @Override
    public double d(Vec3D vec3d) {
        double distX = this.locX() - vec3d.x;
        double distZ = this.locZ() - vec3d.z;

        return distX * distX + distZ * distZ;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                    IAttackLevelingMob                                     //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void initAttackLevelingMob() {
        this.attackLevelingController = new AttackLevelingController(50, 125, 250);
    }

    public int getAttacks() {
        return this.attackLevelingController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        for (int metThreshold : this.attackLevelingController.increaseAttacks(increase)) {
            int[] attackThresholds = this.attackLevelingController.getAttacksThresholds();
            if (metThreshold == attackThresholds[0]) {
                /* After 50 attacks, blazes shoot an exploding fireball with power 1 */
                double d1 = this.getGoalTarget().locX() - this.locX();
                double d2 = this.getGoalTarget().e(0.5D) - this.e(0.5D);
                double d3 = this.getGoalTarget().locZ() - this.locZ();
                CustomEntityLargeFireball entityLargeFireball = new CustomEntityLargeFireball(this.getWorld(), this, d1, d2, d3, 1);
                entityLargeFireball.setPosition(entityLargeFireball.locX(), this.e(0.5D) + 0.5D, entityLargeFireball.locZ());
                this.getWorld().addEntity(entityLargeFireball);
            } else if (metThreshold == attackThresholds[1]) {
                /* After 125 attacks, blazes shoot out a ring of fireballs */
                new RunnableRingOfFireballs(this, 0.5, 1).run();
            } else if (metThreshold == attackThresholds[2]) {
                /* After 250 attacks, blazes enter rapid fire state */
                this.rapidFire = true;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                     IGoalRemovingMob                                      //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void initGoalRemovingMob() {
        this.vanillaTargetSelector = super.targetSelector;
        // remove vanilla HurtByTarget and NearestAttackableTarget goals to replace them with custom ones
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this);
    }

    public PathfinderGoalSelector getVanillaTargetSelector() {
        return this.vanillaTargetSelector;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                  Other custom functions                                   //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public boolean getRapidFire() {
        return this.rapidFire;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                               Overridden vanilla functions                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void initPathfinder() {
        super.initPathfinder();
        /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this));
        /* Takes buffs from bats and piglins etc. */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));
        this.goalSelector.a(3, new PathfinderGoalBlazeFireballAttack(this));
        /* Doesn't need line of sight to find targets and start attacking */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class));
    }

    @Override
    public boolean dN() {
        /* No longer damaged by water */
        return false;
    }

    static class PathfinderGoalBlazeFireballAttack extends PathfinderGoal {
        private final CustomEntityBlaze blaze;
        private final World nmsWorld;
        private int rangedAttackCooldown;
        private int meleeAttackCooldown;

        public PathfinderGoalBlazeFireballAttack(CustomEntityBlaze entityBlaze) {
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
            this.rangedAttackCooldown = 0;
            this.meleeAttackCooldown = 0;
        }

        @Override // tick()
        public void e() {
            --this.rangedAttackCooldown;
            --this.meleeAttackCooldown;
            EntityLiving goalTarget = this.blaze.getGoalTarget();

            // now enforces attacking when line of sight is broken (the old goal stopped the mob from attacking a target
            // if LoS is broken, even if the mob has already recognized its target via the nearestAttackableTarget goal) */
            if (goalTarget != null) {
                double distSquaredToGoalTarget = this.blaze.h((Entity) goalTarget);

                if (distSquaredToGoalTarget < 3.0D) { // melee attack
                    if (this.meleeAttackCooldown <= 0) {
                        this.meleeAttackCooldown = 20;

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

                    if (this.rangedAttackCooldown <= 0) {
                        this.blaze.increaseAttacks(1);

                        if (!this.blaze.isSilent()) {
                            this.nmsWorld.a(null, 1018, this.blaze.getChunkCoordinates(), 0);
                        }

                        float defaultInaccuracy = MathHelper.c(MathHelper.sqrt(distSquaredToGoalTarget)) * 0.5F;

                        /* Blazes do not pause between each volley and instead shoots constantly */
                        if (this.blaze.getRapidFire()) {
                            this.rangedAttackCooldown = 4;

                            /* In rapid fire state, blazes shoot 2 fireballs at a time, with 250% of their vanilla inaccuracy */
                            CustomEntitySmallFireball entitySmallFireball;
                            for (int i = 0; i < 2; i++) {
                                entitySmallFireball = new CustomEntitySmallFireball(this.nmsWorld, this.blaze,
                                        distToGoalTargetX + this.blaze.getRandom().nextGaussian() * defaultInaccuracy * 2.5,
                                        distToGoalTargetY,
                                        distToGoalTargetZ + this.blaze.getRandom().nextGaussian() * defaultInaccuracy * 2.5);
                                entitySmallFireball.setPosition(entitySmallFireball.locX(), this.blaze.e(0.5D) + 0.5D, entitySmallFireball.locZ());
                                this.nmsWorld.addEntity(entitySmallFireball);
                            }
                        } else {
                            this.rangedAttackCooldown = 6;

                            /* In normal firing state, blazes have 20% of their vanilla inaccuracy */
                            CustomEntitySmallFireball entitySmallFireball = new CustomEntitySmallFireball(this.nmsWorld, this.blaze,
                                    distToGoalTargetX + this.blaze.getRandom().nextGaussian() * defaultInaccuracy * 0.2,
                                    distToGoalTargetY,
                                    distToGoalTargetZ + this.blaze.getRandom().nextGaussian() * defaultInaccuracy * 0.2);
                            entitySmallFireball.setPosition(entitySmallFireball.locX(), this.blaze.e(0.5D) + 0.5D, entitySmallFireball.locZ());
                            this.nmsWorld.addEntity(entitySmallFireball);
                        }
                    }

                    this.blaze.getControllerLook().a(goalTarget, 10.0F, 10.0F);
                }

                super.e();
            }
        }
    }
}