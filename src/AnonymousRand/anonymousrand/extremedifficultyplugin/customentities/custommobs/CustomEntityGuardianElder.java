package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalCobwebMoveFaster;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableBreakBlocks;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class CustomEntityGuardianElder extends EntityGuardianElder implements ICommonCustomMethods {

    public CustomEntityGuardianElder(World world) {
        super(EntityTypes.ELDER_GUARDIAN, world);
        this.a(PathType.LAVA, 0.0F); /**no longer avoids lava*/
        this.a(PathType.DAMAGE_FIRE, 0.0F); /**no longer avoids fire*/
    }

    @Override
    public void initPathfinder() {
        PathfinderGoalMoveTowardsRestriction pathfindergoalmovetowardsrestriction = new PathfinderGoalMoveTowardsRestriction(this, 1.0D);
        this.goalRandomStroll = new PathfinderGoalRandomStroll(this, 1.0D, 80);
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(3, new CustomEntityGuardianElder.CustomPathfinderGoalGuardianAttack(this));
        this.goalSelector.a(4, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 40.0F));
        this.goalSelector.a(5, pathfindergoalmovetowardsrestriction);
        this.goalSelector.a(7, this.goalRandomStroll);
        this.goalRandomStroll.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        pathfindergoalmovetowardsrestriction.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityLiving.class, 10, false, false, new CustomEntityGuardianElder.EntitySelectorGuardianTargetHumanSquid(this))); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (!this.eO() && !damagesource.isMagic() && damagesource.j() instanceof EntityLiving) {
            EntityLiving entityliving = (EntityLiving) damagesource.j();

            if (!damagesource.isExplosion()) {
                entityliving.damageEntity(DamageSource.a(this), 6.0F); /**thorns damage increased from 2 to 6*/
            }
        }

        if (this.goalRandomStroll != null) {
            this.goalRandomStroll.h();
        }

        return super.damageEntity(damagesource, f);
    }

    @Override
    protected void mobTick() {
        super.mobTick();

        if ((this.ticksLived  + this.getId()) % 20 == 0) { /**applies mining fatigue every second, but effect duration decreased to 150 seconds*/
            MobEffectList mobeffectlist = MobEffects.SLOWER_DIG;
            List<EntityPlayer> list = ((WorldServer)this.getWorld()).a((entityplayer) -> {
                return this.h((Entity)entityplayer) < 2500.0D && entityplayer.playerInteractManager.d();
            });

            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                EntityPlayer entityplayer = (EntityPlayer)iterator.next();

                //plays the animation every time mining fatigue happens (every second)
                entityplayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.k, this.isSilent() ? 0.0F : 1.0F));
                entityplayer.addEffect(new MobEffect(mobeffectlist, 3000, 2));
            }
        }
    }

    public double getFollowRange() { /**elder guardians have 40 block detection range (setting attribute doesn't work)*/
        return 40.0;
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

                int k = this.getEntityType().e().g() + 28; /**random despawn distance increased to 60 blocks*/
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

    static class CustomPathfinderGoalGuardianAttack extends PathfinderGoal { /**guardian no longer stops attacking if player is too close*/

        private final CustomEntityGuardianElder entity;
        private int b;
        private final boolean isElder;

        public CustomPathfinderGoalGuardianAttack(CustomEntityGuardianElder entityguardian) {
            this.entity = entityguardian;
            this.isElder = true;
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
                } else if (this.b + 40 == this.entity.eL()) { /**2 seconds before laser finishes firing, the elder guardian will break all blocks between it and the player*/
                    BlockIterator iterator = new BlockIterator(this.entity.getWorld().getWorld(), new Vector(this.entity.locX(), this.entity.locY(), this.entity.locZ()), new Vector(entityliving.locX() - this.entity.locX(), entityliving.locY() - this.entity.locY(), entityliving.locZ() - this.entity.locZ()), 1.0, (int)Math.pow(this.entity.getNormalDistanceSq(this.entity.getPositionVector(), entityliving.getPositionVector()), 0.5) + 1);

                    while (iterator.hasNext()) {
                        new RunnableBreakBlocks(iterator.next().getLocation(), this.entity.getWorld().getWorld(), 1, 1, 1, 0, false).run();
                    }
                }

                if (this.b >= this.entity.eL() / 3.35 && this.entity.ticksLived % 3 == 0) { /**stronger tractor beam-like effect every 3 ticks for the latter ~70% of the laser charging period*/
                    LivingEntity bukkitEntity = (LivingEntity)entityliving.getBukkitEntity();
                    bukkitEntity.setVelocity(new Vector((this.entity.locX() - bukkitEntity.getLocation().getX()) / 20.0, (this.entity.locY() - bukkitEntity.getLocation().getY()) / 20.0, (this.entity.locZ() - bukkitEntity.getLocation().getZ()) / 20.0));
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
