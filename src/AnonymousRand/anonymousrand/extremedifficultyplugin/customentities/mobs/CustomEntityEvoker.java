package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.AttackLevelingController;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.Predicates;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import com.google.common.collect.Lists;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;

public class CustomEntityEvoker extends EntityEvoker implements ICustomHostile, IAttackLevelingMob {

    private EntitySheep wololoTarget;
    private AttackLevelingController attackLevelingController = null;

    public CustomEntityEvoker(World world) {
        super(EntityTypes.EVOKER, world);
        this.initCustom();
        this.initAttackLevelingMob();
    }

    private void initCustom() {
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                      ICustomHostile                                       //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public double getDetectionRange() { /* evokers have 28 block detection range */
        return 28.0;
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

    private void initAttackLevelingMob() {
        this.attackLevelingController = new AttackLevelingController(25, 35, 60);
    }

    public int getAttacks() {
        return this.attackLevelingController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        for (int metThreshold : this.attackLevelingController.increaseAttacks(increase)) {
            int[] attackThresholds = this.getAttacksThresholds();
            if (metThreshold == attackThresholds[0]) {
                /* After 25 attacks, evokers summon 3 vexes and gain regen 2 */
                new SpawnEntity(this.getWorld(), new CustomEntityVex(this.getWorld()), 3, null, null, this, false, false);
                this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
            } else if (metThreshold == attackThresholds[1]) {
                /* After 35 attacks, evokers gain regen 3 */
                this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));
            } else if (metThreshold == attackThresholds[2]) {
                /* After 60 attacks, evokers gain speed 1 */
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 0));
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
    protected void initPathfinder() { /* no longer targets iron golems */
        this.goalSelector.a(1, new EntityRaider.b<>(this));
        this.goalSelector.a(3, new PathfinderGoalRaid<>(this));
        this.goalSelector.a(5, new c(this));
        this.goalSelector.a(4, new d(this, 1.0499999523162842D, 1));

        /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this));
        /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 20, 2, 1, 2, 2, true)); /* Breaks most blocks around the mob periodically */
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(1, new CustomEntityEvoker.PathfinderGoalEvokerCastSpell());
        this.goalSelector.a(2, new PathfinderGoalAvoidTarget<>(this, EntityPlayer.class, 8.0F, 0.6D, 1.0D));
        this.goalSelector.a(4, new CustomEntityEvoker.PathfinderGoalEvokerSummonVexSpell());
        this.goalSelector.a(5, new CustomEntityEvoker.PathfinderGoalEvokerFangSpell());
        this.goalSelector.a(6, new CustomEntityEvoker.PathfinderGoalEvokerWololoSpell());
        this.goalSelector.a(8, new PathfinderGoalRandomStroll(this, 0.6D));
        this.goalSelector.a(9, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 3.0F, 1.0F));
        this.goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));
        this.targetSelector.a(0, (new CustomPathfinderGoalHurtByTarget(this, new Class[]{EntityRaider.class})).a(EntityRaider.class)); /* Doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */
        this.targetSelector.a(1, (new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)).a(300)); /* Doesn't take into account y-level or line of sight to aggro a target */
        this.targetSelector.a(2, (new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class)).a(300));
    }

    @Override
    public void die() {
        super.die();

        if (this.getAttacks() >= 60) { /* after 60 attacks, evokers summon 7 vexes when killed */
            new SpawnEntity(this.getWorld(), new CustomEntityVex(this.getWorld()), 7, null, null, this, false, false);
        }
    }

    private void a(@Nullable EntitySheep entitySheep) { // private setWololoTarget()
        this.wololoTarget = entitySheep;
    }

    @Nullable
    private EntitySheep fh() { // private getWololoTarget()
        return this.wololoTarget;
    }

    class PathfinderGoalEvokerSummonVexSpell extends EntityIllagerWizard.c {

        private final PathfinderTargetCondition e;

        private PathfinderGoalEvokerSummonVexSpell() {
            super();
            this.e = (new CustomPathfinderTargetCondition()).a(28.0D).c().e().a().b();
        }

        @Override
        public boolean a() {
            if (!super.a()) {
                return false;
            } else {
                int i = CustomEntityEvoker.this.getWorld().a(EntityVex.class, this.e, CustomEntityEvoker.this, CustomEntityEvoker.this.getBoundingBox().g(28.0D)).size();

                return 25 > i; // evoker can have up to 25 vexes in its vicinity (28 blocks)
            }
        }

        @Override
        protected int g() { // attack delay for any spell
            return 0; /* does not increase global attack cooldown */
        }

        @Override
        protected int h() { // attack delay between each vex summon spell
            return 500; /* delay between each vex spawn increased to 25 seconds */
        }

        @Override
        protected void j() {
            CustomEntityEvoker.this.increaseAttacks(6);

            for (int i = 0; i < 6; ++i) { /* summons 6 vexes at a time instead of 3 */
                BlockPosition blockPosition = CustomEntityEvoker.this.getChunkCoordinates().b(-2 + random.nextInt(5), 1, -2 + random.nextInt(5));
                CustomEntityVex newVex = new CustomEntityVex(CustomEntityEvoker.this.getWorld());

                newVex.setPositionRotation(blockPosition, 0.0F, 0.0F);
                newVex.prepare(CustomEntityEvoker.this.getWorld(), CustomEntityEvoker.this.getWorld().getDamageScaler(blockPosition), EnumMobSpawn.MOB_SUMMONED, null, null);
                newVex.a(CustomEntityEvoker.this);
                newVex.g(blockPosition);
                newVex.a(20 * (30 + random.nextInt(90)));
                CustomEntityEvoker.this.getWorld().addEntity(newVex, CreatureSpawnEvent.SpawnReason.NATURAL);
            }
        }

        @Override
        protected SoundEffect k() {
            return SoundEffects.ENTITY_EVOKER_PREPARE_SUMMON;
        }

        @Override
        protected EntityIllagerWizard.Spell l() {
            return EntityIllagerWizard.Spell.SUMMON_VEX;
        }
    }

    class PathfinderGoalEvokerFangSpell extends EntityIllagerWizard.c {

        private PathfinderGoalEvokerFangSpell() {
            super();
        }

        @Override
        protected int g() {
            return 0; /* does not increase global attack cooldown */
        }

        @Override
        protected int h() { /* summons fangs every 85 ticks instead of 100 */
            return 85;
        }

        @Override
        protected void j() {
            EntityLiving entityLiving = CustomEntityEvoker.this.getGoalTarget();
            double d0 = Math.min(entityLiving.locY(), CustomEntityEvoker.this.locY());
            double d1 = Math.max(entityLiving.locY(), CustomEntityEvoker.this.locY()) + 1.0D;
            float f = (float) MathHelper.d(entityLiving.locZ() - CustomEntityEvoker.this.locZ(), entityLiving.locX() - CustomEntityEvoker.this.locX());
            int i;

            if (CustomEntityEvoker.this.h((Entity) entityLiving) < 9.0) {
                float f1;

                for (i = 0; i < 5; ++i) {
                    f1 = f + (float) i * 3.1415927F * 0.4F;
                    this.spawnFangs(CustomEntityEvoker.this.locX() + (double) MathHelper.cos(f1) * 1.5D, CustomEntityEvoker.this.locZ() + (double) MathHelper.sin(f1) * 1.5D, d0, d1, f1, 0);
                }

                for (i = 0; i < 8; ++i) {
                    f1 = f + (float) i * 3.1415927F * 2.0F / 8.0F + 1.2566371F;
                    this.spawnFangs(CustomEntityEvoker.this.locX() + (double) MathHelper.cos(f1) * 2.5D, CustomEntityEvoker.this.locZ() + (double) MathHelper.sin(f1) * 2.5D, d0, d1, f1, 3);
                }
            } else {
                for (i = 0; i < CustomEntityEvoker.this.getDetectionRange(); ++i) { // fang range increased to the same as follow range
                    double d2 = 1.25D * (double) (i + 1);
                    int j = 1 * i;

                    this.spawnFangs(CustomEntityEvoker.this.locX() + (double) MathHelper.cos(f) * d2, CustomEntityEvoker.this.locZ() + (double) MathHelper.sin(f) * d2, d0, d1, f, j);
                }
            }

            new RunnableEvokerStopPlayer(entityLiving, 8).runTaskTimer(StaticPlugin.plugin, 0L, 3L); /* every time the fangs attack, the player is slowed for 1.2 seconds */
        }

        public void spawnFangs(double d0, double d1, double d2, double d3, float f, int i) {
            EntityLiving entityLiving = CustomEntityEvoker.this.getGoalTarget();
            BlockPosition blockPosition = new BlockPosition(d0, d3, d1);
            boolean flag = false;
            double d4 = 0.0D;

            do {
                BlockPosition blockPosition1 = blockPosition.down();
                IBlockData iblockdata = CustomEntityEvoker.this.getWorld().getType(blockPosition1);

                if (iblockdata.d(CustomEntityEvoker.this.getWorld(), blockPosition1, EnumDirection.UP)) {
                    if (!CustomEntityEvoker.this.getWorld().isEmpty(blockPosition)) {
                        IBlockData iblockdata1 = CustomEntityEvoker.this.getWorld().getType(blockPosition);
                        VoxelShape voxelshape = iblockdata1.getCollisionShape(CustomEntityEvoker.this.getWorld(), blockPosition);

                        if (!voxelshape.isEmpty()) {
                            d4 = voxelshape.c(EnumDirection.EnumAxis.Y);
                        }
                    }

                    flag = true;
                    break;
                }

                blockPosition = blockPosition.down();
            } while (blockPosition.getY() >= MathHelper.floor(d2) - 1);

            if (flag) {
                CustomEntityEvoker.this.increaseAttacks(2);
                Location bukkitLocBase, bukkitLoc;
                Block bukkitBlock;
                org.bukkit.Material bukkitMaterial;

                BlockIterator iterator = new BlockIterator(CustomEntityEvoker.this.getWorld().getWorld(), new Vector(CustomEntityEvoker.this.locX(), CustomEntityEvoker.this.locY(), CustomEntityEvoker.this.locZ()), new Vector(entityLiving.locX() - CustomEntityEvoker.this.locX(), entityLiving.locY() - CustomEntityEvoker.this.locY(), entityLiving.locZ() - CustomEntityEvoker.this.locZ()), 1.0, (int) Math.ceil(CustomEntityEvoker.this.getDetectionRange()));
                while (iterator.hasNext()) { /* every time fangs are used, the evoker breaks all blocks within follow distance of itself towards the target, drilling a 3 by 3 hole through any blocks */
                    bukkitLocBase = iterator.next().getLocation();
                    Random random = new Random();

                    for (int x = -1; x <= 1; x++) {
                        for (int y = -1; y <= 1; y++) {
                            for (int z = -1; z <= 1; z++) {
                                bukkitLoc = new Location(CustomEntityEvoker.this.getWorld().getWorld(), bukkitLocBase.getX() + x, bukkitLocBase.getY() + y, bukkitLocBase.getZ() + z);
                                bukkitBlock = bukkitLoc.getBlock();
                                bukkitMaterial = bukkitBlock.getType();

                                if (Predicates.blockBreakableDefault.test(bukkitMaterial) && Predicates.notBedrock.test(bukkitMaterial) && Predicates.notHardBlocks.test(bukkitMaterial)) { // as long as it isn't one of these blocks
                                    bukkitBlock.setType(org.bukkit.Material.AIR);
                                } else if (!Predicates.notHardBlocks.test(bukkitMaterial)) { // 50% chance to break these blocks
                                    if (random.nextDouble() < 0.5) {
                                        bukkitBlock.setType(org.bukkit.Material.AIR);
                                    }
                                }
                            }
                        }
                    }
                }

                CustomEntityEvoker.this.getWorld().addEntity(new EntityEvokerFangs(CustomEntityEvoker.this.getWorld(), d0, (double) blockPosition.getY() + d4, d1, f, i, CustomEntityEvoker.this));
            }
        }

        @Override
        protected SoundEffect k() {
            return SoundEffects.ENTITY_EVOKER_PREPARE_ATTACK;
        }

        @Override
        protected EntityIllagerWizard.Spell l() {
            return EntityIllagerWizard.Spell.FANGS;
        }
    }

    class PathfinderGoalEvokerWololoSpell extends EntityIllagerWizard.c {

        private final PathfinderTargetCondition e = (new PathfinderTargetCondition()).a(32.0D).a().a((entityLiving)-> {
            return !((EntitySheep)entityLiving).getColor().equals(EnumColor.PINK); /* can target all non-pink sheep now within 32 blocks and with line of sight */
        });

        public PathfinderGoalEvokerWololoSpell() {
            super();
        }

        @Override
        public boolean a() {
            if (CustomEntityEvoker.this.getGoalTarget() != null) {
                return false;
            } else if (CustomEntityEvoker.this.eX()) {
                return false;
            } else if (CustomEntityEvoker.this.ticksLived < this.c) {
                return false;
            } else if (!CustomEntityEvoker.this.getWorld().getGameRules().getBoolean(GameRules.MOB_GRIEFING)) {
                return false;
            } else {
                List<EntitySheep> list = CustomEntityEvoker.this.getWorld().a(EntitySheep.class, this.e, CustomEntityEvoker.this, CustomEntityEvoker.this.getBoundingBox().grow(32.0D, 128.0D, 32.0D));

                if (list.isEmpty()) {
                    return false;
                } else {
                    CustomEntityEvoker.this.a(list.get(random.nextInt(list.size())));
                    return true;
                }
            }
        }

        @Override
        public boolean b() {
            return CustomEntityEvoker.this.fh() != null && this.b > 0;
        }

        @Override
        public void d() {
            super.d();
            CustomEntityEvoker.this.a((EntitySheep)null);
        }

        @Override
        protected void j() {
            CustomEntityEvoker.this.increaseAttacks(1);
            EntitySheep entitySheep = CustomEntityEvoker.this.fh();

            if (entitySheep != null && entitySheep.isAlive()) { /* instead of turning sheep red, the evoker summons a hyper-aggressive pink sheep */
                new SpawnEntity(entitySheep.getWorld(), new CustomEntitySheepAggressive(entitySheep.getWorld()), 1, null, null, entitySheep, true, true);
            }
        }

        @Override
        protected int m() {
            return 40;
        }

        @Override
        protected int g() {
            return 60;
        }

        @Override
        protected int h() {
            return 140;
        }

        @Override
        protected SoundEffect k() {
            return SoundEffects.ENTITY_EVOKER_PREPARE_WOLOLO;
        }

        @Override
        protected EntityIllagerWizard.Spell l() {
            return EntityIllagerWizard.Spell.WOLOLO;
        }
    }

    class PathfinderGoalEvokerCastSpell extends EntityIllagerWizard.b {

        private PathfinderGoalEvokerCastSpell() {
            super();
        }

        @Override
        public void e() {
            if (CustomEntityEvoker.this.getGoalTarget() != null) {
                CustomEntityEvoker.this.getControllerLook().a(CustomEntityEvoker.this.getGoalTarget(), (float) CustomEntityEvoker.this.ep(), (float) CustomEntityEvoker.this.eo());
            } else if (CustomEntityEvoker.this.fh() != null) {
                CustomEntityEvoker.this.getControllerLook().a(CustomEntityEvoker.this.fh(), (float) CustomEntityEvoker.this.ep(), (float) CustomEntityEvoker.this.eo());
            }
        }
    }

    public class c extends PathfinderGoal { // from EntityRaider.java

        private final CustomEntityEvoker evoker;

        public c(CustomEntityEvoker entityRaider) {
            this.evoker = entityRaider;
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE));
        }

        @Override
        public boolean a() {
            Raid raid = this.evoker.fb();

            return this.evoker.isAlive() && this.evoker.getGoalTarget() == null && raid != null && raid.isLoss();
        }

        @Override
        public void c() {
            this.evoker.x(true);
            super.c();
        }

        @Override
        public void d() {
            this.evoker.x(false);
            super.d();
        }

        @Override
        public void e() {
            if (!this.evoker.isSilent() && this.evoker.getRandom().nextInt(100) == 0) {
                CustomEntityEvoker.this.playSound(CustomEntityEvoker.this.eM(), CustomEntityEvoker.this.getSoundVolume(), CustomEntityEvoker.this.dG());
            }

            if (!this.evoker.isPassenger() && this.evoker.getRandom().nextInt(50) == 0) {
                this.evoker.getControllerJump().jump();
            }

            super.e();
        }
    }

    static class d extends PathfinderGoal { // from EntityRaider.java

        private final EntityRaider raider;
        private final double b;
        private BlockPosition c;
        private final List<BlockPosition> d = Lists.newArrayList();
        private final int e;
        private boolean f;

        public d(EntityRaider entityRaider, double d0, int i) {
            this.raider = entityRaider;
            this.b = d0;
            this.e = i;
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE));
        }

        @Override
        public boolean a() {
            this.j();
            return this.g() && this.h() && this.raider.getGoalTarget() == null;
        }

        private boolean g() {
            return this.raider.fc() && !this.raider.fb().a();
        }

        private boolean h() {
            WorldServer worldserver = (WorldServer) this.raider.world;
            BlockPosition blockPosition = this.raider.getChunkCoordinates();
            Optional<BlockPosition> optional = worldserver.x().a((villageplacetype) -> villageplacetype == VillagePlaceType.r, this::a, VillagePlace.Occupancy.ANY, blockPosition, 48, this.raider.getRandom());

            if (!optional.isPresent()) {
                return false;
            } else {
                this.c = (optional.get()).immutableCopy();
                return true;
            }
        }

        @Override
        public boolean b() {
            return this.raider.getNavigation().m() ? false : this.raider.getGoalTarget() == null && !this.c.a(this.raider.getPositionVector(), this.raider.getWidth() + (float) this.e) && !this.f;
        }

        @Override
        public void d() {
            if (this.c.a(this.raider.getPositionVector(), this.e)) {
                this.d.add(this.c);
            }

        }

        @Override
        public void c() {
            super.c();
            this.raider.n(0);
            this.raider.getNavigation().a(this.c.getX(), this.c.getY(), this.c.getZ(), this.b);
            this.f = false;
        }

        @Override
        public void e() {
            if (this.raider.getNavigation().m()) {
                Vec3D vec3d = Vec3D.c(this.c);
                Vec3D vec3d1 = RandomPositionGenerator.a(this.raider, 16, 7, vec3d, 0.3141592741012573D);

                if (vec3d1 == null) {
                    vec3d1 = RandomPositionGenerator.b(this.raider, 8, 7, vec3d);
                }

                if (vec3d1 == null) {
                    this.f = true;
                    return;
                }

                this.raider.getNavigation().a(vec3d1.x, vec3d1.y, vec3d1.z, this.b);
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

    static class RunnableEvokerStopPlayer extends BukkitRunnable {

        private final EntityLiving target;
        private int cycles;
        private final int maxCycles;

        public RunnableEvokerStopPlayer(EntityLiving target, int maxCycles) {
            this.target = target;
            this.cycles = 0;
            this.maxCycles = maxCycles;
        }

        @Override
        public void run() {
            if (++this.cycles > this.maxCycles) {
                this.cancel();
                return;
            }

            LivingEntity bukkitEntity = (LivingEntity) target.getBukkitEntity();
            bukkitEntity.setVelocity(new Vector(0.0, 0.0, 0.0));
        }
    }
}
