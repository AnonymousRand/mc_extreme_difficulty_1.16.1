package AnonymousRand.anonymousrand.plugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.attack.CustomPathfinderGoalAttackRangedSkeleton;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.attackmvmt.CustomPathfinderGoalAttackMvmtRangedSkeleton;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.target.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.plugin.util.NmsUtil;
import AnonymousRand.anonymousrand.plugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.plugin.util.bukkitrunnables.RunnableMobShootArrows;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class CustomEntitySkeletonStray extends EntitySkeletonStray implements ICustomHostile, IAttackLevelingMob {

    private static final boolean IGNORE_LOS = false;
    private static final boolean IGNORE_Y = false;
    public boolean spawnMob, spawnExplodingArrow;
    private int attacks;
    private boolean a20, a45, a60;

    public CustomEntitySkeletonStray(World world) {
        super(EntityTypes.STRAY, world);
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.BOW)); // makes sure that it has a bow
        this.spawnMob = false;
        this.spawnExplodingArrow = false;
        this.attacks = 0;
        this.a20 = false;
        this.a45 = false;
        this.a60 = false;
    }

    @Override
    protected void initPathfinder() { /* no longer avoids sun and wolves or targets iron golems */
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new CustomPathfinderGoalSummonLightningRandomly(this, 1.0)); /* Spawns lightning randomly */
        this.goalSelector.a(0, new CustomPathfinderGoalTeleportNearTarget(this, this.getDetectionRange(), 300.0, 0.001)); /* Occasionally teleports to a spot near its target */
        this.goalSelector.a(4, new CustomPathfinderGoalAttackRangedSkeleton<>(this, 25)); /* strays shoot 25% slower; uses the custom goal that attacks regardless of the y-level (the old goal stopped the mob from attack even if the mob has already recognized a target via CustomNearestAttackableTarget goal) */
        this.goalSelector.a(4, new CustomPathfinderGoalAttackMvmtRangedSkeleton<>(this));
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget<>(this));               /* Always retaliates against players and teleports to them if they are out of range/do not have line of sight, but doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the nearest option */
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, false, false, EntityTurtle.bv));
    }

    @Override
    public void a(EntityLiving attackTarget, float distFactor) {
        this.attacks++;
        double rand = random.nextDouble();

        if (rand < (this.attacks < 30 ? 0.9 : this.attacks < 45 ? 0.85 : this.attacks < 60 ? 0.8 : 0.7)) { /* strays have 4 choices of attack: 70% (50% if more than 12 attacks) chance to shoot 50 normal slowness arrows, 20% (40% if more than 12 attacks) chance to shoot 50 flaming slowness arrows, 5% chance to shoot 10 power 1 exploding arrows, 5% chance to shoot a custom arrow that spawns a mob on impact */
            boolean fire = random.nextDouble() < (this.attacks < 12 ? 0.222222222 : 0.444444444);
            new RunnableMobShootArrows(this, attackTarget, 50, 1, 25.5, random.nextDouble() < 0.025 ? 1 : 0, fire, false).run(); /* shoots 50 arrows at a time with increased inaccuracy to seem like a cone; 2.5% of arrows shot are piercing 1 */
        } else if (rand < (this.attacks < 45 ? 0.95 : 0.9)) { /* increase chances of mob and exploding arrows as more attacks go on */
            new RunnableMobShootArrows(this, attackTarget, 10, 2, 40.0, 0, false, false).run();
        } else {
            new RunnableMobShootArrows(this, attackTarget, 1, 3, 0.0, 0, false, true).run(); /* no inaccuracy or less in y-level for this arrow */
        }
    }

    public double getDetectionRange() { /* strays have 24 block detection range (32 after 20 attacks) */
        return this.attacks < 20 ? 24.0 : 32.0;
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

    @Override
    public int bL() {
        return Integer.MAX_VALUE;
    }

    public int getAttacks() {
        return this.attacks;
    }

    public void increaseAttacks(int increase) {
        this.attacks += increase;
    }

//    public int[] getAttacksThreshs() {
//        return this.attackLevelingController.getAttacksThreshs();
//    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 20 && !this.a20) { /* after 20 attacks, strays get 30 max health and health */
            this.a20 = true;
            ((LivingEntity) this.getBukkitEntity()).setMaxHealth(30.0);
            this.setHealth(30.0F);
            this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); // update follow range
        }

        if (this.attacks == 45 && !this.a45) { /* after 45 attacks, strays get 40 max health and health and regen 2 */
            this.a45 = true;
            ((LivingEntity) this.getBukkitEntity()).setMaxHealth(40.0);
            this.setHealth(40.0F);
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
            this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); // update follow range
        }

        if (this.attacks == 60 && !this.a60) { /* after 60 attacks, strays summon 5 vanilla skeletons */
            this.a60 = true;

            new SpawnEntity(this.world, new EntitySkeleton(EntityTypes.SKELETON, this.world), 5, CreatureSpawnEvent.SpawnReason.DROWNED, null, this, false, true);
        }

        if (this.ticksLived % 2 == 0) {
            if (this.getGoalTarget() != null) {
                EntityLiving target = this.getGoalTarget();

                if (NmsUtil.distSq(this, target, false) > Math.pow(this.getDetectionRange(), 2)) { // deaggro if player out of y-level-included sphere for performance reasons
                    this.setGoalTarget(null, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, false);
                }
            }
        }
    }
}
