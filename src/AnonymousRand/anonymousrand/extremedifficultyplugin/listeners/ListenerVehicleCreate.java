package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityBoat;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftVehicle;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;

public class ListenerVehicleCreate implements Listener {

    @EventHandler
    public void vehicleCreate(VehicleCreateEvent event) { // replace boats with custom boats
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
