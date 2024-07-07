package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.AttackLevelingController;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityTargetEvent;

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
        /* Endermen no longer target endermites, avoid water, or stop if stared at */ // todo test no staring stop
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this));                                 /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));                                    /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(2, new CustomPathfinderGoalMeleeAttack(this, 1.0D));                               /* Continues attacking regardless of y-level and line of sight (the old goal stopped the mob from attacking even if it had already recognized a target via CustomNearestAttackableTarget) */
        this.goalSelector.a(3, new PathfinderGoalFloat(this));
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.goalSelector.a(10, new CustomEntityEnderman.PathfinderGoalEndermanPlaceBlock(this));
        this.goalSelector.a(11, new CustomEntityEnderman.PathfinderGoalEndermanPickupBlock(this));
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Doesn't take into account y-level or line of sight to aggro a target, and always aggros instead of only when angry */
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
    /* Endermen no longer burn and teleport away from lava */ // todo test
    protected void burnFromLava() {}

    protected boolean shouldAttackPlayer(EntityHuman human) {
        /* Carved pumpkins no longer work */
        Vec3D vec3d = human.f(1.0F).d();
        Vec3D vec3d1 = new Vec3D(this.locX() - human.locX(), this.getHeadY() - human.getHeadY(), this.locZ() - human.locZ());
        double d0 = vec3d1.f();

        vec3d1 = vec3d1.d();
        double d1 = vec3d.b(vec3d1);

        /* Endermen no longer need line of sight to aggro by sight (i.e. can be aggroed through blocks) */ // todo test
        return d1 > 1.0D - 0.025D / d0;
    }

    @Override // teleportRandomly()
    protected boolean eM() {
        if (!this.getWorld().s_() && this.isAlive()) {
            /* Random teleportation range decreased to 10 blocks so that if it somehow teleports away it is likely still in range of the player */
            double d0 = this.locX() + (random.nextDouble() - 0.5D) * 20.0D;
            double d1 = this.locY() + (double) (random.nextInt(64) - 32);
            double d2 = this.locZ() + (random.nextDouble() - 0.5D) * 24.0D;

            return this.o(d0, d1, d2);
        } else {
            return false;
        }
    }

    // override private teleportToEntity()
    protected boolean a(Entity entity) {
        Vec3D vec3d = new Vec3D(this.locX() - entity.locX(), this.e(0.5D) - entity.getHeadY(), this.locZ() - entity.locZ());

        vec3d = vec3d.d();
        double d1 = this.locX() + (random.nextDouble() - 0.5D) * 8.0D - vec3d.x * 16.0D;
        double d2 = this.locY() + (double) (random.nextInt(16) - 8) - vec3d.y * 16.0D;
        double d3 = this.locZ() + (random.nextDouble() - 0.5D) * 8.0D - vec3d.z * 16.0D;

        return this.o(d1, d2, d3);
    }

    // override private teleportTo()
    protected boolean o(double d0, double d1, double d2) {
        BlockPosition.MutableBlockPosition mutableblockPosition = new BlockPosition.MutableBlockPosition(d0, d1, d2);
        while (mutableblockPosition.getY() > 0 && !this.getWorld().getType(mutableblockPosition).getMaterial().isSolid()) {
            mutableblockPosition.c(EnumDirection.DOWN);
        }

        /* Endermen can now teleport onto fluids and non-solid blocks */ // todo test, difference between this check and in a()?
        boolean flag2 = this.a(d0, d1, d2, true);
        if (flag2 && !this.isSilent()) {
            this.getWorld().playSound(null, this.lastX, this.lastY, this.lastZ, SoundEffects.ENTITY_ENDERMAN_TELEPORT,
                    this.getSoundCategory(), 1.0F, 1.0F);
            this.playSound(SoundEffects.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);

            /* Endermen have a 20% chance to summon an endermite where it teleports to */
            if (random.nextDouble() < 0.2) {
                new SpawnEntity(this.getWorld(), new CustomEntityEndermite(this.getWorld()), 1, null, null, this, false, true);
            }
        }

        return flag2;
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

            if (!this.getWorld().s_() && random.nextInt(10) != 0) {
                this.eM();
            }

            return tookDamage;
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getGoalTarget() != null) {
            // deaggros if out of range
            if (this.d(this.getGoalTarget().getPositionVector()) > Math.pow(this.getDetectionRange(), 2)) {
                this.setGoalTarget(null, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, false);
            }

            if (this.getGoalTarget() != null) {
                EntityLiving target = this.getGoalTarget();

                /* Endermen teleport to player if player is more than 12 blocks away horizontally */
                if (this.d(target.getPositionVector()) > 144.0) {
                    this.o(target.locX(), target.locY(), target.locZ());
                }

                /* Endermen teleport to player if player can't be seen (includes when the player is towering up) */ // todo test
                if (!this.getEntitySenses().a(target)) {
                    Bukkit.broadcastMessage("tp");
                    this.o(target.locX(), target.locY(), target.locZ());
                }
            }
        }
    }

    @Override
    public boolean a(double d0, double d1, double d2, boolean flag) {
        double d3 = this.locX();
        double d4 = this.locY();
        double d5 = this.locZ();
        double d6 = d1;
        boolean flag1 = false;
        BlockPosition blockPosition = new BlockPosition(d0, d1, d2);
        World world = this.getWorld();

        if (world.isLoaded(blockPosition)) {
            boolean flag2 = false;

            while (!flag2 && blockPosition.getY() > 0) {
                BlockPosition blockPosition1 = blockPosition.down();
                IBlockData iblockdata = world.getType(blockPosition1);

                if (iblockdata.getMaterial().isSolid()) {
                    flag2 = true;
                } else {
                    --d6;
                    blockPosition = blockPosition1;
                }
            }

            if (flag2) {
                this.enderTeleportTo(d0, d6, d2);
                /* Endermen can teleport onto fluids */
                if (world.getCubes(this)) {
                    flag1 = true;
                }
            }
        }

        if (!flag1) {
            this.enderTeleportTo(d3, d4, d5);
            return false;
        } else {
            if (flag) {
                world.broadcastEntityEffect(this, (byte) 46);
            }

            this.getNavigation().o();
            return true;
        }
    }

    static class PathfinderGoalPlayerWhoLookedAtTarget extends CustomPathfinderGoalNearestAttackableTarget<EntityPlayer> {

        private final CustomEntityEnderman entity;
        private EntityHuman target;
        private final CustomPathfinderTargetCondition m;
        private final CustomPathfinderTargetCondition n = (CustomPathfinderTargetCondition)
                (new CustomPathfinderTargetCondition()).c();

        public PathfinderGoalPlayerWhoLookedAtTarget(CustomEntityEnderman entityEnderman) {
            super(entityEnderman, EntityPlayer.class);
            this.entity = entityEnderman;
            this.m = (new CustomPathfinderTargetCondition()).a(this.k()).a((entityLiving)-> {
                return entityEnderman.shouldAttackPlayer((EntityHuman) entityLiving);
            });
        }

        @Override
        public boolean a() {
            this.target = this.entity.world.a(this.m, this.entity);
            return this.target != null;
        }

        @Override
        public void d() {
            this.target = null;
            super.d();
        }

        @Override
        public boolean b() {
            if (this.target != null) {
                if (!this.entity.shouldAttackPlayer(this.target)) {
                    return false;
                } else {
                    this.entity.a(this.target, 10.0F, 10.0F);
                    return true;
                }
            } else {
                return this.nearestTarget != null && this.n.a(this.entity, this.nearestTarget) ? true : super.b();
            }
        }

        @Override
        public void e() {
            this.entity.setGoalTarget(this.target, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, false); // simplified to deal with some bugs
            super.e();
        }
    }

    static class PathfinderGoalEndermanPickupBlock extends PathfinderGoal {

        private final CustomEntityEnderman entity;

        public PathfinderGoalEndermanPickupBlock(CustomEntityEnderman entityEnderman) {
            this.entity = entityEnderman;
        }

        @Override
        public boolean a() {
            return this.entity.getCarried() != null ? false : (!this.entity.world.getGameRules().getBoolean(
                    GameRules.MOB_GRIEFING)? false : this.entity.getRandom().nextInt(20) == 0);
        }

        @Override
        public void e() {
            Random random = this.entity.getRandom();
            World world = this.entity.world;
            int i = MathHelper.floor(this.entity.locX() - 2.0D + random.nextDouble() * 4.0D);
            int j = MathHelper.floor(this.entity.locY() + random.nextDouble() * 3.0D);
            int k = MathHelper.floor(this.entity.locZ() - 2.0D + random.nextDouble() * 4.0D);
            BlockPosition blockPosition = new BlockPosition(i, j, k);
            IBlockData iblockdata = world.getType(blockPosition);
            Block block = iblockdata.getBlock();
            Vec3D vec3d = new Vec3D((double) MathHelper.floor(this.entity.locX()) + 0.5D, (double) j + 0.5D,
                    (double) MathHelper.floor(this.entity.locZ()) + 0.5D);
            Vec3D vec3d1 = new Vec3D((double) i + 0.5D, (double) j + 0.5D, (double) k + 0.5D);
            MovingObjectPositionBlock movingObjectPositionBlock = world.rayTrace(new RayTrace(vec3d, vec3d1,
                    RayTrace.BlockCollisionOption.OUTLINE, RayTrace.FluidCollisionOption.NONE, this.entity));
            boolean flag = movingObjectPositionBlock.getBlockPosition().equals(blockPosition);

            if (block.a(TagsBlock.ENDERMAN_HOLDABLE) && flag) {
                this.entity.setCarried(iblockdata);
                world.a(blockPosition, false);
            }

        }
    }

    static class PathfinderGoalEndermanPlaceBlock extends PathfinderGoal {

        private final CustomEntityEnderman entity;

        public PathfinderGoalEndermanPlaceBlock(CustomEntityEnderman entityEnderman) {
            this.entity = entityEnderman;
        }

        @Override
        public boolean a() {
            return this.entity.getCarried() == null ? false : (!this.entity.world.getGameRules().getBoolean(
                    GameRules.MOB_GRIEFING) ? false : this.entity.getRandom().nextInt(2000) == 0);
        }

        @Override
        public void e() {
            Random random = this.entity.getRandom();
            World world = this.entity.world;
            int i = MathHelper.floor(this.entity.locX() - 1.0D + random.nextDouble() * 2.0D);
            int j = MathHelper.floor(this.entity.locY() + random.nextDouble() * 2.0D);
            int k = MathHelper.floor(this.entity.locZ() - 1.0D + random.nextDouble() * 2.0D);
            BlockPosition blockPosition = new BlockPosition(i, j, k);
            IBlockData iblockdata = world.getType(blockPosition);
            BlockPosition blockPosition1 = blockPosition.down();
            IBlockData iblockdata1 = world.getType(blockPosition1);
            IBlockData iblockdata2 = this.entity.getCarried();

            if (iblockdata2 != null && this.a(world, blockPosition, iblockdata2, iblockdata, iblockdata1, blockPosition1)) {
                world.setTypeAndData(blockPosition, iblockdata2, 3);
                this.entity.setCarried(null);
            }

        }

        private boolean a(IWorldReader iworldreader, BlockPosition blockPosition, IBlockData iblockdata,
                          IBlockData iblockdata1, IBlockData iblockdata2, BlockPosition blockPosition1) {
            return iblockdata1.isAir() && !iblockdata2.isAir() && iblockdata2.r(iworldreader, blockPosition1)
                    && iblockdata.canPlace(iworldreader, blockPosition);
        }
    }
}