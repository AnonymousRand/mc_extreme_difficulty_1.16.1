package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.AttackLevelingController;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.EntityFilter;
import AnonymousRand.anonymousrand.extremedifficultyplugin.listeners.ListenerMobSpawnAndReplaceWithCustom;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityTargetEvent;

import java.lang.reflect.Field;
import java.util.EnumSet;

public class CustomEntityPhantom extends EntityPhantom implements ICustomHostile, IAttackLevelingMob {

    /* Ignores line of sight and y-level for initially finding a player target and maintaining it as the target,
     * as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;

    private AttackLevelingController attackLevelingController = null;
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
        this.initCustom();
        this.initAttackLevelingMob();
    }

    private void initCustom() {
        try {
            this.orbitPosition = EntityPhantom.class.getDeclaredField("d");
            this.orbitPosition.setAccessible(true);
            this.orbitOffset = EntityPhantom.class.getDeclaredField("c");
            this.orbitOffset.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);

        ListenerMobSpawnAndReplaceWithCustom.phantomSize += 0.07 / Math.max(Bukkit.getServer().getOnlinePlayers().size(), 1.0); /* every custom phantom spawned per player increases the server-wide size of future phantom spawns by 0.07 */
        this.setSize(0);
        this.attackPhase = CustomEntityPhantom.AttackPhase.CIRCLE;
        this.noclip = true; /* phantoms can fly through blocks */
        this.deathExplosion = false;
        this.duplicate = false;

        /* No longer despawns or takes up the mob cab */
        this.getBukkitEntity().setCustomName("Do you regret voting for me");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // ICustomHostile
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public double getDetectionRange() { /* phantoms have 64 block detection range */
        return 64.0;
    }

    public boolean ignoresLOS() {
        return IGNORE_LOS;
    }

