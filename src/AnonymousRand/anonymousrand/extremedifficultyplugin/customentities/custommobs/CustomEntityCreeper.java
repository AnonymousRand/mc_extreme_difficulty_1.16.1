package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.Collection;

public class CustomEntityCreeper extends EntityCreeper implements ICustomMob {

    public static Field fuseTicks;

    public CustomEntityCreeper(World world, int fuse) {
        super(EntityTypes.CREEPER, world);
        this.maxFuseTicks = fuse;
        this.a(PathType.LAVA, 0.0F); /** no longer avoids lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F); /** no longer avoids fire */
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.4375); /** creepers move 75% faster */
    }

    static {
        try {
            fuseTicks = EntityCreeper.class.getDeclaredField("fuseTicks");
            fuseTicks.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initPathfinder() { /** creeper is no longer scared of cats and ocelots */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /** custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /** custom goal that allows this mob to take certain buffs from bats etc. */
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 1.0)); /** custom goal that spawns lightning randomly */
        this.goalSelector.a(0, new NewPathfinderGoalTeleportToPlayerAdjustY(this, 2.5, random.nextDouble() * 5 + 10.0, 0.00045)); /** custom goal that gives mob a chance every tick to teleport to a spot where its y level difference from its target is reduced if its y level difference is too large */
        this.goalSelector.a(0, new NewPathfinderGoalTeleportTowardsPlayer(this, this.getFollowRange(), 300.0, 0.001)); /** custom goal that gives mob a chance every tick to teleport to within initial follow_range-2 to follow_range+13 blocks of nearest player if it has not seen a player target within follow range for 15 seconds */
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalSwell(this));
        this.goalSelector.a(4, new PathfinderGoalMeleeAttack(this, 1.0D, false));
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 0.8D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, false)); /** uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
        this.targetSelector.a(1, new CustomPathfinderGoalHurtByTarget(this, new Class[0])); /** custom goal that prevents mobs from retaliating against other mobs in case the mob damage event doesn't register and cancel the damage */
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (damagesource.getEntity() instanceof EntityPlayer && this.getHealth() - f > 0.0 && random.nextDouble() < (this.isPowered() ? 1.0 : 0.5)) { /** creeper has a 50% chance to duplicate when hit by player and not killed (extra fuse on new creeper) (100% chance to duplicate into 10 if powered) */
            new SpawnEntity(this.getWorld(), this.maxFuseTicks, new CustomEntityCreeper(this.getWorld(), 20), this.isPowered() ? 10 : 1, null, null, this, false, true);
        }

        return super.damageEntity(damagesource, f);
    }

    @Override
    public void explode() {
        if (this.getGoalTarget() != null) {
            if (this.getNormalDistanceSq(this.getPositionVector(), this.getGoalTarget().getPositionVector()) > (this.isPowered() ? 36.0 : 25.0)) { // charged creepers only explode within 6 blocks of player and normal creepers only explode within 5
                try {
                    fuseTicks.setInt(this, 0);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                return;
            }

            if (!this.getWorld().isClientSide) {
                if (this.isPowered()) {
                    this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), (float)(75.0F + Math.max(((Math.sqrt(this.getNormalDistanceSq(this.getPositionVector(), this.getGoalTarget().getPositionVector())) - 3.0) * 0.225 / 0.39), 0.0)), true, Explosion.Effect.DESTROY); /** charged creepers explode with power 75; creepers explode more powerfully the more th player tried to distance themselves from the creeper */
                } else {
                    this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), (float)(this.explosionRadius + Math.max(((Math.sqrt(this.getNormalDistanceSq(this.getPositionVector(), this.getGoalTarget().getPositionVector())) - 3.0) * 0.225 / 0.39), 0.0)), false, this.getWorld().getGameRules().getBoolean(GameRules.MOB_GRIEFING) ? Explosion.Effect.DESTROY : Explosion.Effect.NONE);
                }

                this.killed = true;
                this.die();

                if (this.getEffects().size() > 0) {
                    this.createEffectCloud();
                }
            }
        }
    }

    @Override
    public void onLightningStrike(EntityLightning entityLightning) {
        super.onLightningStrike(entityLightning);
        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(200.0); /** charged creepers have 200 health */
        this.setHealth(200.0F);
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, true)); // updates follow range
    }

    private void createEffectCloud() {
        Collection<MobEffect> collection = this.getEffects();

        if (!collection.isEmpty()) {
            EntityAreaEffectCloud entityAreaEffectCloud = new EntityAreaEffectCloud(this.getWorld(), this.locX(), this.locY(), this.locZ());

            entityAreaEffectCloud.setInvisible(true);
            entityAreaEffectCloud.setRadius(2.5F);
            entityAreaEffectCloud.setRadiusOnUse(-0.5F);
            entityAreaEffectCloud.setWaitTime(10);
            entityAreaEffectCloud.setDuration(entityAreaEffectCloud.getDuration() / 2);
            entityAreaEffectCloud.setRadiusPerTick(-entityAreaEffectCloud.getRadius() / (float)entityAreaEffectCloud.getDuration());

            for (MobEffect mobeffect : collection) { /** creepers only create area effect clouds of negative effects */
                MobEffectList effect = mobeffect.getMobEffect();
                
                if (effect.equals(MobEffects.BAD_OMEN) || effect.equals(MobEffects.BLINDNESS) || effect.equals(MobEffects.CONFUSION) || effect.equals(MobEffects.HUNGER) || effect.equals(MobEffects.LEVITATION)|| effect.equals(MobEffects.POISON) || effect.equals(MobEffects.SLOWER_DIG) || effect.equals(MobEffects.SLOWER_MOVEMENT) || effect.equals(MobEffects.UNLUCK) || effect.equals(MobEffects.WEAKNESS) || effect.equals(MobEffects.WITHER)) { /** creepers only create area effect clouds of negative effects */
                    entityAreaEffectCloud.addEffect(new MobEffect(mobeffect));
                }
            }

            this.getWorld().addEntity(entityAreaEffectCloud);
        }

    }

    public double getFollowRange() { /** creepers have 28 block detection range (64 if powered) */
        return this.isPowered() ? 64.0 : 28.0;
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
    public double g(double d0, double d1, double d2) {
        double d3 = this.locX() - d0; /** for determining distance to entities, y level does not matter, e.g. mob follow range, attacking (can hit player no matter the y level) */
        double d5 = this.locZ() - d2;

        return d3 * d3 + d5 * d5;
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /** for determining distance to entities, y level does not matter, e.g. mob follow range, attacking (can hit player no matter the y level) */
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }

    @Override
    public int bL() { // getMaxFallHeight
        return Integer.MAX_VALUE; /** mobs are willing to take any fall to reach the player as they don't take fall damage */
    }
}
