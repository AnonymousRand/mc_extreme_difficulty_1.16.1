package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderTargetCondition;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.NewPathfinderGoalBreakBlocksAround;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.NewPathfinderGoalCobweb;
import com.google.common.collect.Lists;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;

public class CustomEntityEvoker extends EntityEvoker {

    private JavaPlugin plugin;
    private EntitySheep wololoTarget;
    public int attacks;
    private boolean a25, a36, a60;

    public CustomEntityEvoker(World world, JavaPlugin plugin) {
        super(EntityTypes.EVOKER, world);
        this.plugin = plugin;
        this.attacks = 0;
        this.a25 = false;
        this.a36 = false;
        this.a60 = false;
    }

    @Override
    protected void initPathfinder() { /**no longer targets iron golems*/
        this.goalSelector.a(1, new EntityRaider.b<>(this));
        this.goalSelector.a(3, new PathfinderGoalRaid<>(this));
        this.goalSelector.a(4, new d(this, 1.0499999523162842D, 1));
        this.goalSelector.a(5, new c(this));

        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(0, new NewPathfinderGoalCobweb(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(1, new CastingSpellGoal());
        this.goalSelector.a(2, new PathfinderGoalAvoidTarget<>(this, EntityHuman.class, 8.0F, 0.6D, 1.0D));
        this.goalSelector.a(2, new NewPathfinderGoalBreakBlocksAround(this, 20, 2, 1, 2, 2, true)); /**custom goal that breaks blocks around the mob periodically*/
        this.goalSelector.a(4, new SummonVexSpell());
        this.goalSelector.a(5, new FangAttackSpell());
        this.goalSelector.a(6, new WololoSpellGoal());
        this.goalSelector.a(8, new PathfinderGoalRandomStroll(this, 0.6D));
        this.goalSelector.a(9, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 3.0F, 1.0F));
        this.goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));
        this.targetSelector.a(1, (new PathfinderGoalHurtByTarget(this, new Class[]{EntityRaider.class})).a(EntityRaider.class));
        this.targetSelector.a(2, (new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)).a(300)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
        this.targetSelector.a(3, (new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class, false)).a(300));
    }

    @Override
    public void die() {
        super.die();

        if (this.attacks >= 60) { /**after 60 attacks, evokers summon 8 vexes on death*/
            EntityVex vex;

            for (int i = 0; i < 8; i++) {
                vex = new EntityVex(EntityTypes.VEX, this.getWorld());
                vex.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
                this.getWorld().addEntity(vex, CreatureSpawnEvent.SpawnReason.NATURAL);
            }
        }
    }

    public double getFollowRange() {
        return this.attacks < 36 ? 22.0 : 30.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 25 && !this.a25) { /**after 25 attacks, evokers spawn 3 vexes*/ //todo: replace with custom vex
            this.a25 = true;
            EntityVex vex;

            for (int i = 0; i < 3; i++) {
                vex = new EntityVex(EntityTypes.VEX, this.getWorld());
                vex.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
                this.getWorld().addEntity(vex, CreatureSpawnEvent.SpawnReason.NATURAL);
            }
        }

        if (this.attacks == 36 && !this.a36) { /**after 36 attacks, evokers gain regen 2*/ //todo: replace with custom vex
            this.a36 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
        }

        if (this.attacks == 60 && !this.a60) { /**after 60 attacks, evokers gain speed 1 and regen 3*/ //todo: replace with custom vex
            this.a60 = true;
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 0));
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));
        }

        if (this.ticksLived == 10) { /**evokers only have 20 health*/
            this.setHealth(20.0f);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(20.0);
        }

        if (this.ticksLived % 40 == 10) { /**evokers have 22 block detection range (setting attribute doesn't work) (30 after 36 attacks)*/
            EntityPlayer player = this.getWorld().a(EntityPlayer.class, new CustomPathfinderTargetCondition(), this, this.locX(), this.locY(), this.locZ(), this.getBoundingBox().grow(this.getFollowRange(), 128.0, this.getFollowRange())); //get closest player within bounding box
            if (player != null && !player.isInvulnerable() && this.getGoalTarget() == null) {
                this.setGoalTarget(player);
            }

            if (this.getGoalTarget() != null) {
                EntityLiving target = this.getGoalTarget();

                if (target.isInvulnerable() || this.d(target.getPositionVector()) > Math.pow(this.getFollowRange(), 2)) {
                    this.setGoalTarget(null);
                }
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

                if (d0 > (double)j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 8; /**random despawn distance increased to 40 blocks*/
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

    private void a(@Nullable EntitySheep entitysheep) { //private setWololoTarget()
        this.wololoTarget = entitysheep;
    }

    @Nullable
    private EntitySheep fh() { //private getWololoTarget()
        return this.wololoTarget;
    }

    class SummonVexSpell extends EntityIllagerWizard.c {

        private final PathfinderTargetCondition e;

        private SummonVexSpell() {
            super();
            this.e = (new PathfinderTargetCondition()).a(16.0D).c().e().a().b();
        }

        @Override
        public boolean a() {
            if (!super.a()) {
                return false;
            } else {
                int i = CustomEntityEvoker.this.world.a(EntityVex.class, this.e, CustomEntityEvoker.this, CustomEntityEvoker.this.getBoundingBox().g(16.0D)).size();

                return 24 > i; //evoker can have up to 24 vexes in its vicinity
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
        protected void j() { //todo: replace with custom vex
            CustomEntityEvoker.this.attacks += 6;

            for (int i = 0; i < 6; ++i) { /**summons 6 vexes at a time instead of 3*/
                BlockPosition blockposition = CustomEntityEvoker.this.getChunkCoordinates().b(-2 + CustomEntityEvoker.this.random.nextInt(5), 1, -2 + CustomEntityEvoker.this.random.nextInt(5));
                EntityVex newVex = (EntityVex)EntityTypes.VEX.a(CustomEntityEvoker.this.world);

                newVex.setPositionRotation(blockposition, 0.0F, 0.0F);
                newVex.prepare(CustomEntityEvoker.this.world, CustomEntityEvoker.this.world.getDamageScaler(blockposition), EnumMobSpawn.MOB_SUMMONED, (GroupDataEntity)null, (NBTTagCompound)null);
                newVex.a((EntityInsentient) CustomEntityEvoker.this);
                newVex.g(blockposition);
                newVex.a(20 * (30 + CustomEntityEvoker.this.random.nextInt(90)));
                CustomEntityEvoker.this.world.addEntity(newVex, CreatureSpawnEvent.SpawnReason.NATURAL);

                //todo: also summon a random other illager besides ravager and evoker
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

    class FangAttackSpell extends EntityIllagerWizard.c {

        private FangAttackSpell() {
            super();
        }

        @Override
        protected int g() {
            return 0; /**does not increase global attack cooldown*/
        }

        @Override
        protected int h() {
            return 100;
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

            new StopPlayer(CustomEntityEvoker.this, entityliving, 7).runTaskTimer(CustomEntityEvoker.this.plugin, 0L, 3L); /**every time the fangs attack, the player is slowed for 1.05 seconds*/
        }

        public void spawnFangs(double d0, double d1, double d2, double d3, float f, int i) {
            EntityLiving entityliving = CustomEntityEvoker.this.getGoalTarget();
            BlockPosition blockposition = new BlockPosition(d0, d3, d1);
            boolean flag = false;
            double d4 = 0.0D;

            do {
                BlockPosition blockposition1 = blockposition.down();
                IBlockData iblockdata = CustomEntityEvoker.this.world.getType(blockposition1);

                if (iblockdata.d(CustomEntityEvoker.this.world, blockposition1, EnumDirection.UP)) {
                    if (!CustomEntityEvoker.this.world.isEmpty(blockposition)) {
                        IBlockData iblockdata1 = CustomEntityEvoker.this.world.getType(blockposition);
                        VoxelShape voxelshape = iblockdata1.getCollisionShape(CustomEntityEvoker.this.world, blockposition);

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
                    Random rand = new Random();

                    for (int x = -1; x <= 1; x++) {
                        for (int y = -1; y <= 1; y++) {
                            for (int z = -1; z <= 1; z++) {
                                loc = new Location(CustomEntityEvoker.this.getWorld().getWorld(), locBase.getX() + x, locBase.getY() + y, locBase.getZ() + z);

                                if (loc.getBlock().getType() != org.bukkit.Material.BEDROCK && loc.getBlock().getType() != org.bukkit.Material.END_GATEWAY && loc.getBlock().getType() != org.bukkit.Material.END_PORTAL && loc.getBlock().getType() != org.bukkit.Material.END_PORTAL_FRAME && loc.getBlock().getType() != org.bukkit.Material.NETHER_PORTAL && loc.getBlock().getType() != org.bukkit.Material.OBSIDIAN && loc.getBlock().getType() != org.bukkit.Material.CRYING_OBSIDIAN && loc.getBlock().getType() != org.bukkit.Material.COMMAND_BLOCK && loc.getBlock().getType() != org.bukkit.Material.COMMAND_BLOCK_MINECART && loc.getBlock().getType() != org.bukkit.Material.STRUCTURE_BLOCK && loc.getBlock().getType() != org.bukkit.Material.JIGSAW && loc.getBlock().getType() != org.bukkit.Material.BARRIER && loc.getBlock().getType() != org.bukkit.Material.END_STONE && loc.getBlock().getType() != org.bukkit.Material.SPAWNER && loc.getBlock().getType() != org.bukkit.Material.COBWEB) { //as long as it isn't one of these blocks
                                    loc.getBlock().setType(org.bukkit.Material.AIR);
                                } else if (loc.getBlock().getType() == org.bukkit.Material.OBSIDIAN || loc.getBlock().getType() == org.bukkit.Material.CRYING_OBSIDIAN || loc.getBlock().getType() == org.bukkit.Material.ANCIENT_DEBRIS || loc.getBlock().getType() == org.bukkit.Material.NETHERITE_BLOCK) { //50% chance to break these blocks
                                    if (rand.nextDouble() < 0.5) {
                                        loc.getBlock().setType(org.bukkit.Material.AIR);
                                    }
                                }
                            }
                        }
                    }
                }

                CustomEntityEvoker.this.world.addEntity(new EntityEvokerFangs(CustomEntityEvoker.this.world, d0, (double)blockposition.getY() + d4, d1, f, i, CustomEntityEvoker.this));
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

    public class WololoSpellGoal extends EntityIllagerWizard.c {

        private final PathfinderTargetCondition e = (new PathfinderTargetCondition()).a(32.0D).a().a((entityliving) -> {
            return !((EntitySheep)entityliving).getColor().equals(EnumColor.PINK); /**can target all non-pink sheep now within 32 blocks and with line of sight*/
        });

        public WololoSpellGoal() {
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
            } else if (!CustomEntityEvoker.this.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING)) {
                return false;
            } else {
                List<EntitySheep> list = CustomEntityEvoker.this.world.a(EntitySheep.class, this.e, CustomEntityEvoker.this, CustomEntityEvoker.this.getBoundingBox().grow(32.0D, 128.0D, 32.0D));

                if (list.isEmpty()) {
                    return false;
                } else {
                    CustomEntityEvoker.this.a((EntitySheep)list.get(CustomEntityEvoker.this.random.nextInt(list.size())));
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
                Vec3D loc = entitysheep.getPositionVector();

                org.bukkit.inventory.ItemStack boots = new ItemStack(org.bukkit.Material.LEATHER_BOOTS);
                boots.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DEPTH_STRIDER, 3); /**most mobs spawn with depth strider 6 to avoid loopholes such as using water flow to keep them back*/
                boots.addUnsafeEnchantment(Enchantment.DURABILITY, 255);

                CustomEntitySheepAggressive newSheep = new CustomEntitySheepAggressive(entitysheep.getWorld(), CustomEntityEvoker.this.plugin);
                newSheep.setPosition(loc.getX(), loc.getY(), loc.getZ());
                ((LivingEntity)newSheep.getBukkitEntity()).getEquipment().setBoots(boots);
                newSheep.setColor(EnumColor.PINK);
                entitysheep.getWorld().addEntity(newSheep, CreatureSpawnEvent.SpawnReason.NATURAL);
                entitysheep.die();
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

    class CastingSpellGoal extends EntityIllagerWizard.b {

        private CastingSpellGoal() {
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

    static class d extends PathfinderGoal { //from EntityRaider.java

        private final EntityRaider a;
        private final double b;
        private BlockPosition c;
        private final List<BlockPosition> d = Lists.newArrayList();
        private final int e;
        private boolean f;

        public d(EntityRaider entityraider, double d0, int i) {
            this.a = entityraider;
            this.b = d0;
            this.e = i;
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE));
        }

        @Override
        public boolean a() {
            this.j();
            return this.g() && this.h() && this.a.getGoalTarget() == null;
        }

        private boolean g() {
            return this.a.fc() && !this.a.fb().a();
        }

        private boolean h() {
            WorldServer worldserver = (WorldServer) this.a.world;
            BlockPosition blockposition = this.a.getChunkCoordinates();
            Optional<BlockPosition> optional = worldserver.x().a((villageplacetype) -> {
                return villageplacetype == VillagePlaceType.r;
            }, this::a, VillagePlace.Occupancy.ANY, blockposition, 48, this.a.getRandom());

            if (!optional.isPresent()) {
                return false;
            } else {
                this.c = ((BlockPosition) optional.get()).immutableCopy();
                return true;
            }
        }

        @Override
        public boolean b() {
            return this.a.getNavigation().m() ? false : this.a.getGoalTarget() == null && !this.c.a((IPosition) this.a.getPositionVector(), (double)(this.a.getWidth() + (float)this.e)) && !this.f;
        }

        @Override
        public void d() {
            if (this.c.a((IPosition) this.a.getPositionVector(), (double)this.e)) {
                this.d.add(this.c);
            }

        }

        @Override
        public void c() {
            super.c();
            this.a.n(0);
            this.a.getNavigation().a((double)this.c.getX(), (double)this.c.getY(), (double)this.c.getZ(), this.b);
            this.f = false;
        }

        @Override
        public void e() {
            if (this.a.getNavigation().m()) {
                Vec3D vec3d = Vec3D.c((BaseBlockPosition) this.c);
                Vec3D vec3d1 = RandomPositionGenerator.a(this.a, 16, 7, vec3d, 0.3141592741012573D);

                if (vec3d1 == null) {
                    vec3d1 = RandomPositionGenerator.b(this.a, 8, 7, vec3d);
                }

                if (vec3d1 == null) {
                    this.f = true;
                    return;
                }

                this.a.getNavigation().a(vec3d1.x, vec3d1.y, vec3d1.z, this.b);
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

    public class c extends PathfinderGoal { //from EntityRaider.java

        private final CustomEntityEvoker b;

        c(CustomEntityEvoker entityraider) {
            this.b = entityraider;
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE));
        }

        @Override
        public boolean a() {
            Raid raid = this.b.fb();

            return this.b.isAlive() && this.b.getGoalTarget() == null && raid != null && raid.isLoss();
        }

        @Override
        public void c() {
            this.b.x(true);
            super.c();
        }

        @Override
        public void d() {
            this.b.x(false);
            super.d();
        }

        @Override
        public void e() {
            if (!this.b.isSilent() && this.b.getRandom().nextInt(100) == 0) {
                CustomEntityEvoker.this.playSound(CustomEntityEvoker.this.eM(), CustomEntityEvoker.this.getSoundVolume(), CustomEntityEvoker.this.dG());
            }

            if (!this.b.isPassenger() && this.b.random.nextInt(50) == 0) {
                this.b.getControllerJump().jump();
            }

            super.e();
        }
    }

    static class StopPlayer extends BukkitRunnable {

        private CustomEntityEvoker evoker;
        private EntityLiving target;
        private int cycles, maxCycles;

        public StopPlayer(CustomEntityEvoker evoker, EntityLiving target, int maxCycles) {
            this.evoker = evoker;
            this.target = target;
            this.cycles = 0;
            this.maxCycles = maxCycles;
        }

        public void run() {
            if (++this.cycles >= maxCycles) {
                this.cancel();
            }

            LivingEntity bukkitEntity = (LivingEntity)target.getBukkitEntity();
            bukkitEntity.setVelocity(new Vector(0.0, 0.0, 0.0));
        }
    }
}
