package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class DropItemListeners implements Listener {

    private static final Random random = new Random();

    @EventHandler
    public void itemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        Material type = item.getItemStack().getType();

        switch (type) {
            case GOLD_ORE -> { /**when mined, gold ore drops a single gold nugget instead of gold ore*/
                item.getWorld().dropItem(item.getLocation(), new ItemStack(Material.GOLD_NUGGET));
                item.remove();
            }
            case IRON_ORE -> { /**when mined, iron ore drops 1-3 iron nuggets*/
                item.getWorld().dropItem(item.getLocation(), new ItemStack(Material.IRON_NUGGET, random.nextInt(3) + 1));
                item.remove();
            }
            case LEATHER_BOOTS -> { /**depth strider 3, unbreakable 255 boots can't drop from mobs that are killed while wearing one*/
                if (item.getItemStack().getEnchantments().containsKey(Enchantment.DURABILITY)) {
                    if (item.getItemStack().getEnchantments().get(Enchantment.DURABILITY) == 255) {
                        item.remove();
                    }
                }
            }
            case POTATO -> /**every potato item dropped is replaced with 16 poisonous potatoes*/
                item.setItemStack(new ItemStack(Material.POISONOUS_POTATO, 16));
            case SAND -> /**dropped sand despawns after 5 sec to reduce lag*/
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, item::remove, 100);
        }

        if (type.toString().contains("_BED") || type.toString().contains("VINE")) {  /**delete bed and vine items immediately when they are spawned (eg. broken or thrown out)*/
            item.remove();
        }
    }
}