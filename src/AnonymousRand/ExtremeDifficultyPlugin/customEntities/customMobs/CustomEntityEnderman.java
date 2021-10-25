package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalMeleeAttack;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderTargetCondition;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.NewPathfinderGoalBreakBlocksAround;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Random;

public class CustomEntityEnderman extends EntityEnderman {

    public EntityPlayer playerWhoLooked;
    public int attacks;
    private boolean a12, a50, a100;

    public CustomEntityEnderman(World world) {
        super(EntityTypes.ENDERMAN, world);
        this.playerWhoLooked = null;
        this.a(PathType.WATER, 0.0F); //no longer avoids water
        this.a(PathType.LAVA, 0.0F); //no longer avoids lava
        this.attacks = 0;
        this.a12 = false;
        this.a50 = false;
        this.a100 = false;
    }

    @Override
    protected void initPathfinder() { /**no longer targets endermites, avoids water and stops if stared at*/
        this.goalSelector.a(3, new PathfinderGoalFloat(this));
        this.goalSelector.a(1, new NewPathfinderGoalBreakBlocksAround(this, 20, 1, 1, 1, 2, false)); /**custom goal that breaks blocks around the mob periodically*/
        this.goalSelector.a(2, new CustomPathfinderGoalMeleeAttack(this, 1.0D, false));  /**uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal); this custom goal also allows the spider to continue attacking regardless of light level*/
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.goalSelector.a(10, new CustomEntityEnderman.PathfinderGoalEndermanPlaceBlock(this));
        this.goalSelector.a(11, new CustomEntityEnderman.PathfinderGoalEndermanPickupBlock(this));
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal that removes line of sight requirement but more importantly targets players regardless of if they are angry or not*/
        this.targetSelector.a(2, new CustomPathfinderGoalPlayerWhoLookedAtTarget(this));
        this.targetSelector.a(3, new PathfinderGoalHurtByTarget(this, new Class[0]));
        this.targetSelector.a(5, new PathfinderGoalUniversalAngerReset<>(this, false));
    }

    @Override
    public boolean dN() { /**no longer damaged by water*/
        return false;
    } /**no longer damaged by water*/

    @Override
    protected void burnFromLava() {} /**no longer teleports away from lava*/

    private double getFollowRange() { /**endermen have a 20 block detection range (28 after 25 attacks, 36 after 75 attacks)*/
        return this.attacks < 25 ? 20.0 : this.attacks < 75 ? 28.0 : 36.0;
    }

    protected boolean shouldAttackPlayer(EntityHuman entityhuman) {
        /**carved pumpkins no longer work*/

        Vec3D vec3d = entityhuman.f(1.0F).d();
        Vec3D vec3d1 = new Vec3D(this.locX() - entityhuman.locX(), this.getHeadY() - entityhuman.getHeadY(), this.locZ() - entityhuman.locZ());
        double d0 = vec3d1.f();

        vec3d1 = vec3d1.d();
        double d1 = vec3d.b(vec3d1);

        if (d1 > 1.0D - 0.025D / d0) {
            this.playerWhoLooked = (EntityPlayer)entityhuman;
        }

        return d1 > 1.0D - 0.025D / d0; /**removed line of sight requirement*/
    }

    @Override
    protected boolean eM() { //teleportRandomly()
        if (!this.world.s_() && this.isAlive()) {
            double d0 = this.locX() + (this.random.nextDouble() - 0.5D) * 24.0D; /**random teleportation range decreased to 12 blocks so that if it teleports away due to fire etc. it is still in range of the player*/
            double d1 = this.locY() + (double)(this.random.nextInt(64) - 32);
            double d2 = this.locZ() + (this.random.nextDouble() - 0.5D) * 24.0D;

            return this.o(d0, d1, d2);
        } else {
            return false;
        }
    }

