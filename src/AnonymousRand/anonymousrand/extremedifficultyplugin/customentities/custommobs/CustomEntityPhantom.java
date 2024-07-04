package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.AttackController;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderTargetCondition;
import AnonymousRand.anonymousrand.extremedifficultyplugin.listeners.ListenerMobSpawnAndReplaceWithCustom;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityTargetEvent;

import java.lang.reflect.Field;
import java.util.EnumSet;

public class CustomEntityPhantom extends EntityPhantom implements ICustomHostile, IAttackLevelingMob {

    private AttackController attackController;
    private boolean deathExplosion, duplicate;
    private CustomEntityPhantom.AttackPhase attackPhase;
    private Field orbitPosition, orbitOffset;

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
        this.initCustomHostile();
        this.initAttackLevelingMob();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                      ICustomHostile                                       //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public void initCustomHostile() {
        try {
            this.orbitPosition = EntityPhantom.class.getDeclaredField("d");
            this.orbitPosition.setAccessible(true);
            this.orbitOffset = EntityPhantom.class.getDeclaredField("c");
            this.orbitOffset.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        /** No longer avoids lava */
        this.a(PathType.LAVA, 0.0F);
        /** No longer avoids fire */
        this.a(PathType.DAMAGE_FIRE, 0.0F);

        ListenerMobSpawnAndReplaceWithCustom.phantomSize += 0.07 / Math.max(Bukkit.getServer().getOnlinePlayers().size(), 1.0); /** every custom phantom spawned per player increases the server-wide size of future phantom spawns by 0.07 */
        this.setSize(0);
        this.attackPhase = CustomEntityPhantom.AttackPhase.CIRCLE;
        this.noclip = true; /** phantoms can fly through blocks */
        this.deathExplosion = false;
        this.duplicate = false;

        /** No longer despawns or takes up the mob cab */
        this.getBukkitEntity().setCustomName("Do you regret voting for me");
    }

    public double getFollowRange() { /** phantoms have 64 block detection range (setting attribute doesn't work) */
        return 64.0;
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityHuman = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (entityHuman != null) {
                /** Mobs only despawn along horizontal axes, so if you are at y=256, mobs will still spawn below you and prevent sleeping */
                double distToNearestPlayer = Math.pow(entityHuman.getPositionVector().getX() - this.getPositionVector().getX(), 2)
                        + Math.pow(entityHuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2);
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (distToNearestPlayer > (double)j && this.isTypeNotPersistent(distToNearestPlayer)) {
                    this.die();
                }

                /** Random despawn distance increased to 40 blocks */
                int k = this.getEntityType().e().g() + 8;
                int l = k * k;

                if (this.ticksFarFromPlayer > 600 && this.random.nextInt(800) == 0 && d0 > (double)l && this.isTypeNotPersistent(d0)) {
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

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                    IAttackLevelingMob                                     //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void initAttackLevelingMob() {
        this.attackController = new AttackController(30);
    }

    public int getAttacks() {
        return this.attackController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        for (int metThreshold : this.attackController.increaseAttacks(increase)) {
            int[] attackThresholds = this.attackController.getAttacksThresholds();
            if (metThreshold == attackThresholds[0]) {
                /** After 30 attacks, phantoms get regen 3 */
                this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                               Overridden vanilla functions                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void initPathfinder() {
        this.goalSelector.a(1, new CustomEntityPhantom.PathfinderGoalPhantomPickAttack());
        this.goalSelector.a(2, new PathfinderGoalPhantomSweepAttack());
        this.goalSelector.a(3, new CustomEntityPhantom.PathfinderGoalPhantomOrbitPoint());
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class));
    }

    private void updateSizeStats(int change) { /** phantoms gain +0.3 health and 0.125 damage per size and starts with 11 health and 2 damage at size 0 */
        this.updateSize();
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(2.0 + 0.125 * this.getSize());
        double maxHealth = 11.0 + 0.3 * this.getSize();
        double health = this.getHealth() + 0.3 * change;

        this.setHealth((float)health);
        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(maxHealth);
        this.setHealth((float)health);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived % (this.getAttacks() < 10 ? 170 : this.getAttacks() < 20 ? 150 : this.getAttacks() < 30 ? 120 : 100) == 0 && this.ticksLived != 0 && this.getHealth() > 0.0) { /** phantoms increase in size by 1 every 8.5 seconds (7.5 seconds after 10 attacks, 6 seconds after 20 attacks, 5 seconds after 30 attacks) */
            this.setSize(this.getSize() + 1);
            this.updateSizeStats(1);
        }

        if (this.getHealth() <= 0.0 && this.getAttacks() >= 15 && !this.deathExplosion) { /** after 15 attacks, phantoms explode when killed */
            this.deathExplosion = true;
            this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), (float)Math.ceil(this.getSize() / 32.0), false, Explosion.Effect.DESTROY);
        }

        if (this.ticksLived == 5) {
            this.updateSizeStats(this.getSize());
        }
    }

    @Override
    public void die() {
        super.die();

        if (this.getAttacks() >= 40 || this.duplicate) {
            /** after 40 attacks, phantoms split into 2 phantoms each with half its size when killed, all the way down to size 4 */
            if (this.getSize() > 7) {
                new SpawnEntity(this.getWorld(), (int) this.getSize() / 2, true, new CustomEntityPhantom(this.getWorld(), this.getSize() / 2, true), 2, null, null, this, false, false);
            }
        }
    }

    enum AttackPhase {

        CIRCLE, SWOOP;

        AttackPhase() {}
    }

    abstract class PathfinderGoalMove extends PathfinderGoal {

