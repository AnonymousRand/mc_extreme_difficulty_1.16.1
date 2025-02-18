package AnonymousRand.anonymousrand.plugin.listeners;

import AnonymousRand.anonymousrand.plugin.ExtremeDifficultyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ListenerDropItem implements Listener {

    private static final Random random = new Random();

    @EventHandler
    public void itemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        Material bukkitMaterial = item.getItemStack().getType();

        switch (bukkitMaterial) {
            case GOLD_ORE: /* when mined, gold ore drops a single gold nugget instead of gold ore */
                item.getWorld().dropItem(item.getLocation(), new ItemStack(Material.GOLD_NUGGET));
                item.remove();
                break;
            case IRON_ORE: /* when mined, iron ore drops 1-3 iron nuggets */
                item.getWorld().dropItem(item.getLocation(), new ItemStack(Material.IRON_NUGGET, random.nextInt(3) + 1));
                item.remove();
                break;
            case LEATHER_BOOTS: /* depth strider 3, unbreakable 255 boots can't drop from mobs that are killed while wearing one */
                if (item.getItemStack().getEnchantments().containsKey(Enchantment.DURABILITY)) {
                    if (item.getItemStack().getEnchantments().get(Enchantment.DURABILITY) == 255) {
                        item.remove();
                    }
                }
                break;
            case POTATO: /* every potato item dropped is replaced with 16 poisonous potatoes */
                item.setItemStack(new ItemStack(Material.POISONOUS_POTATO, 16));
                break;
            case SAND: /* dropped sand despawns after 5 sec to reduce lag */
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(ExtremeDifficultyPlugin.plugin, item::remove, 100);
                break;
        }

        if (bukkitMaterial.toString().contains("_BED") || bukkitMaterial.toString().contains("VINE") || bukkitMaterial.toString().contains("ROSE")) {  /* delete bed, vine, and rose (for wither roses) items immediately when they are spawned (e.g. broken or thrown out) */
            item.remove();
        }
    }
}