    protected boolean a(Entity entity) { //override private teleportToEntity()
        Vec3D vec3d = new Vec3D(this.locX() - entity.locX(), this.e(0.5D) - entity.getHeadY(), this.locZ() - entity.locZ());

        vec3d = vec3d.d();
        double d0 = 16.0D;
        double d1 = this.locX() + (this.random.nextDouble() - 0.5D) * 8.0D - vec3d.x * 16.0D;
        double d2 = this.locY() + (double)(this.random.nextInt(16) - 8) - vec3d.y * 16.0D;
        double d3 = this.locZ() + (this.random.nextDouble() - 0.5D) * 8.0D - vec3d.z * 16.0D;

        return this.o(d1, d2, d3);
    }

    protected boolean o(double d0, double d1, double d2) { //override private teleportTo()
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition(d0, d1, d2);

        while (blockposition_mutableblockposition.getY() > 0 && !this.world.getType(blockposition_mutableblockposition).getMaterial().isSolid()) {
            blockposition_mutableblockposition.c(EnumDirection.DOWN);
        }

        boolean flag = true; /**can now teleport onto fluids and non-solid blocks*/
        boolean flag1 = false;

        if (flag && !flag1) {
            boolean flag2 = this.a(d0, d1, d2, true);

            if (flag2 && !this.isSilent()) {
                this.world.playSound((EntityHuman)null, this.lastX, this.lastY, this.lastZ, SoundEffects.ENTITY_ENDERMAN_TELEPORT, this.getSoundCategory(), 1.0F, 1.0F);
                this.playSound(SoundEffects.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }

            return flag2;
        } else {
            return false;
        }
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable(damagesource)) {
            return false;
        } else if (damagesource instanceof EntityDamageSourceIndirect) { /**no longer teleports away from projectiles*/
            return false;
        } else {
            boolean flag = super.damageEntity(damagesource, f);

            if (flag) {
                if (this.attacks >= 100) { /**after 100 attacks, endermen summon an endermite on damage tick*/
                    CustomEntityEndermite endermite = new CustomEntityEndermite(this.getWorld());
                    endermite.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
                    this.getWorld().addEntity(endermite, CreatureSpawnEvent.SpawnReason.NATURAL);
                }
            }

            if (!this.world.s_() && this.random.nextInt(10) != 0) {
                this.eM();
            }

            return flag;
        }
    }

    @Override
    public void die() {
        super.die();

        if (this.attacks >= 100) { /**after 100 attacks, endermen summon 8 endermites on death*/
            CustomEntityEndermite endermite;

            for (int i = 0; i < 8; i++) {
                endermite = new CustomEntityEndermite(this.getWorld());
                endermite.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
                this.getWorld().addEntity(endermite, CreatureSpawnEvent.SpawnReason.NATURAL);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 12 && !this.a12) { /**after 12 attacks, endermen gain speed 1*/
            this.a12 = true;
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 0));
        }

