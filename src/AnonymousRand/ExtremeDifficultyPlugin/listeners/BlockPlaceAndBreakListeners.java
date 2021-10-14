package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceAndBreakListeners implements Listener {

    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        if (event.getPlayer().getLocation().getY() >= 129.0) { //can't build above y level 128 in all dimensions to prevent towering up etc. to avoid mobs
            event.setCancelled(true);
            Bukkit.broadcastMessage("Not so fast, smartypants");
            Bukkit.broadcastMessage("You have reached the build height limit of 128 blocks :tf:");
        }
    }
}