package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Random;

public class CustomEntityDrowned extends EntityDrowned implements ICommonCustomMethods {

    public int attacks;
    private boolean a50, a100;

    public CustomEntityDrowned(World world) {
        super (EntityTypes.DROWNED, world);
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.TRIDENT)); /**drowned always spawn with tridents*/
        this.a(PathType.LAVA, 0.0F); /**no longer avoids lava*/
        this.a(PathType.DAMAGE_FIRE, 0.0F); /**no longer avoids fire*/
        this.attacks = 0;
        this.a50 = false;
        this.a100 = false;
    }

    @Override
    public void m() { /**drowned no longer target iron golems*/
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 3.0)); /**custom goal that spawns lightning randomly*/
        this.goalSelector.a(1, new CustomEntityDrowned.CustomPathfinderGoalDrownedTridentAttack(this, 1.0D, 2, 40.0F)); /**throws a trident every 2 ticks and uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal)*/
        this.goalSelector.a(2, new PathfinderGoalDrownedGoToWater(this, 1.0D));
        this.goalSelector.a(2, new CustomEntityDrowned.CustomPathfinderGoalDrownedAttack(this, 1.0D, false)); /**uses the custom melee attack goal that attacks even when line of sight is broken*/
        this.goalSelector.a(5, new PathfinderGoalDrownedGoToBeach(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalSwimUp(this, 1.0D, this.getWorld().getSeaLevel()));
        this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
        this.targetSelector.a(0, (new CustomPathfinderGoalHurtByTarget(this, new Class[]{EntityDrowned.class})).a(EntityPigZombie.class)); /**custom goal that prevents mobs from retaliating against other mobs in case the mob damage event doesn't register and cancel the damage*/
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, 10, true, false, this::j)); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class, false));
        this.targetSelector.a(4, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, true, false, EntityTurtle.bv));
    }

    @Override
    public boolean j(@Nullable EntityLiving entityliving) {
        return true; /**always attacks even in the day*/
    }

    public double getFollowRange() { /**drowned have 40 block detection range (setting attribute doesn't work)*/
        return 40.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 50 && !this.a50) { /**after 50 attacks, drowned summon a guardian*/
            this.a50 = true;
            new SpawnLivingEntity(this.getWorld(), new CustomEntityGuardian(this.getWorld()), 2, null, null, this, false, true);
        }

        if (this.attacks == 100 && !this.a100) { /**after 100 attacks, drowned summon an elder guardian*/
            this.a100 = true;
            new SpawnLivingEntity(this.getWorld(), new CustomEntityGuardianElder(this.getWorld()), 1, null, null, this, false, true);
        }

        if (this.ticksLived == 10) { /**drowned only have 13.5 health*/
            this.setHealth(13.5F);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(13.5);
        }
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityhuman = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (entityhuman != null) {
                double d0 = Math.pow(entityhuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityhuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); /**mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleepingdouble d0 = entityhuman.h(this);*/
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (d0 > (double)j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 8; /**random despawn distance increased to 40 blocks*/
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
        double d3 = this.locX() - d0; /**for determining distance to entities, y level does not matter, eg. mob follow range, attacking (can hit player no matter the y level)*/
        double d5 = this.locZ() - d2;

        return d3 * d3 + d5 * d5;
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /**for determining distance to entities, y level does not matter, eg. mob follow range, attacking (can hit player no matter the y level)*/
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }

    @Override
    public int bL() { //getMaxFallHeight
        if (this.getGoalTarget() == null) {
            return 3;
        } else {
            int i = (int)(this.getHealth() * 20.0); /**mobs are willing to take 20 times the fall distance (same damage) to reach and do not stop taking falls if it is at less than 33% health*/

            return i + 3;
        }
    }

    static class CustomPathfinderGoalDrownedAttack extends CustomPathfinderGoalZombieAttack {

        private final CustomEntityDrowned drowned;

        public CustomPathfinderGoalDrownedAttack(CustomEntityDrowned drowned, double d0, boolean flag) {
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

        public PathfinderGoalDrownedGoToBeach(CustomEntityDrowned entitydrowned, double d0) {
            super(entitydrowned, d0, 8, 2);
            this.drowned = entitydrowned;
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
        protected boolean a(IWorldReader iworldreader, BlockPosition blockposition) {
            BlockPosition blockposition1 = blockposition.up();

            return iworldreader.isEmpty(blockposition1) && iworldreader.isEmpty(blockposition1.up()) ? iworldreader.getType(blockposition).a((IBlockAccess) iworldreader, blockposition, (Entity) this.drowned) : false;
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
            BlockPosition blockposition = this.drowned.getChunkCoordinates();

            for (int i = 0; i < 10; ++i) {
                BlockPosition blockposition1 = blockposition.b(random.nextInt(20) - 10, 2 - random.nextInt(8), random.nextInt(20) - 10);

                if (this.world.getType(blockposition1).a(Blocks.WATER)) {
                    return Vec3D.c((BaseBlockPosition)blockposition1);
                }
            }

            return null;
        }
    }

    protected static Method eX;
    private static Field d;

    static {
        try {
            eX = EntityDrowned.class.getDeclaredMethod("eX");
            eX.setAccessible(true);
            d = EntityDrowned.class.getDeclaredField("d");
            d.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    static class PathfinderGoalSwimUp extends PathfinderGoal {

        private final CustomEntityDrowned drowned;
        private final double b;
        private final int c;
        private boolean d;

        public PathfinderGoalSwimUp(CustomEntityDrowned entitydrowned, double d0, int i) {
            this.drowned = entitydrowned;
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
                Vec3D vec3d = RandomPositionGenerator.b(this.drowned, 4, 8, new Vec3D(this.drowned.locX(), (double)(this.c - 1), this.drowned.locZ()));

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

    static class CustomPathfinderGoalDrownedTridentAttack extends CustomPathfinderGoalArrowAttack {

        private final CustomEntityDrowned drowned;
        private int attackCount;

        public CustomPathfinderGoalDrownedTridentAttack(IRangedEntity irangedentity, double d0, int i, float f) {
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
            for (int i = 0; i < (this.drowned.attacks < 30 ? 1 : this.drowned.attacks < 70 ? 3 : 6); i++) { /**shoots 1, 3 or 6 tridents at a time depending on attack count*/
                super.e();
            }

            if (++this.attackCount == 20) { //attack count only goes up every second
                this.attackCount = 0;
                this.drowned.attacks++;
            }
        }
    }
}
