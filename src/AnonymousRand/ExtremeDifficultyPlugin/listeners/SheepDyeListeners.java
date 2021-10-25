package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntitySheepAggressive;
import net.minecraft.server.v1_16_R1.EnumColor;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.inventory.ItemStack;

public class SheepDyeListeners implements Listener {

    @EventHandler
    public void sheepDyeWool(SheepDyeWoolEvent event) { /**sheep turn aggressive when dyed*/
        Sheep sheep = event.getEntity();
        Location loc = sheep.getLocation();

        org.bukkit.inventory.ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        boots.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 3); /**most mobs spawn with depth strider 6 to avoid loopholes such as using water flow to keep them back*/
        boots.addUnsafeEnchantment(Enchantment.DURABILITY, 255);

        CustomEntitySheepAggressive newSheep = new CustomEntitySheepAggressive(((CraftWorld)sheep.getWorld()).getHandle());
        newSheep.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        ((LivingEntity)newSheep.getBukkitEntity()).getEquipment().setBoots(boots);
        newSheep.setColor(EnumColor.PINK);
        ((CraftWorld)sheep.getWorld()).getHandle().addEntity(newSheep, CreatureSpawnEvent.SpawnReason.NATURAL);
        sheep.remove();
    }
}
