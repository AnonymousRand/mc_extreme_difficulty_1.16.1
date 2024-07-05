package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
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
        Material bukkitMaterial = event.getItem().getType();

        switch (bukkitMaterial) {
            case CHICKEN:
            case COOKED_CHICKEN:
                bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 1200, 0)); /* eating chicken meat gives you hunger for 60 seconds */
                break;
            case HONEY_BOTTLE:
            case MILK_BUCKET: /* milk and poison doubles negative potion effect durations and adds +1 amplifier instead of removing them */
                PotionEffectType bukkitPotionEffectType;
                milkTest = false;

                for (PotionEffect effect : bukkitPlayer.getActivePotionEffects()) {
                    bukkitPotionEffectType = effect.getType();
                    if (bukkitPotionEffectType.equals(PotionEffectType.SLOW) || bukkitPotionEffectType.equals(PotionEffectType.SLOW_DIGGING) || bukkitPotionEffectType.equals(PotionEffectType.CONFUSION) || bukkitPotionEffectType.equals(PotionEffectType.BLINDNESS) || bukkitPotionEffectType.equals(PotionEffectType.HUNGER) || bukkitPotionEffectType.equals(PotionEffectType.WEAKNESS) || bukkitPotionEffectType.equals(PotionEffectType.POISON) || bukkitPotionEffectType.equals(PotionEffectType.WITHER) || bukkitPotionEffectType.equals(PotionEffectType.LEVITATION) || bukkitPotionEffectType.equals(PotionEffectType.UNLUCK) || bukkitPotionEffectType.equals(PotionEffectType.BAD_OMEN)) {
                        if (!milkTest) {
                            milkTest = true;
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + bukkitPlayer.getName() + " \"You thought...\"");
                        }

                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> bukkitPlayer.addPotionEffect(new PotionEffect(effect.getType(), effect.getDuration() * 2, effect.getAmplifier() + 1)), 1);
                    }
                }

                break;
            case POISONOUS_POTATO: /* poisonous potatoes give 5 minutes of poison 256 and 3 seconds of hunger 256 */
                bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 6000, 255));
                bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 60, 255));
                break;
        }
    }
}
