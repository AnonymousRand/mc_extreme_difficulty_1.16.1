package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderTargetCondition;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.*;

public class CustomEntityPhantom extends EntityPhantom implements ICommonCustomMethods {

    public int attacks;
    private boolean a30, deathExplosion, duplicate;
    private CustomEntityPhantom.AttackPhase attackPhase;
    private static Field orbitPosition, orbitOffset;
    private static final Random random = new Random();

    public CustomEntityPhantom(World world, int size, boolean duplicate) {
        this(world);
        this.setSize(size);
        this.duplicate = duplicate;
    }

    public CustomEntityPhantom(World world, int size) {
        this(world);
        this.setSize(size);
    }

    public CustomEntityPhantom(World world) {
        super(EntityTypes.PHANTOM, world);
        this.a(PathType.LAVA, 0.0F); /**no longer avoids lava*/
        this.a(PathType.DAMAGE_FIRE, 0.0F); /**no longer avoids fire*/
        this.setSize(0);
        this.attackPhase = CustomEntityPhantom.AttackPhase.CIRCLE;
        this.noclip = true; /**phantoms can fly through blocks*/
        this.attacks = 0;
        this.a30 = false;
        this.deathExplosion = false;
        this.duplicate = false;
        this.getBukkitEntity().setCustomName("You voted for me");
    }

    static {
        try {
            orbitPosition = EntityPhantom.class.getDeclaredField("d");
            orbitPosition.setAccessible(true);
            orbitOffset = EntityPhantom.class.getDeclaredField("c");
            orbitOffset.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initPathfinder() {
        this.goalSelector.a(1, new CustomEntityPhantom.PathfinderGoalPhantomPickAttack());
        this.goalSelector.a(2, new CustomEntityPhantom.CustomPathfinderGoalPhantomSweepAttack());
        this.goalSelector.a(3, new CustomEntityPhantom.PathfinderGoalPhantomOrbitPoint());
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false));
    }

    private void updateSizeStats(int change) { /**phantoms gain +0.8 health and 0.06 damage per size and starts with 8 health and 1 damage at size 0*/
        this.updateSize();
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(1.0 + 0.06 * this.getSize());
        double maxHealth = 8.0 + 0.8 * this.getSize();
        double health = this.getHealth() + 0.8 * change;

        this.setHealth((float)health);
        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(maxHealth);
        this.setHealth((float)health);
    }

    @Override
    public void die() {
        super.die();

        if (this.attacks >= 40 || this.duplicate) { /**after 40 attacks, phantoms split into 2 phantoms each with half its size when killed, up to size 4*/
            if (this.getSize() > 7) {
                new SpawnLivingEntity(this.getWorld(), this.getSize() / 2, true, new CustomEntityPhantom(this.getWorld(), this.getSize() / 2, true), 2, null, null, this, false, false);
            }
        }
    }

    public double getFollowRange() { /**phantoms have 64 block detection range (setting attribute doesn't work)*/
        return 64.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived % (this.attacks < 5 ? 240 : this.attacks < 20 ? 180 : this.attacks < 30 ? 150 : 110) == 0 && this.ticksLived != 0) { /**phantoms increase in size by 1 every 12 seconds (9 seconds after 5 attacks, 7.5 seconds after 20 attacks, 5.5 seconds after 30 attacks)*/
            this.setSize(this.getSize() + 1);
            this.updateSizeStats(1);
        }

