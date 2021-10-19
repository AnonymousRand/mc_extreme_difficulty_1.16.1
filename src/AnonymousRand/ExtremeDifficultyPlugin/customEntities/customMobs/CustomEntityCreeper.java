package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.CustomEntityLightning;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderTargetCondition;
import AnonymousRand.ExtremeDifficultyPlugin.util.CoordsFromHypotenuse;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;
import java.util.Random;

public class CustomEntityCreeper extends EntityCreeper {

    public CustomEntityCreeper(World world, int fuse) {
        super(EntityTypes.CREEPER, world);

        this.maxFuseTicks = fuse;
    }

    @Override
    protected void initPathfinder() { /**creeper is no longer scared of cats and ocelots*/
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalSwell(this));
        this.goalSelector.a(4, new PathfinderGoalMeleeAttack(this, 1.0D, false));
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 0.8D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, true)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
        this.targetSelector.a(2, new PathfinderGoalHurtByTarget(this, new Class[0]));
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (damagesource instanceof EntityDamageSource && this.getHealth() - f > 0.0 && rand.nextDouble() < 0.5) { /**creeper has a 50% chance to duplicate when hit and not killed (double fuse on new creeper)*/
            CustomEntityCreeper newCreeper = new CustomEntityCreeper(this.getWorld(), 20);
            newCreeper.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
            this.getWorld().addEntity(newCreeper, CreatureSpawnEvent.SpawnReason.NATURAL);
        }

        return super.damageEntity(damagesource, f);
    }

    @Override
    public void explode() {
        if (normalGetDistanceSq(this.getPositionVector(), this.getGoalTarget().getPositionVector()) <= 18.0) { //still only explodes within 3 blocks of player
           super.explode();
       }
    }

    public double normalGetDistanceSq(Vec3D vec3d1, Vec3D vec3dt) {
        double d0 = vec3dt.getX() - vec3d1.getX(); //explode function still takes into account y level
        double d1 = vec3dt.getY() - vec3d1.getY();
        double d2 = vec3dt.getZ() - vec3d1.getZ();

        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    protected Random rand = new Random();
    protected CoordsFromHypotenuse coordsFromHypotenuse = new CoordsFromHypotenuse();

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived == 10) { /**creepers have +40% movement speed, 28 block detection range but only 13.4 health*/
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.35);
            this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(28);
            this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(13.4);

            if (rand.nextDouble() < 0.05) { /**upon spawning, creepers have a 5% chance to teleport to 10-15 blocks of player*/
                this.initiateTeleport(rand.nextDouble() * 5.0 + 10.0, false);
            }
        }

        if (this.getGoalTarget() instanceof EntityPlayer) { //todo: change probability
            if (Math.abs(this.getGoalTarget().locY() - this.locY()) > 2.5 && rand.nextDouble() < 0.1) { /**every tick the creeper is more than 2.5 blocks of elevation different than its target, it has a 0.05% chance to teleport near or onto the target onto a block that is within 3 y levels of the player*/
                this.initiateTeleport(rand.nextDouble() * 3.0 + 12.0, true);
            }
        }

        Location thisLoc = new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ());
        if (thisLoc.getBlock().getType() == org.bukkit.Material.COBWEB) { /**non-player mobs gain Speed 11 while in a cobweb (approx original speed)*/
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, 2, 10));
        }

        if (this.world.isRainingAt(new BlockPosition(this.locX(), this.locY(), this.locZ()))) { /**chance to summon lightning within 50 blocks of it every tick, increased chance if raining and in 40 block radius*/
            if (rand.nextDouble() < 0.0003) {
                double hypo = rand.nextDouble() * 40;
                BlockPosition pos = new BlockPosition(coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(new BlockPosition(this.locX(), this.locY(), this.locZ()),  hypo, this.locY(), 361.0));

                CustomEntityLightning lightning = new CustomEntityLightning(this.getWorld());
                lightning.setLocation(pos.getX(), this.getWorld().getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, pos).getY(), pos.getZ(), 0.0f, 0.0f);
                this.world.addEntity(lightning);
            }
        } else {
            if (rand.nextDouble() < 0.000025) {
                double hypo = rand.nextDouble() * 50;
                BlockPosition pos = new BlockPosition(coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(new BlockPosition(this.locX(), this.locY(), this.locZ()),  hypo, this.locY(), 361.0));

                CustomEntityLightning lightning = new CustomEntityLightning(this.getWorld());
                lightning.setLocation(pos.getX(), pos.getY(), pos.getZ(), 0.0f, 0.0f);
                this.world.addEntity(lightning);
            }
        }
    }

    protected void initiateTeleport(double h, boolean adjustY) {
        EntityPlayer player;

        if (!adjustY) { //default teleportation used by most other monsters
            player = this.getWorld().a(EntityPlayer.class, new CustomPathfinderTargetCondition(), this, this.locX(), this.locY(), this.locZ(), this.getBoundingBox().grow(128.0, 128.0, 128.0)); //get closes monster within 128 sphere radius of player

            if (player != null) {
                BlockPosition pos = coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(new BlockPosition(player.locX(), player.locY(), player.locZ()), h, this.locY() + 2.0, 361.0); //gets coords for a random angle (0-360) with fixed hypotenuse to teleport to (so possible teleport area is a washer-like disc around the player)
                BlockPosition pos2 = this.getWorld().getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, pos); //highest block at those coords

                if (pos2 != null && pos2.getY() < 128.0) { //teleport to highest block if there is one in that location
                    this.teleportTo(pos2);
                } else { //clear out 5 by 5 by 5 area around teleport destination before teleporting there
                    this.initiateTeleportBreakBlocks(pos);
                }
            }
        } else { //try to ensure that creeper ends up within detonating range of player
            player = (EntityPlayer)this.getGoalTarget();

            BlockPosition pos = coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(new BlockPosition(player.locX(), player.locY(), player.locZ()), h, this.locY() + 2.0, 361.0); //gets coords for a random angle (0-360) with fixed hypotenuse to teleport to (so possible teleport area is a washer-like disc around the player)
            BlockPosition pos2 = this.getWorld().getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, pos); //highest block at those coords

            if (pos2 != null) {
                if (Math.abs(this.getGoalTarget().locY() - pos2.getY()) < 3.0) { //if the teleport allows the creeper to be within 3 y levels of player
                    if (pos2.getY() < 128.0) {
                        this.maxFuseTicks = 25; //increase fuse length by 150% if teleporting very close to player
                        this.teleportTo(pos2);
                        return;
                    }
                }
            }

            //else use recursive method that checks smaller areas each time
            if (h > 6.0) {
                this.initiateTeleport(h - 2.0, true);
            } else if (h > 1.0){
                this.initiateTeleport(h - 1.0, true);
            } else { //teleport onto player if that's the only avaliable block
                this.maxFuseTicks = 25; //increase fuse length by 150% if teleporting very close to player
                this.teleportTo(new BlockPosition(player.locX(), player.locY(), player.locZ()));
            }
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
                    if (loc.getBlock().getType() != org.bukkit.Material.BEDROCK && loc.getBlock().getType() != org.bukkit.Material.END_GATEWAY && loc.getBlock().getType() != org.bukkit.Material.END_PORTAL && loc.getBlock().getType() != org.bukkit.Material.END_PORTAL_FRAME && loc.getBlock().getType() != org.bukkit.Material.NETHER_PORTAL && loc.getBlock().getType() != org.bukkit.Material.COMMAND_BLOCK  && loc.getBlock().getType() != org.bukkit.Material.COMMAND_BLOCK_MINECART && loc.getBlock().getType() != org.bukkit.Material.STRUCTURE_BLOCK && loc.getBlock().getType() != org.bukkit.Material.JIGSAW && loc.getBlock().getType() != org.bukkit.Material.BARRIER && loc.getBlock().getType() != org.bukkit.Material.SPAWNER) { //as long as it isn't one of these blocks
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
                this.world.playSound((EntityHuman) null, this.lastX, this.lastY, this.lastZ, SoundEffects.ENTITY_ENDERMAN_TELEPORT, this.getSoundCategory(), 1.0F, 1.0F);
                this.playSound(SoundEffects.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }

            return flag2;
        } else {
            return false;
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
}
