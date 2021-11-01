package AnonymousRand.ExtremeDifficultyPlugin.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class PotionEffectListeners implements Listener {

    @EventHandler
    public void potionEffect(EntityPotionEffectEvent event) {
        if (event.getNewEffect() != null) {
            if (event.getNewEffect().getType().equals(PotionEffectType.DOLPHINS_GRACE)) { /**dolphin's grace drags down the player*/
                event.getEntity().setVelocity(new Vector(0.0, -0.1, 0.0));
            }
        }
    }
}