        if (this.attacks == 50 && !this.a50) { /**after 50 attacks, endermen gain 30 max health and regen 2*/
            this.a50 = true;
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(50.0);
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 0));
        }

        if (this.attacks == 100 && !this.a100) { /**after 100 attacks, endermen summon 5 endermites*/
            this.a100 = true;
            CustomEntityEndermite endermite;

            for (int i = 0; i < 5; i++) {
                endermite = new CustomEntityEndermite(this.getWorld());
                endermite.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
                this.getWorld().addEntity(endermite, CreatureSpawnEvent.SpawnReason.NATURAL);
            }
        }

        if (this.getGoalTarget() != null) {
            if (this.d(this.getGoalTarget().getPositionVector()) > Math.pow(this.getFollowRange(), 2) && !this.getGoalTarget().equals(this.playerWhoLooked)) { //deaggros if out of range and has not been looked at
                this.setGoalTarget(null);
            }

            if (this.getGoalTarget() != null) {
                Entity target = this.getGoalTarget();
                
                if (this.d(target.getPositionVector()) > 144.0) { /**teleports to player if player is more than 12 blocks horizontally but less than 20 blocks away*/
                    this.o(target.locX(), target.locY(), target.locZ());

                    CustomEntityEndermite endermite = new CustomEntityEndermite(this.getWorld()); /**summons an endermite where it teleports to*/
                    endermite.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
                    this.getWorld().addEntity(endermite);
                }

                if (!this.getEntitySenses().a(target)) { /**teleports to player if player can't be seen (includes when the player is towering up to avoid enderman)*/
                    this.o(target.locX(), target.locY(), target.locZ());

                    CustomEntityEndermite endermite = new CustomEntityEndermite(this.getWorld()); /**summons an endermite where it teleports to*/
                    endermite.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
                    this.getWorld().addEntity(endermite);
                }
            }
        }

        if (this.ticksLived == 10) { /**endermen only have 26 health and 2 damage*/
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(2.0);
            this.setHealth(26.0f);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(26.0);
        }

        Location thisLoc = new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ());
        Location thisLoc2 = new Location(this.getWorld().getWorld(), this.locX(), this.locY() + 1.0, this.locZ());
        Location thisLoc3 = new Location(this.getWorld().getWorld(), this.locX(), this.locY() + 2.0, this.locZ());
        if (thisLoc.getBlock().getType() == org.bukkit.Material.COBWEB || thisLoc2.getBlock().getType() == org.bukkit.Material.COBWEB || thisLoc3.getBlock().getType() == org.bukkit.Material.COBWEB) { /**non-player mobs gain Speed 11 while in a cobweb (approx original speed)*/
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

                if (d0 > (double)j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 8; /**random despawn distance increased to 40 blocks*/
                int l = k * k;

                if (this.ticksFarFromPlayer > 600 && this.random.nextInt(800) == 0 && d0 > (double)l && this.isTypeNotPersistent(d0)) {
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

    @Override
    public boolean a(double d0, double d1, double d2, boolean flag) {
        double d3 = this.locX();
        double d4 = this.locY();
        double d5 = this.locZ();
        double d6 = d1;
        boolean flag1 = false;
        BlockPosition blockposition = new BlockPosition(d0, d1, d2);
        World world = this.world;

        if (world.isLoaded(blockposition)) {
            boolean flag2 = false;

            while (!flag2 && blockposition.getY() > 0) {
                BlockPosition blockposition1 = blockposition.down();
                IBlockData iblockdata = world.getType(blockposition1);

                if (iblockdata.getMaterial().isSolid()) {
                    flag2 = true;
                } else {
                    --d6;
                    blockposition = blockposition1;
                }
            }

            if (flag2) {
                this.enderTeleportTo(d0, d6, d2);
                if (world.getCubes(this)) { /**can teleport onto fluids*/
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

            if (this instanceof EntityCreature) {
                ((EntityCreature)this).getNavigation().o();
            }

            return true;
        }
    }

    static class CustomPathfinderGoalPlayerWhoLookedAtTarget extends CustomPathfinderGoalNearestAttackableTarget<EntityHuman> {

        private final CustomEntityEnderman entity;
        private EntityHuman target;
        private int k;
        private int l;
        private final CustomPathfinderTargetCondition m;
        private final CustomPathfinderTargetCondition n = (CustomPathfinderTargetCondition)(new CustomPathfinderTargetCondition()).c();

        public CustomPathfinderGoalPlayerWhoLookedAtTarget(CustomEntityEnderman entityenderman) {
            super(entityenderman, EntityHuman.class, false);
            this.entity = entityenderman;
            this.m = (new CustomPathfinderTargetCondition()).a(this.k()).a((entityliving) -> {
                return entityenderman.shouldAttackPlayer((EntityHuman)entityliving);
            });
        }

        @Override
        public boolean a() {
            this.target = this.entity.world.a(this.m, (EntityLiving)this.entity);
            return this.target != null;
        }

        @Override
        public void c() {
            this.k = 5;
            this.l = 0;
            this.entity.eQ();
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
                    this.entity.a((Entity)this.target, 10.0F, 10.0F);
                    return true;
                }
            } else {
                return this.c != null && this.n.a(this.entity, this.c) ? true : super.b();
            }
        }

        @Override
        public void e() {
            if (this.entity.getGoalTarget() == null) {
                super.a((EntityLiving)null);
            }

            if (this.target != null) {
                if (--this.k <= 0) {
                    this.c = this.target;
                    this.target = null;
                    super.c();
                }
            } else {
                if (this.c != null && !this.entity.isPassenger()) {
                    if (this.entity.shouldAttackPlayer((EntityHuman)this.c)) {
                        //no longer teleports randomly if player is less than 4 blocks away

                        this.l = 0;
                    } else if (this.c.h((Entity)this.entity) > 256.0D && this.l++ >= 30 && this.entity.a((Entity)this.c)) {
                        this.l = 0;
                    }
                }

                super.e();
            }
        }
    }

    static class PathfinderGoalEndermanPickupBlock extends PathfinderGoal {

        private final CustomEntityEnderman entity;

        public PathfinderGoalEndermanPickupBlock(CustomEntityEnderman entityenderman) {
            this.entity = entityenderman;
        }

        @Override
        public boolean a() {
            return this.entity.getCarried() != null ? false : (!this.entity.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING) ? false : this.entity.getRandom().nextInt(20) == 0);
        }

        @Override
        public void e() {
            Random random = this.entity.getRandom();
            World world = this.entity.world;
            int i = MathHelper.floor(this.entity.locX() - 2.0D + random.nextDouble() * 4.0D);
            int j = MathHelper.floor(this.entity.locY() + random.nextDouble() * 3.0D);
            int k = MathHelper.floor(this.entity.locZ() - 2.0D + random.nextDouble() * 4.0D);
            BlockPosition blockposition = new BlockPosition(i, j, k);
            IBlockData iblockdata = world.getType(blockposition);
            Block block = iblockdata.getBlock();
            Vec3D vec3d = new Vec3D((double)MathHelper.floor(this.entity.locX()) + 0.5D, (double)j + 0.5D, (double)MathHelper.floor(this.entity.locZ()) + 0.5D);
            Vec3D vec3d1 = new Vec3D((double)i + 0.5D, (double)j + 0.5D, (double)k + 0.5D);
            MovingObjectPositionBlock movingobjectpositionblock = world.rayTrace(new RayTrace(vec3d, vec3d1, RayTrace.BlockCollisionOption.OUTLINE, RayTrace.FluidCollisionOption.NONE, this.entity));
            boolean flag = movingobjectpositionblock.getBlockPosition().equals(blockposition);

            if (block.a((Tag) TagsBlock.ENDERMAN_HOLDABLE) && flag) {
                this.entity.setCarried(iblockdata);
                world.a(blockposition, false);
            }

        }
    }

    static class PathfinderGoalEndermanPlaceBlock extends PathfinderGoal {

        private final CustomEntityEnderman entity;

        public PathfinderGoalEndermanPlaceBlock(CustomEntityEnderman entityenderman) {
            this.entity = entityenderman;
        }

        @Override
        public boolean a() {
            return this.entity.getCarried() == null ? false : (!this.entity.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING) ? false : this.entity.getRandom().nextInt(2000) == 0);
        }

        @Override
        public void e() {
            Random random = this.entity.getRandom();
            World world = this.entity.world;
            int i = MathHelper.floor(this.entity.locX() - 1.0D + random.nextDouble() * 2.0D);
            int j = MathHelper.floor(this.entity.locY() + random.nextDouble() * 2.0D);
            int k = MathHelper.floor(this.entity.locZ() - 1.0D + random.nextDouble() * 2.0D);
            BlockPosition blockposition = new BlockPosition(i, j, k);
            IBlockData iblockdata = world.getType(blockposition);
            BlockPosition blockposition1 = blockposition.down();
            IBlockData iblockdata1 = world.getType(blockposition1);
            IBlockData iblockdata2 = this.entity.getCarried();

            if (iblockdata2 != null && this.a(world, blockposition, iblockdata2, iblockdata, iblockdata1, blockposition1)) {
                world.setTypeAndData(blockposition, iblockdata2, 3);
                this.entity.setCarried((IBlockData) null);
            }

        }

        private boolean a(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata, IBlockData iblockdata1, IBlockData iblockdata2, BlockPosition blockposition1) {
            return iblockdata1.isAir() && !iblockdata2.isAir() && iblockdata2.r(iworldreader, blockposition1) && iblockdata.canPlace(iworldreader, blockposition);
        }
    }
}
