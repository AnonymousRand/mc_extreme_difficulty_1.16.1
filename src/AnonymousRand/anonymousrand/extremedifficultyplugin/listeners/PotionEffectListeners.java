package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

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

public class PotionEffectListeners implements Listener {

    public static JavaPlugin plugin;

    @EventHandler
    public void potionEffect(EntityPotionEffectEvent event) {
        Entity bukkitEntity = event.getEntity();
        PotionEffect bukkitNewEffect = event.getNewEffect();
        EntityPotionEffectEvent.Cause bukkitCause = event.getCause();

        if (bukkitNewEffect != null) {
            if (bukkitNewEffect.getType().equals(PotionEffectType.DOLPHINS_GRACE)) { /**dolphin's grace drags down the player*/
                bukkitEntity.setVelocity(new Vector(0.0, -0.1, 0.0));
            }

            if ((bukkitCause == EntityPotionEffectEvent.Cause.POTION_DRINK || bukkitCause == EntityPotionEffectEvent.Cause.POTION_SPLASH || bukkitCause == EntityPotionEffectEvent.Cause.AREA_EFFECT_CLOUD) && bukkitEntity instanceof Player) { /**positive potion effects last 20 times shorter*/
                PotionEffectType bukkitEffectType = bukkitNewEffect.getType();

                if (bukkitEffectType.equals(PotionEffectType.ABSORPTION) || bukkitEffectType.equals(PotionEffectType.CONDUIT_POWER) || bukkitEffectType.equals(PotionEffectType.DAMAGE_RESISTANCE) || bukkitEffectType.equals(PotionEffectType.DOLPHINS_GRACE) || bukkitEffectType.equals(PotionEffectType.FAST_DIGGING) || bukkitEffectType.equals(PotionEffectType.FIRE_RESISTANCE) || bukkitEffectType.equals(PotionEffectType.HEAL) || bukkitEffectType.equals(PotionEffectType.HEALTH_BOOST) || bukkitEffectType.equals(PotionEffectType.HERO_OF_THE_VILLAGE) || bukkitEffectType.equals(PotionEffectType.INCREASE_DAMAGE) || bukkitEffectType.equals(PotionEffectType.INVISIBILITY) || bukkitEffectType.equals(PotionEffectType.JUMP) || bukkitEffectType.equals(PotionEffectType.LUCK) || bukkitEffectType.equals(PotionEffectType.NIGHT_VISION) || bukkitEffectType.equals(PotionEffectType.REGENERATION) || bukkitEffectType.equals(PotionEffectType.SATURATION) || bukkitEffectType.equals(PotionEffectType.SLOW_FALLING) || bukkitEffectType.equals(PotionEffectType.SPEED) || bukkitEffectType.equals(PotionEffectType.WATER_BREATHING)) {
                    event.setCancelled(true);

                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
                    {
                        @Override
                        public void run() {
                            Bukkit.broadcastMessage("3");
                            ((LivingEntity)bukkitEntity).addPotionEffect(new PotionEffect(bukkitNewEffect.getType(), (int)(bukkitNewEffect.getDuration() * 0.05), bukkitNewEffect.getAmplifier()));
                            Bukkit.broadcastMessage("Enjoy your " + (int)(bukkitNewEffect.getDuration() * 0.05 / 20.0) + " seconds of " + bukkitNewEffect.getType().getName().toLowerCase());
                        }
                    }, 1);
                }
            }
        }
    }
}
