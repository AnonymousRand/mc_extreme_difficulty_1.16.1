package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.AttackLevelingController;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Random;

public class CustomEntityDrowned extends EntityDrowned implements ICustomHostile, IAttackLevelingMob {

    public CustomEntityDrowned(World world) {
        super (EntityTypes.DROWNED, world);
        this.initCustom();
        this.initAttackLevelingMob();
    }

    private void initCustom() {
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);

        /* Drowned always spawn with tridents */
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.TRIDENT));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                      ICustomHostile                                       //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /* Drowned have 40 block detection range */
    public double getDetectionRange() {
        return 40.0;
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.getWorld().findNearbyPlayer(this, -1.0D);

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

    @Override
    public int bL() {
        return Integer.MAX_VALUE;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                    IAttackLevelingMob                                     //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private AttackLevelingController attackLevelingController = null;

    private void initAttackLevelingMob() {
        this.attackLevelingController = new AttackLevelingController(150, 350);
    }

    public int getAttacks() {
        return this.attackLevelingController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        for (int metThreshold : this.attackLevelingController.increaseAttacks(increase)) {
            int[] attackThresholds = this.getAttacksThresholds();
            if (metThreshold == attackThresholds[0]) {
                /* After 150 attacks, drowned summon a guardian */
                new SpawnEntity(this.getWorld(), new CustomEntityGuardian(this.getWorld()), 1, null, null, this, false, true);
            } else if (metThreshold == attackThresholds[1]) {
                /* After 350 attacks, drowned summon an elder guardian */
                new SpawnEntity(this.getWorld(), new CustomEntityGuardianElder(this.getWorld()), 1, null, null, this, false, true);
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
    public void m() {
        /* No longer targets iron golems and villagers */
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this));                                 /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));                                    /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 3.0));                       /* Spawns lightning randomly */
        this.goalSelector.a(1, new PathfinderGoalDrownedTridentBowAttack(this, 1.0D, 7, 40.0F));               /* Drowned throw tridents every 7 ticks, and continue attacking regardless of y-level and line of sight (the old goal stopped the mob from attacking even if it had already recognized a target via CustomNearestAttackableTarget) */
        this.goalSelector.a(2, new PathfinderGoalDrownedGoToWater(this, 1.0D));
        this.goalSelector.a(2, new CustomPathfinderGoalZombieAttack(this, 1.0D));                              /* Also attacks in the day, and continues attacking regardless of y-level and line of sight */
        this.goalSelector.a(5, new PathfinderGoalDrownedGoToBeach(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalSwimUp(this, 1.0D, this.getWorld().getSeaLevel()));
        this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget(this, new Class[0]));                    /* Doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Doesn't take into account y-level or line of sight to aggro a target */
        this.targetSelector.a(4, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, EntityTurtle.bv));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                Mob-specific goals/classes                                 //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    static class PathfinderGoalDrownedGoToBeach extends PathfinderGoalGotoTarget {

        private final CustomEntityDrowned drowned;

        public PathfinderGoalDrownedGoToBeach(CustomEntityDrowned drowned, double speed) {
            super(drowned, speed, 8, 2);
            this.drowned = drowned;
        }

        @Override
        public boolean a() {
            return super.a() && !this.drowned.world.isDay() && this.drowned.isInWater()
                    && this.drowned.locY() >= (double) (this.drowned.world.getSeaLevel() - 3);
        }

        @Override // shouldMoveTo()
        protected boolean a(IWorldReader iWorldReader, BlockPosition blockPosition) {
            BlockPosition blockPosition1 = blockPosition.up();

            return (iWorldReader.isEmpty(blockPosition1) && iWorldReader.isEmpty(blockPosition1.up()))
                    ? iWorldReader.getType(blockPosition).a(iWorldReader, blockPosition, this.drowned) : false;
        }

        @Override
        public void c() {
            this.drowned.t(false);
            this.drowned.navigation = this.drowned.navigationLand;
            super.c();
        }
    }

    static class PathfinderGoalDrownedGoToWater extends PathfinderGoal {

        private final CustomEntityDrowned drowned;
        private double waterX;
        private double waterY;
        private double waterZ;
        private final double speedTowardsTarget;
        private final World world;

        public PathfinderGoalDrownedGoToWater(CustomEntityDrowned drowned, double speedTowardsTarget) {
            this.drowned = drowned;
            this.speedTowardsTarget = speedTowardsTarget;
            this.world = drowned.getWorld();
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE));
        }

        @Override
        public boolean a() {
            if (!this.world.isDay()) {
                return false;
            } else if (this.drowned.isInWater()) {
                return false;
            } else {
                Vec3D vec3d = this.findWaterNearby();

                if (vec3d == null) {
                    return false;
                } else {
                    this.waterX = vec3d.x;
                    this.waterY = vec3d.y;
                    this.waterZ = vec3d.z;
                    return true;
                }
            }
        }

        @Override
        public boolean b() {
            return !this.drowned.getNavigation().m();
        }

        @Override
        public void c() {
            this.drowned.getNavigation().a(this.waterX, this.waterY, this.waterZ, this.speedTowardsTarget);
        }

        @Nullable
        private Vec3D findWaterNearby() {
            Random random = this.drowned.getRandom();
            BlockPosition currentPosition = this.drowned.getChunkCoordinates();

            for (int i = 0; i < 10; ++i) {
                BlockPosition blockPosition = currentPosition.b(random.nextInt(20) - 10, 2 - random.nextInt(8), random.nextInt(20) - 10);

                if (this.world.getType(blockPosition).a(Blocks.WATER)) {
                    return Vec3D.c(blockPosition);
                }
            }

            return null;
        }
    }

    static class PathfinderGoalSwimUp extends PathfinderGoal {

        private final CustomEntityDrowned drowned;
        private final double speedTowardsTarget;
        private final int targetY;
        private boolean obstructed;

        public PathfinderGoalSwimUp(CustomEntityDrowned drowned, double speedTowardsTarget, int targetY) {
            this.drowned = drowned;
            this.speedTowardsTarget = speedTowardsTarget;
            this.targetY = targetY;
        }

        @Override
        public boolean a() {
            return !this.drowned.getWorld().isDay() && this.drowned.isInWater()
                    && this.drowned.locY() < (double) (this.targetY - 2);
        }

        @Override
        public boolean b() {
            return this.a() && !this.obstructed;
        }

        @Override
        public void e() {
            if (this.drowned.locY() < (double) (this.targetY - 1) && (this.drowned.getNavigation().m() || this.drowned.eP())) {
                Vec3D vec3d = RandomPositionGenerator.b(this.drowned, 4, 8,
                        new Vec3D(this.drowned.locX(), this.targetY - 1, this.drowned.locZ()));

                if (vec3d == null) {
                    this.obstructed = true;
                    return;
                }

                this.drowned.getNavigation().a(vec3d.x, vec3d.y, vec3d.z, this.speedTowardsTarget);
            }
        }

        @Override
        public void c() {
            this.drowned.t(true);
            this.obstructed = false;
        }

        @Override
        public void d() {
            this.drowned.t(false);
        }
    }

    static class PathfinderGoalDrownedTridentBowAttack<T extends CustomEntityDrowned> extends CustomPathfinderGoalRangedAttack<T> {

        public PathfinderGoalDrownedTridentBowAttack(T drowned, double d0, int i, float f) {
            super(drowned, d0, i, f);
        }

        @Override
        public boolean a() {
            return super.a() && this.entity.getItemInMainHand().getItem() == Items.TRIDENT;
        }

        @Override
        public void c() {
            super.c();
            this.entity.setAggressive(true);
            this.entity.c(EnumHand.MAIN_HAND);
        }

        @Override
        public void d() {
            super.d();
            this.entity.clearActiveItem();
            this.entity.setAggressive(false);
        }
    }
}