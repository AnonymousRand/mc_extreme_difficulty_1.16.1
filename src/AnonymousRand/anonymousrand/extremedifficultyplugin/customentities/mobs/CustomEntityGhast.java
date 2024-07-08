package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.AttackLevelingController;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.projectiles.CustomEntityLargeFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalBreakBlocksAround;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalMoveFasterInCobweb;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableRingOfFireballs;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

import java.util.EnumSet;
import java.util.Random;

public class CustomEntityGhast extends EntityGhast implements ICustomHostile, IAttackLevelingMob {

    private AttackLevelingController attackLevelingController = null;
    private boolean deathFireballs;

    public CustomEntityGhast(World world) {
        super(EntityTypes.GHAST, world);
        this.initCustom();
        this.initAttackLevelingMob();
    }

    private void initCustom() {
        this.deathFireballs = false;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                      ICustomHostile                                       //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public double getDetectionRange() { /* ghasts have 80 block detection range */
        return 80.0;
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.world.findNearbyPlayer(this, -1.0D);

            if (nearestPlayer != null) {
                /* Mobs only despawn along horizontal axes, so even at y=256, mobs will spawn below you and prevent sleeping */
                double distSqToNearestPlayer = Math.pow(nearestPlayer.getPositionVector().getX() - this.getPositionVector().getX(), 2)
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
        this.attackLevelingController = new AttackLevelingController(20);
    }

    public int getAttacks() {
        return this.attackLevelingController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        for (int metThreshold : this.attackLevelingController.increaseAttacks(increase)) {
            int[] attackThresholds = this.getAttacksThresholds();
            if (metThreshold == attackThresholds[0]) {
                /* After 20 attacks, ghasts get 16 max health and health */
                ((LivingEntity) this.getBukkitEntity()).setMaxHealth(16.0);
                this.setHealth(16.0F);
            }
        }
    }

    public int[] getAttacksThresholds() {
        return this.attackLevelingController.getAttacksThresholds();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                               Overridden vanilla functions                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void initPathfinder() {
        /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this));
        /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 80, 2, 2, 2, 0, false)); /* Breaks most blocks around the mob periodically */
        this.goalSelector.a(5, new CustomEntityGhast.PathfinderGoalGhastIdleMove(this));
        this.goalSelector.a(7, new CustomEntityGhast.PathfinderGoalGhastMoveTowardsTarget(this));
        this.goalSelector.a(7, new PathfinderGoalGhastFireball(this)); /* Continues attacking regardless of y-level and line of sight (the old goal stopped the mob from attacking even if it has a target via CustomNearestAttackableTarget) */
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Doesn't take into account y-level or line of sight to aggro a target or and maintain it as the target */
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damageAmount) {
        if (this.isInvulnerable(damageSource)) {
            return false;
        } else if (damageSource.j() instanceof EntityLargeFireball && damageSource.getEntity() instanceof EntityHuman) { /* rebounded fireballs do not do damage */
            return false;
        } else {
            return super.damageEntity(damageSource, damageAmount);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getHealth() <= 0.0 && !this.deathFireballs) { // do this here instead of in die() so that the fireballs don't have to wait until the death animation finishes playing to start firing
            this.deathFireballs = true;
            new RunnableRingOfFireballs(this, 0.5, this.getAttacks() < 50 ? 2 : 5).runTaskTimer(StaticPlugin.plugin, 0L, 30L); /* when killed, ghasts summon a lot of power 1 fireballs in all directions (2.5x more) after 50 attacks */
        }
    }

    static class PathfinderGoalGhastFireball extends PathfinderGoal {

        private final CustomEntityGhast ghast;
        public int chargeTime, attackIncrement;
        private boolean power3, ring;

        public PathfinderGoalGhastFireball(CustomEntityGhast entityGhast) {
            this.ghast = entityGhast;
            this.attackIncrement = 0;
            this.power3 = false;
            this.ring = false;
        }

        @Override
        public boolean a() {
            return this.ghast.getGoalTarget() != null;
        }

        @Override
        public void c() {
            this.chargeTime = 0;
        }

        @Override
        public void d() {
            this.ghast.t(false);
        }

        @Override
        public void e() {
            EntityLiving entityLiving = this.ghast.getGoalTarget();

            if (this.ghast.d(entityLiving.getPositionVector()) < 6400.0D) { /* removed line of sight requirement for ghast attack, and too much vertical distance no longer stops the ghast from firing */
                World world = this.ghast.getWorld();

                ++this.chargeTime;
                if (this.chargeTime == 10 && !this.ghast.isSilent()) { // this doesn't seem to affect anything
                    world.a(null, 1015, this.ghast.getChunkCoordinates(), 0);
                }

                this.ghast.t(this.chargeTime > 2); // shooting animation only plays for 2 ticks

                if (this.chargeTime == 5) { /* shoots a fireball every 5 ticks */
                    if (++this.attackIncrement == 6) { // attacks only count every 1.5 seconds, or 6 shots
                        this.ghast.increaseAttacks(1);
                        this.attackIncrement = 0;
                    }

                    Vec3D vec3d = this.ghast.f(1.0F);
                    double d2 = entityLiving.locX() - (this.ghast.locX() + vec3d.x * 4.0D);
                    double d3 = entityLiving.e(0.5D) - (0.5D + this.ghast.e(0.5D));
                    double d4 = entityLiving.locZ() - (this.ghast.locZ() + vec3d.z * 4.0D);

                    if (!this.ghast.isSilent()) {
                        world.a(null, 1016, this.ghast.getChunkCoordinates(), 0);
                    }

                    if (this.ghast.getAttacks() >= 30 && (this.ghast.getAttacks() - 30) % 5 == 0 && this.ring) { // reset booleans for next cycle
                        this.ring = false;
                    }

                    if (this.ghast.getAttacks() >= 50 && (this.ghast.getAttacks() - 50) % 7 == 0 && this.power3) { // reset booleans for next cycle
                        this.power3 = false;
                    }

                    if (this.ghast.getAttacks() >= 30 && (this.ghast.getAttacks() - 30) % 6 == 0 && !this.ring) { /* after 30 attacks, the ghast shoots a ring of power 1 fireballs every 9 seconds */
                        this.ring = true;
                        new RunnableRingOfFireballs(this.ghast, 0.5, 1).runTaskTimer(StaticPlugin.plugin, 0L, 20L);
                    }

                    CustomEntityLargeFireball largeFireball;

                    if (this.ghast.getAttacks() >= 50 && (this.ghast.getAttacks() - 50) % 8 == 0 && !this.power3) { /* after 50 attacks, the ghast shoots a power 3 fireball every 12 seconds */
                        this.power3 = true;
                        largeFireball = new CustomEntityLargeFireball(world, this.ghast, d2, d3, d4, 3);
                    } else {
                        largeFireball = new CustomEntityLargeFireball(world, this.ghast, d2, d3, d4, this.ghast.getPower());
                    }

                    largeFireball.setPosition(this.ghast.locX() + vec3d.x * 4.0D, this.ghast.e(0.5D) + 0.5D, largeFireball.locZ() + vec3d.z * 4.0D);
                    world.addEntity(largeFireball);
                    this.chargeTime = 0;
                }
            } else if (this.chargeTime > 0) {
                --this.chargeTime;
            }
        }
    }

    static class PathfinderGoalGhastMoveTowardsTarget extends PathfinderGoal {

        private final EntityGhast a;

        public PathfinderGoalGhastMoveTowardsTarget(EntityGhast entityGhast) {
            this.a = entityGhast;
            this.a(EnumSet.of(PathfinderGoal.Type.LOOK));
        }

        @Override
        public boolean a() {
            return true;
        }

        @Override
        public void e() {
            if (this.a.getGoalTarget() == null) {
                Vec3D vec3d = this.a.getMot();

                this.a.yaw = -((float) MathHelper.d(vec3d.x, vec3d.z)) * 57.295776F;
                this.a.aH = this.a.yaw;
            } else {
                EntityLiving entityLiving = this.a.getGoalTarget();

                if (entityLiving.h(this.a) < 4096.0D) {
                    double d1 = entityLiving.locX() - this.a.locX();
                    double d2 = entityLiving.locZ() - this.a.locZ();

                    this.a.yaw = -((float) MathHelper.d(d1, d2)) * 57.295776F;
                    this.a.aH = this.a.yaw;
                }
            }
        }
    }

    static class PathfinderGoalGhastIdleMove extends PathfinderGoal {

        private final EntityGhast a;

        public PathfinderGoalGhastIdleMove(EntityGhast entityGhast) {
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

                return d3 < 1.0D || d3 > 3600.0D;
            }
        }

        @Override
        public boolean b() {
            return false;
        }

        @Override
        public void c() {
            Random random = this.a.getRandom();
            double d0 = this.a.locX() + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double d1 = this.a.locY() + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double d2 = this.a.locZ() + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);

            this.a.getControllerMove().a(d0, d1, d2, 1.0D);
        }
    }
}
