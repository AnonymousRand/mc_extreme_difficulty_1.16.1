package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import net.minecraft.server.v1_16_R1.BlockPosition;
import net.minecraft.server.v1_16_R1.Entity;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.function.Predicate;

public class RunnableBreakBlocks extends BukkitRunnable {

    protected final Entity entity;
    protected Location loc;
    protected final World bukkitWorld;
    protected final int radX, radY, radZ, yOffset;
    protected final boolean removeFluids;
    protected int cycles;
    protected final int maxCycles;
    protected int X, Y, Z;
    protected Predicate<Material> blockBreakable = (type) -> {
        return type != Material.BEDROCK && type != Material.END_GATEWAY && type != Material.END_PORTAL && type != Material.END_PORTAL_FRAME && type != Material.NETHER_PORTAL && type != Material.COMMAND_BLOCK  && type != Material.COMMAND_BLOCK_MINECART && type != Material.STRUCTURE_BLOCK && type != Material.JIGSAW && type != Material.BARRIER && type != Material.SPAWNER && type != Material.COBWEB && type != Material.OBSIDIAN && type != Material.CRYING_OBSIDIAN && type != Material.RESPAWN_ANCHOR && type != Material.ANCIENT_DEBRIS && type != Material.NETHERITE_BLOCK && type != Material.FIRE && type != Material.WITHER_ROSE;
    };
    protected static final Random random = new Random();

    public RunnableBreakBlocks(Entity entity, int radX, int radY, int radZ, int yOffset, boolean removeFluids) {
        this.entity = entity;
        this.bukkitWorld = entity.getWorld().getWorld();
        this.radX = radX;
        this.radY = radY;
        this.radZ = radZ;
        this.yOffset = yOffset;
        this.removeFluids = removeFluids;
        this.cycles = 0;
        this.maxCycles = 1;
    }

    public RunnableBreakBlocks(Entity entity, int radX, int radY, int radZ, int yOffset, boolean removeFluids, int maxCycles) {
        this.entity = entity;
        this.bukkitWorld = entity.getWorld().getWorld();
        this.radX = radX;
        this.radY = radY;
        this.radZ = radZ;
        this.yOffset = yOffset;
        this.removeFluids = removeFluids;
        this.cycles = 0;
        this.maxCycles = maxCycles;
    }

    public RunnableBreakBlocks(Location loc, World bukktWorld, int radX, int radY, int radZ, int yOffset, boolean removeFluids) {
        this.entity = null;
        this.loc = loc;
        this.bukkitWorld = bukktWorld;
        this.radX = radX;
        this.radY = radY;
        this.radZ = radZ;
        this.yOffset = yOffset;
        this.removeFluids = removeFluids;
        this.cycles = 0;
        this.maxCycles = 1;
    }

    @Override
    public void run() {
        if (++this.cycles > this.maxCycles) {
            this.cancel();
            return;
        }

        if (this.entity != null) {
            this.X = (int)Math.floor(this.entity.getPositionVector().getX());
            this.Y = (int)Math.floor(this.entity.getPositionVector().getY()) + this.yOffset;
            this.Z = (int)Math.floor(this.entity.getPositionVector().getZ());
        } else {
            this.X = (int)Math.floor(this.loc.getX());
            this.Y = (int)Math.floor(this.loc.getY()) + this.yOffset;
            this.Z = (int)Math.floor(this.loc.getZ());
        }

        if (this.entity instanceof EntityInsentient) {
            if (((EntityInsentient)this.entity).getGoalTarget() != null) {
                if (((EntityInsentient)this.entity).getGoalTarget().locY() < this.entity.locY()) { //move downwards if player is below entity
                    this.Y--;
                }
            }
        }

        for (int x = -this.radX; x <= this.radX; x++) {
            for (int y = -this.radY; y <= this.radY; y++) {
                for (int z = -this.radZ; z <= this.radZ; z++) {
                    int x1 = this.X + x, y1 = this.Y + y, z1 = this.Z + z;
                    Material type = this.bukkitWorld.getBlockAt(x1, y1, z1).getType();

                    if (this.blockBreakable.test(type) && (this.removeFluids ? true : (type != Material.WATER && type != Material.LAVA))) { //as long as it isn't one of these blocks
                        this.bukkitWorld.getBlockAt(x1, y1, z1).setType(Material.AIR);
                    } else if (type == Material.OBSIDIAN || type == Material.CRYING_OBSIDIAN || type == Material.RESPAWN_ANCHOR || type == Material.ANCIENT_DEBRIS || type == Material.NETHERITE_BLOCK) { //50% chance to break these blocks
                        if (random.nextDouble() < 0.5) {
                            this.bukkitWorld.getBlockAt(x1, y1, z1).setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }
}
