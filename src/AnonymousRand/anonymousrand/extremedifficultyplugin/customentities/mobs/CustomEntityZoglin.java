package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityTNTPrimed;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import com.mojang.serialization.Dynamic;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class CustomEntityZoglin extends EntityZoglin implements ICustomHostile, IAttackLevelingMob {

    public PathfinderGoalSelector vanillaTargetSelector;
    private int attacks;
    private boolean a8, a30;

    public CustomEntityZoglin(World world) {
        super(EntityTypes.ZOGLIN, world);
        this.vanillaTargetSelector = super.targetSelector;
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.attacks = 0;
        this.a8 = false;
        this.a30 = false;
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(this.isBaby() ? 0.9 : 0.7); /* zoglins move 75% faster (125% faster for babies) and do 2 damage (4 for babies) */
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(this.isBaby() ? 4.0 : 2.0);
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(1, new PathfinderGoalZoglinMeleeAttack(this, 1.0D)); /* uses the custom melee attack goal that attacks regardless of the y-level */
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0D)); // instead of using behavior-controlled idle actions
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Doesn't take into account y-level or line of sight to aggro a target or and maintain it as the target */
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damageAmount) {
        boolean tookDamage = super.damageEntity(damageSource, damageAmount);

        if (this.getWorld().isClientSide) {
            return false;
        } else if (tookDamage && damageSource.getEntity() instanceof EntityLiving) {
            EntityLiving entityLiving = (EntityLiving) damageSource.getEntity();

            if (entityLiving instanceof EntityPlayer && !BehaviorUtil.a(this, entityLiving, 4.0D)) { /* only retaliate against players */
                this.k(entityLiving);
            }

            return tookDamage;
        } else {
            return tookDamage;
        }
    }

    public double getDetectionRange() { /* zoglins have 64 block detection range */
        return 64.0;
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

    public int getAttacks() {
        return this.attacks;
    }

    public void increaseAttacks(int increase) {
        this.attacks += increase;
    }

//    public int[] getAttacksThresholds() {
//        return this.attackLevelingController.getAttacksThresholds();
//    }

    @Override
    protected BehaviorController<?> a(Dynamic<?> dynamic) { // removes all the weird behavior-controlled attack goals and instead use the standard pathfinder goals
        return this.cJ().a(dynamic);
    } // todo what?

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 8 && !this.a8) { /* after 8 attacks, zoglins gain regen 2 and shoot a power 1 ghast fireball every 5 seconds */
            this.a8 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
            this.goalSelector.a(1, new NewPathfinderGoalShootLargeFireballs(this, 100, 1, false));
        }

        if (this.attacks == 30 &&!this.a30) { /* after 30 attacks, zoglins gain speed 5 */
            this.a30 = true;
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 4));
        }

        if (this.ticksLived % 10 == 0 && this.getGoalTarget() != null) {
            Location bukkitLoc = new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ());

            if (bukkitLoc.getBlock().getType() == org.bukkit.Material.AIR) { /* zoglins create a path of power 1 tnt on itself as long as it is inside an air block */
                CustomEntityTNTPrimed newTNT = new CustomEntityTNTPrimed(this.getWorld(), 35, (this.attacks >= 15 && this.ticksLived % 100 == 0) ? 2.0F : 1.0F); /* after 15 attacks, zoglins spawn a power 2 tnt instead every 5 seconds */
                newTNT.setPosition(this.locX(), this.locY(), this.locZ());
                this.getWorld().addEntity(newTNT);
            }
        }
    }

    static class PathfinderGoalZoglinMeleeAttack extends CustomPathfinderGoalMeleeAttack {

        protected final CustomEntityZoglin zoglin;
        private boolean moveEverywhere;

        public PathfinderGoalZoglinMeleeAttack(CustomEntityZoglin zoglin, double speedTowardsTarget) {
            super(zoglin, speedTowardsTarget);
            this.zoglin = zoglin;
            this.moveEverywhere = false;
        }

        @Override
        public void e() {
            super.e();

            EntityLiving entityLiving = this.zoglin.getGoalTarget();

            if (entityLiving == null) {
                return;
            }

            if (this.zoglin.attacks >= 8) {
                if (this.zoglin.attacks == 25 && !this.moveEverywhere) { /* after 25 attacks, zoglins throw players around erratically, often high in the air, for a few seconds before teleporting to the player to continue attacking */
                    this.moveEverywhere = true;
                    new RunnableZoglinThrowPlayerAround(this.zoglin, entityLiving, 12).runTaskTimer(StaticPlugin.plugin, 0L, 5L);
                }
            }
        }
    }

    static class RunnableZoglinThrowPlayerAround extends BukkitRunnable {

        private final CustomEntityZoglin zoglin;
        private final EntityLiving target;
        private int cycles;
        private final int maxCycles;
        private static final Random random = new Random();

        public RunnableZoglinThrowPlayerAround(CustomEntityZoglin zoglin, EntityLiving target, int maxCycles) {
            this.zoglin = zoglin;
            this.target = target;
            this.cycles = 0;
            this.maxCycles = maxCycles;
        }

        @Override
        public void run() {
            if (++this.cycles <= this.maxCycles) {
                LivingEntity bukkitEntity = (LivingEntity) this.target.getBukkitEntity();
                bukkitEntity.setVelocity(new Vector(random.nextDouble() * 6 - random.nextDouble() * 3, random.nextDouble() * 3 - random.nextDouble() * 1.5, random.nextDouble() * 6 - random.nextDouble() * 3));
            } else if (++this.cycles - 32 >= this.maxCycles) {
                this.cancel();
                this.zoglin.setPosition(this.target.locX(), this.target.locY(), this.target.locZ()); // zoglin teleports to player after 8 seconds so that it can continue attacking
            }
        }
    }
}
