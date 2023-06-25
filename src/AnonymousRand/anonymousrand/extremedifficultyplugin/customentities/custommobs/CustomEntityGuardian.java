package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.AttackController;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalCobwebMoveFaster;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

public class CustomEntityGuardian extends EntityGuardian implements ICustomMob, IAttackLevelingMob {

    private AttackController attackController;

    public CustomEntityGuardian(World world) {
        super(EntityTypes.GUARDIAN, world);
        this.initCustom();
        this.initAttacks();
    }

    //////////////////////////////  ICustomMob  //////////////////////////////
    public void initCustom() {
        /** No longer avoids lava */
        this.a(PathType.LAVA, 0.0F);
        /** No longer avoids fire */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
    }

    public double getFollowRange() { /** guardians have 24 block detection range (setting attribute doesn't work) (32 after 8 attacks) */
        return this.getAttacks() < 8 ? 24.0 : 32.0;
    }

    //////////////////////////  IAttackLevelingMob  //////////////////////////
    public void initAttacks() {
        this.attackController = new AttackController(8, 12, 40);
    }

    public int getAttacks() {
        return this.attackController.getAttacks();
    }

    public void incrementAttacks(int increment) {
        for (int metThreshold : this.attackController.incrementAttacks(increment)) {
            int[] attackThresholds = this.attackController.getAttackThresholds();
            if (metThreshold == attackThresholds[0]) {
                this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); // updates follow range
            } else if (metThreshold == attackThresholds[1]) {
                /** After 12 attacks, guardians gain regen 3 and 40 max health */
                ((LivingEntity)this.getBukkitEntity()).setMaxHealth(40.0);
                this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));
            } else if (metThreshold == attackThresholds[2]) {
                /** After 40 attacks, guardians summon an elder guardian */
                new SpawnEntity(this.getWorld(), new CustomEntityGuardianElder(this.getWorld()), 1, null, null, this, false, true);
            }
        }
    }

    //////////////////////  Other or vanilla functions  //////////////////////
    @Override
    public void initPathfinder() {
        PathfinderGoalMoveTowardsRestriction pathfindergoalmovetowardsrestriction = new PathfinderGoalMoveTowardsRestriction(this, 1.0D);
        this.goalRandomStroll = new PathfinderGoalRandomStroll(this, 1.0D, 80);
        /** Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this));
        /** Takes buffs from bats and piglins etc. */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));
        this.goalSelector.a(4, new CustomEntityGuardian.PathfinderGoalGuardianAttack(this));
        this.goalSelector.a(5, pathfindergoalmovetowardsrestriction);
        this.goalSelector.a(7, this.goalRandomStroll);
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityGuardian.class, 12.0F, 0.01F));
        this.goalSelector.a(9, new PathfinderGoalRandomLookaround(this));
        this.goalRandomStroll.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        pathfindergoalmovetowardsrestriction.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityLiving.class, 10, new CustomEntityGuardian.EntitySelectorGuardianTargetHumanSquid(this))); /** uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (!this.eO() && !damagesource.isMagic() && damagesource.j() instanceof EntityLiving) {
            EntityLiving entityLiving = (EntityLiving)damagesource.j();

            if (!damagesource.isExplosion()) {
                entityLiving.damageEntity(DamageSource.a(this), f * 0.5F); /** thorns damage increased from 2 to 50% of the damage dealt */
                entityLiving.addEffect(new MobEffect(MobEffects.SLOWER_DIG, 400, this.getAttacks() < 55 ? 0 : 1)); /** guardians give players that hit them mining fatigue 1 (2 after 55 attacks) for 20 seconds */
            }
        }

        if (this.goalRandomStroll != null) {
            this.goalRandomStroll.h();
        }

        return super.damageEntity(damagesource, f);
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

    static class PathfinderGoalGuardianAttack extends PathfinderGoal { /** guardian no longer stops attacking if player is too close */

        private final CustomEntityGuardian guardian;
        private int b;
        private final boolean isElder;

        public PathfinderGoalGuardianAttack(CustomEntityGuardian entityGuardian) {
            this.guardian = entityGuardian;
            this.isElder = false;
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        }

        @Override
        public boolean a() {
            EntityLiving entityLiving = this.guardian.getGoalTarget();

            return entityLiving != null && entityLiving.isAlive();
        }

        @Override
        public void c() {
            this.b = -10;
            this.guardian.getNavigation().o();
            this.guardian.getControllerLook().a(this.guardian.getGoalTarget(), 90.0F, 90.0F);
            this.guardian.impulse = true;
        }

        @Override
        public void d() {
            this.guardian.a(0);
            this.guardian.setGoalTarget(null, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, false);
            this.guardian.goalRandomStroll.h();
        }

        @Override
        public void e() {
            EntityLiving entityLiving = this.guardian.getGoalTarget();

            this.guardian.getNavigation().o();
            this.guardian.getControllerLook().a(entityLiving, 90.0F, 90.0F);

            if (entityLiving != null) {
                ++this.b; /** laser no longer disengages when there is a block between guardian and player */

                if (this.b == 0) {
                    this.guardian.a(this.guardian.getGoalTarget().getId());
                    if (!this.guardian.isSilent()) {
                        this.guardian.world.broadcastEntityEffect(this.guardian, (byte) 21);
                    }
                } else if (this.b >= this.guardian.eL()) {
                    float f = 1.0F;

                    if (this.guardian.world.getDifficulty() == EnumDifficulty.HARD) {
                        f += 2.0F;
                    }

                    if (this.isElder) {
                        f += 2.0F;
                    }

                    this.guardian.incrementAttacks(1);
                    entityLiving.damageEntity(DamageSource.c(this.guardian, this.guardian), f);
                    entityLiving.damageEntity(DamageSource.mobAttack(this.guardian), (float)this.guardian.b(GenericAttributes.ATTACK_DAMAGE));
                    this.guardian.setGoalTarget(null, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, false);
                }

                if (this.b >= this.guardian.eL() / 2.5 && this.guardian.ticksLived % (this.guardian.getAttacks() < 10 ? 4 : 3) == 0) { /** tractor beam-like effect every 4 ticks (3 after 10 attacks) for the latter 60% of the laser charging period */
                    LivingEntity bukkitEntity = (LivingEntity)entityLiving.getBukkitEntity();
                    bukkitEntity.setVelocity(new Vector((this.guardian.locX() - bukkitEntity.getLocation().getX()) / 48.0, (this.guardian.locY() - bukkitEntity.getLocation().getY()) / 48.0, (this.guardian.locZ() - bukkitEntity.getLocation().getZ()) / 48.0));

                    if (this.guardian.getAttacks() >= 35) { /** after 35 attacks, guardians inflict poison 1 while the tractor beam is engaged */
                        if (this.guardian.getAttacks() >= 55) { /** after 55 attacks, guardians inflict hunger 1 and weakness 1 while the tractor beam is engaged */
                            entityLiving.addEffect(new MobEffect(MobEffects.HUNGER, 51, 0));
                            entityLiving.addEffect(new MobEffect(MobEffects.WEAKNESS, 51, 0));
                        }

                        entityLiving.addEffect(new MobEffect(MobEffects.POISON, 51, 0));
                    }
                }
            }

            super.e();
        }
    }

    static class EntitySelectorGuardianTargetHumanSquid implements Predicate<EntityLiving> {

        private final EntityGuardian a;

        public EntitySelectorGuardianTargetHumanSquid(EntityGuardian entityGuardian) {
            this.a = entityGuardian;
        }

        public boolean test(@Nullable EntityLiving entityLiving) {
            return (entityLiving instanceof EntityHuman || entityLiving instanceof EntitySquid) && entityLiving.h(this.a) > 9.0D;
        }
    }
}
