package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.AttackLevelingController;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.util.EntityFilter;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.Random;

public class CustomEntityEnderman extends EntityEnderman implements ICustomHostile, IAttackLevelingMob {

    /* Ignores y-level and line of sight for initially finding a player target and maintaining it
       as the target, as well as for retaliating against players */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;
    private boolean lookedAt;

    public CustomEntityEnderman(World world) {
        super(EntityTypes.ENDERMAN, world);
        this.initCustom();
        this.initAttackLevelingMob();
    }

    private void initCustom() {
        this.initAttributes();

        /* No longer avoids fire, lava, and water */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.a(PathType.WATER, 0.0F);

        this.lookedAt = false;
    }

    private void initAttributes() {
        /* Endermen only have 20 health */
        this.setHealth(20.0F);
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(20.0);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                      ICustomHostile                                       //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /* Endermen have 16 block detection range (24 after 12 attacks, 32 after 25 attacks) */
    public double getDetectionRange() {
        return (this.attackLevelingController == null || this.getAttacks() < 12)
                ? 16.0 : this.getAttacks() < 25 ? 24.0 : 32.0;
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.world.findNearbyPlayer(this, -1.0D);

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

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                    IAttackLevelingMob                                     //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private AttackLevelingController attackLevelingController = null;

    private void initAttackLevelingMob() {
        this.attackLevelingController = new AttackLevelingController(12, 25, 40);
    }

    public int getAttacks() {
        return this.attackLevelingController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        for (int metThreshold : this.attackLevelingController.increaseAttacks(increase)) {
            int[] attackThresholds = this.getAttacksThresholds();
            if (metThreshold == attackThresholds[0]) {
                /* After 12 attacks, endermen gain speed 1 */
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 0));
                this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, IGNORE_LOS, IGNORE_Y)); // update follow range
            } else if (metThreshold == attackThresholds[1]) {
                /* After 25 attacks, endermen get 40 max health and regen 3 */
                ((LivingEntity) this.getBukkitEntity()).setMaxHealth(40.0);
                this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));
                this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, IGNORE_LOS, IGNORE_Y)); // update follow range
            } else if (metThreshold == attackThresholds[2]) {
                /* After 40 attacks, endermen summon 5 endermites */
                new SpawnEntity(this.world, new CustomEntityEndermite(this.world), 5, null, null, this, false, true);
            }
        }
    }

    public int[] getAttacksThresholds() {
        return this.attackLevelingController.getAttacksThresholds();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                  Other custom functions                                   //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public boolean hasBeenLookedAt() {
        return this.lookedAt;
    }

    public void setLookedAt(boolean lookedAt) {
        this.lookedAt = lookedAt;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                               Overridden vanilla functions                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void initPathfinder() {
        /* Endermen no longer target endermites, avoid water, or stop if stared at */
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this));                                                /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));                                                   /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(2, new CustomPathfinderGoalMeleeAttack(this, 1.0D));                                              /* Continues attacking regardless of y-level and line of sight (the old goal stopped the mob from attacking even if it still has a target) */
        this.goalSelector.a(3, new PathfinderGoalFloat(this));
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.goalSelector.a(10, new PathfinderGoalPlaceBlock(this));
        this.goalSelector.a(11, new PathfinderGoalPickUpBlock(this));
        this.targetSelector.a(0, new CustomEntityEnderman.PathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Always aggros instead of only when angry, ignores invis/skulls to initially find a target or maintain it as the target, and periodically retargets the closest option */
        this.targetSelector.a(2, new CustomEntityEnderman.PathfinderGoalPlayerWhoLookedAtTarget(this));
        this.targetSelector.a(3, new CustomEntityEnderman.PathfinderGoalHurtByTarget(this, IGNORE_LOS, IGNORE_Y));            /* Always retaliates against players and teleports to them if they are out of range/do not have line of sight, but doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */
        this.targetSelector.a(5, new PathfinderGoalUniversalAngerReset<>(this, false));
    }

    @Override
    /* Endermen are no longer damaged by water */
    public boolean dN() {
        return false;
    }

    @Override
    /* Endermen no longer burn and teleport away from lava */
    protected void burnFromLava() {}

    // Overrides private g() (shouldAttackPlayer()); name change because not used elsewhere
    private boolean validPlayerIsLooking(EntityPlayer player) {
        if (player.isSpectator() || player.abilities.isInvulnerable) {
            return false;
        }

        /* Carved pumpkins no longer work */
        Vec3D playerLookDirection = player.f(1.0F).d();
        Vec3D directionToPlayer = new Vec3D(this.locX() - player.locX(), this.getHeadY() - player.getHeadY(),
                this.locZ() - player.locZ());
        double directionToPlayerMagnitude = directionToPlayer.f();
        directionToPlayer = directionToPlayer.d(); // normalize

        double dotProd = playerLookDirection.b(directionToPlayer);
        /* Endermen no longer need line of sight to aggro by sight (i.e. can be aggroed through blocks) */
        return dotProd > 1.0D - 0.025D / directionToPlayerMagnitude;
    }

    @Override // teleportRandomly()
    protected boolean eM() {
        if (!this.world.isClientSide && this.isAlive()) {
            /* Random teleportation range decreased to 10 blocks in each direction so that if it somehow teleports away it is likely still in range of the player */
            double x = this.locX() + (random.nextDouble() - 0.5D) * 20.0D;
            double y = this.locY() + random.nextInt(10);
            double z = this.locZ() + (random.nextDouble() - 0.5D) * 20.0D;

            boolean success = this.teleportTo(x, y, z);
            return success;
        } else {
            return false;
        }
    }

    // Overrides private o() (teleportTo())
    private boolean teleportTo(double x, double y, double z) {
        // code merged from a() (attemptTeleport())
        BlockPosition targetBlockPosition = new BlockPosition(x, y, z);
        double currX = this.locX();
        double currY = this.locY();
        double currZ = this.locZ();
        World world = this.world;

        if (world.isLoaded(targetBlockPosition)) {
            IBlockData targetBlockData = world.getType(targetBlockPosition);

            /* Endermen can teleport onto fluids, waterlogged blocks, and non-solid blocks */
            while (targetBlockData.getMaterial() == Material.AIR && targetBlockPosition.getY() > 0) {
                targetBlockPosition = targetBlockPosition.down();
                targetBlockData = world.getType(targetBlockPosition);
            }

            if (targetBlockData.getMaterial() != Material.AIR) {
                this.enderTeleportTo(targetBlockPosition.getX(), targetBlockPosition.getY() + 1.0, targetBlockPosition.getZ());
                if (world.getCubes(this)) { // check for collisions; if passed, then teleport is successful
                    world.broadcastEntityEffect(this, (byte) 46);
                    this.getNavigation().o();

                    if (!this.isSilent()) {
                        this.world.playSound(null, this.lastX, this.lastY, this.lastZ,
                                SoundEffects.ENTITY_ENDERMAN_TELEPORT, this.getSoundCategory(), 1.0F, 1.0F);
                        this.playSound(SoundEffects.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                    }

                    /* Endermen have a 10% chance to summon an endermite where it teleports to */
                    if (random.nextDouble() < 0.1) {
                        new SpawnEntity(this.world, new CustomEntityEndermite(this.world), 1, null, null, this, false, true);
                    }

                    return true;
                }
            }
        }

        // if teleport attempt failed
        this.enderTeleportTo(currX, currY, currZ);
        return false;
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damageAmount) {
        if (this.isInvulnerable(damageSource)) {
            return false;
        } else if (damageSource instanceof EntityDamageSourceIndirect) {
            /* Endermen no longer teleport away from projectiles */
            return false;
        } else {
            // only call super.damageEntity() on player damage, otherwise endermen might still teleport away from explosions etc. whose damage was canceled by listener
            if (!(damageSource.getEntity() instanceof EntityPlayer)) {
                return false;
            }

            boolean wasDamageTaken = super.damageEntity(damageSource, damageAmount);
            if (wasDamageTaken && this.isAlive()) {
                /* After 40 attacks, endermen summon an endermite when hit and not killed */
                if (this.getAttacks() >= 40) {
                    new SpawnEntity(this.world, new CustomEntityEndermite(this.world), 1, null, null, this, false, true);
                }
            }

            return wasDamageTaken;
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getGoalTarget() != null) {
            EntityLiving target = this.getGoalTarget();

            /* Endermen have a chance to teleport to target if target do not have line of sight or is on a different y-level.
               In the latter case, there is a higher chance the closer horizontally the player is (and thus the more likely they are towering). */
            if ((!this.getEntitySenses().a(target) && this.random.nextDouble() < 0.01)
                    || (Math.abs(this.locY() - target.locY()) >= 2.0 && this.random.nextDouble()
                    < 0.05 / NMSUtil.distSq(this, target, true))) {
                this.teleportTo(target.locX(), target.locY(), target.locZ());
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                Mob-specific goals/classes                                 //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    // e() (tick()) logic has been made more straightfoward
    static class PathfinderGoalPlayerWhoLookedAtTarget
            extends CustomPathfinderGoalNearestAttackableTarget<CustomEntityEnderman, EntityPlayer> {

        private final CustomEntityEnderman enderman;

        public PathfinderGoalPlayerWhoLookedAtTarget(CustomEntityEnderman enderman) {
            super(enderman, EntityPlayer.class, CustomEntityEnderman.IGNORE_LOS, CustomEntityEnderman.IGNORE_Y);
            this.targetCondition = new EntityFilter(64.0, true, true, (entityLiving) ->
                    enderman.validPlayerIsLooking((EntityPlayer) entityLiving)); // endermen can still be aggroed from up to 64 blocks away
            this.enderman = enderman;
        }

        @Override
        // Note that this function is somehow not run if enderman already has a target, i.e. look-aggro does not take precendence over other aggro. This is a feature, not a bug.
        public boolean a() {
            /* Endermen can be aggroed regardless of y-level and line of sight */
            this.potentialTarget = this.findClosestPotentialTarget();
            return this.potentialTarget != null;
        }

        @Override
        public boolean b() {
            return this.potentialTarget != null && !this.potentialTarget.isSpectator()
                    && !this.potentialTarget.abilities.isInvulnerable;
        }

        @Override
        public void c() {
            this.enderman.setLookedAt(true);
            super.c();
        }

        @Override
        public void d() {
            this.enderman.setLookedAt(false);
            this.potentialTarget = null;
            super.d();
        }
    }

    static class PathfinderGoalNearestAttackableTarget<S extends CustomEntityEnderman & ICustomHostile, T extends EntityLiving>
            extends CustomPathfinderGoalNearestAttackableTarget<S, T> {

        private final CustomEntityEnderman enderman;

        public PathfinderGoalNearestAttackableTarget(S enderman, Class<T> targetClass) {
            super(enderman, targetClass, CustomEntityEnderman.IGNORE_LOS, CustomEntityEnderman.IGNORE_Y);
            this.enderman = enderman;
        }

        @Override
        public void d() {
            /* Endermen will not deaggro a player who looked at them if they go out of range */
            if (!this.enderman.hasBeenLookedAt()) {
                this.e.setGoalTarget(null, EntityTargetEvent.TargetReason.FORGOT_TARGET, true);
            }

            this.g = null;
        }
    }

    static class PathfinderGoalHurtByTarget extends CustomPathfinderGoalHurtByTarget {

        private final CustomEntityEnderman enderman;

        public PathfinderGoalHurtByTarget(
                CustomEntityEnderman enderman,
                boolean ignoreLOS,
                boolean ignoreY,
                Class<?>... reinforcementClasses) {
            super(enderman, ignoreLOS, ignoreY, reinforcementClasses);
            this.enderman = enderman;
        }

        @Override
        public void d() {
            /* Endermen will not deaggro a player who looked at them if they hit the enderman but then went out of range */
            if (!this.enderman.hasBeenLookedAt()) {
                this.e.setGoalTarget(null, EntityTargetEvent.TargetReason.FORGOT_TARGET, true);
            }

            this.g = null;
        }
    }

    static class PathfinderGoalPickUpBlock extends PathfinderGoal {

        private final CustomEntityEnderman enderman;

        public PathfinderGoalPickUpBlock(CustomEntityEnderman enderman) {
            this.enderman = enderman;
        }

        @Override
        /* Endermen pick up blocks four times as frequently */
        public boolean a() {
            return this.enderman.getCarried() == null && this.enderman.getWorld().getGameRules().getBoolean(GameRules.MOB_GRIEFING)
                    && this.enderman.getRandom().nextInt(5) == 0;
        }

        @Override
        public void e() {
            Random random = this.enderman.getRandom();
            World world = this.enderman.getWorld();

            int x = MathHelper.floor(this.enderman.locX() - 2.0D + random.nextDouble() * 4.0D);
            int y = MathHelper.floor(this.enderman.locY() + random.nextDouble() * 3.0D);
            int z = MathHelper.floor(this.enderman.locZ() - 2.0D + random.nextDouble() * 4.0D);
            BlockPosition targetBlockPosition = new BlockPosition(x, y, z);
            IBlockData targetBlockData = world.getType(targetBlockPosition);
            Block targetBlock = targetBlockData.getBlock();

            Vec3D idk1 = new Vec3D((double) MathHelper.floor(this.enderman.locX()) + 0.5D, (double) y + 0.5D,
                    (double) MathHelper.floor(this.enderman.locZ()) + 0.5D);
            Vec3D idk2 = new Vec3D((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D);
            MovingObjectPositionBlock movingObjectPositionBlock = world.rayTrace(new RayTrace(idk1, idk2,
                    RayTrace.BlockCollisionOption.OUTLINE, RayTrace.FluidCollisionOption.NONE, this.enderman));

            if (movingObjectPositionBlock.getBlockPosition().equals(targetBlockPosition)
                    && targetBlock.a(TagsBlock.ENDERMAN_HOLDABLE)) {
                this.enderman.setCarried(targetBlockData);
                world.a(targetBlockPosition, false);
            }
        }
    }

    static class PathfinderGoalPlaceBlock extends PathfinderGoal {

        private final CustomEntityEnderman enderman;

        public PathfinderGoalPlaceBlock(CustomEntityEnderman enderman) {
            this.enderman = enderman;
        }

        @Override
        /* Endermen place down blocks four times as frequently */
        public boolean a() {
            return this.enderman.getCarried() != null && this.enderman.getWorld().getGameRules().getBoolean(GameRules.MOB_GRIEFING)
                    && this.enderman.getRandom().nextInt(500) == 0;
        }

        @Override
        public void e() {
            Random random = this.enderman.getRandom();
            World world = this.enderman.getWorld();

            int x = MathHelper.floor(this.enderman.locX() - 1.0D + random.nextDouble() * 2.0D);
            int y = MathHelper.floor(this.enderman.locY() + random.nextDouble() * 2.0D);
            int z = MathHelper.floor(this.enderman.locZ() - 1.0D + random.nextDouble() * 2.0D);
            BlockPosition targetBlockPosition = new BlockPosition(x, y, z);
            IBlockData targetBlockData = world.getType(targetBlockPosition);
            BlockPosition targetBlockPositionBelow = targetBlockPosition.down();
            IBlockData targetBlockDataBelow = world.getType(targetBlockPositionBelow);
            IBlockData carriedBlockData = this.enderman.getCarried();

            if (carriedBlockData != null && this.canPlace(world, targetBlockPosition, targetBlockData,
                    targetBlockDataBelow, targetBlockPositionBelow, carriedBlockData)) {
                world.setTypeAndData(targetBlockPosition, carriedBlockData, 3);
                this.enderman.setCarried(null);
            }
        }

        private boolean canPlace(IWorldReader iWorldReader, BlockPosition targetBlockPosition, IBlockData targetBlockData,
                IBlockData targetBlockDataBelow, BlockPosition targetBlockPositionBelow, IBlockData carriedBlockData) {
            return targetBlockData.isAir() && !targetBlockDataBelow.isAir()
                    && targetBlockDataBelow.r(iWorldReader, targetBlockPositionBelow)
                    && carriedBlockData.canPlace(iWorldReader, targetBlockPosition);
        }
    }
}
