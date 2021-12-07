package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityArrow;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityLightning;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableMeteorRain;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableThorLightningEffectStorm;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableTornado;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Random;

import static org.bukkit.entity.EntityType.VILLAGER;

public class ListenerRaidAndVillager implements Listener {

    private static final Random random = new Random();

    @EventHandler
    public void villagerDamage(EntityDamageByEntityEvent event) {
        Entity nmsDamager = ((CraftEntity)event.getDamager()).getHandle();

        if ((event.getEntityType() == VILLAGER)) {
            event.setDamage(event.getDamage() * 2.0); /**villagers take twice as much damage from everything*/

            if (nmsDamager instanceof EntityPlayer) { /**villagers give players bad omen if they are hit by a player*/
                ((EntityPlayer)nmsDamager).addEffect(new MobEffect(MobEffects.BAD_OMEN, Integer.MAX_VALUE, 255));
            } else if (nmsDamager instanceof CustomEntityArrow) {
                if (((CustomEntityArrow)nmsDamager).getShooter() instanceof EntityPlayer) {
                    ((EntityPlayer)((CustomEntityArrow)nmsDamager).getShooter()).addEffect(new MobEffect(MobEffects.BAD_OMEN, Integer.MAX_VALUE, 255));
                }
            }
        }
    }

    @EventHandler
    public void villagerDeath(EntityDeathEvent event) {
        if (event.getEntityType() == VILLAGER) {
            LivingEntity bukkitVillager = event.getEntity();
            EntityLiving nmsEntity = ((CraftLivingEntity)bukkitVillager).getHandle();

            if (!(nmsEntity instanceof CustomEntityVillagerAggressive) || !(nmsEntity.getLastDamager() instanceof EntityPlayer)) { /**when killed, villagers summon an aggressive villager (aggressive villagers can continue to respawn if they are not killed by a player)*/
                World nmsWorld = ((CraftLivingEntity)bukkitVillager).getHandle().getWorld();
                new SpawnEntity(nmsWorld, new CustomEntityVillagerAggressive(nmsWorld), 1, null, bukkitVillager.getLocation(), true);

                nmsEntity.getWorld().getEntities(nmsEntity, nmsEntity.getBoundingBox().grow(64.0, 128.0, 64.0), entity -> entity instanceof CustomEntityIronGolem).forEach(entity -> { /**golems within 64 blocks horizontally of killed villager get a 25% stat boost and summon a lightning effect storm like thor around it for 5 seconds*/
                    ((CustomEntityIronGolem)entity).increaseStatsMultiply(1.25);
                    new RunnableThorLightningEffectStorm(entity, 50 , true).runTaskTimer(StaticPlugin.plugin, 0L, 2L);
                });
            }
        }
    }

