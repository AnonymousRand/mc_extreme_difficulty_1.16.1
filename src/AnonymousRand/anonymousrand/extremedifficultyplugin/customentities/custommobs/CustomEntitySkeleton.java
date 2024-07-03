package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableMobShootArrows;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityTargetEvent;

public class CustomEntitySkeleton extends EntitySkeleton implements ICustomHostile, IAttackLevelingMob {

    public boolean spawnExplodingArrow;
    private int attacks;
    private boolean a20, a90;

    public CustomEntitySkeleton(World world) {
        super(EntityTypes.SKELETON, world);

        if (random.nextDouble() < 0.05) { /** skeletons have a 5% chance to spawn as a stray instead and a 5% chance to spawn as a pillager instead */
            new SpawnEntity(this.getWorld(), new CustomEntitySkeletonStray(this.getWorld()), 1, null, null, this, true, true);
        } else if (random.nextDouble() < 0.05) {
            new SpawnEntity(this.getWorld(), new CustomEntityPillager(this.getWorld()), 1, null, null, this, true, true);
        }

        this.a(PathType.LAVA, 0.0F); /** no longer avoids lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F); /** no longer avoids fire */
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.BOW)); // makes sure that it has a bow
        this.spawnExplodingArrow = false;
        this.attacks = 0;
        this.a20 = false;
        this.a90 = false;
    }

    @Override
    protected void initPathfinder() { /** no longer avoids sun and wolves or targets iron golems */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /** custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /** custom goal that allows this mob to take certain buffs from bats etc. */
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 1.0)); /** custom goal that spawns lightning randomly */
        this.goalSelector.a(0, new NewPathfinderGoalTeleportTowardsPlayer(this, this.getFollowRange(), 300.0, 0.001)); /** custom goal that gives mob a chance every tick to teleport to within initial follow_range-2 to follow_range+13 blocks of nearest player if it has not seen a player target within follow range for 15 seconds */
        this.goalSelector.a(4, new CustomPathfinderGoalRangedBowAttack<>(this, 1.0D, 25, 32.0F)); /** skeletons shoot 25% slower; uses the custom goal that attacks regardless of the y level (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal) */
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget(this, new Class[0])); /** custom goal that prevents mobs from retaliating against other mobs in case the mob damage event doesn't register and cancel the damage */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /** uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, EntityTurtle.bv));
    }

    @Override
    public void a(EntityLiving entityLiving, float f) { // shoot()
        this.attacks++;

        if (this.attacks >= 14 && this.attacks <= 30 && this.attacks % 8 == 6) { /** between these attack counts, shoot exploding arrows every 8 shots */
            new RunnableMobShootArrows(this, entityLiving, 10, 2, 40.0, 0, false, false).run();
        } else if (this.attacks < 30) { /** shoots 50 arrows at a time with increased inaccuracy to seem like a cone */
            new RunnableMobShootArrows(this, entityLiving, 50, 1, 25.0, random.nextDouble() < 0.025 ? 1 : 0, this.attacks >= 15, this.attacks >= 15).run(); /** 2.5% of arrows shot are piercing 1, and after 15 attacks, arrows are on fire and do not lose y level */
        } else { /** if more than 30 attacks, rapidfire; if more than 40, even faster rapidfire */
            new RunnableMobShootArrows(this, entityLiving, this.attacks < 40 ? 10 : 1, 1, this.attacks < 40 ? 30.0 : 0.0, random.nextDouble() < 0.05 ? 1 : 0, true, this.attacks >= 40, this.attacks < 40 ? 8 : 40).runTaskTimer(StaticPlugin.plugin, 0L, this.attacks < 40 ? 5L : 1L); /** 5% of arrows shot are piercing 1 */
        }
    }

    @Override
    public EntityArrow b(ItemStack itemstack, float f) { // make it public
        return ProjectileHelper.a(this, itemstack, f);
    }

    public double getFollowRange() { /** skeletons have 24 block detection range (setting attribute doesn't work) (32 after 20 attacks) */
        return this.attacks < 20 ? 24.0 : 32.0;
    }

    public int getAttacks() {
        return this.attacks;
    }

    public void increaseAttacks(int increase) {
        this.attacks += increase;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 20 && !this.a20) { /** after 20 attacks, skeletons get 35 max health and health */
            this.a20 = true;
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(35.0);
            this.setHealth(35.0F);
            this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); // updates follow range
        }

        if (this.attacks == 90 && !this.a90) { /** after 90 attacks, skeletons summon an iron golem */
            this.a90 = true;
            new SpawnEntity(this.getWorld(), new CustomEntityIronGolem(this.getWorld()), 1, null, null, this, false, true);
        }

        if (this.ticksLived % (random.nextInt(2) + 2) == 0) {
            if (this.getGoalTarget() != null) {
                EntityLiving target = this.getGoalTarget();

                if (this.getNormalDistanceSq(this.getPositionVector(), target.getPositionVector()) > Math.pow(this.getFollowRange(), 2)) { // deaggro if player out of y level-included sphere for performance reasons
                    this.setGoalTarget(null, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, false);
                }
            }
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
    public int bL() {
        return Integer.MAX_VALUE; /** mobs are willing to take any fall to reach the player as they don't take fall damage */
    }
}