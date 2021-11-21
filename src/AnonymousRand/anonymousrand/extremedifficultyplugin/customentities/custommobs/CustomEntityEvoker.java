package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import com.google.common.collect.Lists;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;

public class CustomEntityEvoker extends EntityEvoker implements ICustomMob {

    private EntitySheep wololoTarget;
    public int attacks;
    private boolean a25, a36, a60;
    private static final Random random = new Random();

    public CustomEntityEvoker(World world) {
        super(EntityTypes.EVOKER, world);
        this.a(PathType.LAVA, 0.0F); /**no longer avoids lava*/
        this.a(PathType.DAMAGE_FIRE, 0.0F); /**no longer avoids fire*/
        this.attacks = 0;
        this.a25 = false;
        this.a36 = false;
        this.a60 = false;
    }

    @Override
    protected void initPathfinder() { /**no longer targets iron golems*/
        this.goalSelector.a(1, new EntityRaider.b<>(this));
        this.goalSelector.a(3, new PathfinderGoalRaid<>(this));
        this.goalSelector.a(5, new c(this));
        this.goalSelector.a(4, new d(this, 1.0499999523162842D, 1));

        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 20, 2, 1, 2, 2, true)); /**custom goal that breaks blocks around the mob periodically*/
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(1, new CustomEntityEvoker.PathfinderGoalEvokerCastSpell());
        this.goalSelector.a(2, new PathfinderGoalAvoidTarget<>(this, EntityPlayer.class, 8.0F, 0.6D, 1.0D));
        this.goalSelector.a(4, new CustomEntityEvoker.PathfinderGoalEvokerSummonVexSpell());
        this.goalSelector.a(5, new CustomEntityEvoker.PathfinderGoalEvokerFangSpell());
        this.goalSelector.a(6, new CustomEntityEvoker.PathfinderGoalEvokerWololoSpell());
        this.goalSelector.a(8, new PathfinderGoalRandomStroll(this, 0.6D));
        this.goalSelector.a(9, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 3.0F, 1.0F));
        this.goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));
        this.targetSelector.a(0, (new CustomPathfinderGoalHurtByTarget(this, new Class[]{EntityRaider.class})).a(EntityRaider.class)); /**custom goal that prevents mobs from retaliating against other mobs in case the mob damage event doesn't register and cancel the damage*/
        this.targetSelector.a(1, (new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, false)).a(300)); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
        this.targetSelector.a(2, (new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class, false)).a(300));
    }

    @Override
    public void die() {
        super.die();

        if (this.attacks >= 60) { /**after 60 attacks, evokers summon 7 vexes when killed*/
            new SpawnEntity(this.getWorld(), new CustomEntityVex(this.getWorld()), 7, null, null, this, false, false);
        }
    }

    public double getFollowRange() { /**evokers have 28 block detection range (setting attribute doesn't work)*/
        return 28.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 25 && !this.a25) { /**after 25 attacks, evokers spawn 3 vexes*/
            this.a25 = true;
            new SpawnEntity(this.getWorld(), new CustomEntityVex(this.getWorld()), 3, null, null, this, false, false);
        }

        if (this.attacks == 36 && !this.a36) { /**after 36 attacks, evokers gain regen 2*/
            this.a36 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
        }

        if (this.attacks == 60 && !this.a60) { /**after 60 attacks, evokers gain speed 1 and regen 3*/
            this.a60 = true;
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 0));
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));
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

    private void a(@Nullable EntitySheep entitysheep) { //private setWololoTarget()
        this.wololoTarget = entitysheep;
    }

    @Nullable
    private EntitySheep fh() { //private getWololoTarget()
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

                return 24 > i; //evoker can have up to 24 vexes in its vicinity (28 blocks)
            }
        }

        @Override
        protected int g() { //attack delay for any spell
            return 0; /**does not increase global attack cooldown*/
        }

        @Override
        protected int h() { //attack delay between each vex summon spell
            return 500; /**delay between each vex spawn increased to 25 seconds*/
        }

        @Override
        protected void j() {
            CustomEntityEvoker.this.attacks += 6;

            for (int i = 0; i < 6; ++i) { /**summons 6 vexes at a time instead of 3*/
                BlockPosition blockposition = CustomEntityEvoker.this.getChunkCoordinates().b(-2 + CustomEntityEvoker.random.nextInt(5), 1, -2 + CustomEntityEvoker.random.nextInt(5));
                CustomEntityVex newVex = new CustomEntityVex(CustomEntityEvoker.this.getWorld());

                newVex.setPositionRotation(blockposition, 0.0F, 0.0F);
                newVex.prepare(CustomEntityEvoker.this.getWorld(), CustomEntityEvoker.this.getWorld().getDamageScaler(blockposition), EnumMobSpawn.MOB_SUMMONED, (GroupDataEntity)null, (NBTTagCompound)null);
                newVex.a((EntityInsentient)CustomEntityEvoker.this);
                newVex.g(blockposition);
                newVex.a(20 * (30 + CustomEntityEvoker.random.nextInt(90)));
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
            return 0; /**does not increase global attack cooldown*/
        }

        @Override
        protected int h() { /**summons fangs every 90 ticks instead of 100*/
            return 90;
        }

        @Override
        protected void j() {
            EntityLiving entityliving = CustomEntityEvoker.this.getGoalTarget();
            double d0 = Math.min(entityliving.locY(), CustomEntityEvoker.this.locY());
            double d1 = Math.max(entityliving.locY(), CustomEntityEvoker.this.locY()) + 1.0D;
            float f = (float)MathHelper.d(entityliving.locZ() - CustomEntityEvoker.this.locZ(), entityliving.locX() - CustomEntityEvoker.this.locX());
            int i;

            if (CustomEntityEvoker.this.h((Entity)entityliving) < 9.0) {
                float f1;

                for (i = 0; i < 5; ++i) {
                    f1 = f + (float)i * 3.1415927F * 0.4F;
                    this.spawnFangs(CustomEntityEvoker.this.locX() + (double)MathHelper.cos(f1) * 1.5D, CustomEntityEvoker.this.locZ() + (double)MathHelper.sin(f1) * 1.5D, d0, d1, f1, 0);
                }

                for (i = 0; i < 8; ++i) {
                    f1 = f + (float)i * 3.1415927F * 2.0F / 8.0F + 1.2566371F;
                    this.spawnFangs(CustomEntityEvoker.this.locX() + (double)MathHelper.cos(f1) * 2.5D, CustomEntityEvoker.this.locZ() + (double)MathHelper.sin(f1) * 2.5D, d0, d1, f1, 3);
                }
            } else {
                for (i = 0; i < CustomEntityEvoker.this.getFollowRange(); ++i) { //fang range increased to the same as follow range
                    double d2 = 1.25D * (double)(i + 1);
                    int j = 1 * i;

                    this.spawnFangs(CustomEntityEvoker.this.locX() + (double)MathHelper.cos(f) * d2, CustomEntityEvoker.this.locZ() + (double)MathHelper.sin(f) * d2, d0, d1, f, j);
                }
            }

            new RunnableEvokerStopPlayer(entityliving, 7).runTaskTimer(StaticPlugin.plugin, 0L, 3L); /**every time the fangs attack, the player is slowed for 1.05 seconds*/
        }

        public void spawnFangs(double d0, double d1, double d2, double d3, float f, int i) {
            EntityLiving entityliving = CustomEntityEvoker.this.getGoalTarget();
            BlockPosition blockposition = new BlockPosition(d0, d3, d1);
            boolean flag = false;
            double d4 = 0.0D;

            do {
                BlockPosition blockposition1 = blockposition.down();
                IBlockData iblockdata = CustomEntityEvoker.this.getWorld().getType(blockposition1);

                if (iblockdata.d(CustomEntityEvoker.this.getWorld(), blockposition1, EnumDirection.UP)) {
                    if (!CustomEntityEvoker.this.getWorld().isEmpty(blockposition)) {
                        IBlockData iblockdata1 = CustomEntityEvoker.this.getWorld().getType(blockposition);
                        VoxelShape voxelshape = iblockdata1.getCollisionShape(CustomEntityEvoker.this.getWorld(), blockposition);

                        if (!voxelshape.isEmpty()) {
                            d4 = voxelshape.c(EnumDirection.EnumAxis.Y);
                        }
                    }

                    flag = true;
                    break;
                }

                blockposition = blockposition.down();
            } while (blockposition.getY() >= MathHelper.floor(d2) - 1);

            if (flag) {
                CustomEntityEvoker.this.attacks += 2;

                BlockIterator iterator = new BlockIterator(CustomEntityEvoker.this.getWorld().getWorld(), new Vector(CustomEntityEvoker.this.locX(), CustomEntityEvoker.this.locY(), CustomEntityEvoker.this.locZ()), new Vector(entityliving.locX() - CustomEntityEvoker.this.locX(), entityliving.locY() - CustomEntityEvoker.this.locY(), entityliving.locZ() - CustomEntityEvoker.this.locZ()), 1.0, (int)Math.ceil(CustomEntityEvoker.this.getFollowRange()));
                while (iterator.hasNext()) { /**every time fangs are used, the evoker breaks all blocks within follow distance of itself towards the target, drilling a 3 by 3 hole through any blocks*/
                    Location locBase = iterator.next().getLocation();
                    Location loc;
                    Random random = new Random();

                    for (int x = -1; x <= 1; x++) {
                        for (int y = -1; y <= 1; y++) {
                            for (int z = -1; z <= 1; z++) {
                                loc = new Location(CustomEntityEvoker.this.getWorld().getWorld(), locBase.getX() + x, locBase.getY() + y, locBase.getZ() + z);

                                if (loc.getBlock().getType() != org.bukkit.Material.BEDROCK && loc.getBlock().getType() != org.bukkit.Material.END_GATEWAY && loc.getBlock().getType() != org.bukkit.Material.END_PORTAL && loc.getBlock().getType() != org.bukkit.Material.END_PORTAL_FRAME && loc.getBlock().getType() != org.bukkit.Material.NETHER_PORTAL && loc.getBlock().getType() != org.bukkit.Material.OBSIDIAN && loc.getBlock().getType() != org.bukkit.Material.CRYING_OBSIDIAN && loc.getBlock().getType() != org.bukkit.Material.COMMAND_BLOCK && loc.getBlock().getType() != org.bukkit.Material.COMMAND_BLOCK_MINECART && loc.getBlock().getType() != org.bukkit.Material.STRUCTURE_BLOCK && loc.getBlock().getType() != org.bukkit.Material.JIGSAW && loc.getBlock().getType() != org.bukkit.Material.BARRIER && loc.getBlock().getType() != org.bukkit.Material.END_STONE && loc.getBlock().getType() != org.bukkit.Material.SPAWNER && loc.getBlock().getType() != org.bukkit.Material.COBWEB) { //as long as it isn't one of these blocks
                                    loc.getBlock().setType(org.bukkit.Material.AIR);
                                } else if (loc.getBlock().getType() == org.bukkit.Material.OBSIDIAN || loc.getBlock().getType() == org.bukkit.Material.CRYING_OBSIDIAN || loc.getBlock().getType() == org.bukkit.Material.ANCIENT_DEBRIS || loc.getBlock().getType() == org.bukkit.Material.NETHERITE_BLOCK) { //50% chance to break these blocks
                                    if (random.nextDouble() < 0.5) {
                                        loc.getBlock().setType(org.bukkit.Material.AIR);
                                    }
                                }
                            }
                        }
                    }
                }

                CustomEntityEvoker.this.getWorld().addEntity(new EntityEvokerFangs(CustomEntityEvoker.this.getWorld(), d0, (double)blockposition.getY() + d4, d1, f, i, CustomEntityEvoker.this));
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

        private final PathfinderTargetCondition e = (new PathfinderTargetCondition()).a(32.0D).a().a((entityliving) -> {
            return !((EntitySheep)entityliving).getColor().equals(EnumColor.PINK); /**can target all non-pink sheep now within 32 blocks and with line of sight*/
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
                    CustomEntityEvoker.this.a((EntitySheep)list.get(CustomEntityEvoker.random.nextInt(list.size())));
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
            CustomEntityEvoker.this.attacks++;
            EntitySheep entitysheep = CustomEntityEvoker.this.fh();

            if (entitysheep != null && entitysheep.isAlive()) { /**instead of turning sheep red, the evoker summons a hyper-aggressive pink sheep*/
                new SpawnEntity(entitysheep.getWorld(), new CustomEntitySheepAggressive(entitysheep.getWorld()), 1, null, null, entitysheep, true, true);
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
                CustomEntityEvoker.this.getControllerLook().a(CustomEntityEvoker.this.getGoalTarget(), (float)CustomEntityEvoker.this.ep(), (float)CustomEntityEvoker.this.eo());
            } else if (CustomEntityEvoker.this.fh() != null) {
                CustomEntityEvoker.this.getControllerLook().a(CustomEntityEvoker.this.fh(), (float)CustomEntityEvoker.this.ep(), (float)CustomEntityEvoker.this.eo());
            }
        }
    }

    public class c extends PathfinderGoal { //from EntityRaider.java

        private final CustomEntityEvoker evoker;

        public c(CustomEntityEvoker entityraider) {
            this.evoker = entityraider;
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

            if (!this.evoker.isPassenger() && this.evoker.random.nextInt(50) == 0) {
                this.evoker.getControllerJump().jump();
            }

            super.e();
        }
    }

    static class d extends PathfinderGoal { //from EntityRaider.java

        private final EntityRaider raider;
        private final double b;
        private BlockPosition c;
        private final List<BlockPosition> d = Lists.newArrayList();
        private final int e;
        private boolean f;

        public d(EntityRaider entityraider, double d0, int i) {
            this.raider = entityraider;
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
            BlockPosition blockposition = this.raider.getChunkCoordinates();
            Optional<BlockPosition> optional = worldserver.x().a((villageplacetype) -> {
                return villageplacetype == VillagePlaceType.r;
            }, this::a, VillagePlace.Occupancy.ANY, blockposition, 48, this.raider.getRandom());

            if (!optional.isPresent()) {
                return false;
            } else {
                this.c = ((BlockPosition) optional.get()).immutableCopy();
                return true;
            }
        }

        @Override
        public boolean b() {
            return this.raider.getNavigation().m() ? false : this.raider.getGoalTarget() == null && !this.c.a((IPosition) this.raider.getPositionVector(), (double)(this.raider.getWidth() + (float)this.e)) && !this.f;
        }

        @Override
        public void d() {
            if (this.c.a((IPosition) this.raider.getPositionVector(), (double)this.e)) {
                this.d.add(this.c);
            }

        }

        @Override
        public void c() {
            super.c();
            this.raider.n(0);
            this.raider.getNavigation().a((double)this.c.getX(), (double)this.c.getY(), (double)this.c.getZ(), this.b);
            this.f = false;
        }

        @Override
        public void e() {
            if (this.raider.getNavigation().m()) {
                Vec3D vec3d = Vec3D.c((BaseBlockPosition) this.c);
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

        private boolean a(BlockPosition blockposition) {
            Iterator iterator = this.d.iterator();

            BlockPosition blockposition1;

            do {
                if (!iterator.hasNext()) {
                    return true;
                }

                blockposition1 = (BlockPosition) iterator.next();
            } while (!Objects.equals(blockposition, blockposition1));

            return false;
        }

        private void j() {
            if (this.d.size() > 2) {
                this.d.remove(0);
            }

        }
    }

    static class RunnableEvokerStopPlayer extends BukkitRunnable {

        private EntityLiving target;
        private int cycles, maxCycles;

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

            LivingEntity bukkitEntity = (LivingEntity)target.getBukkitEntity();
            bukkitEntity.setVelocity(new Vector(0.0, 0.0, 0.0));
        }
    }
}
