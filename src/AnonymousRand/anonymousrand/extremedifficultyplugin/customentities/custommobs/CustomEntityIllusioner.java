package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import com.google.common.collect.Lists;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.*;

public class CustomEntityIllusioner extends EntityIllagerIllusioner implements ICustomMob, IAttackLevelingMob {

    public ArrayList<CustomEntityIllusionerFake> fakeIllusioners = new ArrayList<>();
    private int attacks;
    private boolean a40;

    public CustomEntityIllusioner(World world) {
        super(EntityTypes.ILLUSIONER, world);
        this.a(PathType.LAVA, 0.0F); /** no longer avoids lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F); /** no longer avoids fire */
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.BOW)); // makes sure that it has a bow
        this.attacks = 0;
        this.a40 = false;
        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(40.0); /** illusioners have 40 health */
        this.setHealth(40.0F);
        this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2)); /** illusioners and fake illusioners have regen 3 */
    }

    @Override
    protected void initPathfinder() {
        this.goalSelector.a(1, new EntityRaider.b<>(this));
        this.goalSelector.a(3, new PathfinderGoalRaid<>(this));
        this.goalSelector.a(5, new CustomEntityIllusioner.c(this));
        this.goalSelector.a(4, new CustomEntityIllusioner.d(this, 1.0499999523162842D, 1));

        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlockLookingAt(this)); /** custom goal that allows the mob to break the block it is looking at every 4 seconds as long as it has a target, it breaks the block that it is looking at up to 40 blocks away */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /** custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /** custom goal that allows this mob to take certain buffs from bats etc. */
        this.goalSelector.a(1, new EntityIllagerWizard.b());
        this.goalSelector.a(4, new PathfinderGoalIllusionerDuplicationSpell());
        this.goalSelector.a(5, new PathfinderGoalIllusionerBlindnessSpell());
        this.goalSelector.a(6, new CustomPathfinderGoalBowShoot<>(this, 0.5D, 25, 24.0F)); /** illusioners attack every 25 ticks instead of 20; uses the custom goal that attacks regardless of the y level (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal) */
        this.goalSelector.a(8, new PathfinderGoalRandomStroll(this, 0.6D));
        this.goalSelector.a(9, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 3.0F, 1.0F));
        this.goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));
        this.targetSelector.a(1, (new CustomPathfinderGoalHurtByTarget(this, new Class[0])));
        this.targetSelector.a(2, (new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, false)).a(300)); /** uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
        this.targetSelector.a(3, (new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class, false)).a(300));
    }

    @Override
    public void a(EntityLiving entityLiving, float f) {
        this.attacks++;

        ItemStack itemstack = this.f(this.b(ProjectileHelper.a(this, Items.BOW)));
        EntityArrow entityArrow = ProjectileHelper.a(this, itemstack, f);
        double d0 = entityLiving.locX() - this.locX();
        double d1 = entityLiving.e(0.3333333333333333D) - entityArrow.locY();
        double d2 = entityLiving.locZ() - this.locZ();
        double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);

        entityArrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, 0.0F); /** arrows have no inaccuracy */
        this.playSound(SoundEffects.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.world.addEntity(entityArrow);
    }

    public double getFollowRange() { /** illusioners have 32 block detection range (setting attribute doesn't work) */
        return 32.0;
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

        if (this.attacks == 40 && !this.a40) { /** after 40 attacks, illusioners get regen 4, and 50 max health and health */
            this.a40 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 3));
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(50.0);
            this.setHealth(50.0F);
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

    class PathfinderGoalIllusionerBlindnessSpell extends EntityIllagerWizard.c {

        private PathfinderGoalIllusionerBlindnessSpell() {
            super();
        }

        @Override
        public boolean a() { /** can now cast blindness multiple times */
            return super.a() && CustomEntityIllusioner.this.getGoalTarget() != null;
        }

        @Override
        public void c() {
            super.c();
        }

        @Override
        protected int g() {
            return 40;
        }

        @Override
        protected int h() { /** delay between each blindness spell increased to 15 seconds */
            return 300;
        }

        @Override
        protected void j() { /** only applies blindness for 5 seconds (8 seconds after 40 attacks) */
            CustomEntityIllusioner.this.getGoalTarget().addEffect(new MobEffect(MobEffects.BLINDNESS, CustomEntityIllusioner.this.attacks < 40 ? 100 : 160));
        }

        @Override
        protected SoundEffect k() {
            return SoundEffects.ENTITY_ILLUSIONER_PREPARE_BLINDNESS;
        }

        @Override
        protected EntityIllagerWizard.Spell l() {
            return EntityIllagerWizard.Spell.BLINDNESS;
        }
    }

    class PathfinderGoalIllusionerDuplicationSpell extends EntityIllagerWizard.c {

        private PathfinderGoalIllusionerDuplicationSpell() {
            super();
        }

        @Override
        public boolean a() {
            return !super.a() ? false : !CustomEntityIllusioner.this.hasEffect(MobEffects.INVISIBILITY);
        }

        @Override
        public void e() {
            super.e();
        }

        @Override
        protected int g() {
            return 20;
        }

        @Override
        protected int h() {
            return 0; /** can start casting duplicates spell immediately */
        }

        @Override
        protected void j() {
            CustomEntityIllusioner.this.addEffect(new MobEffect(MobEffects.INVISIBILITY, 1200));


            for (CustomEntityIllusionerFake fakeIllusioner : CustomEntityIllusioner.this.fakeIllusioners) { // reset the fake illusioners
                fakeIllusioner.setHealth(0.0F);
            }

            CustomEntityIllusioner.this.fakeIllusioners.clear();
            CustomEntityIllusionerFake fakeIllusioner;

            for (int i = 0; i < (CustomEntityIllusioner.this.attacks < 12 ? 4 : 5); i++) { /** summons 4 additional fake illusioners that actually shoot arrows as well with varying speeds (5 after 12 attacks) */
                fakeIllusioner = new CustomEntityIllusionerFake(CustomEntityIllusioner.this.getWorld(), CustomEntityIllusioner.this);
                fakeIllusioner.setPosition(CustomEntityIllusioner.this.locX(), CustomEntityIllusioner.this.locY(), CustomEntityIllusioner.this.locZ());
                CustomEntityIllusioner.this.getWorld().addEntity(fakeIllusioner);
                CustomEntityIllusioner.this.fakeIllusioners.add(fakeIllusioner);
            }
        }

        @Nullable
        @Override
        protected SoundEffect k() {
            return SoundEffects.ENTITY_ILLUSIONER_PREPARE_MIRROR;
        }

        @Override
        protected EntityIllagerWizard.Spell l() {
            return EntityIllagerWizard.Spell.DISAPPEAR;
        }
    }

    public class c extends PathfinderGoal { // from EntityRaider.java

        private final CustomEntityIllusioner illusioner;

        public c(CustomEntityIllusioner entityRaider) {
            this.illusioner = entityRaider;
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE));
        }

        @Override
        public boolean a() {
            Raid raid = this.illusioner.fb();

            return this.illusioner.isAlive() && this.illusioner.getGoalTarget() == null && raid != null && raid.isLoss();
        }

        @Override
        public void c() {
            this.illusioner.x(true);
            super.c();
        }

        @Override
        public void d() {
            this.illusioner.x(false);
            super.d();
        }

        @Override
        public void e() {
            if (!this.illusioner.isSilent() && this.illusioner.getRandom().nextInt(100) == 0) {
                CustomEntityIllusioner.this.playSound(CustomEntityIllusioner.this.eM(), CustomEntityIllusioner.this.getSoundVolume(), CustomEntityIllusioner.this.dG());
            }

            if (!this.illusioner.isPassenger() && this.illusioner.random.nextInt(50) == 0) {
                this.illusioner.getControllerJump().jump();
            }

            super.e();
        }
    }

    static class d extends PathfinderGoal { // from EntityRaider.java

        private final EntityRaider entity;
        private final double b;
        private BlockPosition c;
        private final List<BlockPosition> d = Lists.newArrayList();
        private final int e;
        private boolean f;

        public d(EntityRaider entityRaider, double d0, int i) {
            this.entity = entityRaider;
            this.b = d0;
            this.e = i;
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE));
        }

        @Override
        public boolean a() {
            this.j();
            return this.g() && this.h() && this.entity.getGoalTarget() == null;
        }

        private boolean g() {
            return this.entity.fc() && !this.entity.fb().a();
        }

        private boolean h() {
            WorldServer worldserver = (WorldServer) this.entity.world;
            BlockPosition blockPosition = this.entity.getChunkCoordinates();
            Optional<BlockPosition> optional = worldserver.x().a((villageplacetype) -> villageplacetype == VillagePlaceType.r, this::a, VillagePlace.Occupancy.ANY, blockPosition, 48, this.entity.getRandom());

            if (!optional.isPresent()) {
                return false;
            } else {
                this.c = (optional.get()).immutableCopy();
                return true;
            }
        }

        @Override
        public boolean b() {
            return this.entity.getNavigation().m() ? false : this.entity.getGoalTarget() == null && !this.c.a(this.entity.getPositionVector(), (double)(this.entity.getWidth() + (float)this.e)) && !this.f;
        }

        @Override
        public void d() {
            if (this.c.a(this.entity.getPositionVector(), this.e)) {
                this.d.add(this.c);
            }

        }

        @Override
        public void c() {
            super.c();
            this.entity.n(0);
            this.entity.getNavigation().a(this.c.getX(), this.c.getY(), this.c.getZ(), this.b);
            this.f = false;
        }

        @Override
        public void e() {
            if (this.entity.getNavigation().m()) {
                Vec3D vec3d = Vec3D.c(this.c);
                Vec3D vec3d1 = RandomPositionGenerator.a(this.entity, 16, 7, vec3d, 0.3141592741012573D);

                if (vec3d1 == null) {
                    vec3d1 = RandomPositionGenerator.b(this.entity, 8, 7, vec3d);
                }

                if (vec3d1 == null) {
                    this.f = true;
                    return;
                }

                this.entity.getNavigation().a(vec3d1.x, vec3d1.y, vec3d1.z, this.b);
            }

        }

        private boolean a(BlockPosition blockPosition) {
            Iterator iterator = this.d.iterator();

            BlockPosition blockPosition1;

            do {
                if (!iterator.hasNext()) {
                    return true;
                }

                blockPosition1 = (BlockPosition) iterator.next();
            } while (!Objects.equals(blockPosition, blockPosition1));

            return false;
        }

        private void j() {
            if (this.d.size() > 2) {
                this.d.remove(0);
            }

        }
    }
}
