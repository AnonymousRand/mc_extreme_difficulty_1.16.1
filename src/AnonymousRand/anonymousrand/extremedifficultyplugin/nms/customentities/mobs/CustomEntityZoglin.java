package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.ExtremeDifficultyPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IGoalRemovingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.misc.CustomEntityTNTPrimed;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.*;
import com.mojang.serialization.Dynamic;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class CustomEntityZoglin extends EntityZoglin implements ICustomHostile, IAttackLevelingMob, IGoalRemovingMob {

    /* Ignores line of sight and y-level for initially finding a player target and maintaining it as the target,
       as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;

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
        this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack<>(this, 1.0)); /* uses the custom melee attack goal that attacks regardless of the y-level */
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0)); // instead of using behavior-controlled idle actions
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget<>(this));                                /* Always retaliates against players and teleports to them if they are out of range/do not have line of sight, but doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */ // todo does the listener actually not work sometimes? also if this is no longer needed, don't remove old goal in igoalremovingmob
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the closest option */
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damageAmount) {
        boolean wasDamageTaken = super.damageEntity(damageSource, damageAmount);

        if (this.world.isClientSide) {
            return false;
        } else if (wasDamageTaken && damageSource.getEntity() instanceof EntityLiving) {
            EntityLiving entityLiving = (EntityLiving) damageSource.getEntity();

            if (entityLiving instanceof EntityPlayer && !BehaviorUtil.a(this, entityLiving, 4.0D)) { /* only retaliate against players */
                this.k(entityLiving);
            }

            return wasDamageTaken;
        } else {
            return wasDamageTaken;
        }
    }

    public double getDetectionRange() { /* zoglins have 64 block detection range */
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
                /* Mobs only despawn along horizontal axes, so even at build height, mobs will spawn below you and prevent sleeping */
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
            Location bukkitLoc = new Location(this.world.getWorld(), this.locX(), this.locY(), this.locZ());

            if (bukkitLoc.getBlock().getType() == org.bukkit.Material.AIR) { /* zoglins create a path of power 1 tnt on itself as long as it is inside an air block */
                CustomEntityTNTPrimed newTNT = new CustomEntityTNTPrimed(this.world, 35, (this.attacks >= 15 && this.ticksLived % 100 == 0) ? 2.0F : 1.0F); /* after 15 attacks, zoglins spawn a power 2 tnt instead every 5 seconds */
                newTNT.setPosition(this.locX(), this.locY(), this.locZ());
                this.world.addEntity(newTNT);
            }
        }
    }

    @Override
    public PathfinderGoalSelector getVanillaTargetSelector() {
        return this.vanillaTargetSelector;
    }
}
