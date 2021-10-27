package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.*;
import AnonymousRand.ExtremeDifficultyPlugin.util.CoordsFromHypotenuse;
import net.minecraft.server.v1_16_R1.*;
import net.minecraft.server.v1_16_R1.EntitySkeletonAbstract;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class CustomEntitySkeleton extends EntitySkeleton {

    private JavaPlugin plugin;
    private final CustomPathfinderGoalBowShoot<EntitySkeletonAbstract> b = new CustomPathfinderGoalBowShoot<>(this, 1.0D, 21, 22.0F); /**uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal)*/
    public boolean spawnExplodingArrow;
    public int attacks;
    private boolean a20, a90;

    public CustomEntitySkeleton(World world, JavaPlugin plugin) {
        super(EntityTypes.SKELETON, world);
        this.plugin = plugin;
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.BOW)); //makes sure that it has a bow
        this.teleportToPlayer = 0;
        this.spawnExplodingArrow = false;
        this.attacks = 0;
        this.a20 = false;
        this.a90 = false;
    }

    @Override
    protected void initPathfinder() { /**no longer avoids sun and wolves or targets iron golems*/
        this.goalSelector.a(0, new NewPathfinderGoalCobweb(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 1.0)); /**custom goal that spawns lightning randomly*/
        this.goalSelector.a(0, new NewPathfinderGoalTeleportToPlayer(this, this.getFollowRange())); /**custom goal that gives mob a 0.25% chance every tick to teleport to within initial follow_range-2 to follow_range+14 blocks of nearest player if it has not seen a player target within follow range for 15 seconds*/
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, new Class[0]));
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
        this.targetSelector.a(3, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, false, false, EntityTurtle.bv));
    }

    @Override
    public void a(EntityLiving entityliving, float f) { //shoot
        this.attacks++;

        if (this.attacks >= 20 && this.attacks <= 55 && this.attacks % 8 == 0) { /**between these attack counts, shoot exploding arrows every 8 shots*/
            for (int i = 0; i < 10; i++) {
                this.spawnExplodingArrow = true;
                ItemStack itemstack = this.f(this.b(ProjectileHelper.a(this, Items.BOW)));
                EntityArrow entityarrow = this.b(itemstack, f);

                double d0 = entityliving.locX() - this.locX();
                double d1 = entityliving.locY() - this.locY();
                double d2 = entityliving.locZ() - this.locZ();

                entityarrow.shoot(d0, d1, d2, 1.6F, (float)(50 - this.world.getDifficulty().a() * 4)); //more inaccuracy
                this.playSound(SoundEffects.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
                this.world.addEntity(entityarrow);
            }
        } else if (this.attacks < 20) {
            this.spawnExplodingArrow = false;

            for (int i = 0; i < 75; i++) { /**shoots 75 arrows at a time with increased inaccuracy to seem like a cone*/
                ItemStack itemstack = this.f(this.b(ProjectileHelper.a(this, Items.BOW)));
                EntityArrow entityarrow = this.b(itemstack, f);
                double d0 = entityliving.locX() - this.locX();
                double d1 = entityliving.locY() - this.locY();
                double d2 = entityliving.locZ() - this.locZ();
                double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);

                if (random.nextDouble() <= 0.02) { /**2% of arrows shot are piercing 1*/
                    entityarrow.setPierceLevel((byte)1);
                }

                if (this.attacks >= 10) { /**after the 10th attack, arrows are on fire and do not lose y level*/
                    entityarrow.setOnFire(50);
                    entityarrow.setNoGravity(true);
                }

                entityarrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float)(40 - this.world.getDifficulty().a() * 4));
                this.playSound(SoundEffects.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
                this.world.addEntity(entityarrow);
            }
        } else { /**if more than 20 attacks, rapidfire; if more than 35, even faster rapidfire*/
            this.spawnExplodingArrow = false;

            new ShootArrowRepeating(this, entityliving, this.attacks >= 35 ? 40 : 8, f).runTaskTimer(this.plugin, 0L, this.attacks >= 35 ? 1L : 5L); //custom repeating runnable class
        }
    }

    @Override
    public void eM() { //"re-registers" the new field "b" since reflection doesn't seem to work
        if (this.world != null && !this.world.isClientSide) {
            this.goalSelector.a((PathfinderGoal)this.b);
            ItemStack itemstack = this.b(ProjectileHelper.a(this, Items.BOW));

            if (itemstack.getItem() == Items.BOW) {
                this.goalSelector.a(4, this.b);
            }
        }
    }

    public double normalGetDistanceSq(Vec3D vec3d1, Vec3D vec3dt) {
        double d0 = vec3dt.getX() - vec3d1.getX(); //skeletons generally still include vertical distance for performance reasons
        double d1 = vec3dt.getY() - vec3d1.getY();
        double d2 = vec3dt.getZ() - vec3d1.getZ();

        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    //todo: copy all from this point onwards to all applicable mobs
    public double getFollowRange() {
        return this.attacks < 20 ? 22.0 : 32.0;
    }

    protected int teleportToPlayer;
    protected CoordsFromHypotenuse coordsFromHypotenuse = new CoordsFromHypotenuse();

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 20 && !this.a20) { /**after 20 attacks, skeletons get 30 max health and health*/
            this.a20 = true;
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(30.0);
            this.setHealth(30.0f);
        }

        if (this.attacks == 90 && !this.a90) { /**after 90 attacks, skeletons summon an iron golem*/ //todo: custom mob
            this.a90 = true;
            EntityIronGolem golem = new EntityIronGolem(EntityTypes.IRON_GOLEM, this.getWorld());
            golem.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
            this.getWorld().addEntity(golem, CreatureSpawnEvent.SpawnReason.NATURAL);
        }

        if (this.ticksLived == 10) { /**skeletons only have 13.5 health*/
            this.setHealth(13.5f);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(13.5);

            if (random.nextDouble() < 0.05) { /**skeletons have a 5% chance to spawn as a stray instead*/
                CustomEntitySkeletonStray newStray = new CustomEntitySkeletonStray(this.getWorld());
                newStray.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
                this.getWorld().addEntity(newStray, CreatureSpawnEvent.SpawnReason.NATURAL);
                this.die();
            }
        }

        if (this.ticksLived % 40 == 10) { /**skeletons have 22 block detection range (setting attribute doesn't work) (32 after 20 attacks)*/
            EntityPlayer player = this.getWorld().a(EntityPlayer.class, new CustomPathfinderTargetCondition(), this, this.locX(), this.locY(), this.locZ(), this.getBoundingBox().grow(this.getFollowRange(), this.getFollowRange() / 2.0, this.getFollowRange())); //get closest player within bounding box
            if (player != null && !player.isInvulnerable() && this.getGoalTarget() == null && this.normalGetDistanceSq(this.getPositionVector(), player.getPositionVector()) <= Math.pow(this.getFollowRange(), 2)) {
                this.setGoalTarget(player);
            }

            if (this.getGoalTarget() != null) {
                EntityLiving target = this.getGoalTarget();

                if (target.isInvulnerable() || this.normalGetDistanceSq(this.getPositionVector(), target.getPositionVector()) > Math.pow(this.getFollowRange(), 2)) {
                    this.setGoalTarget(null);
                }
            }
        }

        if (this.getGoalTarget() == null) { //does not see a target within follow range
            this.teleportToPlayer++;
        } else {
            this.teleportToPlayer = 0;
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

    static class ShootArrowRepeating extends BukkitRunnable {

        private CustomEntitySkeleton skeleton;
        private EntityLiving target;
        private int cycles, maxCycles;
        private float f;
        private final Random rand = new Random();

        public ShootArrowRepeating(CustomEntitySkeleton skeleton, EntityLiving target, int maxCycles, float f) {
            this.skeleton = skeleton;
            this.target = target;
            this.cycles = 0;
            this.maxCycles = maxCycles;
            this.f = f;
        }

        public void run() {
            if (++this.cycles >= maxCycles) {
                this.cancel();
            }

            for (int i = 0; i < (skeleton.attacks < 35 ? 10 : 1); i++) {
                ItemStack itemstack = skeleton.f(skeleton.b(ProjectileHelper.a(skeleton, Items.BOW)));
                EntityArrow entityarrow = skeleton.b(itemstack, f);
                double d0 = target.locX() - skeleton.locX();
                double d1 = target.locY() - skeleton.locY();
                double d2 = target.locZ() - skeleton.locZ();

                if (rand.nextDouble() <= 0.02) { /**2% of arrows shot are piercing 1*/
                    entityarrow.setPierceLevel((byte)1);
                }

                if (this.skeleton.attacks >= 35) { /**starting from the 35th attack, arrows do not lose y level*/
                    entityarrow.setNoGravity(true);
                }

                entityarrow.setOnFire(50);

                entityarrow.shoot(d0, d1, d2, 1.6F, skeleton.attacks < 35 ? (float)(30 - skeleton.world.getDifficulty().a() * 4) : 0.0F); /**no inaccuracy after 35 attacks*/
                skeleton.playSound(SoundEffects.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (skeleton.getRandom().nextFloat() * 0.4F + 0.8F));
                skeleton.world.addEntity(entityarrow);
            }
        }
    }
}