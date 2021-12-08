package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableMobShootArrowsNormally;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CustomEntitySkeletonStray extends EntitySkeletonStray implements ICustomMob {

    public boolean spawnMob, spawnExplodingArrow;
    public int attacks;
    private boolean a25, a60;

    public CustomEntitySkeletonStray(World world) {
        super(EntityTypes.STRAY, world);
        this.a(PathType.LAVA, 0.0F); /**no longer avoids lava*/
        this.a(PathType.DAMAGE_FIRE, 0.0F); /**no longer avoids fire*/
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.BOW)); //makes sure that it has a bow
        this.spawnMob = false;
        this.spawnExplodingArrow = false;
        this.attacks = 0;
        this.a25 = false;
        this.a60 = false;
    }

    @Override
    protected void initPathfinder() { /**no longer avoids sun and wolves or targets iron golems*/
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 1.0)); /**custom goal that spawns lightning randomly*/
        this.goalSelector.a(0, new NewPathfinderGoalTeleportTowardsPlayer(this, this.getFollowRange(), 300, 0.004)); /**custom goal that gives mob a chance every tick to teleport to within initial follow_range-2 to follow_range+13 blocks of nearest player if it has not seen a player target within follow range for 15 seconds*/
        this.goalSelector.a(4, new CustomPathfinderGoalBowShoot<>(this, 1.0D, 21, 32.0F)); /**uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal)*/
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new CustomPathfinderGoalHurtByTarget(this, new Class[0])); /**custom goal that prevents mobs from retaliating against other mobs in case the mob damage event doesn't register and cancel the damage*/
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, false, false, EntityTurtle.bv));
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, false)); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public void a(EntityLiving entityliving, float f) {
        this.attacks++;
        double rand = random.nextDouble();

        if (rand < (this.attacks < 25 ? 0.9 : this.attacks < 35 ? 0.85 : this.attacks < 45 ? 0.8 : 0.7)) { /**strays have 4 choices of attack: 70% (50% if more than 15 attacks) chance to shoot 70 normal slowness arrows, 20% (40% if more than 15 attacks) chance to shoot 70 flaming slowness arrows, 5% chance to shoot 10 power 1 exploding arrows, 5% chance to shoot a custom arrow that spawns a mob on impact*/
            boolean fire = random.nextDouble() < (this.attacks < 15 ? 0.222222222 : 0.444444444);
            new RunnableMobShootArrowsNormally(this, entityliving, 75, 1, 35.0, random.nextDouble() < 0.025 ? 1 : 0, fire, false).run(); /**shoots 75 arrows at a time with increased inaccuracy to seem like a cone; 2.25% of arrows shot are piercing 1*/
        } else if (rand < (this.attacks < 35 ? 0.95 : 0.9)) { /**increase chances of mob and exploding arrows as more attacks go on*/
            new RunnableMobShootArrowsNormally(this, entityliving, 10, 2, 45.0, 0, false, false).run();
        } else {
            new RunnableMobShootArrowsNormally(this, entityliving, 1, 3, 0.0, 0, false, true).run(); /**no inaccuracy or less in y level for this arrow*/
        }
    }

    public double getFollowRange() { /**strays have 22 block detection range (setting attribute doesn't work) (32 after 25 attacks)*/
        return this.attacks < 25 ? 22.0 : 32.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 25 && !this.a25) {
            this.a25 = true;
            this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, true)); //updates follow range
        }

        if (this.attacks == 60 && !this.a60) { /**after 60 attacks, strays summon 6 vanilla skeletons*/
            this.a60 = true;

            new SpawnEntity(this.getWorld(), new EntitySkeleton(EntityTypes.SKELETON, this.getWorld()), 6, CreatureSpawnEvent.SpawnReason.DROWNED, null, this, false, true);
        }

        if (this.ticksLived % 5 == 2) {
            if (this.getGoalTarget() != null) {
                EntityLiving target = this.getGoalTarget();

                if (this.getNormalDistanceSq(this.getPositionVector(), target.getPositionVector()) > Math.pow(this.getFollowRange(), 2)) { //deaggro if player out of y-level-included sphere for performance reasons
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
}
