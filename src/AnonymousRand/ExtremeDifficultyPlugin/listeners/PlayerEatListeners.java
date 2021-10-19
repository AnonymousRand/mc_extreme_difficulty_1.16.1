package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerEatListeners implements Listener {

    @EventHandler
    public void playerItemConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.POISONOUS_POTATO) { /**poisonous potatoes do 1 million damage*/
            event.getPlayer().damage(1000000.0);
        }

        if (event.getItem().getType() == Material.CHICKEN || event.getItem().getType() == Material.COOKED_CHICKEN) { /**eating chicken meat gives you hunger for 20 seconds*/
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 400, 0));
        }
    }
}
