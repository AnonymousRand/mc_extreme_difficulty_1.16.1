package AnonymousRand.ExtremeDifficultyPlugin.Listeners;

import AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.CustomMobs.CustomEntityBat;
import AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.CustomMobs.CustomEntityChickenAggressive;
import AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.misc.CustomEntityBoat;
import AnonymousRand.ExtremeDifficultyPlugin.util.SpawnLivingEntity;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftVehicle;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;

public class VehicleCreateListeners implements Listener {

    @EventHandler
    public void vehicleCreate(VehicleCreateEvent event) { //replace boats with custom boats
        Vehicle bukkitVehicle = event.getVehicle();
        World nmsWorld = ((CraftWorld)bukkitVehicle.getWorld()).getHandle();
        Location loc = bukkitVehicle.getLocation();

        if (bukkitVehicle instanceof Boat && !(((CraftVehicle)bukkitVehicle).getHandle() instanceof CustomEntityBoat)) {
            CustomEntityBoat newBoat = new CustomEntityBoat(nmsWorld, loc.getX(), loc.getY(), loc.getZ());
            nmsWorld.addEntity(newBoat);
            bukkitVehicle.remove();
        }
    }
}
