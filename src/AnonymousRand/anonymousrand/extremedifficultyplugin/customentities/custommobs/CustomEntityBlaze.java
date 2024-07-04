package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityLargeFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntitySmallFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalCobwebMoveFaster;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableRingOfFireballs;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumSet;

public class CustomEntityBlaze extends EntityBlaze implements ICustomHostile, IAttackLevelingMob, IGoalRemovingMob {

    private AttackController attackController;
    public PathfinderGoalSelector vanillaTargetSelector;
    private boolean rapidFire;

    public CustomEntityBlaze(World world) {
        super(EntityTypes.BLAZE, world);
        initCustomHostile();
        initAttackLevelingMob();
        initGoalRemoval();
    }

    //////////////////////////////////////  ICustomHostile  ///////////////////////////////////////

    public void initCustomHostile() {
        /** No longer avoids water */
        this.a(PathType.WATER, 0.0F);
        /** No longer avoids lava */
        this.a(PathType.LAVA, 0.0F);
        /** No longer avoids fire */
        this.a(PathType.DAMAGE_FIRE, 0.0F);

        this.rapidFire = false;

        this.initAttributes();
    }

    private void initAttributes() {
        /** Blazes only have 12.5 health */
        this.setHealth(12.5F);
        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(12.5);
    }

    public double getFollowRange() {
        /** Blazes have 40 block detection range (setting attribute doesn't work) */
        return 40.0;
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityHuman = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (entityHuman != null) {
                double d0 = Math.pow(entityHuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityHuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); /** mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleeping */
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (d0 > (double)j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 8; /** random despawn distance increased to 40 blocks */
                int l = k * k;

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && d0 > (double)l && this.isTypeNotPersistent(d0)) {
                    this.die();
                } else if (d0 < (double)l) {
                    this.ticksFarFromPlayer = 0;
                }
            }

        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    @Override
    public double g(double d0, double d1, double d2) {
        double d3 = this.locX() - d0; /** for determining distance to entities, y level does not matter, e.g. mob follow range, attacking (can hit player no matter the y level) */
        double d5 = this.locZ() - d2;

        return d3 * d3 + d5 * d5;
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /** for determining distance to entities, y level does not matter, e.g. mob follow range, attacking (can hit player no matter the y level) */
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }

    ////////////////////////////////////  IAttackLevelingMob  /////////////////////////////////////

    public void initAttackLevelingMob() {
        this.attackController = new AttackController(75, 175, 350);
    }

    public int getAttacks() {
        return this.attackController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        for (int metThreshold : this.attackController.increaseAttacks(increase)) {
            int[] attackThresholds = this.attackController.getAttacksThresholds();
            if (metThreshold == attackThresholds[0]) {
                /** After 75 attacks, blazes shoot an exploding fireball with power 1 */
                double d1 = this.getGoalTarget().locX() - this.locX();
                double d2 = this.getGoalTarget().e(0.5D) - this.e(0.5D);
                double d3 = this.getGoalTarget().locZ() - this.locZ();
                CustomEntityLargeFireball entityLargeFireball = new CustomEntityLargeFireball(this.getWorld(), this, d1, d2, d3, 1);
                entityLargeFireball.setPosition(entityLargeFireball.locX(), this.e(0.5D) + 0.5D, entityLargeFireball.locZ());
                this.getWorld().addEntity(entityLargeFireball);
            } else if (metThreshold == attackThresholds[1]) {
                /** After 175 attacks, blazes shoot out a ring of fireballs */
                new RunnableRingOfFireballs(this, 0.5, 1).run();
            } else if (metThreshold == attackThresholds[2]) {
                /** After 350 attacks, blazes shoot even faster (+100% vanilla speed compared to +50%, both with no attack cooldown between volleys), and shoot 3 fireballs at a time in a cone-like shape */
                this.rapidFire = true;
            }
        }
    }

    /////////////////////////////////////  IGoalRemovingMob  //////////////////////////////////////

    public void initGoalRemoval() {
        this.vanillaTargetSelector = super.targetSelector;
        // remove vanilla HurtByTarget and NearestAttackableTarget goals to replace them with custom ones
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this);
    }

    public PathfinderGoalSelector getVanillaTargetSelector() {
        return this.vanillaTargetSelector;
    }

    //////////////////////////////////  Other custom functions  ///////////////////////////////////

    public boolean getRapidFire() {
        return this.rapidFire;
    }

    ///////////////////////////////  Overridden vanilla functions  ////////////////////////////////