        if (this.getHealth() <= 0.0 && this.attacks >= 15 && !this.deathExplosion) { /**after 15 attacks, phantoms explode when killed*/
            this.deathExplosion = true;
            this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), (float)Math.ceil(this.getSize() / 32.0), false, Explosion.Effect.DESTROY);
        }

        if (this.attacks == 30 && !this.a30) { /**after 30 attacks, phantoms get regen 3*/
            this.a30 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));
        }

        if (this.ticksLived == 10) {
            this.updateSizeStats(this.getSize());
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

    static enum AttackPhase {

        CIRCLE, SWOOP;

        private AttackPhase() {}
    }

    abstract class PathfinderGoalMove extends PathfinderGoal {

        public PathfinderGoalMove() {
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE));
        }

        protected boolean g() {
            try {
                return ((Vec3D)CustomEntityPhantom.orbitOffset.get(CustomEntityPhantom.this)).c(CustomEntityPhantom.this.locX(), CustomEntityPhantom.this.locY(), CustomEntityPhantom.this.locZ()) < 4.0D;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    class PathfinderGoalPhantomPickAttack extends PathfinderGoal {

        private int b;

        private PathfinderGoalPhantomPickAttack() {}

        @Override
        public boolean a() {
            EntityLiving entityliving = CustomEntityPhantom.this.getGoalTarget();

            return entityliving != null ? CustomEntityPhantom.this.a(CustomEntityPhantom.this.getGoalTarget(), CustomPathfinderTargetCondition.a) : false;
        }

        @Override
        public void c() {
            this.b = 10;
            CustomEntityPhantom.this.attackPhase = CustomEntityPhantom.AttackPhase.CIRCLE;
            this.g();
        }

        @Override
        public void d() {
            try {
                CustomEntityPhantom.orbitPosition.set(CustomEntityPhantom.this, CustomEntityPhantom.this.world.getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, (BlockPosition)CustomEntityPhantom.orbitPosition.get(CustomEntityPhantom.this)).up(10 + CustomEntityPhantom.random.nextInt(20)));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void e() {
            if (CustomEntityPhantom.this.attackPhase == CustomEntityPhantom.AttackPhase.CIRCLE) {
                --this.b;
                if (this.b <= 0) {
                    CustomEntityPhantom.this.attackPhase = CustomEntityPhantom.AttackPhase.SWOOP;
                    this.g();
                    this.b = (8 + CustomEntityPhantom.random.nextInt(4)) * 20;
                    CustomEntityPhantom.this.playSound(SoundEffects.ENTITY_PHANTOM_SWOOP, 10.0F, 0.95F + CustomEntityPhantom.random.nextFloat() * 0.1F);
                }
            }
        }

        private void g() {
            if (CustomEntityPhantom.this.getGoalTarget() != null) {
                try {
                    CustomEntityPhantom.orbitPosition.set(CustomEntityPhantom.this, CustomEntityPhantom.this.getGoalTarget().getChunkCoordinates().up(20 + CustomEntityPhantom.random.nextInt(20)));
                    BlockPosition orbitPos = ((BlockPosition)CustomEntityPhantom.orbitPosition.get(CustomEntityPhantom.this));

                    if (orbitPos.getY() < CustomEntityPhantom.this.world.getSeaLevel()) {
                        CustomEntityPhantom.orbitPosition.set(CustomEntityPhantom.this, new BlockPosition(orbitPos.getX(), CustomEntityPhantom.this.world.getSeaLevel() + 1, orbitPos.getZ()));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CustomPathfinderGoalPhantomSweepAttack extends PathfinderGoalMove {

        private CustomPathfinderGoalPhantomSweepAttack() {
            super();
        }

        @Override
        public boolean a() {
            return CustomEntityPhantom.this.getGoalTarget() != null && CustomEntityPhantom.this.attackPhase == CustomEntityPhantom.AttackPhase.SWOOP;
        }

        @Override
        public boolean b() {
            EntityLiving entityliving = CustomEntityPhantom.this.getGoalTarget();

            if (entityliving == null) {
                return false;
            } else if (!entityliving.isAlive()) {
                return false;
            } else if (entityliving instanceof EntityHuman && (((EntityHuman) entityliving).isSpectator() || ((EntityHuman) entityliving).isCreative())) {
                return false;
            } else if (!this.a()) {
                return false;
            } else { /**phantoms are no longer scared of cats and ocelots*/
                return true;
            }
        }

        @Override
        public void c() {}

        @Override
        public void d() {
            CustomEntityPhantom.this.setGoalTarget((EntityLiving)null);
            CustomEntityPhantom.this.attackPhase = CustomEntityPhantom.AttackPhase.CIRCLE;
        }

        @Override
        public void e() {
            EntityLiving entityliving = CustomEntityPhantom.this.getGoalTarget();

            try {
                CustomEntityPhantom.orbitOffset.set(CustomEntityPhantom.this, new Vec3D(entityliving.locX(), entityliving.e(0.5D), entityliving.locZ()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            if (CustomEntityPhantom.this.getBoundingBox().g(0.20000000298023224D).c(entityliving.getBoundingBox())) {
                CustomEntityPhantom.this.attackEntity(entityliving);
                CustomEntityPhantom.this.attackPhase = CustomEntityPhantom.AttackPhase.CIRCLE;
                if (!CustomEntityPhantom.this.isSilent()) {
                    CustomEntityPhantom.this.world.triggerEffect(1039, CustomEntityPhantom.this.getChunkCoordinates(), 0);
                }
            } else if (CustomEntityPhantom.this.positionChanged || CustomEntityPhantom.this.hurtTicks > 0) {
                CustomEntityPhantom.this.attackPhase = CustomEntityPhantom.AttackPhase.CIRCLE;
            }
        }
    }

    class PathfinderGoalPhantomOrbitPoint extends CustomEntityPhantom.PathfinderGoalMove {

        private float c;
        private float d;
        private float e;
        private float f;

        private PathfinderGoalPhantomOrbitPoint() {
            super();
        }

        @Override
        public boolean a() {
            return CustomEntityPhantom.this.getGoalTarget() == null || CustomEntityPhantom.this.attackPhase == CustomEntityPhantom.AttackPhase.CIRCLE;
        }

        @Override
        public void c() {
            this.d = 5.0F + CustomEntityPhantom.random.nextFloat() * 10.0F;
            this.e = -4.0F + CustomEntityPhantom.random.nextFloat() * 9.0F;
            this.f = CustomEntityPhantom.random.nextBoolean() ? 1.0F : -1.0F;
            this.h();
        }

        @Override
        public void e() {
            if (CustomEntityPhantom.random.nextInt(350) == 0) {
                this.e = -4.0F + CustomEntityPhantom.random.nextFloat() * 9.0F;
            }

            if (CustomEntityPhantom.random.nextInt(250) == 0) {
                ++this.d;
                if (this.d > 15.0F) {
                    this.d = 5.0F;
                    this.f = -this.f;
                }
            }

            if (CustomEntityPhantom.random.nextInt(450) == 0) {
                this.c = CustomEntityPhantom.random.nextFloat() * 2.0F * 3.1415927F;
                this.h();
            }

            if (this.g()) {
                this.h();
            }

            try {
                Vec3D orbitOff = ((Vec3D)CustomEntityPhantom.orbitOffset.get(CustomEntityPhantom.this));

                if (orbitOff.y < CustomEntityPhantom.this.locY() && !CustomEntityPhantom.this.world.isEmpty(CustomEntityPhantom.this.getChunkCoordinates().down(1))) {
                    this.e = Math.max(1.0F, this.e);
                    this.h();
                }

                if (orbitOff.y > CustomEntityPhantom.this.locY() && !CustomEntityPhantom.this.world.isEmpty(CustomEntityPhantom.this.getChunkCoordinates().up(1))) {
                    this.e = Math.min(-1.0F, this.e);
                    this.h();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        private void h() {
            try {
                BlockPosition orbitPos = (BlockPosition)CustomEntityPhantom.orbitPosition.get(CustomEntityPhantom.this);
                if (BlockPosition.ZERO.equals(orbitPos)) {
                    CustomEntityPhantom.orbitPosition.set(CustomEntityPhantom.this, CustomEntityPhantom.this.getChunkCoordinates());
                }

                this.c += this.f * 15.0F * 0.017453292F;
                CustomEntityPhantom.orbitOffset.set(CustomEntityPhantom.this, Vec3D.b((BaseBlockPosition)orbitPos).add((double) (this.d * MathHelper.cos(this.c)), (double) (-4.0F + this.e), (double) (this.d * MathHelper.sin(this.c))));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}