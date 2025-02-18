package AnonymousRand.anonymousrand.plugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.CustomPathfinderGoalMoveFasterInCobweb;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.CustomPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.plugin.util.NmsUtil;
import AnonymousRand.anonymousrand.plugin.util.bukkitrunnables.RunnableBreakBlocks;
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

    /* Ignores line of sight and y-level for initially finding a player target and maintaining it as the target,
     * as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;

    public CustomEntityGuardianElder(World world) {
        super(EntityTypes.ELDER_GUARDIAN, world); // todo dont we need the custom stuff?
    }

    protected void initCustom() {
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `ICustomHostile`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public double getDetectionRange() { /* elder guardians have 40 block detection range */
        return 40.0;
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

                /* Random despawn distance increased to 64 blocks */
                int randomDespawnDist = this.getEntityType().e().g() + 32;
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

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Overridden Vanilla Functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initPathfinder() {
        PathfinderGoalMoveTowardsRestriction pathfindergoalmovetowardsrestriction = new PathfinderGoalMoveTowardsRestriction(this, 1.0);
        this.goalRandomStroll = new PathfinderGoalRandomStroll(this, 1.0, 80);
        /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this));
        /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this));
        this.goalSelector.a(3, new CustomEntityGuardianElder.PathfinderGoalGuardianAttack(this));
        this.goalSelector.a(4, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 40.0F));
        this.goalSelector.a(5, pathfindergoalmovetowardsrestriction);
        this.goalSelector.a(7, this.goalRandomStroll);
        this.goalRandomStroll.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        pathfindergoalmovetowardsrestriction.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityLiving.class, IGNORE_LOS, IGNORE_Y, new CustomEntityGuardianElder.EntitySelectorGuardianTargetHumanSquid(this))); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the nearest option */
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damageAmount) {
        if (!this.eO() && !damageSource.isMagic() && damageSource.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) damageSource.getEntity();

            if (!damageSource.isExplosion()) {
                player.damageEntity(DamageSource.a(this), f); /* thorns damage increased from 2 to 100% of the damage dealt */
            }
        }

        if (this.goalRandomStroll != null) {
            this.goalRandomStroll.h();
        }

        return super.damageEntity(damageSource, damageAmount);
    }

    @Override
    protected void mobTick() {
        super.mobTick();

        if ((this.ticksLived  + this.getId()) % 40 == 0) { /* applies mining fatigue every 2 seconds, but effect duration decreased to 1 minute */
            MobEffectList mobeffectlist = MobEffects.SLOWER_DIG;
            List<EntityPlayer> list = ((WorldServer)this.world).a((entityPlayer) -> NmsUtil.distSq(this, entityPlayer, true) < 2500.0 && entityPlayer.playerInteractManager.d());

            for (EntityPlayer entityPlayer : list) {
                // plays the animation every time mining fatigue happens (every second)
                entityPlayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.k, this.isSilent() ? 0.0F : 1.0F));
                entityPlayer.addEffect(new MobEffect(mobeffectlist, 1200, 2));
            }
        }
    }

    static class PathfinderGoalGuardianAttack extends PathfinderGoal { /* guardian no longer stops attack if player is too close */

        private final CustomEntityGuardianElder elderGuardian;
        private int b;
        private final boolean isElder;

        public PathfinderGoalGuardianAttack(CustomEntityGuardianElder entityGuardian) {
            this.elderGuardian = entityGuardian;
            this.isElder = true;
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        }

        @Override
        public boolean a() {
            EntityLiving attackTarget = this.elderGuardian.getGoalTarget();

            return attackTarget != null && attackTarget.isAlive();
        }

        @Override
        public void c() {
            this.b = -10;
            this.elderGuardian.getNavigation().o();
            this.elderGuardian.getControllerLook().a(this.elderGuardian.getGoalTarget(), 90.0F, 90.0F);
            this.elderGuardian.impulse = true;
        }

        @Override
        public void d() {
            this.elderGuardian.a(0);
            this.elderGuardian.setGoalTarget(null, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, false);
            this.elderGuardian.goalRandomStroll.h();
        }

        @Override
        public void e() {
            EntityLiving attackTarget = this.elderGuardian.getGoalTarget();

            this.elderGuardian.getNavigation().o();
            this.elderGuardian.getControllerLook().a(attackTarget, 90.0F, 90.0F);

            if (attackTarget != null) {
                ++this.b; /* laser no longer disengages when there is a block between guardian and player */

                if (this.b == 0) {
                    this.elderGuardian.a(this.elderGuardian.getGoalTarget().getId());
                    if (!this.elderGuardian.isSilent()) {
                        this.elderGuardian.getWorld().broadcastEntityEffect(this.elderGuardian, (byte) 21);
                    }
                } else if (this.b >= this.elderGuardian.eL()) {
                    float f = 1.0F;

                    if (this.elderGuardian.getWorld().getDifficulty() == EnumDifficulty.HARD) {
                        f += 2.0F;
                    }

                    if (this.isElder) {
                        f += 2.0F;
                    }

                    attackTarget.damageEntity(DamageSource.c(this.elderGuardian, this.elderGuardian), f);
                    attackTarget.damageEntity(DamageSource.mobAttack(this.elderGuardian), (float) this.elderGuardian.b(GenericAttributes.ATTACK_DAMAGE));
                    this.elderGuardian.setGoalTarget(null, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, false);
                } else if (this.b + 40 == this.elderGuardian.eL()) { /* 2 seconds before laser finishes firing, the elder guardian will break all blocks between it and the player */ // todo test
                    BlockIterator iterator = new BlockIterator(this.elderGuardian.getWorld().getWorld(), new Vector(this.elderGuardian.locX(), this.elderGuardian.locY(), this.elderGuardian.locZ()), new Vector(attackTarget.locX() - this.elderGuardian.locX(), attackTarget.locY() - this.elderGuardian.locY(), attackTarget.locZ() - this.elderGuardian.locZ()), 1.0, (int) Math.pow(NmsUtil.distSq(this.elderGuardian, attackTarget, false), 0.5) + 1);

                    while (iterator.hasNext()) {
                        new RunnableBreakBlocks(iterator.next().getLocation(), this.elderGuardian.getWorld().getWorld(), 1, 1, 1, 0, false).run();
                    }
                }

                if (this.b >= this.elderGuardian.eL() / 3.35 && this.elderGuardian.ticksLived % 3 == 0) { /* stronger tractor beam-like effect every 3 ticks for the latter ~70% of the laser charging period */
                    LivingEntity bukkitEntity = (LivingEntity) attackTarget.getBukkitEntity();
                    bukkitEntity.setVelocity(new Vector((this.elderGuardian.locX() - bukkitEntity.getLocation().getX()) / 20.0, (this.elderGuardian.locY() - bukkitEntity.getLocation().getY()) / 20.0, (this.elderGuardian.locZ() - bukkitEntity.getLocation().getZ()) / 20.0));
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
            return (entityLiving instanceof EntityHuman || entityLiving instanceof EntitySquid) && NmsUtil.distSq(this.a, entityLiving, true) > 9.0;
        }
    }
}
