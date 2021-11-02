package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

public class NewPathfinderGoalBreakBlocksAround extends PathfinderGoal { //custom goal to break blocks around a mob

    public EntityInsentient entity;
    protected final int delayTimer, radX, radY, radZ, yOffset;
    private final boolean removeFluids;
    protected final Random random = new Random();

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
        return this.a();
    }

    @Override
    public void e() { //tick
        if (this.entity.ticksLived % this.delayTimer == 0) {
            int X = (int)Math.floor(this.entity.getPositionVector().getX());
            int Y = (int)Math.floor(this.entity.getPositionVector().getY()) + this.yOffset;
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

                        Material type = world.getBlockAt(x1, y1, z1).getType();
                        if (type != Material.BEDROCK && type != Material.END_GATEWAY && type != Material.END_PORTAL && type != Material.END_PORTAL_FRAME && type != Material.NETHER_PORTAL && type != Material.COMMAND_BLOCK  && type != Material.COMMAND_BLOCK_MINECART && type != Material.STRUCTURE_BLOCK && type != Material.JIGSAW && type != Material.BARRIER && type != Material.SPAWNER && type != Material.COBWEB && type != Material.OBSIDIAN && type != Material.CRYING_OBSIDIAN && type != Material.ANCIENT_DEBRIS && type != Material.NETHERITE_BLOCK && (this.removeFluids ? true : (type != Material.WATER && type != Material.LAVA))) { //as long as it isn't one of these blocks
                            world.getBlockAt(x1, y1, z1).setType(Material.AIR);
                        } else if (type == Material.OBSIDIAN || type == Material.CRYING_OBSIDIAN || type == Material.ANCIENT_DEBRIS || type == Material.NETHERITE_BLOCK) { //50% chance to break these blocks
                            if (this.random.nextDouble() < 0.5) {
                                world.getBlockAt(x1, y1, z1).setType(Material.AIR);
                            }
                        }
                    }
                }
            }
        }
    }
}
