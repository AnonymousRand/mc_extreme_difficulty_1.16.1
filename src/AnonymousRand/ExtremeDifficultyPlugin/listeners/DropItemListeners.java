package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import net.minecraft.server.v1_16_R1.Items;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.Bed;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftItem;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

public class DropItemListeners implements Listener {

    @EventHandler
    public void itemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        Material type = item.getItemStack().getType();

        if (type.toString().contains("_BED") || type.toString().contains("_HEAD") || type.toString().contains("_SKULL") || type == Material.TOTEM_OF_UNDYING) {  /**delete bed, mob head and totem items immediately when they are spawned (eg. broken or thrown out)*/
            item.remove();
        }

        if (type == Material.POTATO) { /**every potato item dropped is replaced with 16 poisonous potatoes*/
            item.setItemStack(new ItemStack(Material.POISONOUS_POTATO, 16));
        }

        if (type == Material.LEATHER_BOOTS && item.getItemStack().getEnchantments().containsKey(Enchantment.DURABILITY)) { /**depth strider 3, unbreakable 255 boots can't drop from mobs that are killed while wearing one*/
            if (item.getItemStack().getEnchantments().get(Enchantment.DURABILITY) == 255) {
                item.remove();
            }
        }
    }
}