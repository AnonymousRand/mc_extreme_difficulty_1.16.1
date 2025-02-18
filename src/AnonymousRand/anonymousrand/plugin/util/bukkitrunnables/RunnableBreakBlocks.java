package AnonymousRand.anonymousrand.plugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.plugin.util.Predicates;
import net.minecraft.server.v1_16_R1.Entity;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.function.Predicate;

public class RunnableBreakBlocks extends BukkitRunnable {

    protected final Entity runnableOwner;
    protected final World bukkitWorld;
    protected Location bukkitLoc;
    protected final int radX, radY, radZ, offsetY;
    protected final boolean removeFluids, immuneBlocks;
    protected int cycleCount;
    protected final int maxCycleCount;
    protected int baseX, baseY, baseZ;
    protected Block bukkitBlock;
    protected Material bukkitMaterial;
    protected Predicate<Material> blockBreakable = (type) ->
            Predicates.blockBreakableDefault.test(type)
            && Predicates.notBedrock.test(type)
            && Predicates.notHardBlocks.test(type)
            && Predicates.notFireOrWitherRose.test(type);
    protected static final Random random = new Random();

    public RunnableBreakBlocks(Entity runnableOwner, int radX, int radY, int radZ, int offsetY, boolean removeFluids) {
        this.runnableOwner = runnableOwner;
        this.bukkitWorld = runnableOwner.getWorld().getWorld();
        this.radX = radX;
        this.radY = radY;
        this.radZ = radZ;
        this.offsetY = offsetY;
        this.removeFluids = removeFluids;
        this.immuneBlocks = true;
        this.cycleCount = 0;
        this.maxCycleCount = 1;
    }

    public RunnableBreakBlocks(Entity runnableOwner, int radX, int radY, int radZ, int offsetY, boolean removeFluids, boolean immuneBlocks, int maxCycleCount) {
        this.runnableOwner = runnableOwner;
        this.bukkitWorld = runnableOwner.getWorld().getWorld();
        this.radX = radX;
        this.radY = radY;
        this.radZ = radZ;
        this.offsetY = offsetY;
        this.removeFluids = removeFluids;
        this.immuneBlocks = immuneBlocks;
        this.cycleCount = 0;
        this.maxCycleCount = maxCycleCount;
    }

    public RunnableBreakBlocks(Location bukkitLoc, World bukkitWorld, int radX, int radY, int radZ, int offsetY, boolean removeFluids) {
        this.runnableOwner = null;
        this.bukkitLoc = bukkitLoc;
        this.bukkitWorld = bukkitWorld;
        this.radX = radX;
        this.radY = radY;
        this.radZ = radZ;
        this.offsetY = offsetY;
        this.removeFluids = removeFluids;
        this.immuneBlocks = true;
        this.cycleCount = 0;
        this.maxCycleCount = 1;
    }

    @Override
    public void run() {
        this.cycleCount++;
        if (this.cycleCount > this.maxCycleCount) {
            this.cancel();
            return;
        }

        if (this.runnableOwner != null) {
            this.baseX = (int) Math.floor(this.runnableOwner.getPositionVector().getX());
            this.baseY = (int) Math.floor(this.runnableOwner.getPositionVector().getY()) + this.offsetY;
            this.baseZ = (int) Math.floor(this.runnableOwner.getPositionVector().getZ());
        } else {
            this.baseX = (int) Math.floor(this.bukkitLoc.getX());
            this.baseY = (int) Math.floor(this.bukkitLoc.getY()) + this.offsetY;
            this.baseZ = (int) Math.floor(this.bukkitLoc.getZ());
        }

        if (this.runnableOwner instanceof EntityInsentient) {
            if (((EntityInsentient) this.runnableOwner).getGoalTarget() != null) {
                if (((EntityInsentient) this.runnableOwner).getGoalTarget().locY() < this.runnableOwner.locY()) { // move downwards if player is below entity
                    this.baseY--;
                }
            }
        }

        for (int x = -this.radX; x <= this.radX; x++) {
            for (int y = -this.radY; y <= this.radY; y++) {
                for (int z = -this.radZ; z <= this.radZ; z++) {
                    int x1 = this.baseX + x, y1 = this.baseY + y, z1 = this.baseZ + z;
                    this.bukkitBlock = this.bukkitWorld.getBlockAt(x1, y1, z1);
                    this.bukkitMaterial = this.bukkitBlock.getType();

                    if (blockBreakable.test(this.bukkitMaterial) && (this.removeFluids ? true : Predicates.notFluid.test(this.bukkitMaterial)) && (this.immuneBlocks ? Predicates.notPreciousBlocks.test(this.bukkitMaterial) : true)) { // as long as it isn't one of these blocks
                        this.bukkitBlock.setType(Material.AIR);
                    } else if (!Predicates.notHardBlocks.test(this.bukkitMaterial)) { // 50% chance to break these blocks
                        if (random.nextDouble() < 0.5) {
                            this.bukkitBlock.setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }
}
