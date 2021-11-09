package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import net.minecraft.server.v1_16_R1.EntityInsentient;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class RunnableSpiderSilverfishSummonMaterialBlock extends BukkitRunnable {

    private final EntityInsentient entity;
    private final org.bukkit.Material material;
    private final int radius;
    private Location loc;

    public RunnableSpiderSilverfishSummonMaterialBlock(EntityInsentient entity, Material material, int radius) {
        this.entity = entity;
        this.material = material;
        this.radius = radius;
        this.run();
    }

    @Override
    public void run() {
        for (int x = -this.radius; x <= this.radius; x++) {
            for (int y = -this.radius; y <= this.radius; y++) {
                for (int z = -this.radius; z <= this.radius; z++) {
                    this.loc = new Location(this.entity.getWorld().getWorld(), Math.floor(this.entity.locX()) + x, Math.floor(this.entity.locY()) + y, Math.floor(this.entity.locZ()) + z);

                    if (this.loc.getBlock().getType() == org.bukkit.Material.AIR) {
                        this.loc.getBlock().setType(this.material);

                        if (this.material == Material.COBWEB) {
                            Bukkit.getPluginManager().callEvent(new BlockPlaceEvent(this.loc.getBlock(), this.loc.getBlock().getState(), null, null, null, false, null)); //fire event that would otherwise not be fired so that the cobweb block can be broken after 4 seconds
                        }
                    }
                }
            }
        }
    }
}
