package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntityChickenAggressive;
import net.minecraft.server.v1_16_R1.EntityMonster;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.entity.EntityType.*;

public class MobDamageListeners implements Listener {

    @EventHandler
    public void entityDamage(EntityDamageEvent event) {
        EntityType entityType = event.getEntityType();
        EntityDamageEvent.DamageCause cause = event.getCause();
        
        switch (entityType) { //natural damage immunities by specific mobs
            case DROWNED:
            case SKELETON:
            case STRAY:
            case ZOMBIE: /**drowned, skeletons and zombies are immune to fire damage*/
                event.setCancelled(cause == EntityDamageEvent.DamageCause.FIRE_TICK || cause == EntityDamageEvent.DamageCause.FIRE);
                break;
            case ENDERMAN: /**endermen don't take fire, lava, or lava damage*/
                event.setCancelled(cause == EntityDamageEvent.DamageCause.FIRE_TICK || cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.LAVA || cause == EntityDamageEvent.DamageCause.FALL);
                break;
            case ENDERMITE:
            case SILVERFISH:
                event.setCancelled(cause == EntityDamageEvent.DamageCause.FIRE_TICK || cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.LAVA); /**silverfish and endermites are immune to lava and fire*/
                break;
        }

        boolean mobReduceDamage = cause.equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) || cause.equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) || cause.equals(EntityDamageEvent.DamageCause.LAVA) || cause.equals(EntityDamageEvent.DamageCause.FALL) || cause.equals(EntityDamageEvent.DamageCause.FALLING_BLOCK) || cause.equals(EntityDamageEvent.DamageCause.FIRE) || cause.equals(EntityDamageEvent.DamageCause.FIRE_TICK) || cause.equals(EntityDamageEvent.DamageCause.LIGHTNING) || cause.equals(EntityDamageEvent.DamageCause.SUFFOCATION);

        if (entityType != PLAYER && entityType != ENDER_DRAGON && entityType != WITHER) {  /**all non-player mobs take 95% less damage from these sources*/
            if (mobReduceDamage) {
                event.setDamage(event.getDamage() * 0.05);
            }
        }

        if (entityType == ENDER_DRAGON || entityType == WITHER) { /**ender dragon and wither heal from these damage sources*/
            if (mobReduceDamage) {
                ((LivingEntity)event.getEntity()).setMaxHealth(((LivingEntity)event.getEntity()).getMaxHealth() + event.getDamage() * 0.25); //increase max health by 25% of the damage dealt
                ((LivingEntity)event.getEntity()).setHealth(((LivingEntity)event.getEntity()).getHealth() + event.getDamage() * 0.25); //ender dragon and wither heal 25%
                event.setDamage(0.0);
            }
        }

        if (cause.equals(EntityDamageEvent.DamageCause.CRAMMING)) { /**no entity cramming to make sure that duplicating mobs don't cause an endless cycle*/
            event.setCancelled(true);
        }

        if (cause.equals(EntityDamageEvent.DamageCause.MAGIC) && entityType != PLAYER) { /**non-player mobs do not take damage from area effect clouds*/
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void entityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) && !(((CraftEntity)event.getEntity()).getHandle() instanceof CustomEntityChickenAggressive) && !(event.getDamager() instanceof Player) && !(((CraftEntity)event.getDamager()).getHandle() instanceof CustomEntityChickenAggressive)) { /**hostile mobs can't damage each other except aggressive chickens*/ //gettype doesn't seem to work so I'm using instanceof
            event.setCancelled(true);
        }
    }
}
