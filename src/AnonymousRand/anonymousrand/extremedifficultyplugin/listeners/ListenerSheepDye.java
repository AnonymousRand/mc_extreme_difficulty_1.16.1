package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.CustomEntitySheepAggressive;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.EntitySheep;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftSheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SheepDyeWoolEvent;

public class ListenerSheepDye implements Listener {

    @EventHandler
    public void sheepDyeWool(SheepDyeWoolEvent event) { /* sheep turn aggressive and pink when dyed */
        EntitySheep nmsSheep = ((CraftSheep)event.getEntity()).getHandle();
        World nmsWorld = nmsSheep.getWorld();

        CustomEntitySheepAggressive newSheep = new CustomEntitySheepAggressive(nmsWorld);
        new SpawnEntity(nmsWorld, newSheep, 1, null, null, nmsSheep, true, true);
    }
}