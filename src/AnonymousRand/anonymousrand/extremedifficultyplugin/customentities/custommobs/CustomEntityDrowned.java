package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.AttackLevelingController;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Random;

public class CustomEntityDrowned extends EntityDrowned implements ICustomHostile, IAttackLevelingMob {

    private AttackLevelingController attackLevelingController;

    public CustomEntityDrowned(World world) {
        super (EntityTypes.DROWNED, world);
        this.initCustom();
        this.initAttackLevelingMob();
    }

    private void initCustom() {
        /* No longer avoids lava and fire */
        this.a(PathType.LAVA, 0.0F);
        this.a(PathType.DAMAGE_FIRE, 0.0F);

        /* Drowned always spawn with tridents */
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.TRIDENT));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                      ICustomHostile                                       //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public double getFollowRange() { /* drowned have 40 block detection range (setting attribute doesn't work) */
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
        double dist_x = this.locX() - x;
        double dist_z = this.locZ() - z;

        return dist_x * dist_x + dist_z * dist_z;
    }

    @Override
    public double d(Vec3D vec3d) {
        double dist_x = this.locX() - vec3d.x;
        double dist_z = this.locZ() - vec3d.z;

        return dist_x * dist_x + dist_z * dist_z;
    }

    @Override
    public int bL() {
        return Integer.MAX_VALUE;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                    IAttackLevelingMob                                     //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void initAttackLevelingMob() {
        this.attackLevelingController = new AttackLevelingController(150, 350);
    }

    public int getAttacks() {
        return this.attackLevelingController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        for (int metThreshold : this.attackLevelingController.increaseAttacks(increase)) {
            int[] attackThresholds = this.attackLevelingController.getAttacksThresholds();
            if (metThreshold == attackThresholds[0]) {
                /* After 150 attacks, drowned summon a guardian */
                new SpawnEntity(this.getWorld(), new CustomEntityGuardian(this.getWorld()), 1, null, null, this, false, true);
            } else if (metThreshold == attackThresholds[1]) {
                /* After 350 attacks, drowned summon an elder guardian */
                new SpawnEntity(this.getWorld(), new CustomEntityGuardianElder(this.getWorld()), 1, null, null, this, false, true);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                               Overridden vanilla functions                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void m() { /* drowned no longer target iron golems */
        /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this));
        /* Takes buffs from bats and piglins etc. */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 3.0)); /* custom goal that spawns lightning randomly */
        this.goalSelector.a(1, new PathfinderGoalDrownedTridentBowAttack(this, 1.0D, 6, 40.0F)); /* throws a trident every 6 ticks and uses the custom goal that attacks regardless of the y level (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal) */
        this.goalSelector.a(2, new PathfinderGoalDrownedGoToWater(this, 1.0D));
        this.goalSelector.a(2, new CustomEntityDrowned.PathfinderGoalDrownedAttack(this, 1.0D)); /* uses the custom melee attack goal that attacks regardless of the y level */
        this.goalSelector.a(5, new PathfinderGoalDrownedGoToBeach(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalSwimUp(this, 1.0D, this.getWorld().getSeaLevel()));
        this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
        this.targetSelector.a(0, (new CustomPathfinderGoalHurtByTarget(this, new Class[]{EntityDrowned.class})).a(EntityPigZombie.class)); /* custom goal that prevents mobs from retaliating against other mobs in case the mob damage event doesn't register and cancel the damage */
        /* Doesn't need line of sight to find targets and start attacking */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class));
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class));
        this.targetSelector.a(4, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, EntityTurtle.bv));
    }

    @Override
    public boolean j(@Nullable EntityLiving entityLiving) { /* always attacks even in the day */
        return true;
    }

    static class PathfinderGoalDrownedAttack extends CustomPathfinderGoalZombieAttack {

        private final CustomEntityDrowned drowned;

        public PathfinderGoalDrownedAttack(CustomEntityDrowned drowned, double d0) {
            super(drowned, d0);
            this.drowned = drowned;
        }

        @Override
        public boolean a() {
            return super.a() && this.drowned.j(this.drowned.getGoalTarget());
        }

        @Override
        public boolean b() {
            return super.b() && this.drowned.j(this.drowned.getGoalTarget());
        }
    }

    static class PathfinderGoalDrownedGoToBeach extends PathfinderGoalGotoTarget {

        private final CustomEntityDrowned drowned;

        public PathfinderGoalDrownedGoToBeach(CustomEntityDrowned entityDrowned, double d0) {
            super(entityDrowned, d0, 8, 2);
            this.drowned = entityDrowned;
        }

        @Override
        public boolean a() {
            return super.a() && !this.drowned.world.isDay() && this.drowned.isInWater() && this.drowned.locY() >= (double)(this.drowned.world.getSeaLevel() - 3);
        }

        @Override
        public boolean b() {
            return super.b();
        }

        @Override
        protected boolean a(IWorldReader iworldreader, BlockPosition blockPosition) {
            BlockPosition blockPosition1 = blockPosition.up();

            return iworldreader.isEmpty(blockPosition1) && iworldreader.isEmpty(blockPosition1.up()) ? iworldreader.getType(blockPosition).a(iworldreader, blockPosition, this.drowned) : false;
        }

        @Override
        public void c() {
            this.drowned.t(false);
            this.drowned.navigation = this.drowned.navigationLand;
            super.c();
        }

        @Override
        public void d() {
            super.d();
        }
    }

    static class PathfinderGoalDrownedGoToWater extends PathfinderGoal {

        private final CustomEntityDrowned drowned;
        private double b;
        private double c;
        private double d;
        private final double e;
        private final World world;

        public PathfinderGoalDrownedGoToWater(CustomEntityDrowned drowned, double d0) {
            this.drowned = drowned;
            this.e = d0;
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
                Vec3D vec3d = this.g();

                if (vec3d == null) {
                    return false;
                } else {
                    this.b = vec3d.x;
                    this.c = vec3d.y;
                    this.d = vec3d.z;
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
            this.drowned.getNavigation().a(this.b, this.c, this.d, this.e);
        }

        @Nullable
        private Vec3D g() {
            Random random = this.drowned.getRandom();
            BlockPosition blockPosition = this.drowned.getChunkCoordinates();

            for (int i = 0; i < 10; ++i) {
                BlockPosition blockPosition1 = blockPosition.b(random.nextInt(20) - 10, 2 - random.nextInt(8), random.nextInt(20) - 10);

                if (this.world.getType(blockPosition1).a(Blocks.WATER)) {
                    return Vec3D.c(blockPosition1);
                }
            }

            return null;
        }
    }

    static class PathfinderGoalSwimUp extends PathfinderGoal {

        private final CustomEntityDrowned drowned;
        private final double b;
        private final int c;
        private boolean d;

        public PathfinderGoalSwimUp(CustomEntityDrowned entityDrowned, double d0, int i) {
            this.drowned = entityDrowned;
            this.b = d0;
            this.c = i;
        }

        @Override
        public boolean a() {
            return !this.drowned.world.isDay() && this.drowned.isInWater() && this.drowned.locY() < (double)(this.c - 2);
        }

        @Override
        public boolean b() {
            return this.a() && !this.d;
        }

        @Override
        public void e() {
            if (this.drowned.locY() < (double)(this.c - 1) && (this.drowned.getNavigation().m() || this.drowned.eP())) {
                Vec3D vec3d = RandomPositionGenerator.b(this.drowned, 4, 8, new Vec3D(this.drowned.locX(), this.c - 1, this.drowned.locZ()));

                if (vec3d == null) {
                    this.d = true;
                    return;
                }

                this.drowned.getNavigation().a(vec3d.x, vec3d.y, vec3d.z, this.b);
            }

        }

        @Override
        public void c() {
            this.drowned.t(true);
            this.d = false;
        }

        @Override
        public void d() {
            this.drowned.t(false);
        }
    }

    static class PathfinderGoalDrownedTridentBowAttack<T extends CustomEntityDrowned> extends CustomPathfinderGoalRangedAttack<T> {

        private final CustomEntityDrowned drowned;

        public PathfinderGoalDrownedTridentBowAttack(T drowned, double d0, int i, float f) {
            super(drowned, d0, i, f);
            this.drowned = drowned;
        }

        @Override
        public boolean a() {
            return super.a() && this.drowned.getItemInMainHand().getItem() == Items.TRIDENT;
        }

        @Override
        public void c() {
            super.c();
            this.drowned.setAggressive(true);
            this.drowned.c(EnumHand.MAIN_HAND);
        }

        @Override
        public void d() {
            super.d();
            this.drowned.clearActiveItem();
            this.drowned.setAggressive(false);
        }

        @Override
        public void e() {
            for (int i = 0; i < (this.drowned.getAttacks() < 30 ? 1 : this.drowned.getAttacks() < 70 ? 2 : 3); i++) { /* shoots 1, 2 or 3 tridents at a time depending on attack count */
                super.e();
            }

            this.drowned.increaseAttacks(1);
        }
    }
}
