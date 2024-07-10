package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.ExtremeDifficultyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class ListenerPotionEffect implements Listener {

    @EventHandler
    public void potionEffect(EntityPotionEffectEvent event) {
        Entity bukkitEntity = event.getEntity();
        PotionEffect bukkitNewEffect = event.getNewEffect();
        EntityPotionEffectEvent.Cause bukkitCause = event.getCause();

        if (bukkitNewEffect != null) {
            PotionEffectType bukkitPotionEffectType = bukkitNewEffect.getType();
            if (bukkitPotionEffectType.equals(PotionEffectType.DOLPHINS_GRACE)) { /* dolphin's grace drags down the player */
                bukkitEntity.setVelocity(new Vector(0.0, -0.3, 0.0));
            }

            if ((bukkitCause == EntityPotionEffectEvent.Cause.POTION_DRINK || bukkitCause == EntityPotionEffectEvent.Cause.POTION_SPLASH || bukkitCause == EntityPotionEffectEvent.Cause.AREA_EFFECT_CLOUD)) { /* positive potion effects applied via potions last 40 times shorter */
                if (bukkitPotionEffectType.equals(PotionEffectType.ABSORPTION) || bukkitPotionEffectType.equals(PotionEffectType.CONDUIT_POWER) || bukkitPotionEffectType.equals(PotionEffectType.DAMAGE_RESISTANCE) || bukkitPotionEffectType.equals(PotionEffectType.DOLPHINS_GRACE) || bukkitPotionEffectType.equals(PotionEffectType.FAST_DIGGING) || bukkitPotionEffectType.equals(PotionEffectType.FIRE_RESISTANCE) || bukkitPotionEffectType.equals(PotionEffectType.HEAL) || bukkitPotionEffectType.equals(PotionEffectType.HEALTH_BOOST) || bukkitPotionEffectType.equals(PotionEffectType.HERO_OF_THE_VILLAGE) || bukkitPotionEffectType.equals(PotionEffectType.INCREASE_DAMAGE) || bukkitPotionEffectType.equals(PotionEffectType.INVISIBILITY) || bukkitPotionEffectType.equals(PotionEffectType.JUMP) || bukkitPotionEffectType.equals(PotionEffectType.LUCK) || bukkitPotionEffectType.equals(PotionEffectType.NIGHT_VISION) || bukkitPotionEffectType.equals(PotionEffectType.REGENERATION) || bukkitPotionEffectType.equals(PotionEffectType.SATURATION) || bukkitPotionEffectType.equals(PotionEffectType.SLOW_FALLING) || bukkitPotionEffectType.equals(PotionEffectType.SPEED) || bukkitPotionEffectType.equals(PotionEffectType.WATER_BREATHING)) {
                    if (bukkitEntity instanceof Player) {
                        event.setCancelled(true);

                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(ExtremeDifficultyPlugin.plugin, () -> {
                            ((LivingEntity) bukkitEntity).addPotionEffect(new PotionEffect(bukkitPotionEffectType, (int) (bukkitNewEffect.getDuration() * 0.025), bukkitNewEffect.getAmplifier()));
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + bukkitEntity.getName() + " \"Enjoy your " + (int) (bukkitNewEffect.getDuration() * 0.025 / 20.0) + " seconds of " + bukkitPotionEffectType.getName().toLowerCase().replaceAll("_", " ") + "\"");
                        }, 1);
                    }
                } else {
                    if (!(bukkitEntity instanceof Player)) { /* non-player mobs are not affected by negative splash/lingering potions */
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}