        public PathfinderGoalMove() {
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE));
        }

        protected boolean g() {
            try {
                return ((Vec3D)CustomEntityPhantom.this.orbitOffset.get(CustomEntityPhantom.this)).c(CustomEntityPhantom.this.locX(), CustomEntityPhantom.this.locY(), CustomEntityPhantom.this.locZ()) < 4.0D;
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
            EntityLiving entityLiving = CustomEntityPhantom.this.getGoalTarget();

            return entityLiving != null && CustomEntityPhantom.this.a(CustomEntityPhantom.this.getGoalTarget(), CustomPathfinderTargetCondition.a);
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
                CustomEntityPhantom.this.orbitPosition.set(CustomEntityPhantom.this, CustomEntityPhantom.this.world.getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, (BlockPosition)CustomEntityPhantom.this.orbitPosition.get(CustomEntityPhantom.this)).up(10 + CustomEntityPhantom.this.random.nextInt(20)));
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
                    this.b = (8 + CustomEntityPhantom.this.random.nextInt(4)) * 20;
                    CustomEntityPhantom.this.playSound(SoundEffects.ENTITY_PHANTOM_SWOOP, 10.0F, 0.95F + CustomEntityPhantom.this.random.nextFloat() * 0.1F);
                }
            }
        }

        private void g() {
            if (CustomEntityPhantom.this.getGoalTarget() != null) {
                try {
                    CustomEntityPhantom.this.orbitPosition.set(CustomEntityPhantom.this, CustomEntityPhantom.this.getGoalTarget().getChunkCoordinates().up(20 + CustomEntityPhantom.this.random.nextInt(20)));
                    BlockPosition orbitPos = ((BlockPosition)CustomEntityPhantom.this.orbitPosition.get(CustomEntityPhantom.this));

                    if (orbitPos.getY() < CustomEntityPhantom.this.world.getSeaLevel()) {
                        CustomEntityPhantom.this.orbitPosition.set(CustomEntityPhantom.this, new BlockPosition(orbitPos.getX(), CustomEntityPhantom.this.world.getSeaLevel() + 1, orbitPos.getZ()));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class PathfinderGoalPhantomSweepAttack extends PathfinderGoalMove {

        private PathfinderGoalPhantomSweepAttack() {
            super();
        }

        @Override
        public boolean a() {
            return CustomEntityPhantom.this.getGoalTarget() != null && CustomEntityPhantom.this.attackPhase == CustomEntityPhantom.AttackPhase.SWOOP;
        }

        @Override
        public boolean b() {
            EntityLiving entityLiving = CustomEntityPhantom.this.getGoalTarget();

            if (entityLiving == null) {
                return false;
            } else if (!entityLiving.isAlive()) {
                return false;
            } else if (entityLiving instanceof EntityHuman && ((entityLiving).isSpectator() || ((EntityHuman) entityLiving).isCreative())) {
                return false;
            } else if (!this.a()) {
                return false;
            } else { /** phantoms are no longer scared of cats and ocelots */
                return true;
            }
        }

        @Override
        public void c() {}

        @Override
        public void d() {
            CustomEntityPhantom.this.setGoalTarget(null, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, false);
            CustomEntityPhantom.this.attackPhase = CustomEntityPhantom.AttackPhase.CIRCLE;
        }

        @Override
        public void e() {
            EntityLiving entityLiving = CustomEntityPhantom.this.getGoalTarget();

            try {
                CustomEntityPhantom.this.orbitOffset.set(CustomEntityPhantom.this, new Vec3D(entityLiving.locX(), entityLiving.e(0.5D), entityLiving.locZ()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            if (CustomEntityPhantom.this.getBoundingBox().g(0.20000000298023224D).c(entityLiving.getBoundingBox())) {
                CustomEntityPhantom.this.attackEntity(entityLiving);
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
            this.d = 5.0F + CustomEntityPhantom.this.random.nextFloat() * 10.0F;
            this.e = -4.0F + CustomEntityPhantom.this.random.nextFloat() * 9.0F;
            this.f = CustomEntityPhantom.this.random.nextBoolean() ? 1.0F : -1.0F;
            this.h();
        }

        @Override
        public void e() {
            if (CustomEntityPhantom.this.random.nextInt(350) == 0) {
                this.e = -4.0F + CustomEntityPhantom.this.random.nextFloat() * 9.0F;
            }

            if (CustomEntityPhantom.this.random.nextInt(250) == 0) {
                ++this.d;
                if (this.d > 15.0F) {
                    this.d = 5.0F;
                    this.f = -this.f;
                }
            }

            if (CustomEntityPhantom.this.random.nextInt(450) == 0) {
                this.c = CustomEntityPhantom.this.random.nextFloat() * 2.0F * 3.1415927F;
                this.h();
            }

            if (this.g()) {
                this.h();
            }

            try {
                Vec3D orbitOff = ((Vec3D)CustomEntityPhantom.this.orbitOffset.get(CustomEntityPhantom.this));

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
                BlockPosition orbitPos = (BlockPosition)CustomEntityPhantom.this.orbitPosition.get(CustomEntityPhantom.this);
                if (BlockPosition.ZERO.equals(orbitPos)) {
                    CustomEntityPhantom.this.orbitPosition.set(CustomEntityPhantom.this, CustomEntityPhantom.this.getChunkCoordinates());
                }

                this.c += this.f * 15.0F * 0.017453292F;
                CustomEntityPhantom.this.orbitOffset.set(CustomEntityPhantom.this, Vec3D.b(orbitPos).add((this.d * MathHelper.cos(this.c)), (-4.0F + this.e), (this.d * MathHelper.sin(this.c))));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}