package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

public class ListenerVillagerTrade implements Listener {

    @EventHandler
    public void openVillagerInventory(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof AbstractVillager) { /**replaces villager trades with these troll trades*/
            ArrayList<MerchantRecipe> trades = new ArrayList<>();

            MerchantRecipe recipe = new MerchantRecipe(new ItemStack(Material.POISONOUS_POTATO), 2);
            recipe.addIngredient(new ItemStack(Material.EMERALD, 1));
            MerchantRecipe recipe2 = new MerchantRecipe(new ItemStack(Material.POISONOUS_POTATO, 64), 1);
            recipe2.addIngredient(new ItemStack(Material.EMERALD_BLOCK, 1));
            MerchantRecipe recipe3 = new MerchantRecipe(new ItemStack(Material.POLISHED_BLACKSTONE_PRESSURE_PLATE, 1), 1);
            recipe3.addIngredient(new ItemStack(Material.EMERALD_BLOCK, 64));
            MerchantRecipe recipe4 = new MerchantRecipe(new ItemStack(Material.AIR, 64), 1);
            recipe4.addIngredient(new ItemStack(Material.NETHERITE_BLOCK, 64));
            MerchantRecipe recipe5 = new MerchantRecipe(new ItemStack(Material.BEACON, 64), 1);
            recipe5.addIngredient(new ItemStack(Material.BEACON, 63));
            MerchantRecipe recipe6 = new MerchantRecipe(new ItemStack(Material.AIR, 1), 1000);
            recipe6.addIngredient(new ItemStack(Material.AIR, 2));
            MerchantRecipe recipe7 = new MerchantRecipe(new ItemStack(Material.BEDROCK, 1), 1);
            recipe7.addIngredient(new ItemStack(Material.DRAGON_EGG, 2));
            MerchantRecipe recipe8 = new MerchantRecipe(new ItemStack(Material.POPPY), 1);
            recipe8.addIngredient(new ItemStack(Material.POPPY, 1));
            MerchantRecipe recipe9 = new MerchantRecipe(new ItemStack(Material.EMERALD), 10000);
            recipe9.addIngredient(new ItemStack(Material.BLAZE_ROD, 1));

            trades.add(recipe);
            trades.add(recipe2);
            trades.add(recipe3);
            trades.add(recipe4);
            trades.add(recipe5);
            trades.add(recipe6);
            trades.add(recipe7);
            trades.add(recipe8);
            trades.add(recipe9);

            ((AbstractVillager)event.getInventory().getHolder()).setRecipes(trades);
        }

        if (event.getInventory().getHolder() instanceof WanderingTrader) { /**replaces wandering trader trades*/
            ArrayList<MerchantRecipe> trades = new ArrayList<>();

            MerchantRecipe recipe = new MerchantRecipe(new ItemStack(Material.AIR), 1);
            recipe.addIngredient(new ItemStack(Material.COAL_BLOCK, 1));
            MerchantRecipe recipe2 = new MerchantRecipe(new ItemStack(Material.IRON_INGOT, 1), 1);
            recipe2.addIngredient(new ItemStack(Material.IRON_BLOCK, 1));
            MerchantRecipe recipe3 = new MerchantRecipe(new ItemStack(Material.GOLD_NUGGET, 1), 1);
            recipe3.addIngredient(new ItemStack(Material.GOLD_BLOCK, 1));
            MerchantRecipe recipe4 = new MerchantRecipe(new ItemStack(Material.PEONY, 1), 1);
            recipe4.addIngredient(new ItemStack(Material.DIAMOND_BLOCK, 1));
            MerchantRecipe recipe5 = new MerchantRecipe(new ItemStack(Material.WANDERING_TRADER_SPAWN_EGG, 1), 1);
            recipe5.addIngredient(new ItemStack(Material.NETHERITE_BLOCK, 1));
            MerchantRecipe recipe6 = new MerchantRecipe(new ItemStack(Material.LLAMA_SPAWN_EGG, 1), 1);
            recipe6.addIngredient(new ItemStack(Material.EMERALD_BLOCK, 1));
            MerchantRecipe recipe7 = new MerchantRecipe(new ItemStack(Material.ORANGE_BANNER, 1), 1);
            recipe7.addIngredient(new ItemStack(Material.COMMAND_BLOCK, 1));
            MerchantRecipe recipe8 = new MerchantRecipe(new ItemStack(Material.DEAD_BUSH, 3), 1000);
            recipe8.addIngredient(new ItemStack(Material.END_PORTAL_FRAME, 64));
            MerchantRecipe recipe9 = new MerchantRecipe(new ItemStack(Material.EMERALD_BLOCK), 10000);
            recipe9.addIngredient(new ItemStack(Material.ENDER_EYE, 1));

            trades.add(recipe);
            trades.add(recipe2);
            trades.add(recipe3);
            trades.add(recipe4);
            trades.add(recipe5);
            trades.add(recipe6);
            trades.add(recipe7);
            trades.add(recipe8);
            trades.add(recipe9);

            ((WanderingTrader)event.getInventory().getHolder()).setRecipes(trades);
        }
    }
}
