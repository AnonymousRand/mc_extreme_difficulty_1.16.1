package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class PlayerItemConsumeListeners implements Listener {

    @EventHandler
    public void playerItemConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.POISONOUS_POTATO) { /**poisonous potatoes do 1 million damage*/
            event.getPlayer().damage(1000000.0);
        }
    }
}
