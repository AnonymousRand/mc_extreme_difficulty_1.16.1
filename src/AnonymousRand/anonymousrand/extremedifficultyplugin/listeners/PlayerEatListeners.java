package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerEatListeners implements Listener {

    public static JavaPlugin plugin;

    @EventHandler
    public void playerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Material type = event.getItem().getType();

        switch (type) {
            case CHICKEN, COOKED_CHICKEN -> player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 400, 0)); /**eating chicken meat gives you hunger for 20 seconds*/
            case HONEY_BOTTLE, MILK_BUCKET -> { /**milk and poison extends negative potion effect durations by 4 times and +1 amplifier instead of removing them*/
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() //delay by 1 tick or else the server does not re-apply the status effects, thinking that the player doesn't exist yet
                {
                    @Override
                    public void run() {
                        for (PotionEffect effect : player.getActivePotionEffects()) {
                            if (effect.getType().equals(PotionEffectType.SLOW) || effect.getType().equals(PotionEffectType.SLOW_DIGGING) || effect.getType().equals(PotionEffectType.CONFUSION) || effect.getType().equals(PotionEffectType.BLINDNESS) || effect.getType().equals(PotionEffectType.HUNGER) || effect.getType().equals(PotionEffectType.WEAKNESS) || effect.getType().equals(PotionEffectType.POISON) || effect.getType().equals(PotionEffectType.WITHER) || effect.getType().equals(PotionEffectType.LEVITATION) || effect.getType().equals(PotionEffectType.UNLUCK) || effect.getType().equals(PotionEffectType.BAD_OMEN)) {

                                player.addPotionEffect(new PotionEffect(effect.getType(), effect.getDuration() * 4, effect.getAmplifier() + 1));
                            }
                        }

                        Bukkit.broadcastMessage("You thought...");
                    }
                }, 1);
            }
            case POISONOUS_POTATO -> { /**poisonous potatoes give 5 mintues of poison 256 and 2 seconds of hunger 256*/
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 6000, 255));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 255));
            }
        }
    }
}
