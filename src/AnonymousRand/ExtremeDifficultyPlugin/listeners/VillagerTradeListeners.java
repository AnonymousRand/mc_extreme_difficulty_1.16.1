package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

public class VillagerTradeListeners implements Listener {

    @EventHandler
    public void openVillagerInventory(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof AbstractVillager) { //replaces villager trades with these troll trades
            ArrayList<MerchantRecipe> trades = new ArrayList<>();

            MerchantRecipe recipe = new MerchantRecipe(new ItemStack(Material.POISONOUS_POTATO), 2);
            recipe.addIngredient(new ItemStack(Material.EMERALD, 1));
            MerchantRecipe recipe2 = new MerchantRecipe(new ItemStack(Material.POISONOUS_POTATO, 64), 1);
            recipe2.addIngredient(new ItemStack(Material.EMERALD_BLOCK, 1));
            MerchantRecipe recipe3 = new MerchantRecipe(new ItemStack(Material.AIR, 1000), 1);
            recipe3.addIngredient(new ItemStack(Material.EMERALD_BLOCK, 64));

            trades.add(recipe);
            trades.add(recipe2);
            trades.add(recipe3);

            ((AbstractVillager)event.getInventory().getHolder()).setRecipes(trades);
        }
    }
}
