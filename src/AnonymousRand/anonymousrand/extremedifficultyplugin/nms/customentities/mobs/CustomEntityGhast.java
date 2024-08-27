package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.ExtremeDifficultyPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.AttackLevelingController;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.projectiles.CustomEntityLargeFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack.CustomPathfinderGoalAttackRanged;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.CustomPathfinderGoalBreakBlocksAround;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.CustomPathfinderGoalMoveFasterInCobweb;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.CustomPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NmsUtil;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableRingOfFireballs;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

import java.util.EnumSet;
import java.util.Random;

public class CustomEntityGhast extends EntityGhast implements ICustomHostile, IAttackLevelingMob, IRangedEntity {

    /* Ignores line of sight and y-level for initially finding a player target and maintaining it as the target,
     * as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = false; // to reduce lag; detection range is so huge already
    private boolean hasSpawnedDeathFireballs;

    public CustomEntityGhast(World world) {
        super(EntityTypes.GHAST, world);
        this.initCustom();
        this.initAttackLevelingMob();
    }

    protected void initCustom() {
        this.hasSpawnedDeathFireballs = false;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `ICustomHostile`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public double getDetectionRange() {
        /* Ghasts have 80 block detection range */
        return 80.0;
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

                /* Random despawn distance increased to 64 blocks */
                int randomDespawnDist = this.getEntityType().e().g() + 32;
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
        this.attackLevelingController = new AttackLevelingController(120, 180, 300);
    }

    public int getAttacks() {
        return this.attackLevelingController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        int[] attackThreshs = this.getAttacksThreshs();
        int[] metThreshs = this.attackLevelingController.increaseAttacks(increase);

        for (int metThresh : metThreshs) {
            if (metThresh == attackThreshs[0]) {
                /* After 120 attacks, ghasts get 16 max health and health */
                ((LivingEntity) this.getBukkitEntity()).setMaxHealth(16.0);
                this.setHealth(16.0F);
            }
        }
    }

    public int[] getAttacksThreshs() {
        return this.attackLevelingController.getAttacksThreshs();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `IRangedEntity`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override // `shoot()`
    public void a(EntityLiving attackTarget, float distFactor) {
        if (!this.isSilent()) {
            world.a(null, 1016, this.getChunkCoordinates(), 0);
        }
        
        Vec3D lookDirection = this.f(1.0F);
        double motX = attackTarget.locX() - (this.locX() + lookDirection.getX() * 4.0);
        double motY = attackTarget.e(0.5) - (0.5 + this.e(0.5));
        double motZ = attackTarget.locZ() - (this.locZ() + lookDirection.getZ() * 4.0);

        int attacks = this.getAttacks();
        int[] attackThreshs = this.getAttacksThreshs();
        /* After 180 attacks, ghasts shoot a ring of power 1 fireballs every 40 attacks */
        if (attacks >= attackThreshs[1] && (attacks - attackThreshs[1]) % 40 == 0) {
            new RunnableRingOfFireballs(this, 0.5, 1).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 20L);
        }

        int fireballPower = this.getPower();
        /* After 300 attacks, ghasts shoot a power 3 fireball every 50 attacks */
        if (attacks >= attackThreshs[2] && (attacks - attackThreshs[2]) % 50 == 0) {
            fireballPower = 3;
        }

        CustomEntityLargeFireball largeFireball =
                new CustomEntityLargeFireball(world, this, motX, motY, motZ, fireballPower);
        largeFireball.setPosition(
                this.locX() + lookDirection.getX() * 4.0,
                this.e(0.5) + 0.5,
                largeFireball.locZ() + lookDirection.getZ() * 4.0);
        world.addEntity(largeFireball);
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Overridden Vanilla Functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void initPathfinder() {
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this));                              /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this));                                 /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new CustomPathfinderGoalBreakBlocksAround(this, 80, 2, 2, 2, 0, false));        /* Breaks most blocks around the mob periodically */
        this.goalSelector.a(5, new CustomEntityGhast.PathfinderGoalIdleMove(this));
        this.goalSelector.a(7, new CustomEntityGhast.PathfinderGoalGhastFireball(this, 5));
        this.goalSelector.a(7, new CustomEntityGhast.PathfinderGoalLook(this));
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the nearest option */
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damageAmount) {
        /* Rebounded fireballs do not damage ghasts */
        if (damageSource.j() instanceof EntityLargeFireball && damageSource.getEntity() instanceof EntityHuman) {
            return false;
        }

        return super.damageEntity(damageSource, damageAmount);
    }

    @Override
    public void tick() {
        super.tick();

        // do this here instead of in die() so that the fireballs don't have to wait until the death animation finishes
        // playing to start firing
        /* When killed, ghasts summon a lot of power 1 fireballs in all directions (more after 300 attacks) */
        if (this.getHealth() <= 0.0 && !this.hasSpawnedDeathFireballs) {
            this.hasSpawnedDeathFireballs = true;
            new RunnableRingOfFireballs(this, 0.5, this.getAttacks() < this.getAttacksThreshs()[2] ? 1 : 3)
                    .runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 30L);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Nested Classes
    ///////////////////////////////////////////////////////////////////////////////////////////////

    static class PathfinderGoalGhastFireball extends CustomPathfinderGoalAttackRanged<CustomEntityGhast> {

        public PathfinderGoalGhastFireball(CustomEntityGhast ghast, int attackCooldown) {
            super(ghast, attackCooldown);
        }

        @Override
        public void c() {
            super.c();

            // ghasts only play starting to shoot sound on first finding a target; otherwise too many overlapping sounds
            if (!this.goalOwner.isSilent()) {
                this.goalOwner.getWorld().a(null, 1015, this.goalOwner.getChunkCoordinates(), 0);
            }
        }

        @Override
        public void d() {
            super.d();

            this.goalOwner.t(false); // `setAttacking()`; controls shooting animation
        }

        @Override
        public void e() {
            this.goalOwner.t(this.remainingCooldownAttack <= 2);
            super.e();
        }
    }

    static class PathfinderGoalLook extends PathfinderGoal {

        private final CustomEntityGhast ghast;

        public PathfinderGoalLook(CustomEntityGhast ghast) {
            this.ghast = ghast;
            this.a(EnumSet.of(PathfinderGoal.Type.LOOK));
        }

        @Override
        public boolean a() {
            return true;
        }

        @Override
        public void e() {
            if (this.ghast.getGoalTarget() == null) {
                // if there's no attack target, look around randomly
                Vec3D ghastMot = this.ghast.getMot();

                this.ghast.yaw = (float) (-MathHelper.d(ghastMot.getX(), ghastMot.getZ()) * 57.295776F);
                this.ghast.aH = this.ghast.yaw; // `aH` is `renderYawOffset`
            } else {
                // else, look at attack target
                EntityLiving attackTarget = this.ghast.getGoalTarget();

                if (NmsUtil.distSq(this.ghast, attackTarget, true) < this.getDetectionRangeSq()) {
                    double distX = attackTarget.locX() - this.ghast.locX();
                    double distZ = attackTarget.locZ() - this.ghast.locZ();

                    this.ghast.yaw = (float) (-(MathHelper.d(distX, distZ)) * 57.295776F);
                    this.ghast.aH = this.ghast.yaw;
                }
            }
        }

        private double getDetectionRangeSq() {
            double detectionRange = this.ghast.getDetectionRange();
            return detectionRange * detectionRange;
        }
    }

    static class PathfinderGoalIdleMove extends PathfinderGoal {

        private final CustomEntityGhast ghast;

        public PathfinderGoalIdleMove(CustomEntityGhast ghast) {
            this.ghast = ghast;
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE));
        }

        @Override
        public boolean a() {
            ControllerMove controllerMove = this.ghast.getControllerMove();

            if (!controllerMove.b()) {
                return true;
            } else {
                double distX = controllerMove.d() - this.ghast.locX();
                double distY = controllerMove.e() - this.ghast.locY();
                double distZ = controllerMove.f() - this.ghast.locZ();
                double distSq = distX * distX + distY * distY + distZ * distZ;

                return distSq < 1.0 || distSq > 3600.0;
            }
        }

        @Override
        public boolean b() {
            return false;
        }

        @Override
        public void c() {
            Random random = this.ghast.getRandom();
            double x = this.ghast.locX() + (random.nextDouble() * 2.0 - 1.0) * 16.0;
            double y = this.ghast.locY() + (random.nextDouble() * 2.0 - 1.0) * 16.0;
            double z = this.ghast.locZ() + (random.nextDouble() * 2.0 - 1.0) * 16.0;

            this.ghast.getControllerMove().a(x, y, z, 1.0); // `setMoveTo()`
        }
    }
}
