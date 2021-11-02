package AnonymousRand.anonymousrand.extremedifficultyplugin.bukkitrunnables.entityrunnables;

import net.minecraft.server.v1_16_R1.EntitySpider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SpiderSummonCobwebBlock extends BukkitRunnable {

    private EntitySpider spider;
    private Location loc;

    public SpiderSummonCobwebBlock(EntitySpider spider) {
        this.spider = spider;
    }

    public void run() {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    this.loc = new Location(this.spider.getWorld().getWorld(), Math.floor(this.spider.locX()) + x, Math.floor(this.spider.locY()) + y, Math.floor(this.spider.locZ()) + z);
                    if (this.loc.getBlock().getType() == org.bukkit.Material.AIR) {
                        this.loc.getBlock().setType(org.bukkit.Material.COBWEB);
                        Bukkit.getPluginManager().callEvent(new BlockPlaceEvent(this.loc.getBlock(), this.loc.getBlock().getState(), null, null, null, false, null)); //fire event that would otherwise not be fired so that the cobweb block can be broken after 4 seconds
                    }
                }
            }
        }
    }
}
