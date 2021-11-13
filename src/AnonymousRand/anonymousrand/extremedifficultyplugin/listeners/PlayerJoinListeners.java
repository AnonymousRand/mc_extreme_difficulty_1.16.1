package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListeners implements Listener {

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().getServer().getOnlinePlayers().size() == 1) { /**remove projectiles etc to reduce lag if this is the first player to join the server*/
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=arrow]");
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=small_fireball]");
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=fireball]");
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=falling_block]");
        }
    }
}
