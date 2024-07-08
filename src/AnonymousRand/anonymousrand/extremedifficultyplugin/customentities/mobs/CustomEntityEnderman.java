package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.AttackLevelingController;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import java.util.Random;

public class CustomEntityEnderman extends EntityEnderman implements ICustomHostile, IAttackLevelingMob {

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
    }

    private void initAttributes() {
        /* Endermen only have 20 health */
        this.setHealth(20.0F);
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(20.0);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                      ICustomHostile                                       //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /* Endermen have 16 block detection range (24 after 12 attacks, 32 after 25 attacks) */ // todo less? change line 262 12 block horizontally too
    public double getDetectionRange() {
        return (this.attackLevelingController == null || this.getAttacks() < 12)
                ? 16.0 : this.getAttacks() < 25 ? 24.0 : 32.0;
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
                this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); // update follow range
            } else if (metThreshold == attackThresholds[1]) {
                /* After 25 attacks, endermen get 40 max health and regen 3 */
                ((LivingEntity) this.getBukkitEntity()).setMaxHealth(40.0);
                this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));
                this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); // update follow range
            } else if (metThreshold == attackThresholds[2]) {
                /* After 40 attacks, endermen summon 5 endermites */
                new SpawnEntity(this.getWorld(), new CustomEntityEndermite(this.getWorld()), 5, null, null, this, false, true);
            }
        }
    }

    public int[] getAttacksThresholds() {
        return this.attackLevelingController.getAttacksThresholds();
    }

    @Override
    protected void initPathfinder() {
        /* Endermen no longer target endermites, avoid water, or stop if stared at */
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this));                                 /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));                                    /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(2, new CustomPathfinderGoalMeleeAttack(this, 1.0D));                               /* Continues attacking regardless of y-level and line of sight (the old goal stopped the mob from attacking even if it has a target via CustomNearestAttackableTarget) */
        this.goalSelector.a(3, new PathfinderGoalFloat(this));
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.goalSelector.a(10, new PathfinderGoalPlaceBlock(this));
        this.goalSelector.a(11, new PathfinderGoalPickUpBlock(this));
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Always aggros instead of only when angry, and doesn't take into account y-level or line of sight to aggro a target or maintain it as the target */
        this.targetSelector.a(2, new CustomEntityEnderman.PathfinderGoalPlayerWhoLookedAtTarget(this));
        this.targetSelector.a(3, new CustomPathfinderGoalHurtByTarget(this, new Class[0]));                    /* Doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */
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

    // overrides private g() (shouldAttackPlayer()); name change because not used elsewhere
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

    // based off teleportRandomly()
    private boolean teleportRandomlyOnHit() {
        if (!this.getWorld().s_() && this.isAlive()) {
            /* Random teleportation range decreased to 10 blocks so that if it somehow teleports away it is likely still in range of the player */ // todo doesnt seem right, print out distance
            double x = this.locX() + (random.nextDouble() - 0.5D) * 20.0D;
            double y = this.locY() + (double) (random.nextInt(20) - 10);
            double z = this.locZ() + (random.nextDouble() - 0.5D) * 20.0D;

            return this.teleportTo(x, y, z);
        } else {
            return false;
        }
    }

    // overrides private o() (teleportTo())
    private boolean teleportTo(double x, double y, double z) {
        BlockPosition.MutableBlockPosition mutableblockPosition = new BlockPosition.MutableBlockPosition(x, y, z);
        while (mutableblockPosition.getY() > 0 && !this.getWorld().getType(mutableblockPosition).getMaterial().isSolid()) { // todo dont need this since already in attemptteleport? or move attemptteleport logic in here
            mutableblockPosition.c(EnumDirection.DOWN);
        }

        /* Endermen can now teleport onto fluids and non-solid blocks */
        boolean isTeleportSuccessful = this.attemptTeleport(x, y, z);
        if (isTeleportSuccessful) {
            if (!this.isSilent()) {
                this.getWorld().playSound(null, this.lastX, this.lastY, this.lastZ, SoundEffects.ENTITY_ENDERMAN_TELEPORT,
                        this.getSoundCategory(), 1.0F, 1.0F);
                this.playSound(SoundEffects.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }

            /* Endermen have a 20% chance to summon an endermite where it teleports to */
            if (random.nextDouble() < 0.2) {
                new SpawnEntity(this.getWorld(), new CustomEntityEndermite(this.getWorld()), 1, null, null, this, false, true);
            }
        }

        return isTeleportSuccessful;
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damageAmount) {
        if (this.isInvulnerable(damageSource)) {
            return false;
        } else if (damageSource instanceof EntityDamageSourceIndirect) {
            /* Endermen no longer teleport away from projectiles */
            return false;
        } else {
            boolean tookDamage = super.damageEntity(damageSource, damageAmount);

            /* After 40 attacks, endermen summon an endermite when hit and not killed */
            if (tookDamage && damageSource.getEntity() instanceof EntityPlayer && !this.killed) {
                if (this.getAttacks() >= 40) {
                    new SpawnEntity(this.getWorld(), new CustomEntityEndermite(this.getWorld()), 1, null, null, this, false, true);
                }
            }

            if (!this.getWorld().isClientSide && random.nextInt(10) != 0) { // todo test do they really tp away 9 out of 10 times on damage? even in vanilla
                this.teleportRandomlyOnHit();
            }

            return tookDamage;
        }
    }

    @Override
    public void tick() {
        super.tick();
        // todo test if not deaggroing out of range is bad

        if (this.getGoalTarget() != null) {
            EntityLiving target = this.getGoalTarget();

            /* Endermen teleport to player if player is more than 12 blocks away horizontally */ // todo test
            if (this.d(target.getPositionVector()) > 144.0) {
                this.teleportTo(target.locX(), target.locY(), target.locZ());
            }

            /* Endermen teleport to player if player can't be seen (includes when the player is towering up) */ // todo test failed
            if (!this.getEntitySenses().a(target)) {
                Bukkit.broadcastMessage("tp");
                this.teleportTo(target.locX(), target.locY(), target.locZ());
            }
        }
    }

    // originally a(); can change name because not used anywhere else
    private boolean attemptTeleport(double x, double y, double z) {
        BlockPosition targetBlockPosition = new BlockPosition(x, y, z);
        double currX = this.locX();
        double currY = this.locY();
        double currZ = this.locZ();
        World world = this.getWorld();

        if (world.isLoaded(targetBlockPosition)) {
            IBlockData targetBlockData = world.getType(targetBlockPosition);
            boolean isTargetBlockSolid = targetBlockData.getMaterial().isSolid();

            while (!isTargetBlockSolid && targetBlockPosition.getY() > 0) {
                targetBlockPosition = targetBlockPosition.down();
                targetBlockData = world.getType(targetBlockPosition);

                if (targetBlockData.getMaterial().isSolid()) { // todo test if this check affects teleporting onto slabs etc
                    isTargetBlockSolid = true;
                }
            }

            if (isTargetBlockSolid) {
                this.enderTeleportTo(targetBlockPosition.getX(), targetBlockPosition.getY(), targetBlockPosition.getZ());
                /* Endermen can teleport onto fluids */
                if (world.getCubes(this)) { // getCubes() is hasNoCollisions(); this checks success of the teleport
                    world.broadcastEntityEffect(this, (byte) 46);
                    this.getNavigation().o();
                    return true;
                }
            }
        }

        // if teleport attempt failed
        this.enderTeleportTo(currX, currY, currZ);
        return false;
    }

    // e() (tick()) logic has been made more straightfoward
    static class PathfinderGoalPlayerWhoLookedAtTarget extends CustomPathfinderGoalNearestAttackableTarget<EntityPlayer> {

        private EntityPlayer playerWhoLooked;

        public PathfinderGoalPlayerWhoLookedAtTarget(CustomEntityEnderman enderman) {
            super(enderman, EntityPlayer.class, new CustomPathfinderTargetCondition().a(64.0).a((entityLiving)
                    -> enderman.validPlayerIsLooking((EntityPlayer) entityLiving))); // endermen can still be aggroed from up to 64 blocks away
        }

        @Override
        public boolean a() {
            /* Endermen can be aggroed regardless of y-level and line of sight */
            this.findPotentialTarget();
            if (this.potentialTarget != null && this.potentialTarget instanceof EntityPlayer) {
                this.playerWhoLooked = (EntityPlayer) this.potentialTarget;
                return true;
            }

            return false;
        }

        @Override
        // modified so that endermen will not deaggro a player who looked at it ever (unless they die or go out of survival)
        public boolean b() {
            return this.playerWhoLooked != null && !this.playerWhoLooked.isSpectator()
                    && !this.playerWhoLooked.abilities.isInvulnerable;
        }

        @Override
        public void d() {
            this.playerWhoLooked = null;
            super.d();
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