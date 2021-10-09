package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EntityDamageListeners implements Listener {
    public JavaPlugin plugin;

    public EntityDamageListeners(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        switch (event.getEntityType()) {
            case SKELETON: //skeletons are immune to fire damage
                event.setCancelled(event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || event.getCause() == EntityDamageEvent.DamageCause.FIRE);
            case ZOMBIE: //zombies are immune to fire damage
                event.setCancelled(event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || event.getCause() == EntityDamageEvent.DamageCause.FIRE);
        }
    }
}
