package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.misc.CustomEntityBoat;
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
        Location bukkitLoc = bukkitVehicle.getLocation();

        if (bukkitVehicle instanceof Boat && !(((CraftVehicle)bukkitVehicle).getHandle() instanceof CustomEntityBoat)) {
            CustomEntityBoat newBoat = new CustomEntityBoat(nmsWorld, bukkitLoc.getX(), bukkitLoc.getY(), bukkitLoc.getZ());
            nmsWorld.addEntity(newBoat);
            bukkitVehicle.remove();
        }
    }
}