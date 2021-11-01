package AnonymousRand.ExtremeDifficultyPlugin.Listeners;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

public class DropItemListeners implements Listener {

    @EventHandler
    public void itemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        Material type = item.getItemStack().getType();

        if (type.toString().contains("_BED")) {  /**delete bed items immediately when they are spawned (eg. broken or thrown out)*/
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