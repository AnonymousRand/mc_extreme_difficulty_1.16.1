package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.plugin.java.JavaPlugin;
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
            if (bukkitNewEffect.getType().equals(PotionEffectType.DOLPHINS_GRACE)) { /**dolphin's grace drags down the player*/
                bukkitEntity.setVelocity(new Vector(0.0, -0.1, 0.0));
            }

            if ((bukkitCause == EntityPotionEffectEvent.Cause.POTION_DRINK || bukkitCause == EntityPotionEffectEvent.Cause.POTION_SPLASH || bukkitCause == EntityPotionEffectEvent.Cause.AREA_EFFECT_CLOUD)) { /**positive potion effects last 20 times shorter*/
                PotionEffectType type = bukkitNewEffect.getType();

                if (type.equals(PotionEffectType.ABSORPTION) || type.equals(PotionEffectType.CONDUIT_POWER) || type.equals(PotionEffectType.DAMAGE_RESISTANCE) || type.equals(PotionEffectType.DOLPHINS_GRACE) || type.equals(PotionEffectType.FAST_DIGGING) || type.equals(PotionEffectType.FIRE_RESISTANCE) || type.equals(PotionEffectType.HEAL) || type.equals(PotionEffectType.HEALTH_BOOST) || type.equals(PotionEffectType.HERO_OF_THE_VILLAGE) || type.equals(PotionEffectType.INCREASE_DAMAGE) || type.equals(PotionEffectType.INVISIBILITY) || type.equals(PotionEffectType.JUMP) || type.equals(PotionEffectType.LUCK) || type.equals(PotionEffectType.NIGHT_VISION) || type.equals(PotionEffectType.REGENERATION) || type.equals(PotionEffectType.SATURATION) || type.equals(PotionEffectType.SLOW_FALLING) || type.equals(PotionEffectType.SPEED) || type.equals(PotionEffectType.WATER_BREATHING)) {
                    if (bukkitEntity instanceof Player) {
                        event.setCancelled(true);

                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> {
                            ((LivingEntity) bukkitEntity).addPotionEffect(new PotionEffect(bukkitNewEffect.getType(), (int) (bukkitNewEffect.getDuration() * 0.05), bukkitNewEffect.getAmplifier()));
                            Bukkit.broadcastMessage("Enjoy your " + (int) (bukkitNewEffect.getDuration() * 0.05 / 20.0) + " seconds of " + bukkitNewEffect.getType().getName().toLowerCase().replaceAll("_", " "));
                        }, 1);
                    }
                } else {
                    if (!(bukkitEntity instanceof Player)) { /**non-player mobs are not affected by negative splash/lingering potions*/
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
