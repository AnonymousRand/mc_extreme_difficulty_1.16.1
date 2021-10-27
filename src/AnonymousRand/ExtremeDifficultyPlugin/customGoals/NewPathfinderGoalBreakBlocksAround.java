package AnonymousRand.ExtremeDifficultyPlugin.customGoals;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;

import java.util.Random;

public class NewPathfinderGoalBreakBlocksAround extends PathfinderGoal { //custom goal to break blocks around a mob

    protected EntityInsentient entity;
    private int delayTimer;
    private int radX, radY, radZ, yOffset;
    private boolean removeFluids;
    private final Random rand = new Random();

    public NewPathfinderGoalBreakBlocksAround(EntityInsentient entity, int delayTimer, int radX, int radY, int radZ, int yOffset, boolean removeFluids) {
        this.entity = entity;
        this.delayTimer = delayTimer;
        this.radX = radX;
        this.radY = radY;
        this.radZ = radZ;
        this.yOffset = yOffset;
        this.removeFluids = removeFluids;
    }

    @Override
    public boolean a() { //should execute
        return this.entity.getGoalTarget() != null ? !this.entity.getGoalTarget().isInvulnerable() : true;
    }

    @Override
    public boolean b() { //should continue executing
        return this.entity.getGoalTarget() != null ? !this.entity.getGoalTarget().isInvulnerable() : true;
    }

    @Override
    public void e() { //tick
        if (this.entity.ticksLived % this.delayTimer == 0) {
            int X = (int)Math.floor(this.entity.getPositionVector().getX());
            int Y = (int)Math.floor(this.entity.getPositionVector().getY()) + yOffset;
            int Z = (int)Math.floor(this.entity.getPositionVector().getZ());
            World world = this.entity.getWorld().getWorld();

            if (this.entity.getGoalTarget() != null) {
                if (this.entity.getGoalTarget().locY() < this.entity.locY()) { //move downwards if player is below entity
                    Y--;
                }
            }

            for (int x = -this.radX; x <= this.radX; x++) {
                for (int y = -this.radY; y <= this.radY; y++) {
                    for (int z = -this.radZ; z <= this.radZ; z++) {
                        int x1 = X + x, y1 = Y + y, z1 = Z + z;

                        if (world.getBlockAt(x1, y1, z1).getType() != Material.BEDROCK && world.getBlockAt(x1, y1, z1).getType() != Material.END_GATEWAY && world.getBlockAt(x1, y1, z1).getType() != Material.END_PORTAL && world.getBlockAt(x1, y1, z1).getType() != Material.END_PORTAL_FRAME && world.getBlockAt(x1, y1, z1).getType() != Material.NETHER_PORTAL && world.getBlockAt(x1, y1, z1).getType() != Material.COMMAND_BLOCK  && world.getBlockAt(x1, y1, z1).getType() != Material.COMMAND_BLOCK_MINECART && world.getBlockAt(x1, y1, z1).getType() != Material.STRUCTURE_BLOCK && world.getBlockAt(x1, y1, z1).getType() != Material.JIGSAW && world.getBlockAt(x1, y1, z1).getType() != Material.BARRIER && world.getBlockAt(x1, y1, z1).getType() != Material.SPAWNER && world.getBlockAt(x1, y1, z1).getType() != Material.COBWEB && (this.removeFluids ? true : (world.getBlockAt(x1, y1, z1).getType() != Material.WATER && world.getBlockAt(x1, y1, z1).getType() != Material.LAVA))) { //as long as it isn't one of these blocks
                            world.getBlockAt(x1, y1, z1).setType(Material.AIR);
                        } else if (world.getBlockAt(x1, y1, z1).getType() == Material.OBSIDIAN || world.getBlockAt(x1, y1, z1).getType() == Material.CRYING_OBSIDIAN || world.getBlockAt(x1, y1, z1).getType() == Material.ANCIENT_DEBRIS || world.getBlockAt(x1, y1, z1).getType() == Material.NETHERITE_BLOCK) { //50% chance to break these blocks
                            if (rand.nextDouble() < 0.5) {
                                world.getBlockAt(x1, y1, z1).setType(Material.AIR);
                            }
                        }
                    }
                }
            }
        }
    }
}
