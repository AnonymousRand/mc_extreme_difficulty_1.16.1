package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntitySheepAggressive;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import net.minecraft.server.v1_16_R1.EntitySheep;
import net.minecraft.server.v1_16_R1.EnumColor;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftSheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SheepDyeWoolEvent;
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
        new SpawnLivingEntity(this.plugin, nmsWorld, newSheep, 1, null, null, nmsSheep, true, true).run();
        newSheep.setColor(EnumColor.PINK);
    }
}
