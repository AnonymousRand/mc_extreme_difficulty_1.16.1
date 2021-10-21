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
    public void itemSpawn(ItemSpawnEvent event) {
        if (event.getEntity().getItemStack().getType().toString().contains("_BED") || event.getEntity().getItemStack().getType().toString().contains("_HEAD") || event.getEntity().getItemStack().getType().toString().contains("_SKULL") || event.getEntity().getItemStack().getType() == Material.TOTEM_OF_UNDYING) {  /**delete bed, mob head and totem items immediately when they are spawned (eg. broken or thrown out)*/
            event.getEntity().remove();
        }

        if (event.getEntity().getItemStack().getType() == Material.POTATO) { /**every potato item dropped is replaced with 16 poisonous potatoes*/
            event.getEntity().setItemStack(new ItemStack(Material.POISONOUS_POTATO, 16));
        }
    }
}