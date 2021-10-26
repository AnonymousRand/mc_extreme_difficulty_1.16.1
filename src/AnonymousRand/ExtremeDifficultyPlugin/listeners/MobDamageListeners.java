package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntityChickenAggressive;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customProjectiles.CustomEntityLargeFireball;
import net.minecraft.server.v1_16_R1.Entity;
import net.minecraft.server.v1_16_R1.EntityMonster;
import net.minecraft.server.v1_16_R1.EntityPlayer;
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
            case CHICKEN: /**chickens don't take fire, lava, or explosion damage*/
                event.setCancelled(cause == EntityDamageEvent.DamageCause.FIRE_TICK || cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.LAVA || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION);
                break;
            case DROWNED:
            case SKELETON:
            case STRAY:
            case ZOMBIE: /**drowned, skeletons, strays and zombies are immune to fire damage*/
                event.setCancelled(cause == EntityDamageEvent.DamageCause.FIRE_TICK || cause == EntityDamageEvent.DamageCause.FIRE);
                break;
            case ENDERMAN:
            case SHEEP:/**endermen and sheep don't take fire, lava, or fall damage*/
                event.setCancelled(cause == EntityDamageEvent.DamageCause.FIRE_TICK || cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.LAVA || cause == EntityDamageEvent.DamageCause.FALL);
                break;
            case ENDERMITE:
            case SILVERFISH: /**silverfish and endermites don't take fire and lava damage*/
                event.setCancelled(cause == EntityDamageEvent.DamageCause.FIRE_TICK || cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.LAVA);
                break;
            case GHAST: /**ghasts don't take explosion damage*/
                event.setCancelled(cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION);
                break;
        }

        boolean checkCause = cause.equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) || cause.equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) || cause.equals(EntityDamageEvent.DamageCause.LAVA) || cause.equals(EntityDamageEvent.DamageCause.FALL) || cause.equals(EntityDamageEvent.DamageCause.FALLING_BLOCK) || cause.equals(EntityDamageEvent.DamageCause.FIRE) || cause.equals(EntityDamageEvent.DamageCause.FIRE_TICK) || cause.equals(EntityDamageEvent.DamageCause.LIGHTNING) || cause.equals(EntityDamageEvent.DamageCause.SUFFOCATION) || cause.equals(EntityDamageEvent.DamageCause.CONTACT);

        if (entityType != PLAYER && entityType != ENDER_DRAGON && entityType != WITHER) { /**all non-player mobs take 95% less damage from these sources*/
            if (checkCause) {
                event.setDamage(event.getDamage() * 0.05);
            }
        }

        if (entityType == ENDER_DRAGON || entityType == WITHER) { /**ender dragon and wither heal from these damage sources*/
            if (checkCause) {
                ((LivingEntity)event.getEntity()).setMaxHealth(((LivingEntity)event.getEntity()).getMaxHealth() + event.getDamage() * 0.25); //increase max health by 25% of the damage dealt
                ((LivingEntity)event.getEntity()).setHealth(((LivingEntity)event.getEntity()).getHealth() + event.getDamage() * 0.25); //ender dragon and wither heal 25%
                event.setDamage(0.0);
            }
        }

        if (cause.equals(EntityDamageEvent.DamageCause.CRAMMING)) { /**no entity cramming to make sure that duplicating mobs on death don't cause an endless cycle*/
            event.setCancelled(true);
        }

        if (cause.equals(EntityDamageEvent.DamageCause.MAGIC) && entityType != PLAYER) { /**non-player mobs do not take damage from area effect clouds*/
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void entityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity nmsEntity = ((CraftEntity)event.getEntity()).getHandle();
        Entity nmsDamager = ((CraftEntity)event.getDamager()).getHandle();
        
        if (!(nmsEntity instanceof EntityPlayer) && !(nmsEntity instanceof CustomEntityChickenAggressive) && !(nmsDamager instanceof EntityPlayer) && !(nmsDamager instanceof CustomEntityChickenAggressive)) { /**hostile mobs can't damage each other except aggressive chickens*/ //gettype doesn't seem to work so I'm using instanceof
            event.setCancelled(true);
        }
    }
}
