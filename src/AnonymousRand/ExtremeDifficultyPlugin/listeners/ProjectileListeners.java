package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.CustomEntityArrow;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class ProjectileListeners implements Listener {
    public JavaPlugin plugin;

    public ProjectileListeners(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void projectileLaunch(ProjectileLaunchEvent event) { //replace arrows when shot with custom arrows

        if (event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow)event.getEntity();
            Vector v = arrow.getVelocity();
            Location loc = arrow.getLocation();
            CustomEntityArrow newArrow = new CustomEntityArrow(((CraftWorld)arrow.getWorld()).getHandle(), v);
            newArrow.setPosition(loc.getX(), loc.getY(), loc.getZ());
            ((CraftWorld)arrow.getWorld()).getHandle().addEntity(newArrow);
            arrow.remove();
        }
    }
}