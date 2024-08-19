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
    private boolean hasSpawnDeathFireballs;

    public CustomEntityGhast(World world) {
        super(EntityTypes.GHAST, world);
        this.initCustom();
        this.initAttackLevelingMob();
    }

    protected void initCustom() {
        this.hasSpawnDeathFireballs = false;
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
        int[] threshsMet = this.attackLevelingController.increaseAttacks(increase);

        for (int threshMet : threshsMet) {
            if (threshMet == attackThreshs[0]) {
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

    @Override
    public void a(EntityLiving goalTarget, float distFactor) {
        if (!this.isSilent()) {
            world.a(null, 1016, this.getChunkCoordinates(), 0);
        }
        
        Vec3D directionLook = this.f(1.0F);
        double motX = goalTarget.locX() - (this.locX() + directionLook.getX() * 4.0D);
        double motY = goalTarget.e(0.5D) - (0.5D + this.e(0.5D));
        double motZ = goalTarget.locZ() - (this.locZ() + directionLook.getZ() * 4.0D);

        /* After 180 attacks, ghasts shoot a ring of power 1 fireballs every 40 attacks */
        if (this.getAttacks() >= this.getAttacksThreshs()[1]
                && (this.getAttacks() - this.getAttacksThreshs()[1]) % 40 == 0) {
            new RunnableRingOfFireballs(this, 0.5, 1).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 20L);
        }

        int fireballPower = this.getPower();
        /* After 300 attacks, ghasts shoot a power 3 fireball every 50 attacks */
        if (this.getAttacks() >= this.getAttacksThreshs()[2]
                && (this.getAttacks() - this.getAttacksThreshs()[2]) % 50 == 0) {
            fireballPower = 3;
        }

        CustomEntityLargeFireball largeFireball = new CustomEntityLargeFireball(
                world, this, motX, motY, motZ, fireballPower);
        largeFireball.setPosition(
                this.locX() + directionLook.getX() * 4.0D,
                this.e(0.5D) + 0.5D,
                largeFireball.locZ() + directionLook.getZ() * 4.0D);
        world.addEntity(largeFireball);
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Overridden vanilla functions
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
        /* Rebounded fireballs do not do damage to ghasts */
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
        if (this.getHealth() <= 0.0 && !this.hasSpawnDeathFireballs) {
            this.hasSpawnDeathFireballs = true;
            new RunnableRingOfFireballs(this, 0.5, this.getAttacks() < 50 ? 1 : 3).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 30L); /* when killed, ghasts summon a lot of power 1 fireballs in all directions (2.5x more) after 50 attacks */
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Nested classes
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

    // todo finish
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
                // if there's no goal target, look around randomly
                Vec3D vec3d = this.ghast.getMot();

                this.ghast.yaw = -((float) MathHelper.d(vec3d.x, vec3d.z)) * 57.295776F;
                this.ghast.aH = this.ghast.yaw;
            } else {
                // else, look at goal target
                EntityLiving goalTarget = this.ghast.getGoalTarget();

                if (NmsUtil.distSq(this.ghast, goalTarget, true) < this.getDetectionRangeSq()) {
                    double d1 = goalTarget.locX() - this.ghast.locX();
                    double d2 = goalTarget.locZ() - this.ghast.locZ();

                    this.ghast.yaw = -((float) MathHelper.d(d1, d2)) * 57.295776F;
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

        private final EntityGhast a;

        public PathfinderGoalIdleMove(EntityGhast entityGhast) {
            this.a = entityGhast;
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE));
        }

        @Override
        public boolean a() {
            ControllerMove controllermove = this.a.getControllerMove();

            if (!controllermove.b()) {
                return true;
            } else {
                double d0 = controllermove.d() - this.a.locX();
                double d1 = controllermove.e() - this.a.locY();
                double d2 = controllermove.f() - this.a.locZ();
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                return d3 < 1.0 || d3 > 3600.0D;
            }
        }

        @Override
        public boolean b() {
            return false;
        }

        @Override
        public void c() {
            Random random = this.a.getRandom();
            double d0 = this.a.locX() + (random.nextDouble() * 2.0 - 1.0) * 16.0;
            double d1 = this.a.locY() + (random.nextDouble() * 2.0 - 1.0) * 16.0;
            double d2 = this.a.locZ() + (random.nextDouble() * 2.0 - 1.0) * 16.0;

            this.a.getControllerMove().a(d0, d1, d2, 1.0);
        }
    }
}