    @Override
    protected void initPathfinder() {
        super.initPathfinder();
        /** Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this));
        /** Takes buffs from bats and piglins etc. */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));
        this.goalSelector.a(3, new PathfinderGoalBlazeFireballAttack(this));
        /** Doesn't need line of sight to find targets and start attacking */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class));
    }

    @Override
    public boolean dN() {
        /** No longer damaged by water */
        return false;
    }

    static class PathfinderGoalBlazeFireballAttack extends PathfinderGoal {
        private final CustomEntityBlaze blaze;
        private final World nmsWorld;
        private int c;
        private int d;

        public PathfinderGoalBlazeFireballAttack(CustomEntityBlaze entityBlaze) {
            this.blaze = entityBlaze;
            this.nmsWorld = entityBlaze.getWorld();
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        }

        @Override
        public boolean a() {
            EntityLiving entityLiving = this.blaze.getGoalTarget();
            return entityLiving != null && entityLiving.isAlive() && this.blaze.d(entityLiving);
        }

        @Override
        public void d() {
            this.d = 0;
        }

        @Override
        public void e() {
            --this.c;
            EntityLiving entityLiving = this.blaze.getGoalTarget();

            if (entityLiving != null) { /** attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via nearestAttackableTarget goal) */
                ++this.d;

                double d0 = this.blaze.h((Entity)entityLiving);

                if (d0 < 3.0D) {
                    if (this.c <= 0) {
                        this.blaze.attackEntity(entityLiving);
                        this.blaze.getWorld().createExplosion(this.blaze, entityLiving.locX(), entityLiving.locY(), entityLiving.locZ(), 0.4F, false, Explosion.Effect.DESTROY); /** melee attack creates a power 0.4 explosion on player's location */
                    }

                    this.blaze.getControllerMove().a(entityLiving.locX(), entityLiving.locY(), entityLiving.locZ(), 1.0D);
                } else if (d0 < this.g() * this.g()) {
                    double d1 = entityLiving.locX() - this.blaze.locX();
                    double d2 = entityLiving.e(0.5D) - this.blaze.e(0.5D);
                    double d3 = entityLiving.locZ() - this.blaze.locZ();

                    if (this.c <= 0) {
                        if (!this.blaze.getRapidFire()) { /** no pause between each volley; shoots constantly */
                            this.c = 4; /** +50% attack speed */
                        } else {
                            this.c = 3; /** doubled attack speed during rapidfire */
                        }

                        float defaultInaccuracy = MathHelper.c(MathHelper.sqrt(d0)) * 0.5F;

                        if (!this.blaze.isSilent()) {
                            this.nmsWorld.a(null, 1018, this.blaze.getChunkCoordinates(), 0);
                        }

                        if (!this.blaze.getRapidFire()) {
                            CustomEntitySmallFireball entitySmallFireball = new CustomEntitySmallFireball(this.nmsWorld, this.blaze, d1, d2, d3); /** blaze has no inaccuracy */
                            entitySmallFireball.setPosition(entitySmallFireball.locX(), this.blaze.e(0.5D) + 0.5D, entitySmallFireball.locZ());
                            this.nmsWorld.addEntity(entitySmallFireball);
                            this.blaze.increaseAttacks(1);
                        } else {
                            CustomEntitySmallFireball entitySmallFireball;
                            for (int i = 0; i < 3; i++) {
                                /** When in rapidfire state, blazes shoot 3 fireballs at a time, with 35% of the default inaccuracy */
                                entitySmallFireball = new CustomEntitySmallFireball(this.nmsWorld, this.blaze, d1 + this.blaze.getRandom().nextGaussian() * defaultInaccuracy * 0.35, d2, d3 + this.blaze.getRandom().nextGaussian() * defaultInaccuracy * 0.35);
                                entitySmallFireball.setPosition(entitySmallFireball.locX(), this.blaze.e(0.5D) + 0.5D, entitySmallFireball.locZ());
                                this.nmsWorld.addEntity(entitySmallFireball);
                            }
                            this.blaze.increaseAttacks(1);
                        }
                    }

                    this.blaze.getControllerLook().a(entityLiving, 10.0F, 10.0F);
                } else if (this.d < 5) {
                    this.blaze.getControllerMove().a(entityLiving.locX(), entityLiving.locY(), entityLiving.locZ(), 1.0D);
                }

                super.e();
            }
        }

        private double g() {
            return this.blaze.b(GenericAttributes.FOLLOW_RANGE);
        }
    }

    static class RunnableBlazeRapidFire extends BukkitRunnable {

        private final CustomEntityBlaze blaze;
        private final World nmsWorld;
        private final double d1, d2, d3, f;

        public RunnableBlazeRapidFire(CustomEntityBlaze blaze, double d1, double d2, double d3, double f) {
            this.blaze = blaze;
            this.nmsWorld = blaze.getWorld();
            this.d1 = d1;
            this.d2 = d2;
            this.d3 = d3;
            this.f = f;
        }

        @Override
        public void run() {
            for (int i = 0; i < 5; i++) { /** shoots 5 fireballs at a time during this phase */
                CustomEntitySmallFireball entitySmallFireball = new CustomEntitySmallFireball(this.nmsWorld, this.blaze, d1 + this.blaze.getRandom().nextGaussian() * f * 0.5, d2, d3 + this.blaze.getRandom().nextGaussian() * f * 0.6); /** blaze has 0.6x default inaccuracy in this phase */

                entitySmallFireball.setPosition(entitySmallFireball.locX(), this.blaze.e(0.5D) + 0.5D, entitySmallFireball.locZ());
                this.nmsWorld.addEntity(entitySmallFireball);
            }
        }
    }
}