    public boolean ignoresY() {
        return IGNORE_Y;
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.world.findNearbyPlayer(this, -1.0);

            if (nearestPlayer != null) {
                /* Mobs only despawn along horizontal axes, so even at build height,
                   mobs will spawn below you and prevent sleeping */
                double distSqToNearestPlayer =
                        Math.pow(nearestPlayer.getPositionVector().getX() - this.getPositionVector().getX(), 2)
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

                if (this.ticksFarFromPlayer > 600
                        && random.nextInt(800) == 0
                        && distSqToNearestPlayer > (double) randomDespawnDistSq
                        && this.isTypeNotPersistent(distSqToNearestPlayer)) {
                    this.die();
                } else if (distSqToNearestPlayer < (double) randomDespawnDistSq) {
                    this.ticksFarFromPlayer = 0;
                }
            }
        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // IAttackLevelingMob
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void initAttackLevelingMob() {
        this.attackLevelingController = new AttackLevelingController(30);
    }

    public int getAttacks() {
        return this.attackLevelingController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        int[] thresholdsMet = this.attackLevelingController.increaseAttacks(increase);

        for (int thresholdMet : thresholdsMet) {
            int[] attackThresholds = this.getAttacksThresholds();
            if (thresholdMet == attackThresholds[0]) {
                /* After 30 attacks, phantoms get regen 3 */
                this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));
            }
        }
    }

    public int[] getAttacksThresholds() {
        return this.attackLevelingController.getAttacksThresholds();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Overridden vanilla functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void initPathfinder() {
        this.goalSelector.a(1, new CustomEntityPhantom.PathfinderGoalPhantomPickAttack());
        this.goalSelector.a(2, new PathfinderGoalPhantomSweepAttack());
        this.goalSelector.a(3, new CustomEntityPhantom.PathfinderGoalPhantomOrbitPoint());
        // todo make sure using normal nearestattackabletarget is fine
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class));
    }

    private void updateSizeStats(int change) { /* phantoms gain +0.3 health and 0.125 damage per size and starts with 11 health and 2 damage at size 0 */
        this.updateSize();
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(2.0 + 0.125 * this.getSize());
        double maxHealth = 11.0 + 0.3 * this.getSize();
        double health = this.getHealth() + 0.3 * change;

        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(maxHealth);
        this.setHealth((float) health);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived % (this.getAttacks() < 10 ? 170 : this.getAttacks() < 20 ? 150 : this.getAttacks() < 30 ? 120 : 100) == 0 && this.ticksLived != 0 && this.getHealth() > 0.0) { /* phantoms increase in size by 1 every 8.5 seconds (7.5 seconds after 10 attacks, 6 seconds after 20 attacks, 5 seconds after 30 attacks) */
            this.setSize(this.getSize() + 1);
            this.updateSizeStats(1);
        }

        if (this.getHealth() <= 0.0 && this.getAttacks() >= 15 && !this.deathExplosion) { /* after 15 attacks, phantoms explode when killed */
            this.deathExplosion = true;
            this.world.createExplosion(this, this.locX(), this.locY(), this.locZ(), (float) Math.ceil(this.getSize() / 32.0), false, Explosion.Effect.DESTROY);
        }

        if (this.ticksLived == 5) {
            this.updateSizeStats(this.getSize());
        }
    }

    @Override
    public void die() {
        super.die();

        if (this.getAttacks() >= 40 || this.duplicate) {
            /* after 40 attacks, phantoms split into 2 phantoms each with half its size when killed, all the way down to size 4 */
            if (this.getSize() > 7) {
                new SpawnEntity(this.world, (int) this.getSize() / 2, true, new CustomEntityPhantom(this.world, this.getSize() / 2, true), 2, null, null, this, false, false);
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
            EntityLiving goalTarget = CustomEntityPhantom.this.getGoalTarget();
            return goalTarget != null;
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
                CustomEntityPhantom.this.orbitPosition.set(CustomEntityPhantom.this, CustomEntityPhantom.this.getWorld().getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, (BlockPosition)CustomEntityPhantom.this.orbitPosition.get(CustomEntityPhantom.this)).up(10 + CustomEntityPhantom.this.random.nextInt(20)));
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

                    if (orbitPos.getY() < CustomEntityPhantom.this.getWorld().getSeaLevel()) {
                        CustomEntityPhantom.this.orbitPosition.set(CustomEntityPhantom.this, new BlockPosition(orbitPos.getX(), CustomEntityPhantom.this.getWorld().getSeaLevel() + 1, orbitPos.getZ()));
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
            EntityLiving goalTarget = CustomEntityPhantom.this.getGoalTarget();

            if (!EntityFilter.BASE.test(goalTarget)) {
                return false;
            }

            if (!this.a()) {
                return false;
            } else { /* phantoms are no longer scared of cats and ocelots */
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
            EntityLiving goalTarget = CustomEntityPhantom.this.getGoalTarget();

            try {
                CustomEntityPhantom.this.orbitOffset.set(CustomEntityPhantom.this, new Vec3D(goalTarget.locX(), goalTarget.e(0.5D), goalTarget.locZ()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            if (CustomEntityPhantom.this.getBoundingBox().g(0.20000000298023224D).c(goalTarget.getBoundingBox())) {
                CustomEntityPhantom.this.attackEntity(goalTarget);
                CustomEntityPhantom.this.attackPhase = CustomEntityPhantom.AttackPhase.CIRCLE;
                if (!CustomEntityPhantom.this.isSilent()) {
                    CustomEntityPhantom.this.getWorld().triggerEffect(1039, CustomEntityPhantom.this.getChunkCoordinates(), 0);
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

                if (orbitOff.y < CustomEntityPhantom.this.locY() && !CustomEntityPhantom.this.getWorld().isEmpty(CustomEntityPhantom.this.getChunkCoordinates().down(1))) {
                    this.e = Math.max(1.0F, this.e);
                    this.h();
                }

                if (orbitOff.y > CustomEntityPhantom.this.locY() && !CustomEntityPhantom.this.getWorld().isEmpty(CustomEntityPhantom.this.getChunkCoordinates().up(1))) {
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
