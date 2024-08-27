package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import net.minecraft.server.v1_16_R1.EntityInsentient;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class RunnableSpawnBlocksAround extends BukkitRunnable {

    private final EntityInsentient runnableOwner;
    private final org.bukkit.Material material;
    private final int radius;

    public RunnableSpawnBlocksAround(EntityInsentient runnableOwner, Material material, int radius) {
        this.runnableOwner = runnableOwner;
        this.material = material;
        this.radius = radius;
    }

    @Override
    public void run() {
        for (int x = -this.radius; x <= this.radius; x++) {
            for (int y = -this.radius; y <= this.radius; y++) {
                for (int z = -this.radius; z <= this.radius; z++) {
                    Location bukkitLoc = new Location(this.runnableOwner.getWorld().getWorld(), Math.floor(this.runnableOwner.locX()) + x, Math.floor(this.runnableOwner.locY()) + y, Math.floor(this.runnableOwner.locZ()) + z);

                    if (bukkitLoc.getBlock().getType() == org.bukkit.Material.AIR) {
                        bukkitLoc.getBlock().setType(this.material);

                        if (this.material == Material.COBWEB) {
                            Bukkit.getPluginManager().callEvent(new BlockPlaceEvent(bukkitLoc.getBlock(), bukkitLoc.getBlock().getState(), null, null, null, false, null)); // fire event that would otherwise not be fired so that the cobweb block can be broken after 4 seconds
                        }
                    }
                }
            }
        }
    }
}
