package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CustomMathHelper;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.Predicates;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Random;

public class NewPathfinderGoalTeleportNearTarget extends PathfinderGoal {

    public EntityInsentient entity;
    private final double detectionRange, delayBeforeStarting, chancePerTick;
    private int teleportToPlayer;
    private static final Random random = new Random();

    public NewPathfinderGoalTeleportNearTarget(EntityInsentient entity, double initialDetectionRange, double delayBeforeStarting, double chancePerTick) {
        this.entity = entity;
        this.detectionRange = initialDetectionRange;
        this.delayBeforeStarting = delayBeforeStarting;
        this.chancePerTick = chancePerTick;
        this.teleportToPlayer = 0;
    }

    @Override
    public boolean a() {
        return this.entity.getGoalTarget() == null;
    }

    @Override
    public boolean b() {
        return this.a();
    }

    @Override
    public void d() { // reset task
        this.teleportToPlayer = 0;
    }

    @Override
    public void e() {
        if (++this.teleportToPlayer >= this.delayBeforeStarting) {
            if (random.nextDouble() < this.chancePerTick) {
                this.initiateTeleport(random.nextDouble() * 13.0 + this.detectionRange - 2.0); // todo customizable distance?
            }
        }
    }

    protected void initiateTeleport(double hypo) {
        EntityPlayer player = this.entity.getWorld().a(EntityPlayer.class, new CustomPathfinderTargetCondition(), this.entity, this.entity.locX(), this.entity.locY(), this.entity.locZ(), this.entity.getBoundingBox().grow(128.0, 128.0, 128.0)); // get closest player within 128 sphere radius of this.entity

        if (player != null) {
            BlockPosition pos = CustomMathHelper.coordsFromHypotenuseAndAngle(new BlockPosition(player.locX(), player.locY(), player.locZ()), hypo, this.entity.locY() + 2.0, 361.0); // gets coords for a random angle (0-360) with fixed hypotenuse to teleport to (so possible teleport area is a washer-like disc around the player)
            BlockPosition pos2 = this.entity.getWorld().getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, pos); // highest block at those coords

            if (pos2 != null && pos2.getY() < 128.0) { // teleport to highest block if there is one in that location
                this.teleportTo(pos2);
            } else { // clear out 5 by 5 by 5 area around teleport destination before teleporting there
                this.initiateTeleportBreakBlocks(pos);
            }

            this.teleportToPlayer = 0;
        }
    }

    protected void initiateTeleportBreakBlocks(BlockPosition pos) {
        Location bukkitLoc = new Location (this.entity.getWorld().getWorld(), pos.getX(), pos.getY(), pos.getZ());
        Block bukkitBlock;
        org.bukkit.Material bukkitMaterial;

        double initX = bukkitLoc.getX();
        double initY = bukkitLoc.getY();
        double initZ = bukkitLoc.getZ();

        for (int x = -2; x < 3; x++) {
            for (int y = -2; y < 3; y++) {
                for (int z = -2; z < 3; z++) {
                    bukkitLoc.setX(initX + x);
                    bukkitLoc.setY(initY + y);
                    bukkitLoc.setZ(initZ + z);
                    bukkitBlock = bukkitLoc.getBlock();
                    bukkitMaterial = bukkitBlock.getType();

                    if (Predicates.blockBreakableDefault.test(bukkitMaterial) && Predicates.notBedrock.test(bukkitMaterial)) { // as long as it isn't one of these blocks
                        bukkitBlock.setType(org.bukkit.Material.AIR);
                    }
                }
            }
        }

        this.teleportTo(pos);
    }

    protected void teleportTo(BlockPosition pos) { // todo copy from enderman instead?
        BlockPosition.MutableBlockPosition blockPosition_MutableBlockPosition = new BlockPosition.MutableBlockPosition(pos.getX(), pos.getY(), pos.getZ());

        while (blockPosition_MutableBlockPosition.getY() > 0 && !this.entity.getWorld().getType(blockPosition_MutableBlockPosition).getMaterial().isSolid()) {
            blockPosition_MutableBlockPosition.c(EnumDirection.DOWN);
        }

        IBlockData iblockdata = this.entity.getWorld().getType(blockPosition_MutableBlockPosition);

        if (iblockdata.getMaterial().isSolid()) {
            if (this.teleportHelper(pos.getX(), pos.getY(), pos.getZ(), true) && !this.entity.isSilent()) {
                this.entity.getWorld().playSound(null, this.entity.lastX, this.entity.lastY, this.entity.lastZ, SoundEffects.ENTITY_ENDERMAN_TELEPORT, this.entity.getSoundCategory(), 1.0F, 1.0F);
                this.entity.playSound(SoundEffects.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
        }
    }

    protected boolean teleportHelper(double d0, double d1, double d2, boolean flag) { // called a() in original living entity/whatever class
        double d3 = this.entity.locX();
        double d4 = this.entity.locY();
        double d5 = this.entity.locZ();
        double d6 = d1;
        boolean flag1 = false;
        BlockPosition blockPosition = new BlockPosition(d0, d1, d2);
        World world = this.entity.getWorld();

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
                this.entity.enderTeleportTo(d0, d6, d2);
                if (world.getCubes(this.entity)) { /* can teleport onto fluids */
                    flag1 = true;
                }
            }
        }

        if (!flag1) {
            this.entity.enderTeleportTo(d3, d4, d5);
            return false;
        } else {
            if (flag) {
                world.broadcastEntityEffect(this.entity, (byte) 46);
            }

            if (this.entity instanceof EntityCreature) {
                this.entity.getNavigation().o();
            }

            return true;
        }
    }
}
