package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.misc.CustomEntityBoat;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftVehicle;
import org.bukkit.entity.Boat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;

public class VehicleCreateListeners implements Listener {

    @EventHandler
    public void vehicleCreate(VehicleCreateEvent event) { //replace boats with custom boats
        Location loc = event.getVehicle().getLocation();

        if (event.getVehicle() instanceof Boat && !(((CraftVehicle)event.getVehicle()).getHandle() instanceof CustomEntityBoat)) {
            Boat boat = (Boat)event.getVehicle();
            CustomEntityBoat newBoat = new CustomEntityBoat(((CraftWorld)boat.getWorld()).getHandle(), loc.getX(), loc.getY(), loc.getZ());
            ((CraftWorld)boat.getWorld()).getHandle().addEntity(newBoat);
            boat.remove();
        }
    }
}
