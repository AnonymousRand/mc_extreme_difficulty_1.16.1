package AnonymousRand.anonymousrand.plugin.nms.customgoals;

import AnonymousRand.anonymousrand.plugin.util.CustomMathHelper;
import AnonymousRand.anonymousrand.plugin.util.NmsUtil;
import AnonymousRand.anonymousrand.plugin.util.Predicates;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Random;

public class CustomPathfinderGoalTeleportNearTarget extends PathfinderGoal {

    public EntityInsentient goalOwner;
    private final double detectionRange, delayBeforeStarting, chancePerTick;
    private int teleportToPlayer;
    private static final Random random = new Random();

    public CustomPathfinderGoalTeleportNearTarget(EntityInsentient goalOwner, double initialDetectionRange, double delayBeforeStarting, double chancePerTick) {
        this.goalOwner = goalOwner;
        this.detectionRange = initialDetectionRange;
        this.delayBeforeStarting = delayBeforeStarting;
        this.chancePerTick = chancePerTick;
        this.teleportToPlayer = 0;
    }

    @Override
    public boolean a() {
        return this.goalOwner.getGoalTarget() == null;
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
        EntityHuman entityHuman = NmsUtil.getNearestEntityFromList(this.goalOwner.getWorld().getPlayers(), null, this.goalOwner);

        if (entityHuman != null) {
            BlockPosition pos = CustomMathHelper.coordsFromHypotAndAngle(new BlockPosition(entityHuman.locX(), entityHuman.locY(), entityHuman.locZ()), hypo, this.goalOwner.locY() + 2.0, 361.0); // gets coords for a random angle (0-360) with fixed hypotenuse to teleport to (so possible teleport area is a washer-like disc around the player)
            BlockPosition pos2 = this.goalOwner.getWorld().getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, pos); // highest block at those coords

            if (pos2 != null && pos2.getY() < 128.0) { // teleport to highest block if there is one in that location
                this.teleportTo(pos2);
            } else { // clear out 5 by 5 by 5 area around teleport destination before teleporting there
                this.initiateTeleportBreakBlocks(pos);
            }

            this.teleportToPlayer = 0;
        }
    }

    protected void initiateTeleportBreakBlocks(BlockPosition pos) {
        Location bukkitLoc = new Location (this.goalOwner.getWorld().getWorld(), pos.getX(), pos.getY(), pos.getZ());
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
        BlockPosition.MutableBlockPosition blockPos_MutableBlockPosition = new BlockPosition.MutableBlockPosition(pos.getX(), pos.getY(), pos.getZ());

        while (blockPos_MutableBlockPosition.getY() > 0 && !this.goalOwner.getWorld().getType(blockPos_MutableBlockPosition).getMaterial().isSolid()) {
            blockPos_MutableBlockPosition.c(EnumDirection.DOWN);
        }

        IBlockData iblockdata = this.goalOwner.getWorld().getType(blockPos_MutableBlockPosition);

        if (iblockdata.getMaterial().isSolid()) {
            if (this.teleportHelper(pos.getX(), pos.getY(), pos.getZ(), true) && !this.goalOwner.isSilent()) {
                this.goalOwner.getWorld().playSound(null, this.goalOwner.lastX, this.goalOwner.lastY, this.goalOwner.lastZ, SoundEffects.ENTITY_ENDERMAN_TELEPORT, this.goalOwner.getSoundCategory(), 1.0F, 1.0F);
                this.goalOwner.playSound(SoundEffects.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
        }
    }

    protected boolean teleportHelper(double d0, double d1, double d2, boolean flag) { // called a() in original living entity/whatever class
        double d3 = this.goalOwner.locX();
        double d4 = this.goalOwner.locY();
        double d5 = this.goalOwner.locZ();
        double d6 = d1;
        boolean flag1 = false;
        BlockPosition blockPos = new BlockPosition(d0, d1, d2);
        World world = this.goalOwner.getWorld();

        if (world.isLoaded(blockPos)) {
            boolean flag2 = false;

            while (!flag2 && blockPos.getY() > 0) {
                BlockPosition blockPos1 = blockPos.down();
                IBlockData iblockdata = world.getType(blockPos1);

                if (iblockdata.getMaterial().isSolid()) {
                    flag2 = true;
                } else {
                    --d6;
                    blockPos = blockPos1;
                }
            }

            if (flag2) {
                this.goalOwner.enderTeleportTo(d0, d6, d2);
                if (world.getCubes(this.goalOwner)) { /* can teleport onto fluids */
                    flag1 = true;
                }
            }
        }

        if (!flag1) {
            this.goalOwner.enderTeleportTo(d3, d4, d5);
            return false;
        } else {
            if (flag) {
                world.broadcastEntityEffect(this.goalOwner, (byte) 46);
            }

            if (this.goalOwner instanceof EntityCreature) {
                this.goalOwner.getNavigation().o();
            }

            return true;
        }
    }
}
