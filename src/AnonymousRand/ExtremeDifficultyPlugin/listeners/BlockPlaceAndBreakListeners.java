package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import net.minecraft.server.v1_16_R1.BlockWeb;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.block.CraftBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BlockPlaceAndBreakListeners implements Listener {

    private JavaPlugin plugin;

    public BlockPlaceAndBreakListeners(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        if (event.getPlayer() != null) {
            if (event.getPlayer().getLocation().getY() >= 129.0) { /**can't build above y level 128 in all dimensions to prevent towering up etc. to avoid mobs*/
                event.setCancelled(true);
                Bukkit.broadcastMessage("Not so fast, smartypants");
                Bukkit.broadcastMessage("You have reached the build height limit of 128 blocks :tf:");
            }
        } else {
            if (event.getBlock().getType() == Material.COBWEB) { /**spider-placed cobwebs are deleted after 10 seconds*/
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() //async thread is used so that the game doesn't pause completely for 10 seconds
                {
                    public void run()
                    {
                        event.getBlock().setType(Material.AIR);
                    }
                }, 200);
            }
        }
    }
}