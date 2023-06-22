package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Random;

public class CustomEntityDrowned extends EntityDrowned implements ICustomMob, IAttackLevelingMob {

    private int attacks;
    private boolean a50, a100;

    public CustomEntityDrowned(World world) {
        super (EntityTypes.DROWNED, world);
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.TRIDENT)); /** drowned always spawn with tridents */
        this.a(PathType.LAVA, 0.0F); /** no longer avoids lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F); /** no longer avoids fire */
        this.attacks = 0;
        this.a50 = false;
        this.a100 = false;
    }

    @Override
    public void m() { /** drowned no longer target iron golems */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /** custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /** custom goal that allows this mob to take certain buffs from bats etc. */
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 3.0)); /** custom goal that spawns lightning randomly */
        this.goalSelector.a(1, new CustomEntityDrowned.PathfinderGoalDrownedTridentAttack(this, 1.0D, 3, 40.0F)); /** throws a trident every 3 ticks and uses the custom goal that attacks regardless of the y level (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal) */
        this.goalSelector.a(2, new PathfinderGoalDrownedGoToWater(this, 1.0D));
        this.goalSelector.a(2, new CustomEntityDrowned.PathfinderGoalDrownedAttack(this, 1.0D, false)); /** uses the custom melee attack goal that attacks regardless of the y level */
        this.goalSelector.a(5, new PathfinderGoalDrownedGoToBeach(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalSwimUp(this, 1.0D, this.getWorld().getSeaLevel()));
        this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
        this.targetSelector.a(0, (new CustomPathfinderGoalHurtByTarget(this, new Class[]{EntityDrowned.class})).a(EntityPigZombie.class)); /** custom goal that prevents mobs from retaliating against other mobs in case the mob damage event doesn't register and cancel the damage */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, 10, true, false, this::j)); /** uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class, false));
        this.targetSelector.a(4, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, true, false, EntityTurtle.bv));
    }

    @Override
    public boolean j(@Nullable EntityLiving entityLiving) { /** always attacks even in the day */
        return true;
    }

    public double getFollowRange() { /** drowned have 40 block detection range (setting attribute doesn't work) */
        return 40.0;
    }

    public int getAttacks() {
        return this.attacks;
    }

    public void incrementAttacks(int increase) {
        this.attacks += increase;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 50 && !this.a50) { /** after 50 attacks, drowned summon a guardian */
            this.a50 = true;
            new SpawnEntity(this.getWorld(), new CustomEntityGuardian(this.getWorld()), 2, null, null, this, false, true);
        }

        if (this.attacks == 100 && !this.a100) { /** after 100 attacks, drowned summon an elder guardian */
            this.a100 = true;
            new SpawnEntity(this.getWorld(), new CustomEntityGuardianElder(this.getWorld()), 1, null, null, this, false, true);
        }
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityHuman = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (entityHuman != null) {
                double d0 = Math.pow(entityHuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityHuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); /** mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleepingdouble d0 = entityHuman.h(this); */
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

    @Override
    public int bL() {
        return Integer.MAX_VALUE; /** mobs are willing to take any fall to reach the player as they don't take fall damage */
    }

    static class PathfinderGoalDrownedAttack extends CustomPathfinderGoalZombieAttack {

        private final CustomEntityDrowned drowned;

        public PathfinderGoalDrownedAttack(CustomEntityDrowned drowned, double d0, boolean flag) {
            super(drowned, d0, flag);
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

    static class PathfinderGoalDrownedTridentAttack extends CustomPathfinderGoalArrowAttack {

        private final CustomEntityDrowned drowned;
        private int attackCount;

        public PathfinderGoalDrownedTridentAttack(IRangedEntity irangedentity, double d0, int i, float f) {
            super(irangedentity, d0, i, f);
            this.drowned = (CustomEntityDrowned)irangedentity;
            this.attackCount = 0;
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
            for (int i = 0; i < (this.drowned.attacks < 30 ? 1 : this.drowned.attacks < 70 ? 3 : 5); i++) { /** shoots 1, 3 or 5 tridents at a time depending on attack count */
                super.e();
            }

            if (++this.attackCount == 20) { // attack count only goes up every second
                this.attackCount = 0;
                this.drowned.attacks++;
            }
        }
    }
}
