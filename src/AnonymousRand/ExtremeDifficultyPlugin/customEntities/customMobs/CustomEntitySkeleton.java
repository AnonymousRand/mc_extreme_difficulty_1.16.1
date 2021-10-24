package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.CustomEntityLightning;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderTargetCondition;
import AnonymousRand.ExtremeDifficultyPlugin.util.CoordsFromHypotenuse;
import net.minecraft.server.v1_16_R1.*;
import net.minecraft.server.v1_16_R1.EntitySkeletonAbstract;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalBowShoot;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class CustomEntitySkeleton extends EntitySkeleton {

    private JavaPlugin plugin;
    private final CustomPathfinderGoalBowShoot<EntitySkeletonAbstract> b = new CustomPathfinderGoalBowShoot<>(this, 1.0D, 21, 22.0F); /**uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal)*/
    public boolean spawnExplodingArrow;
    public int attacks;
    private boolean a75, a100;

    public CustomEntitySkeleton(World world, JavaPlugin plugin) {
        super(EntityTypes.SKELETON, world);
        this.plugin = plugin;
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.BOW)); //makes sure that it has a bow
        this.teleportToPlayer = 0;
        this.spawnExplodingArrow = false;
        this.attacks = 0;
        this.a75 = false;
        this.a100 = false;
    }

    @Override
    protected void initPathfinder() { /**no longer avoids sun and wolves or targets iron golems*/
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, new Class[0]));
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
        this.targetSelector.a(3, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, false, false, EntityTurtle.bv));
    }

    @Override
    public void a(EntityLiving entityliving, float f) { //shoot
        this.attacks++;

        if (this.attacks >= 30 && this.attacks <= 55 && this.attacks % 8 == 0) { /**between these attack counts, shoot an exploding arrow every 8 shots*/
            for (int i = 0; i < 10; i++) {
                this.spawnExplodingArrow = true;
                ItemStack itemstack = this.f(this.b(ProjectileHelper.a(this, Items.BOW)));
                EntityArrow entityarrow = this.b(itemstack, f);

                double d0 = entityliving.locX() - this.locX();
                double d1 = entityliving.locY() - this.locY();
                double d2 = entityliving.locZ() - this.locZ();

                entityarrow.shoot(d0, d1, d2, 1.6F, (float)(50 - this.world.getDifficulty().a() * 4)); //more inaccuracy
                this.playSound(SoundEffects.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
                this.world.addEntity(entityarrow);
            }
        } else if (this.attacks < 40) {
            this.spawnExplodingArrow = false;

            for (int i = 0; i < 75; i++) { /**shoots 75 arrows at a time with increased inaccuracy to seem like a cone*/
                ItemStack itemstack = this.f(this.b(ProjectileHelper.a(this, Items.BOW)));
                EntityArrow entityarrow = this.b(itemstack, f);
                double d0 = entityliving.locX() - this.locX();
                double d1 = entityliving.locY() - this.locY();
                double d2 = entityliving.locZ() - this.locZ();
                double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);

                if (random.nextDouble() <= 0.02) { /**2% of arrows shot are piercing 1*/
                    entityarrow.setPierceLevel((byte)1);
                }

                if (this.attacks >= 20) { /**starting from the 20th attack, arrows are on fire and do not lose y level*/
                    entityarrow.setOnFire(50);
                    entityarrow.setNoGravity(true);
                }

                entityarrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float)(40 - this.world.getDifficulty().a() * 4));
                this.playSound(SoundEffects.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
                this.world.addEntity(entityarrow);
            }
        } else { /**if more than 40 attacks, rapidfire; if more than 55, even faster rapidfire*/
            this.spawnExplodingArrow = false;

            new ShootArrowRepeating(this, entityliving, this.attacks >= 55 ? 40 : 8, f).runTaskTimer(this.plugin, 0L, this.attacks >= 55 ? 1L : 5L); //custom repeating runnable class
        }
    }

    @Override
    public void eM() { //"re-registers" the new field "b" since reflection doesn't seem to work
        if (this.world != null && !this.world.isClientSide) {
            this.goalSelector.a((PathfinderGoal)this.b);
            ItemStack itemstack = this.b(ProjectileHelper.a(this, Items.BOW));

            if (itemstack.getItem() == Items.BOW) {
                this.goalSelector.a(4, this.b);
            }
        }
    }

    //todo: copy all from this point onwards to all applicable mobs
    private double getFollowRange() {
        return this.attacks < 75 ? 22.0 : 32.0;
    }

    protected int teleportToPlayer;
    protected CoordsFromHypotenuse coordsFromHypotenuse = new CoordsFromHypotenuse();

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 75 && !this.a75) { /**after 75 attacks, skeletons get 50 max health*/
            this.a75 = true;
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(50.0);
            this.setHealth(50.0f);
        }

        if (this.attacks == 100 && !this.a100) { /**after 100 attacks, skeletons summon an iron golem*/ //todo: custom mob?
            this.a100 = true;
            EntityIronGolem golem = new EntityIronGolem(EntityTypes.IRON_GOLEM, this.getWorld());
            golem.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
            this.getWorld().addEntity(golem, CreatureSpawnEvent.SpawnReason.NATURAL);
        }

        if (this.ticksLived == 10) { /**skeletons only have 14 health*/
            this.setHealth(14.0f);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(14.0);

            if (random.nextDouble() < 0.05) { /**skeletons have a 5% chance to spawn as a stray instead*/
                CustomEntitySkeletonStray newStray = new CustomEntitySkeletonStray(this.getWorld());
                newStray.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
                this.getWorld().addEntity(newStray, CreatureSpawnEvent.SpawnReason.NATURAL);
                this.die();
            }
        }

        if (this.ticksLived % 40 == 10) { /**skeletons have 22 block detection range (setting attribute doesn't work) (32 after 75 attacks)*/
            EntityPlayer player = this.getWorld().a(EntityPlayer.class, new CustomPathfinderTargetCondition(), this, this.locX(), this.locY(), this.locZ(), this.getBoundingBox().grow(this.getFollowRange(), 128.0, this.getFollowRange())); //get closest player within bounding box
            if (player != null && this.getGoalTarget() == null) {
                this.setGoalTarget(player);
            }
        }

        Location thisLoc = new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ());
        Location thisLoc2 = new Location(this.getWorld().getWorld(), this.locX(), this.locY() + 1.0, this.locZ());
        if (thisLoc.getBlock().getType() == org.bukkit.Material.COBWEB || thisLoc2.getBlock().getType() == org.bukkit.Material.COBWEB) { /**non-player mobs gain Speed 11 while in a cobweb (approx original speed)*/
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, 2, 10));
        }

        if (this.getGoalTarget() == null) { //does not see a target within follow range
            this.teleportToPlayer++;
        } else {
            this.teleportToPlayer = 0;
        }

        if (this.teleportToPlayer > 300) { /**has a 0.5% chance every tick to teleport to within follow_range-2 to follow_range+11 blocks of nearest player if it has not seen a player target within follow range for 15 seconds*/
            if (random.nextDouble() < 0.005) {
                this.initiateTeleport(random.nextDouble() * 13.0 + this.getFollowRange() - 2.0);
            }
        }

        if (this.world.isRainingAt(new BlockPosition(this.locX(), this.locY(), this.locZ()))) { /**chance to summon lightning within 50 blocks of it every tick, increased chance if raining and in 40 block radius*/
            if (random.nextDouble() < 0.0003) {
                double hypo = random.nextDouble() * 40;
                BlockPosition pos = new BlockPosition(coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(new BlockPosition(this.locX(), this.locY(), this.locZ()),  hypo, this.locY(), 361.0));

                CustomEntityLightning lightning = new CustomEntityLightning(this.getWorld());
                lightning.setLocation(pos.getX(), this.getWorld().getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, pos).getY(), pos.getZ(), 0.0f, 0.0f);
                this.world.addEntity(lightning);
            }
        } else {
            if (random.nextDouble() < 0.000025) {
                double hypo = random.nextDouble() * 50;
                BlockPosition pos = new BlockPosition(coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(new BlockPosition(this.locX(), this.locY(), this.locZ()),  hypo, this.locY(), 361.0));

                CustomEntityLightning lightning = new CustomEntityLightning(this.getWorld());
                lightning.setLocation(pos.getX(), pos.getY(), pos.getZ(), 0.0f, 0.0f);
                this.world.addEntity(lightning);
            }
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

    protected void initiateTeleport(double h) {
        double hypo = h;
        EntityPlayer player = this.getWorld().a(EntityPlayer.class, new CustomPathfinderTargetCondition(), this, this.locX(), this.locY(), this.locZ(), this.getBoundingBox().grow(128.0, 128.0, 128.0)); //get closest player within 128 sphere radius of this

        if (player != null) {
            BlockPosition pos = coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(new BlockPosition(player.locX(), player.locY(), player.locZ()), hypo, this.locY() + 2.0, 361.0); //gets coords for a random angle (0-360) with fixed hypotenuse to teleport to (so possible teleport area is a washer-like disc around the player)
            BlockPosition pos2 = this.getWorld().getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, pos); //highest block at those coords

            if (pos2 != null && pos2.getY() < 128.0) { //teleport to highest block if there is one in that location
                this.teleportTo(pos2);
            } else { //clear out 5 by 5 by 5 area around teleport destination before teleporting there
                this.initiateTeleportBreakBlocks(pos);
            }

            this.teleportToPlayer = 0;
        }
    }

    protected void initiateTeleportBreakBlocks(BlockPosition pos) {
        Location loc = new Location (this.getWorld().getWorld(), pos.getX(), pos.getY(), pos.getZ());

        double initX = loc.getX();
        double initY = loc.getY();
        double initZ = loc.getZ();

        for (int x = -2; x < 3; x++) {
            for (int y = -2; y < 3; y++) {
                for (int z = -2; z < 3; z++) {
                    if (loc.getBlock().getType() != org.bukkit.Material.BEDROCK && loc.getBlock().getType() != org.bukkit.Material.END_GATEWAY && loc.getBlock().getType() != org.bukkit.Material.END_PORTAL && loc.getBlock().getType() != org.bukkit.Material.END_PORTAL_FRAME && loc.getBlock().getType() != org.bukkit.Material.NETHER_PORTAL && loc.getBlock().getType() != org.bukkit.Material.COMMAND_BLOCK  && loc.getBlock().getType() != org.bukkit.Material.COMMAND_BLOCK_MINECART && loc.getBlock().getType() != org.bukkit.Material.STRUCTURE_BLOCK && loc.getBlock().getType() != org.bukkit.Material.JIGSAW && loc.getBlock().getType() != org.bukkit.Material.BARRIER && loc.getBlock().getType() != org.bukkit.Material.SPAWNER && loc.getBlock().getType() != org.bukkit.Material.COBWEB) { //as long as it isn't one of these blocks
                        loc.setX(initX + x);
                        loc.setY(initY + y);
                        loc.setZ(initZ + z);
                        loc.getBlock().setType(org.bukkit.Material.AIR);
                    }
                }
            }
        }

        this.teleportTo(pos);
    }

    protected boolean teleportTo(BlockPosition pos) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition(pos.getX(), pos.getY(), pos.getZ());

        while (blockposition_mutableblockposition.getY() > 0 && !this.world.getType(blockposition_mutableblockposition).getMaterial().isSolid()) {
            blockposition_mutableblockposition.c(EnumDirection.DOWN);
        }

        IBlockData iblockdata = this.world.getType(blockposition_mutableblockposition);

        if (iblockdata.getMaterial().isSolid()) {
            boolean flag2 = this.a(pos.getX(), pos.getY(), pos.getZ(), true);

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

    static class ShootArrowRepeating extends BukkitRunnable {

        private CustomEntitySkeleton skeleton;
        private EntityLiving target;
        private int cycles, maxCycles;
        private float f;
        private Random rand = new Random();

        public ShootArrowRepeating(CustomEntitySkeleton skeleton, EntityLiving target, int maxCycles, float f) {
            this.skeleton = skeleton;
            this.target = target;
            this.cycles = 0;
            this.maxCycles = maxCycles;
            this.f = f;
        }

        public void run() {
            if (++this.cycles > maxCycles) {
                this.cancel();
            }

            for (int i = 0; i < (skeleton.attacks < 55 ? 10 : 1); i++) {
                ItemStack itemstack = skeleton.f(skeleton.b(ProjectileHelper.a(skeleton, Items.BOW)));
                EntityArrow entityarrow = skeleton.b(itemstack, f);
                double d0 = target.locX() - skeleton.locX();
                double d1 = target.locY() - skeleton.locY();
                double d2 = target.locZ() - skeleton.locZ();

                if (rand.nextDouble() <= 0.02) { /**2% of arrows shot are piercing 1*/
                    entityarrow.setPierceLevel((byte)1);
                }

                if (this.skeleton.attacks >= 55) { /**starting from the 55th attack, arrows do not lose y level*/
                    entityarrow.setNoGravity(true);
                }

                entityarrow.setOnFire(50);

                entityarrow.shoot(d0, d1, d2, 1.6F, skeleton.attacks < 55 ? (float)(30 - skeleton.world.getDifficulty().a() * 4) : 0.0F); /**no inaccuracy after 55 attacks*/
                skeleton.playSound(SoundEffects.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (skeleton.getRandom().nextFloat() * 0.4F + 0.8F));
                skeleton.world.addEntity(entityarrow);
            }
        }
    }
}