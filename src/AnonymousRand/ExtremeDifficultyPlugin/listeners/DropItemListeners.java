package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import net.minecraft.server.v1_16_R1.Item;
import net.minecraft.server.v1_16_R1.Items;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.Bed;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

public class DropItemListeners implements Listener {

    @EventHandler
    public void itemSpawn(ItemSpawnEvent event) { //delete bed items immediately when they are spawned (eg. broken or thrown out)
        if (event.getEntity().getItemStack().getType().toString().contains("_BED")) {
            event.getEntity().remove();
        }
    }
}