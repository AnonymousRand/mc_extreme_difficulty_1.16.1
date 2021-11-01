package AnonymousRand.ExtremeDifficultyPlugin.Listeners;

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

    private final JavaPlugin plugin;

    public PlayerEatListeners(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void playerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        
        if (event.getItem().getType() == Material.POISONOUS_POTATO) { /**poisonous potatoes give 5 mintues of poison 256 and 2 seconds of hunger 256*/
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 6000, 255));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 255));
        }

        if (event.getItem().getType() == Material.CHICKEN || event.getItem().getType() == Material.COOKED_CHICKEN) { /**eating chicken meat gives you hunger for 20 seconds*/
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 400, 0));
        }

        if (event.getItem().getType() == Material.MILK_BUCKET || event.getItem().getType() == Material.HONEY_BOTTLE) { /**milk and poison extends negative potion effect durations by 10 times and +1 amplifier instead of removing them*/
            for (PotionEffect e : player.getActivePotionEffects()) {
                if (e.getType().equals(PotionEffectType.SLOW) || e.getType().equals(PotionEffectType.SLOW_DIGGING) || e.getType().equals(PotionEffectType.CONFUSION) || e.getType().equals(PotionEffectType.BLINDNESS) || e.getType().equals(PotionEffectType.HUNGER) || e.getType().equals(PotionEffectType.WEAKNESS) || e.getType().equals(PotionEffectType.POISON) || e.getType().equals(PotionEffectType.WITHER) || e.getType().equals(PotionEffectType.LEVITATION) || e.getType().equals(PotionEffectType.UNLUCK) || e.getType().equals(PotionEffectType.BAD_OMEN)) {
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() //delay by 1 tick or else the server does not re-apply the status effects, thinking that the player doesn't exist yet
                    {
                        public void run()
                        {
                            player.addPotionEffect(new PotionEffect(e.getType(), e.getDuration() * 10, e.getAmplifier() + 1));
                            Bukkit.broadcastMessage("You thought...");
                        }
                    }, 1);
                }
            }
        }
    }
}
