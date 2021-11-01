package AnonymousRand.ExtremeDifficultyPlugin.Listeners;

import AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.CustomMobs.CustomEntitySheepAggressive;
import AnonymousRand.ExtremeDifficultyPlugin.util.SpawnLivingEntity;
import net.minecraft.server.v1_16_R1.EntitySheep;
import net.minecraft.server.v1_16_R1.EnumColor;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftSheep;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class SheepDyeListeners implements Listener {

    private final JavaPlugin plugin;

    public SheepDyeListeners(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void sheepDyeWool(SheepDyeWoolEvent event) { /**sheep turn aggressive and pink when dyed*/
        EntitySheep nmsSheep = ((CraftSheep)event.getEntity()).getHandle();
        World nmsWorld = nmsSheep.getWorld();

        CustomEntitySheepAggressive newSheep = new CustomEntitySheepAggressive(nmsWorld, this.plugin);
        new SpawnLivingEntity(nmsWorld, newSheep, 1, null, null, nmsSheep, true, true).run();
        newSheep.setColor(EnumColor.PINK);
    }
}
