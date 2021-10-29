package AnonymousRand.ExtremeDifficultyPlugin.customGoals;

import AnonymousRand.ExtremeDifficultyPlugin.util.CoordsFromHypotenuse;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Random;

public class NewPathfinderGoalTeleportToPlayer extends PathfinderGoal {

    protected EntityInsentient entity;
    private final double followRange, delayBeforeStarting, chancePerTick;
    private int teleportToPlayer;
    private final CoordsFromHypotenuse coordsFromHypotenuse = new CoordsFromHypotenuse();
    private final Random rand = new Random();

    public NewPathfinderGoalTeleportToPlayer(EntityInsentient entity, double initialFollowRange, double delayBeforeStarting, double chancePerTick) {
        this.entity = entity;
        this.followRange = initialFollowRange;
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
        return this.entity.getGoalTarget() == null;
    }

    @Override
    public void d() { //reset task
        this.teleportToPlayer = 0;
    }

    @Override
    public void e() {
        if (++this.teleportToPlayer >= this.delayBeforeStarting) {
            if (this.rand.nextDouble() < this.chancePerTick) {
                this.initiateTeleport(this.rand.nextDouble() * 13.0 + this.followRange - 2.0);
            }
        }
    }

    protected void initiateTeleport(double h) {
        double hypo = h;
        EntityPlayer player = this.entity.getWorld().a(EntityPlayer.class, new CustomPathfinderTargetCondition(), this.entity, this.entity.locX(), this.entity.locY(), this.entity.locZ(), this.entity.getBoundingBox().grow(128.0, 128.0, 128.0)); //get closest player within 128 sphere radius of this.entity

        if (player != null) {
            BlockPosition pos = coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(new BlockPosition(player.locX(), player.locY(), player.locZ()), hypo, this.entity.locY() + 2.0, 361.0); //gets coords for a random angle (0-360) with fixed hypotenuse to teleport to (so possible teleport area is a washer-like disc around the player)
            BlockPosition pos2 = this.entity.getWorld().getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, pos); //highest block at those coords

            if (pos2 != null && pos2.getY() < 128.0) { //teleport to highest block if there is one in that location
                this.teleportTo(pos2);
            } else { //clear out 5 by 5 by 5 area around teleport destination before teleporting there
                this.initiateTeleportBreakBlocks(pos);
            }

            this.teleportToPlayer = 0;
        }
    }

    protected void initiateTeleportBreakBlocks(BlockPosition pos) {
        Location loc = new Location (this.entity.getWorld().getWorld(), pos.getX(), pos.getY(), pos.getZ());

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

        while (blockposition_mutableblockposition.getY() > 0 && !this.entity.world.getType(blockposition_mutableblockposition).getMaterial().isSolid()) {
            blockposition_mutableblockposition.c(EnumDirection.DOWN);
        }

        IBlockData iblockdata = this.entity.world.getType(blockposition_mutableblockposition);

        if (iblockdata.getMaterial().isSolid()) {
            boolean flag2 = this.teleportHelper(pos.getX(), pos.getY(), pos.getZ(), true);

            if (flag2 && !this.entity.isSilent()) {
                this.entity.world.playSound((EntityHuman)null, this.entity.lastX, this.entity.lastY, this.entity.lastZ, SoundEffects.ENTITY_ENDERMAN_TELEPORT, this.entity.getSoundCategory(), 1.0F, 1.0F);
                this.entity.playSound(SoundEffects.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }

            return flag2;
        } else {
            return false;
        }
    }

    protected boolean teleportHelper(double d0, double d1, double d2, boolean flag) { //called a() in original living entity/whatever class
        double d3 = this.entity.locX();
        double d4 = this.entity.locY();
        double d5 = this.entity.locZ();
        double d6 = d1;
        boolean flag1 = false;
        BlockPosition blockposition = new BlockPosition(d0, d1, d2);
        World world = this.entity.world;

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
                this.entity.enderTeleportTo(d0, d6, d2);
                if (world.getCubes(this.entity)) { /**can teleport onto fluids*/
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
                ((EntityCreature)this.entity).getNavigation().o();
            }

            return true;
        }
    }
}
