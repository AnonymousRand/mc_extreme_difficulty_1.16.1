package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalMoveFasterInCobweb;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableBreakBlocks;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class CustomEntityGuardianElder extends EntityGuardianElder implements ICustomHostile {

    public CustomEntityGuardianElder(World world) {
        super(EntityTypes.ELDER_GUARDIAN, world); // todo dont we need the custom stuff?
    }

    private void initCustom() {
        /* No longer avoids lava and fire */
        this.a(PathType.LAVA, 0.0F);
        this.a(PathType.DAMAGE_FIRE, 0.0F);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                      ICustomHostile                                       //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public double getFollowRange() { /* elder guardians have 40 block detection range */
        return 40.0;
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

                /* Random despawn distance increased to 64 blocks */
                int randomDespawnDist = this.getEntityType().e().g() + 32;
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

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                               Overridden vanilla functions                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initPathfinder() {
        PathfinderGoalMoveTowardsRestriction pathfindergoalmovetowardsrestriction = new PathfinderGoalMoveTowardsRestriction(this, 1.0D);
        this.goalRandomStroll = new PathfinderGoalRandomStroll(this, 1.0D, 80);
        /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this));
        /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));
        this.goalSelector.a(3, new CustomEntityGuardianElder.PathfinderGoalGuardianAttack(this));
        this.goalSelector.a(4, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 40.0F));
        this.goalSelector.a(5, pathfindergoalmovetowardsrestriction);
        this.goalSelector.a(7, this.goalRandomStroll);
        this.goalRandomStroll.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        pathfindergoalmovetowardsrestriction.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityLiving.class, 10, new CustomEntityGuardianElder.EntitySelectorGuardianTargetHumanSquid(this))); /* Doesn't take into account y-level or line of sight to aggro a target */
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (!this.eO() && !damagesource.isMagic() && damagesource.j() instanceof EntityLiving) {
            EntityLiving entityLiving = (EntityLiving) damagesource.j();

            if (!damagesource.isExplosion()) {
                entityLiving.damageEntity(DamageSource.a(this), f); /* thorns damage increased from 2 to 100% of the damage dealt */
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

        if ((this.ticksLived  + this.getId()) % 40 == 0) { /* applies mining fatigue every 2 seconds, but effect duration decreased to 1 minute */
            MobEffectList mobeffectlist = MobEffects.SLOWER_DIG;
            List<EntityPlayer> list = ((WorldServer)this.getWorld()).a((entityPlayer) -> this.h((Entity) entityPlayer) < 2500.0D && entityPlayer.playerInteractManager.d());

            for (EntityPlayer entityPlayer : list) {
                // plays the animation every time mining fatigue happens (every second)
                entityPlayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.k, this.isSilent() ? 0.0F : 1.0F));
                entityPlayer.addEffect(new MobEffect(mobeffectlist, 1200, 2));
            }
        }
    }

    static class PathfinderGoalGuardianAttack extends PathfinderGoal { /* guardian no longer stops attacking if player is too close */

        private final CustomEntityGuardianElder entity;
        private int b;
        private final boolean isElder;

        public PathfinderGoalGuardianAttack(CustomEntityGuardianElder entityGuardian) {
            this.entity = entityGuardian;
            this.isElder = true;
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        }

        @Override
        public boolean a() {
            EntityLiving entityLiving = this.entity.getGoalTarget();

            return entityLiving != null && entityLiving.isAlive();
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
            this.entity.setGoalTarget(null, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, false);
            this.entity.goalRandomStroll.h();
        }

        @Override
        public void e() {
            EntityLiving entityLiving = this.entity.getGoalTarget();

            this.entity.getNavigation().o();
            this.entity.getControllerLook().a(entityLiving, 90.0F, 90.0F);

            if (entityLiving != null) {
                ++this.b; /* laser no longer disengages when there is a block between guardian and player */

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

                    entityLiving.damageEntity(DamageSource.c(this.entity, this.entity), f);
                    entityLiving.damageEntity(DamageSource.mobAttack(this.entity), (float) this.entity.b(GenericAttributes.ATTACK_DAMAGE));
                    this.entity.setGoalTarget(null, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, false);
                } else if (this.b + 40 == this.entity.eL()) { /* 2 seconds before laser finishes firing, the elder guardian will break all blocks between it and the player */
                    BlockIterator iterator = new BlockIterator(this.entity.getWorld().getWorld(), new Vector(this.entity.locX(), this.entity.locY(), this.entity.locZ()), new Vector(entityLiving.locX() - this.entity.locX(), entityLiving.locY() - this.entity.locY(), entityLiving.locZ() - this.entity.locZ()), 1.0, (int) Math.pow(this.entity.get3DDistSq(this.entity.getPositionVector(), entityLiving.getPositionVector()), 0.5) + 1);

                    while (iterator.hasNext()) {
                        new RunnableBreakBlocks(iterator.next().getLocation(), this.entity.getWorld().getWorld(), 1, 1, 1, 0, false).run();
                    }
                }

                if (this.b >= this.entity.eL() / 3.35 && this.entity.ticksLived % 3 == 0) { /* stronger tractor beam-like effect every 3 ticks for the latter ~70% of the laser charging period */
                    LivingEntity bukkitEntity = (LivingEntity)entityLiving.getBukkitEntity();
                    bukkitEntity.setVelocity(new Vector((this.entity.locX() - bukkitEntity.getLocation().getX()) / 20.0, (this.entity.locY() - bukkitEntity.getLocation().getY()) / 20.0, (this.entity.locZ() - bukkitEntity.getLocation().getZ()) / 20.0));
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