    //temp, todo end: delete
    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) && event.hasBlock()) {
            if (event.getClickedBlock().getType().equals(Material.OAK_SIGN)) {
                new SpawnEntity(((CraftWorld)event.getPlayer().getWorld()).getHandle(), new CustomEntityLightning(((CraftWorld)event.getPlayer().getWorld()).getHandle()), 1, null, event.getPlayer().getLocation(), false);
            }
        }
    }

    @EventHandler
    public void raidFinish(RaidFinishEvent event) { /**summon meteor rain when raid ends on random player*/
        Player bukkitPlayer = event.getWinners().get(random.nextInt(event.getWinners().size()));

        new RunnableMeteorRain(bukkitPlayer, 1, 70.0, 80).runTaskTimer(StaticPlugin.plugin, 0L, 1L);
        new RunnableMeteorRain(bukkitPlayer, 2, 70.0, 80).runTaskTimer(StaticPlugin.plugin, 0L, 1L);
        new RunnableMeteorRain(bukkitPlayer, 3, 70.0, 100).runTaskTimer(StaticPlugin.plugin, 0L, 1L);
    }

    @EventHandler
    public void openVillagerInventory(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof AbstractVillager) { /**replaces villager trades with these troll trades*/
            ArrayList<org.bukkit.inventory.MerchantRecipe> trades = new ArrayList<>();

            org.bukkit.inventory.MerchantRecipe recipe = new org.bukkit.inventory.MerchantRecipe(new org.bukkit.inventory.ItemStack(Material.POISONOUS_POTATO), 2);
            recipe.addIngredient(new org.bukkit.inventory.ItemStack(Material.EMERALD, 1));
            org.bukkit.inventory.MerchantRecipe recipe2 = new org.bukkit.inventory.MerchantRecipe(new org.bukkit.inventory.ItemStack(Material.POISONOUS_POTATO, 64), 1);
            recipe2.addIngredient(new org.bukkit.inventory.ItemStack(Material.EMERALD_BLOCK, 1));
            org.bukkit.inventory.MerchantRecipe recipe3 = new org.bukkit.inventory.MerchantRecipe(new org.bukkit.inventory.ItemStack(Material.POLISHED_BLACKSTONE_PRESSURE_PLATE, 1), 1);
            recipe3.addIngredient(new org.bukkit.inventory.ItemStack(Material.EMERALD_BLOCK, 64));
            org.bukkit.inventory.MerchantRecipe recipe4 = new org.bukkit.inventory.MerchantRecipe(new org.bukkit.inventory.ItemStack(Material.AIR, 64), 1);
            recipe4.addIngredient(new org.bukkit.inventory.ItemStack(Material.NETHERITE_BLOCK, 64));
            org.bukkit.inventory.MerchantRecipe recipe5 = new org.bukkit.inventory.MerchantRecipe(new org.bukkit.inventory.ItemStack(Material.BEACON, 64), 1);
            recipe5.addIngredient(new org.bukkit.inventory.ItemStack(Material.BEACON, 63));
            org.bukkit.inventory.MerchantRecipe recipe6 = new org.bukkit.inventory.MerchantRecipe(new org.bukkit.inventory.ItemStack(Material.AIR, 1), 1000);
            recipe6.addIngredient(new org.bukkit.inventory.ItemStack(Material.AIR, 2));
            org.bukkit.inventory.MerchantRecipe recipe7 = new org.bukkit.inventory.MerchantRecipe(new org.bukkit.inventory.ItemStack(Material.BEDROCK, 1), 1);
            recipe7.addIngredient(new org.bukkit.inventory.ItemStack(Material.DRAGON_EGG, 2));
            org.bukkit.inventory.MerchantRecipe recipe8 = new org.bukkit.inventory.MerchantRecipe(new org.bukkit.inventory.ItemStack(Material.POPPY), 1);
            recipe8.addIngredient(new org.bukkit.inventory.ItemStack(Material.POPPY, 1));
            org.bukkit.inventory.MerchantRecipe recipe9 = new org.bukkit.inventory.MerchantRecipe(new org.bukkit.inventory.ItemStack(Material.EMERALD), 10000);
            recipe9.addIngredient(new org.bukkit.inventory.ItemStack(Material.BLAZE_ROD, 1));

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
            ArrayList<org.bukkit.inventory.MerchantRecipe> trades = new ArrayList<>();

            org.bukkit.inventory.MerchantRecipe recipe = new org.bukkit.inventory.MerchantRecipe(new org.bukkit.inventory.ItemStack(Material.AIR), 1);
            recipe.addIngredient(new org.bukkit.inventory.ItemStack(Material.COAL_BLOCK, 1));
            org.bukkit.inventory.MerchantRecipe recipe2 = new org.bukkit.inventory.MerchantRecipe(new org.bukkit.inventory.ItemStack(Material.IRON_INGOT, 1), 1);
            recipe2.addIngredient(new org.bukkit.inventory.ItemStack(Material.IRON_BLOCK, 1));
            org.bukkit.inventory.MerchantRecipe recipe3 = new org.bukkit.inventory.MerchantRecipe(new org.bukkit.inventory.ItemStack(Material.GOLD_NUGGET, 1), 1);
            recipe3.addIngredient(new org.bukkit.inventory.ItemStack(Material.GOLD_BLOCK, 1));
            org.bukkit.inventory.MerchantRecipe recipe4 = new org.bukkit.inventory.MerchantRecipe(new org.bukkit.inventory.ItemStack(Material.PEONY, 1), 1);
            recipe4.addIngredient(new org.bukkit.inventory.ItemStack(Material.DIAMOND_BLOCK, 1));
            org.bukkit.inventory.MerchantRecipe recipe5 = new org.bukkit.inventory.MerchantRecipe(new org.bukkit.inventory.ItemStack(Material.WANDERING_TRADER_SPAWN_EGG, 1), 1);
            recipe5.addIngredient(new org.bukkit.inventory.ItemStack(Material.NETHERITE_BLOCK, 1));
            org.bukkit.inventory.MerchantRecipe recipe6 = new org.bukkit.inventory.MerchantRecipe(new org.bukkit.inventory.ItemStack(Material.LLAMA_SPAWN_EGG, 1), 1);
            recipe6.addIngredient(new org.bukkit.inventory.ItemStack(Material.EMERALD_BLOCK, 1));
            org.bukkit.inventory.MerchantRecipe recipe7 = new org.bukkit.inventory.MerchantRecipe(new org.bukkit.inventory.ItemStack(Material.ORANGE_BANNER, 1), 1);
            recipe7.addIngredient(new org.bukkit.inventory.ItemStack(Material.COMMAND_BLOCK, 1));
            org.bukkit.inventory.MerchantRecipe recipe8 = new org.bukkit.inventory.MerchantRecipe(new org.bukkit.inventory.ItemStack(Material.DEAD_BUSH, 3), 1000);
            recipe8.addIngredient(new org.bukkit.inventory.ItemStack(Material.END_PORTAL_FRAME, 64));
            org.bukkit.inventory.MerchantRecipe recipe9 = new org.bukkit.inventory.MerchantRecipe(new org.bukkit.inventory.ItemStack(Material.EMERALD_BLOCK), 10000);
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
