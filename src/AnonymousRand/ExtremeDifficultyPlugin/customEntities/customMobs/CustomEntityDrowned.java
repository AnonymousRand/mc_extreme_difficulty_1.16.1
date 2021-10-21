package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.CustomEntityLightning;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalArrowAttack;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalZombieAttack;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderTargetCondition;
import AnonymousRand.ExtremeDifficultyPlugin.util.CoordsFromHypotenuse;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Random;

public class CustomEntityDrowned extends EntityDrowned {

    public CustomEntityDrowned(World world) {
        super (EntityTypes.DROWNED, world);

        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.TRIDENT)); /**drowned always spawn with tridents*/
    }

    @Override
    public void m() { /**drowned no longer target iron golems*/
        this.goalSelector.a(2, new PathfinderGoalDrownedGoToWater(this, 1.0D));
        this.goalSelector.a(1, new CustomPathfinderGoalDrownedTridentAttack(this, 1.0D, 2, 10.0F)); /**throws a trident every 2 ticks*/
        this.goalSelector.a(2, new CustomEntityDrowned.CustomPathfinderGoalDrownedAttack(this, 1.0D, false)); /**custom melee attack goal continues attacking even when line of sight is broken*/
        this.goalSelector.a(5, new PathfinderGoalDrownedGoToBeach(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalSwimUp(this, 1.0D, this.world.getSeaLevel()));
        this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
        this.targetSelector.a(1, (new PathfinderGoalHurtByTarget(this, new Class[]{EntityDrowned.class})).a(EntityPigZombie.class));
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, 10, true, false, this::j)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
        this.targetSelector.a(3, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class, false));
        this.targetSelector.a(5, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, true, false, EntityTurtle.bv));
    }

    @Override
    public boolean j(@Nullable EntityLiving entityliving) {
        return true; /**always attacks even in the day*/
    }

    //todo: copy all from this point onwards to all applicable mobs
    protected int teleportToPlayer;
    protected CoordsFromHypotenuse coordsFromHypotenuse = new CoordsFromHypotenuse();

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived == 10) { /**drowned only have 14 health*/
            this.setHealth(14.0f);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(14.0);
        }

        Location thisLoc = new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ());
        if (thisLoc.getBlock().getType() == org.bukkit.Material.COBWEB) { /**non-player mobs gain Speed 11 while in a cobweb (approx original speed)*/
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, 2, 10));
        }

        if (this.world.isRainingAt(new BlockPosition(this.locX(), this.locY(), this.locZ()))) { /**chance to summon lightning within 50 blocks of it every tick, increased chance if raining and in 40 block radius*/
            if (random.nextDouble() < 0.0003) {
                double hypo = random.nextDouble() * 40;
                BlockPosition pos = new BlockPosition(coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(new BlockPosition(this.locX(), this.locY(), this.locZ()),  hypo, this.locY(), 361.0));

                CustomEntityLightning lightning = new CustomEntityLightning(this.getWorld());
                lightning.setLocation(pos.getX(), this.getWorld().getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, pos).getY(), pos.getZ(), 0.0f, 0.0f);
                this.world.addEntity(lightning);
            }
        } else {
            if (random.nextDouble() < 0.000025) {
                double hypo = random.nextDouble() * 50;
                BlockPosition pos = new BlockPosition(coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(new BlockPosition(this.locX(), this.locY(), this.locZ()),  hypo, this.locY(), 361.0));

                CustomEntityLightning lightning = new CustomEntityLightning(this.getWorld());
                lightning.setLocation(pos.getX(), pos.getY(), pos.getZ(), 0.0f, 0.0f);
                this.world.addEntity(lightning);
            }
        }
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityhuman = this.world.findNearbyPlayer(this, -1.0D);

            if (entityhuman != null) {
                double d0 = Math.pow(entityhuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityhuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); //mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleepingdouble d0 = entityhuman.h(this);
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (d0 > (double) j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 8; /**random despawn distance increased to 40 blocks*/
                int l = k * k;

                if (this.ticksFarFromPlayer > 600 && this.random.nextInt(800) == 0 && d0 > (double) l && this.isTypeNotPersistent(d0)) {
                    this.die();
                } else if (d0 < (double) l) {
                    this.ticksFarFromPlayer = 0;
                }
            }

        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    @Override
    public double g(double d0, double d1, double d2) {
        double d3 = this.locX() - d0; /**for determining distance to entities, y-level does not matter, eg. mob follow range*/
        double d5 = this.locZ() - d2;

        return d3 * d3 + d5 * d5;
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /**for determining distance to entities, y-level does not matter, eg. mob follow range*/
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
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
            return super.a() && !this.drowned.world.isDay() && this.drowned.isInWater() && this.drowned.locY() >= (double) (this.drowned.world.getSeaLevel() - 3);
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
            this.world = drowned.world;
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
                    return Vec3D.c((BaseBlockPosition) blockposition1);
                }
            }

            return null;
        }
    }

    protected static Method eX;
    protected static Field d;

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
            return !this.drowned.world.isDay() && this.drowned.isInWater() && this.drowned.locY() < (double) (this.c - 2);
        }

        @Override
        public boolean b() {
            return this.a() && !this.d;
        }

        @Override
        public void e() {
            if (this.drowned.locY() < (double) (this.c - 1) && (this.drowned.getNavigation().m() || this.drowned.eP())) {
                Vec3D vec3d = RandomPositionGenerator.b(this.drowned, 4, 8, new Vec3D(this.drowned.locX(), (double) (this.c - 1), this.drowned.locZ()));

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

        private final EntityDrowned drowned;

        public CustomPathfinderGoalDrownedTridentAttack(IRangedEntity irangedentity, double d0, int i, float f) {
            super(irangedentity, d0, i, f);
            this.drowned = (EntityDrowned) irangedentity;
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
    }
}
