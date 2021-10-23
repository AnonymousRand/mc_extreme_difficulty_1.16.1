package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderTargetCondition;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

public class CustomEntityGuardian extends EntityGuardian {

    public CustomEntityGuardian(World world) {
        super(EntityTypes.GUARDIAN, world);
    }

    @Override
    public void initPathfinder() {
        PathfinderGoalMoveTowardsRestriction pathfindergoalmovetowardsrestriction = new PathfinderGoalMoveTowardsRestriction(this, 1.0D);
        this.goalRandomStroll = new PathfinderGoalRandomStroll(this, 1.0D, 80);
        this.goalSelector.a(4, new CustomEntityGuardian.CustomPathfinderGoalGuardianAttack(this));
        this.goalSelector.a(5, pathfindergoalmovetowardsrestriction);
        this.goalSelector.a(7, this.goalRandomStroll);
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityGuardian.class, 12.0F, 0.01F));
        this.goalSelector.a(9, new PathfinderGoalRandomLookaround(this));
        this.goalRandomStroll.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        pathfindergoalmovetowardsrestriction.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityLiving.class, 10, false, false, new CustomEntityGuardian.EntitySelectorGuardianTargetHumanSquid(this))); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (!this.eO() && !damagesource.isMagic() && damagesource.j() instanceof EntityLiving) {
            EntityLiving entityliving = (EntityLiving) damagesource.j();

            if (!damagesource.isExplosion()) {
                entityliving.damageEntity(DamageSource.a(this), 4.0F); /**thorns damage increased from 2 to 4*/
                entityliving.addEffect(new MobEffect(MobEffects.SLOWER_DIG, 400, 0)); /**guardians give players that hit them mining fatigue 1 for 20 seconds*/
            }
        }

        if (this.goalRandomStroll != null) {
            this.goalRandomStroll.h();
        }

        return super.damageEntity(damagesource, f);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived % 40 == 10) { /**guardians have 24 block detection range (setting attribute doesn't work)*/
            EntityPlayer player = this.getWorld().a(EntityPlayer.class, new CustomPathfinderTargetCondition(), this, this.locX(), this.locY(), this.locZ(), this.getBoundingBox().grow(24.0, 128.0, 24.0)); //get closest player within bounding box
            if (player != null && this.getGoalTarget() == null) {
                this.setGoalTarget(player);
            }
        }

        Location thisLoc = new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ());
        Location thisLoc2 = new Location(this.getWorld().getWorld(), this.locX(), this.locY() + 1.0, this.locZ());
        if (thisLoc.getBlock().getType() == org.bukkit.Material.COBWEB || thisLoc2.getBlock().getType() == org.bukkit.Material.COBWEB) { /**non-player mobs gain Speed 11 while in a cobweb (approx original speed)*/
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, 2, 10));
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

                if (d0 > (double) j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 8; /**random despawn distance increased to 40 blocks*/
                int l = k * k;

                if (this.ticksFarFromPlayer > 600 && this.random.nextInt(800) == 0 && d0 > (double) l && this.isTypeNotPersistent(d0)) {
                    this.die();
                } else if (d0 < (double) l) {
                    this.ticksFarFromPlayer = 0;
                }
            }

        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    @Override
    public double g(double d0, double d1, double d2) {
        double d3 = this.locX() - d0; /**for determining distance to entities, y-level does not matter, eg. mob follow range*/
        double d5 = this.locZ() - d2;

        return d3 * d3 + d5 * d5;
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /**for determining distance to entities, y-level does not matter, eg. mob follow range*/
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }

    static class CustomPathfinderGoalGuardianAttack extends PathfinderGoal { /**guardian no longer stops attacking if player is too close*/

        private final CustomEntityGuardian entity;
        private int b;
        private final boolean isElder;

        public CustomPathfinderGoalGuardianAttack(CustomEntityGuardian entityguardian) {
            this.entity = entityguardian;
            this.isElder = false; //todo: copy class to elder guardian and change to true
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        }

        @Override
        public boolean a() {
            EntityLiving entityliving = this.entity.getGoalTarget();

            return entityliving != null && entityliving.isAlive();
        }

        @Override
        public void c() {
            this.b = -10;
            this.entity.getNavigation().o();
            this.entity.getControllerLook().a(this.entity.getGoalTarget(), 90.0F, 90.0F);
            this.entity.impulse = true;
        }

        @Override
        public void d() {
            this.entity.a(0);
            this.entity.setGoalTarget((EntityLiving) null);
            this.entity.goalRandomStroll.h();
        }

        @Override
        public void e() {
            EntityLiving entityliving = this.entity.getGoalTarget();

            this.entity.getNavigation().o();
            this.entity.getControllerLook().a(entityliving, 90.0F, 90.0F);

            if (entityliving != null) {
                ++this.b; /**laser no longer disengages when there is a block between guardian and player*/

                if (this.b == 0) {
                    this.entity.a(this.entity.getGoalTarget().getId());
                    if (!this.entity.isSilent()) {
                        this.entity.world.broadcastEntityEffect(this.entity, (byte) 21);
                    }
                } else if (this.b >= this.entity.eL()) {
                    float f = 1.0F;

                    if (this.entity.world.getDifficulty() == EnumDifficulty.HARD) {
                        f += 2.0F;
                    }

                    if (this.isElder) {
                        f += 2.0F;
                    }

                    entityliving.damageEntity(DamageSource.c(this.entity, this.entity), f);
                    entityliving.damageEntity(DamageSource.mobAttack(this.entity), (float)this.entity.b(GenericAttributes.ATTACK_DAMAGE));
                    this.entity.setGoalTarget((EntityLiving)null);
                }

                if (this.b >= this.entity.eL() / 2.5 && this.entity.ticksLived % 3 == 0) { /**tractor beam-like effect every 3 ticks for the latter 60% of the laser charging period*/
                    LivingEntity bukkitEntity = (LivingEntity)entityliving.getBukkitEntity();
                    bukkitEntity.setVelocity(new Vector((this.entity.locX() - bukkitEntity.getLocation().getX()) / 48.0, (this.entity.locY() - bukkitEntity.getLocation().getY()) / 48.0, (this.entity.locZ() - bukkitEntity.getLocation().getZ()) / 48.0));
                }
            }

            super.e();
        }
    }

    static class EntitySelectorGuardianTargetHumanSquid implements Predicate<EntityLiving> {

        private final EntityGuardian a;

        public EntitySelectorGuardianTargetHumanSquid(EntityGuardian entityguardian) {
            this.a = entityguardian;
        }

        public boolean test(@Nullable EntityLiving entityliving) {
            return (entityliving instanceof EntityHuman || entityliving instanceof EntitySquid) && entityliving.h((Entity)this.a) > 9.0D;
        }
    }
}
