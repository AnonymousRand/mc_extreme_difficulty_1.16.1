package AnonymousRand.anonymousrand.plugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.plugin.ExtremeDifficultyPlugin;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.plugin.nms.customentities.projectiles.CustomEntityWitherSkull;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.attack.CustomPathfinderGoalAttackRanged;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.attackmvmt.CustomPathfinderGoalAttackMvmtRanged;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.target.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.plugin.util.NmsUtil;
import AnonymousRand.anonymousrand.plugin.util.Predicates;
import AnonymousRand.anonymousrand.plugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.plugin.util.bukkitrunnables.RunnableBreakBlocks;
import AnonymousRand.anonymousrand.plugin.util.bukkitrunnables.RunnableTornado;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.Random;

public class CustomEntityWither extends EntityWither implements ICustomHostile {

    /* Ignores line of sight and y-level for initially finding a player target and maintaining it as the target,
     * as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;
    protected boolean dash;
    private static Field bB, bC;

    public CustomEntityWither(World world) {
        super(EntityTypes.WITHER, world);
        this.setInvul(100); /* withers only take 5 seconds to explode when spawned */
        this.dash = false;
        double health = 200.0 + 60.0 * this.world.getServer().getOnlinePlayers().size(); /* withers have 60 more health per player online, and 200 starting health */
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(health);
        this.setHealth((float) health);
    }

    static {
        try {
            bB = EntityWither.class.getDeclaredField("bB");
            bB.setAccessible(true);
            bC = EntityWither.class.getDeclaredField("bC");
            bC.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initPathfinder() {
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new PathfinderGoalWitherDoNothingWhileInvulnerable());
        this.goalSelector.a(1, new CustomEntityWither.PathfinderGoalWitherDashAttack(this)); /* custom goal that allows the wither to do a bedrock-like dash attack (50% chance to occur every 30 seconds) that breaks blocks around it and does 6 damage to all nearby players */
        this.goalSelector.a(2, new CustomPathfinderGoalAttackRanged<>(this, 5)); /* main head shoots a skull every 5 ticks and uses the custom goal that attacks regardless of the y-level (the old goal stopped the mob from attack even if the mob has already recognized a target via CustomNearestAttackableTarget goal) */
        this.goalSelector.a(2, new CustomPathfinderGoalAttackMvmtRanged<>(this));
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 8.0F));
        this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget<>(this));               /* Always retaliates against players and teleports to them if they are out of range/do not have line of sight, but doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* only attacks players; uses the custom goal which doesn't need line of sight to start attack (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
    }

    @Override
    public void a(EntityLiving attackTarget, float distFactor) { // shoot() for main head
        this.shootSkullToEntity(0, attackTarget);
    }

    protected void shootSkullToEntity(int i, EntityLiving entityLiving) {
        this.shootSkullToCoords(i, entityLiving.locX(), entityLiving.locY() + (double) entityLiving.getHeadHeight() * 0.5, entityLiving.locZ(), false);
    }

    protected void shootSkullToEntity(int i, EntityLiving entityLiving, boolean alwaysBlue) {
        this.shootSkullToCoords(i, entityLiving.locX(), entityLiving.locY() + (double) entityLiving.getHeadHeight() * 0.5, entityLiving.locZ(), alwaysBlue);
    }

    protected void shootSkullToCoords(int i, double d0, double d1, double d2, boolean alwaysBlue) {
        if (this.dash) {
            return;
        }

        if (!this.isSilent() && random.nextDouble() < 0.05) { /* withers only play the skull shooting sound 5% of the time */
            this.world.a(null, 1024, this.getChunkCoordinates(), 0);
        }

        /* withers have higher chances to shoot blue skulls the more players are online */
        boolean flag = this.random.nextDouble() < ((-0.3 + (Math.log10(this.world.getServer().getOnlinePlayers().size() + 3.0) / (Math.log10(3.0) - 0.15))) / 150.0);
        double d3 = this.getHeadX(i);
        double d4 = this.getHeadY(i);
        double d5 = this.getHeadZ(i);
        double d6 = d0 - d3;
        double d7 = d1 - d4;
        double d8 = d2 - d5;
        CustomEntityWitherSkull entityWitherSkull = new CustomEntityWitherSkull(this.world, this, d6, d7, d8);
        entityWitherSkull.setShooter(this);

        if (flag || alwaysBlue) {
            entityWitherSkull.setCharged(true);
        }

        entityWitherSkull.setPositionRaw(d3, d4, d5);
        this.world.addEntity(entityWitherSkull);
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damageAmount) {
        if (!super.damageEntity(damageSource, damageAmount)) {
            return false;
        }

        /* Wither now breaks all usual blocks including bedrock when damaged */
        new RunnableWitherBreakBlocks(this, 1, 2, 1, 2, true).run();
        return true;
    }

    @Override
    public void setMot(double x, double y, double z) {
        if (!this.dash) { /* when wither is dashing, always move towards player */
            super.setMot(x, y, z);
        } else if (this.getGoalTarget() != null) {
            EntityLiving target = this.getGoalTarget();
            super.setMot((target.locX() - this.locX()) / 14.0, (target.locY() - this.locY()) / 14.0, (target.locZ() - this.locZ()) / 14.0);
        }
    }

    @Override
    protected void mobTick() {
        int i;

        if (this.getGoalTarget() instanceof EntityPlayer) {
            this.setHeadTarget(0, this.getGoalTarget().getId());
        } else {
            this.setHeadTarget(0, 0);
        }

        if (this.getInvul() > 0) {
            i = this.getInvul() - 1;

            if (i <= 0) {
                this.world.createExplosion(this, this.locX(), this.getHeadY(), this.locZ(), 12.0F, true, Explosion.Effect.DESTROY); /* withers explode with power 12 instead of 7 when spawned and sets blocks on fire */
                new RunnableTornado(this.world, new BlockPosition(this.locX(), this.locY(), this.locZ()), 60, 160).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 1L); /* withers summon a tornado after the spawn explosion */

                if (!this.isSilent()) {
                    this.world.b(1023, this.getChunkCoordinates(), 0);
                }
            }

            this.setInvul(i);
        } else {
            for (i = 1; i < 3; i++) { // main head: 0, right head: 1, left head: 2
                if (this.getGoalTarget() instanceof EntityPlayer) {
                    this.setHeadTarget(i, this.getGoalTarget().getId());

                    if (this.ticksLived % 5 == 0) { /* side heads also shoot a skull every 5 ticks */
                        this.setHeadTarget(i, 0);
                        this.shootSkullToEntity(i, this.getGoalTarget());
                    }
                } else {
                    this.setHeadTarget(i, 0);
                }
            }

            if (this.ticksLived % 20 == 0) {
                this.heal(1.0F);
            }

            this.bossBattle.setProgress(this.getHealth() / this.getMaxHealth());
        }
    }

    public double getDetectionRange() { /* withers have 80 block detection range */
        return 80.0;
    }

    public boolean ignoresLOS() {
        return IGNORE_LOS;
    }

    public boolean ignoresY() {
        return IGNORE_Y;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived % (1500 / Math.max(Math.floor(Math.log10(this.world.getServer().getOnlinePlayers().size()) / Math.log10(2.0)), 1)) == 0) { /* every 75 / floor(log2(numofplayers)) seconds, withers summon a wither skeleton */
            new SpawnEntity(this.world, new CustomEntitySkeletonWither(this.world), 1, null, null, this, false, true);
        }

        if (this.getGoalTarget() != null) {
            if (!this.getEntitySenses().a(this.getGoalTarget()) && random.nextDouble() < 0.05) { /* if the wither can't see its target, it shoots a blue skull on average every second */
                this.shootSkullToEntity(0, this.getGoalTarget(), true);
            }
        }

        if (!(this instanceof CustomEntityWitherMini) && this.ticksLived == 5) {
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "weather thunder"); /* wither causes thunderstorm */
        }
    }

    protected double getHeadX(int i) { // util methods
        if (i <= 0) {
            return this.locX();
        } else {
            float f = (this.aH + (float) (180 * (i - 1))) * 0.017453292F;
            float f1 = MathHelper.cos(f);

            return this.locX() + (double) f1 * 1.3;
        }
    }

    protected double getHeadY(int i) {
        return i <= 0 ? this.locY() + 3.0 : this.locY() + 2.2;
    }

    protected double getHeadZ(int i) {
        if (i <= 0) {
            return this.locZ();
        } else {
            float f = (this.aH + (float) (180 * (i - 1))) * 0.017453292F;
            float f1 = MathHelper.sin(f);

            return this.locZ() + (double) f1 * 1.3;
        }
    }

    class PathfinderGoalWitherDoNothingWhileInvulnerable extends PathfinderGoal {
        public PathfinderGoalWitherDoNothingWhileInvulnerable() {
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.JUMP, PathfinderGoal.Type.LOOK));
        }

        @Override
        public boolean a() {
            return CustomEntityWither.this.getInvul() > 0;
        }
    }

    static class PathfinderGoalWitherDashAttack extends PathfinderGoal {

        private final CustomEntityWither wither;
        private static final Random random = new Random();

        public PathfinderGoalWitherDashAttack(CustomEntityWither wither) {
            this.wither = wither;
        }

        @Override
        public boolean a() {
            return this.wither.getGoalTarget() != null && this.wither.ticksLived % 600 == 0 && random.nextDouble() < 0.5;
        }

        @Override
        public boolean b() {
            return this.a();
        }

        @Override
        public void e() {
            if (this.wither.getGoalTarget() == null) {
                return;
            }

            this.wither.dash = true;
            new RunnableWitherDashAttack(this.wither).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 1L);
        }
    }

    static class RunnableWitherDashAttack extends BukkitRunnable {

        private final CustomEntityWither wither;

        public RunnableWitherDashAttack(CustomEntityWither wither) {
            this.wither = wither;
        }

        @Override
        public void run() {
            RunnableWitherBreakBlocks newRunnableWitherBreakBlocks =  new RunnableWitherBreakBlocks(this.wither, 4, 4, 4, 4, true);

            if (this.wither.getGoalTarget() == null) {
                this.wither.dash = false;
                this.cancel();
            }

            newRunnableWitherBreakBlocks.run();

            if (this.wither.ticksLived % 2 == 0) {
                this.wither.getWorld().getEntities(this.wither, this.wither.getBoundingBox().g(8.0), entity -> entity instanceof EntityPlayer).forEach(entity -> entity.damageEntity(DamageSource.GENERIC, 10.0F));
            }

            // stop dash one we are within 2 blocks of player
            if (NmsUtil.distSq(this.wither, this.wither.getGoalTarget(), false) <= 4.0) {
                this.wither.dash = false;
                this.cancel();
            }
        }
    }

    static class RunnableWitherBreakBlocks extends RunnableBreakBlocks {
        public RunnableWitherBreakBlocks(Entity entity, int radX, int radY, int radZ, int offsetY, boolean removeFluids) {
            super(entity, radX, radY, radZ, offsetY, removeFluids);
            blockBreakable = (type) -> /* withers can now break bedrock */
                    Predicates.blockBreakableDefault.test(type)
                    && Predicates.notHardBlocks.test(type)
                    && Predicates.notFireOrWitherRose.test(type);
        }

        @Override
        public void run() {
            this.cycleCount = 0;
            this.runnableOwner.getWorld().createExplosion(this.runnableOwner, this.runnableOwner.locX(), this.runnableOwner.locY(), this.runnableOwner.locZ(), 0.0F, false, Explosion.Effect.NONE); // constant explosion noises
            super.run();
        }
    }
}
