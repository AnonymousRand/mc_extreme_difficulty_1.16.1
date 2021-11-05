package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import net.minecraft.server.v1_16_R1.*;
import net.minecraft.server.v1_16_R1.EntitySkeletonAbstract;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class CustomEntitySkeleton extends EntitySkeleton {

    private final JavaPlugin plugin;
    public boolean spawnExplodingArrow;
    public int attacks;
    private boolean a25, a90;

    private final CustomPathfinderGoalBowShoot<EntitySkeletonAbstract> b = new CustomPathfinderGoalBowShoot<>(this, 1.0D, 21, 22.0F); /**uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal)*/

    public CustomEntitySkeleton(World world, JavaPlugin plugin) {
        super(EntityTypes.SKELETON, world);
        this.plugin = plugin;
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.BOW)); //makes sure that it has a bow
        this.spawnExplodingArrow = false;
        this.attacks = 0;
        this.a25 = false;
        this.a90 = false;
    }

    @Override
    protected void initPathfinder() { /**no longer avoids sun and wolves or targets iron golems*/
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 1.0)); /**custom goal that spawns lightning randomly*/
        this.goalSelector.a(0, new NewPathfinderGoalTeleportTowardsPlayer(this, this.getFollowRange(), 300, 0.004)); /**custom goal that gives mob a chance every tick to teleport to within initial follow_range-2 to follow_range+13 blocks of nearest player if it has not seen a player target within follow range for 15 seconds*/
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new CustomPathfinderGoalHurtByTarget(this, new Class[0])); /**custom goal that prevents mobs from retaliating against other mobs in case the mob damage event doesn't register and cancel the damage*/
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, false, false, EntityTurtle.bv));
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public void a(EntityLiving entityliving, float f) { //shoot
        this.attacks++;

        if (this.attacks >= 20 && this.attacks <= 45 && this.attacks % 8 == 0) { /**between these attack counts, shoot exploding arrows every 8 shots*/
            for (int i = 0; i < 10; i++) {
                this.spawnExplodingArrow = true;
                ItemStack itemstack = this.f(this.b(ProjectileHelper.a(this, Items.BOW)));
                EntityArrow entityarrow = this.b(itemstack, f);

                double d0 = entityliving.locX() - this.locX();
                double d1 = entityliving.e(0.3333333333333333D) - entityarrow.locY();
                double d2 = entityliving.locZ() - this.locZ();

                entityarrow.shoot(d0, d1, d2, 1.6F, (float)(50 - this.getWorld().getDifficulty().a() * 4)); //more inaccuracy
                this.playSound(SoundEffects.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
                this.getWorld().addEntity(entityarrow);
            }
        } else if (this.attacks < 30) {
            this.spawnExplodingArrow = false;

            for (int i = 0; i < 75; i++) { /**shoots 75 arrows at a time with increased inaccuracy to seem like a cone*/
                ItemStack itemstack = this.f(this.b(ProjectileHelper.a(this, Items.BOW)));
                EntityArrow entityarrow = this.b(itemstack, f);
                double d0 = entityliving.locX() - this.locX();
                double d1 = entityliving.e(0.3333333333333333D) - entityarrow.locY();
                double d2 = entityliving.locZ() - this.locZ();
                double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);

                if (this.random.nextDouble() <= 0.02) { /**2% of arrows shot are piercing 1*/
                    entityarrow.setPierceLevel((byte)1);
                }

                if (this.attacks >= 18) { /**after the 18th attack, arrows are on fire and do not lose y level*/
                    entityarrow.setOnFire(50);
                    entityarrow.setNoGravity(true);
                }

                entityarrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float)(40 - this.getWorld().getDifficulty().a() * 4));
                this.playSound(SoundEffects.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
                this.getWorld().addEntity(entityarrow);
            }
        } else { /**if more than 30 attacks, rapidfire; if more than 45, even faster rapidfire*/
            this.spawnExplodingArrow = false;

            new SkeletonRapidFire(this, entityliving, this.attacks >= 35 ? 40 : 8, f).runTaskTimer(this.plugin, 0L, this.attacks >= 35 ? 1L : 5L); //custom repeating runnable class
        }
    }

    @Override
    public void eM() { //"re-registers" the new field "b" since reflection doesn't seem to work
        if (this.getWorld() != null && !this.getWorld().isClientSide) {
            this.goalSelector.a((PathfinderGoal)this.b);
            ItemStack itemstack = this.b(ProjectileHelper.a(this, Items.BOW));

            if (itemstack.getItem() == Items.BOW) {
                this.goalSelector.a(4, this.b);
            }
        }
    }

    @Override
    public EntityArrow b(ItemStack itemstack, float f) { //make it public
        return ProjectileHelper.a(this, itemstack, f);
    }

    public double normalGetDistanceSq(Vec3D vec3d1, Vec3D vec3dt) {
        double d0 = vec3dt.getX() - vec3d1.getX(); //skeletons generally still include vertical distance for performance reasons
        double d1 = vec3dt.getY() - vec3d1.getY();
        double d2 = vec3dt.getZ() - vec3d1.getZ();

        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public double getFollowRange() { /**skeletons have 22 block detection range (setting attribute doesn't work) (32 after 25 attacks)*/
        return this.attacks < 25 ? 22.0 : 32.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 25 && !this.a25) { /**after 25 attacks, skeletons get 30 max health and health*/
            this.a25 = true;
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(30.0);
            this.setHealth(30.0F);
            this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, true)); //updates attack range
        }

        if (this.attacks == 90 && !this.a90) { /**after 90 attacks, skeletons summon an iron golem*/ //todo
            this.a90 = true;
        }

        if (this.ticksLived == 10) { /**skeletons only have 13.5 health*/
            this.setHealth(13.5F);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(13.5);

            if (this.random.nextDouble() < 0.05) { /**skeletons have a 5% chance to spawn as a stray instead*/
                new SpawnLivingEntity(this.getWorld(), new CustomEntitySkeletonStray(this.getWorld()), 1, null, null, this, true, true).run();
            }
        }

        if (this.ticksLived % 10 == 2) {
            if (this.getGoalTarget() != null) {
                EntityLiving target = this.getGoalTarget();

                if (this.normalGetDistanceSq(this.getPositionVector(), target.getPositionVector()) > Math.pow(this.getFollowRange(), 2)) { //deaggro if player out of y-level-included sphere for performance reasons
                    this.setGoalTarget(null);
                }
            }
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

    static class SkeletonRapidFire extends BukkitRunnable {

        private final CustomEntitySkeleton skeleton;
        private final EntityLiving target;
        private int cycles;
        private final int maxCycles;
        private final float f;
        private final Random random = new Random();

        public SkeletonRapidFire(CustomEntitySkeleton skeleton, EntityLiving target, int maxCycles, float f) {
            this.skeleton = skeleton;
            this.target = target;
            this.cycles = 0;
            this.maxCycles = maxCycles;
            this.f = f;
        }

        @Override
        public void run() {
            if (++this.cycles > this.maxCycles) {
                this.cancel();
            }

            for (int i = 0; i < (skeleton.attacks < 35 ? 10 : 1); i++) {
                ItemStack itemstack = skeleton.f(skeleton.b(ProjectileHelper.a(skeleton, Items.BOW)));
                EntityArrow entityarrow = skeleton.b(itemstack, f);
                double d0 = target.locX() - skeleton.locX();
                double d1 = target.locY() - skeleton.locY();
                double d2 = target.locZ() - skeleton.locZ();

                if (this.random.nextDouble() <= 0.02) { /**2% of arrows shot are piercing 1*/
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