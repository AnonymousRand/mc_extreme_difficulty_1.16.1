package AnonymousRand.ExtremeDifficultyPlugin.customGoals;

import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.PathfinderGoal;
import org.bukkit.Material;
import org.bukkit.World;

public class NewPathfinderGoalBreakSpecificBlocksAround extends NewPathfinderGoalBreakBlocksAround {

    public NewPathfinderGoalBreakSpecificBlocksAround(EntityInsentient entity, int delayTimer, int radX, int radY, int radZ, int yOffset, boolean removeFluids) {
        super(entity, delayTimer, radX, radY, radZ, yOffset, removeFluids);
    }

    @Override
    public void e() {
        int X = (int)Math.floor(this.entity.getPositionVector().getX());
        int Y = (int)Math.floor(this.entity.getPositionVector().getY()) + yOffset;
        int Z = (int)Math.floor(this.entity.getPositionVector().getZ());
        World world = this.entity.getWorld().getWorld();

        for (int x = -this.radX; x <= this.radX; x++) {
            for (int y = -this.radY; y <= this.radY; y++) {
                for (int z = -this.radZ; z <= this.radZ; z++) {
                    int x1 = X + x, y1 = Y + y, z1 = Z + z;

                    if (world.getBlockAt(x1, y1, z1).getType() == Material.WARPED_FUNGUS || world.getBlockAt(x1, y1, z1).getType() == Material.POTTED_WARPED_FUNGUS || world.getBlockAt(x1, y1, z1).getType() == Material.NETHER_PORTAL || world.getBlockAt(x1, y1, z1).getType() == Material.RESPAWN_ANCHOR) {
                        world.getBlockAt(x1, y1, z1).setType(Material.AIR);
                    }
                }
            }
        }
    }
}
