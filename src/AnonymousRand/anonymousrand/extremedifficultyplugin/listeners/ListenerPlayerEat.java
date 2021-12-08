package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ListenerPlayerEat implements Listener {

    private static boolean milkTest;

    static {
        milkTest = false;
    }

    @EventHandler
    public void playerItemConsume(PlayerItemConsumeEvent event) {
        Player bukkitPlayer = event.getPlayer();
        Material type = event.getItem().getType();

        switch (type) {
            case CHICKEN, COOKED_CHICKEN -> bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 1200, 0)); /**eating chicken meat gives you hunger for 60 seconds*/
            case HONEY_BOTTLE, MILK_BUCKET -> { /**milk and poison doubles negative potion effect durations and adds +1 amplifier instead of removing them*/
                milkTest = false;

                for (PotionEffect effect : bukkitPlayer.getActivePotionEffects()) {
                    if (effect.getType().equals(PotionEffectType.SLOW) || effect.getType().equals(PotionEffectType.SLOW_DIGGING) || effect.getType().equals(PotionEffectType.CONFUSION) || effect.getType().equals(PotionEffectType.BLINDNESS) || effect.getType().equals(PotionEffectType.HUNGER) || effect.getType().equals(PotionEffectType.WEAKNESS) || effect.getType().equals(PotionEffectType.POISON) || effect.getType().equals(PotionEffectType.WITHER) || effect.getType().equals(PotionEffectType.LEVITATION) || effect.getType().equals(PotionEffectType.UNLUCK) || effect.getType().equals(PotionEffectType.BAD_OMEN)) {
                        if (!milkTest) {
                            milkTest = true;
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + bukkitPlayer.getName() + " \"You thought...\"");
                        }

                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> bukkitPlayer.addPotionEffect(new PotionEffect(effect.getType(), effect.getDuration() * 2, effect.getAmplifier() + 1)), 1);
                    }
                }
            }
            case POISONOUS_POTATO -> { /**poisonous potatoes give 5 minutes of poison 256 and 3 seconds of hunger 256*/
                bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 6000, 255));
                bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 60, 255));
            }
        }
    }
}
