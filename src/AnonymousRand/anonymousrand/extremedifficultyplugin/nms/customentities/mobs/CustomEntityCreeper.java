package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import java.lang.reflect.Field;
import java.util.Collection;

public class CustomEntityCreeper extends EntityCreeper implements ICustomHostile {

    /* Ignores line of sight and y-level for initially finding a player target and maintaining it as the target,
       as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;
    private static Field fuseTicks;

    public CustomEntityCreeper(World world) {
        /* Creepers have 20 ticks of fuse by default */
        this(world, 20);
    }
    
    public CustomEntityCreeper(World world, int maxFuseTicks) {
        super(EntityTypes.CREEPER, world);
        this.maxFuseTicks = maxFuseTicks;
        this.initCustom();
    }

    private void initCustom() {
        this.initAttributes();

        try {
            fuseTicks = EntityCreeper.class.getDeclaredField("fuseTicks");
            fuseTicks.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
    }

    private void initAttributes() {
        /* Creepers move 75% faster */
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.4375);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                      ICustomHostile                                       //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /* Creepers have 24 block detection range (40 if charged) */
    public double getDetectionRange() {
        return this.isPowered() ? 40.0 : 24.0;
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

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                  Other custom functions                                   //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public void setFuseTicks(int fuseTicks) {
        try {
            CustomEntityCreeper.fuseTicks.setInt(this, fuseTicks);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    public int getMaxFuseTicks() {
        return this.maxFuseTicks;
    }

    public void setMaxFuseTicks(int maxFuseTicks) {
        this.maxFuseTicks = maxFuseTicks;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                               Overridden vanilla functions                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void initPathfinder() {
        /* Creepers are no longer scared of cats and ocelots */
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this));                                                     /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));                                                        /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 1.0));                                           /* Spawns lightning randomly */
        this.goalSelector.a(1, new NewPathfinderGoalTeleportNearTargetYLevel(this, 2.5, random.nextDouble() * 5 + 10.0, 0.00045)); /* Occasionally teleports to a spot closer in y-level to its target */
        this.goalSelector.a(1, new NewPathfinderGoalTeleportNearTarget(this, this.getDetectionRange(), 300.0, 0.001));             /* Occasionally teleports to a spot near its target */
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalSwell(this));                                                                     // this includes moving to target
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 0.8D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class));                     /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the nearest option */
        this.targetSelector.a(1, new CustomPathfinderGoalHurtByTarget<>(this));                                                    /* Always retaliates against players and teleports to them if they are out of range/do not have line of sight, but doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damageAmount) {
        boolean damageSuccess = super.damageEntity(damageSource, damageAmount);
        /* Creepers have a 50% chance to duplicate when hit by player and not killed (100% chance to duplicate into 2
           if charged) */
        if (damageSuccess && this.isAlive() && damageSource.getEntity() instanceof EntityPlayer
                && random.nextDouble() < (this.isPowered() ? 1.0 : 0.5)) {
            new SpawnEntity(this.world, this.getMaxFuseTicks(), new CustomEntityCreeper(this.world),
                    this.isPowered() ? 2 : 1, null, null, this, false, true);
        }

        return damageSuccess;
    }

    @Override
    public void explode() {
        if (this.getGoalTarget() != null) {
            /* Charged creepers explode with power 50, and creepers explode more powerfully the further the player is */
            /* The explosion power is given by $base + max(1.5^{dist - 2} - 1, 0)$ */
            if (!this.world.isClientSide) {
                double extraRadius =
                        Math.pow(1.5, Math.sqrt(NMSUtil.distSq(this, this.getGoalTarget(), false)) - 2.0) - 1.0;
                float explosionRadius =
                        (float) ((this.isPowered() ? 50.0 : this.explosionRadius) + Math.max(extraRadius, 0.0));
                ExplosionPrimeEvent event = new ExplosionPrimeEvent(this.getBukkitEntity(), explosionRadius, false);
                this.world.getServer().getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    this.killed = true;
                    this.world.createExplosion(this, this.locX(), this.locY(), this.locZ(), event.getRadius(),
                            event.getFire(), this.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING)
                            ? Explosion.Effect.DESTROY : Explosion.Effect.NONE);
                    this.die();
                    this.createEffectCloud();
                } else {
                    this.setFuseTicks(0);
                }
            }
        }
    }

    @Override
    public void onLightningStrike(EntityLightning entityLightning) {
        super.onLightningStrike(entityLightning);
        /* Charged creepers have 100 health */
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(100.0);
        this.setHealth(100.0F);
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); // update follow range // todo unnecessary now?
    }

    private void createEffectCloud() {
        Collection<MobEffect> collection = this.getEffects();
        if (collection.isEmpty()) {
            return;
        }

        EntityAreaEffectCloud areaEffectCloud =
                new EntityAreaEffectCloud(this.world, this.locX(), this.locY(), this.locZ());

        areaEffectCloud.setInvisible(true);
        areaEffectCloud.setRadius(2.5F);
        areaEffectCloud.setRadiusOnUse(-0.5F);
        areaEffectCloud.setWaitTime(10);
        areaEffectCloud.setDuration(areaEffectCloud.getDuration() / 2);
        areaEffectCloud.setRadiusPerTick(-areaEffectCloud.getRadius() / (float) areaEffectCloud.getDuration());

        /* creepers only create area effect clouds of negative effects */
        for (MobEffect mobeffect : collection) {
            MobEffectList effect = mobeffect.getMobEffect();

            // todo predicate for this?
            /* Creepers only create area effect clouds of negative effects */
            if (effect.equals(MobEffects.BAD_OMEN)          || effect.equals(MobEffects.BLINDNESS)
                    || effect.equals(MobEffects.CONFUSION)  || effect.equals(MobEffects.HUNGER)
                    || effect.equals(MobEffects.LEVITATION) || effect.equals(MobEffects.POISON)
                    || effect.equals(MobEffects.SLOWER_DIG) || effect.equals(MobEffects.SLOWER_MOVEMENT)
                    || effect.equals(MobEffects.UNLUCK)     || effect.equals(MobEffects.WEAKNESS)
                    || effect.equals(MobEffects.WITHER)) {
                areaEffectCloud.addEffect(new MobEffect(mobeffect));
            }
        }

        this.world.addEntity(areaEffectCloud);
    }
}
