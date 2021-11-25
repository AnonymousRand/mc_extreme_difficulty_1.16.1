package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityChickenAggressive;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityEnderDragon;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityIronGolem;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityZombieVillager;
import net.minecraft.server.v1_16_R1.*;
import net.minecraft.server.v1_16_R1.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import static org.bukkit.entity.EntityType.*;

public class ListenerMobDamage implements Listener {

    @EventHandler
    public void entityDamage(EntityDamageEvent event) {
        EntityType entityType = event.getEntityType();
        EntityDamageEvent.DamageCause cause = event.getCause();
        Entity nmsEntity = ((CraftEntity)event.getEntity()).getHandle();
        boolean checkCause = cause.equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) || cause.equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) || cause.equals(EntityDamageEvent.DamageCause.LAVA) || cause.equals(EntityDamageEvent.DamageCause.FALL) || cause.equals(EntityDamageEvent.DamageCause.LIGHTNING) || cause.equals(EntityDamageEvent.DamageCause.SUFFOCATION) || cause.equals(EntityDamageEvent.DamageCause.CONTACT) || cause.equals(EntityDamageEvent.DamageCause.DROWNING);

        switch (entityType) { //natural damage immunities by specific mobs
            case BAT, CHICKEN, HOGLIN, ZOGLIN -> /**bats, chickens, hoglins and zoglins don't take lava or explosion damage*/
                event.setCancelled(cause == EntityDamageEvent.DamageCause.LAVA || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION);
            case DROWNED, HUSK, IRON_GOLEM, PHANTOM, ZOMBIE, ZOMBIE_VILLAGER -> /**drowned, husks, iron golems, phantoms, zombies, and zombie villagers don't take suffocation, lightning or explosion damage*/
                event.setCancelled(cause == EntityDamageEvent.DamageCause.SUFFOCATION || cause == EntityDamageEvent.DamageCause.LIGHTNING || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION);
            case ENDER_DRAGON, WITHER -> {
                if (checkCause) {
                    LivingEntity livingEntity = (LivingEntity)event.getEntity();
                    livingEntity.setMaxHealth(livingEntity.getMaxHealth() + event.getDamage() * 0.25); //increase max health by 25% of the damage dealt
                    livingEntity.setHealth(livingEntity.getHealth() + event.getDamage() * 0.25); //ender dragon and wither heal 25%
                    event.setDamage(0.0);
                }
            }
            case ENDERMAN, SHEEP -> /**endermen and sheep don't take lava, explosion,or fall damage*/
                event.setCancelled(cause == EntityDamageEvent.DamageCause.LAVA || cause == EntityDamageEvent.DamageCause.FALL || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION);
            case ENDERMITE, SILVERFISH -> /**endermites and silverfish don't take lava, or suffocation damage*/
                event.setCancelled(cause == EntityDamageEvent.DamageCause.LAVA || cause == EntityDamageEvent.DamageCause.SUFFOCATION);
            case GHAST, LLAMA, SHULKER, TRADER_LLAMA -> /**ghasts, llamas, shulkers, and trader llamas don't take explosion damage*/
                event.setCancelled(cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION);
            case ILLUSIONER -> /**illusioners don't take explosion and projectile damage*/
                event.setCancelled(cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || cause == EntityDamageEvent.DamageCause.PROJECTILE);
            case MAGMA_CUBE, SLIME -> /**magma cubes and slimes don't take explosion, fall or suffocation damage*/
                event.setCancelled(cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || cause == EntityDamageEvent.DamageCause.FALL || cause == EntityDamageEvent.DamageCause.SUFFOCATION);
            case PIGLIN -> /**piglins don't take lava, explosion, suffocation, or projectile damage*/
                event.setCancelled(cause == EntityDamageEvent.DamageCause.LAVA || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || cause == EntityDamageEvent.DamageCause.SUFFOCATION || cause == EntityDamageEvent.DamageCause.PROJECTILE);
            case PLAYER -> {
                if (((EntityPlayer)nmsEntity).isBlocking()) { /**all attacks damage shields 50% more (at least 4 damage)*/
                    event.setDamage(Math.max(event.getDamage() * 1.5, 4.0));
                }
            }
            case VEX -> /**vexes don't take starvation damage*/
                event.setCancelled(cause == EntityDamageEvent.DamageCause.STARVATION);
            case WITCH -> /**witches don't take drowning damage*/
                event.setCancelled(cause == EntityDamageEvent.DamageCause.DROWNING);
        }

        if (event.isCancelled()) {
            return;
        }

        switch (cause) {
            case CRAMMING -> { /**no entity cramming to make sure that duplicating mobs when killed don't cause an endless cycle*/
                event.setCancelled(true);
                return;
            }
            case DRAGON_BREATH, FALLING_BLOCK, FIRE, FIRE_TICK, MAGIC, POISON -> { /**non-player mobs do not take damage from area effect clouds, fire, falling anvils, or poison/harm potions*/
                if (entityType != PLAYER) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (entityType != PLAYER && entityType != ENDER_DRAGON && entityType != WITHER) { /**all non-player mobs take 95% less damage from these sources*/
            if (checkCause) {
                event.setDamage(event.getDamage() * 0.05);
            }
        }

        if (entityType != IRON_GOLEM && entityType != PLAYER) { /**golems within 40 blocks horizontally of damaged entity get a 15% stat boost*/
            nmsEntity.getWorld().getEntities(nmsEntity, nmsEntity.getBoundingBox().grow(40.0, 128.0, 40.0), entity -> entity instanceof CustomEntityIronGolem).forEach(entity -> {
                ((CustomEntityIronGolem)entity).increaseStatsMultiply(1.15);
            });
        }
    }

    @EventHandler
    public void entityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity nmsEntity = ((CraftEntity)event.getEntity()).getHandle();
        Entity nmsDamager = ((CraftEntity)event.getDamager()).getHandle();

        if (nmsDamager instanceof EntityArrow) {
            if (((EntityArrow)nmsDamager).getShooter() instanceof EntityPlayer) { /**player-shot arrows still do damage*/
                return;
            }
        }

        if (!(nmsEntity instanceof EntityPlayer) && !(nmsEntity instanceof CustomEntityChickenAggressive) && !(nmsEntity instanceof EntityVillagerAbstract) && !(nmsDamager instanceof EntityPlayer) && !(nmsDamager instanceof CustomEntityChickenAggressive)) { /**hostile mobs can't damage each other except aggressive chickens and villagers/traders*/ //gettype doesn't seem to work so I'm using instanceof
            event.setCancelled(true);
        }

        if (nmsDamager instanceof CustomEntityEnderDragon) {
            if (!(nmsEntity instanceof EntityPlayer)) { /**ender dragon can't fling non-player mobs*/
                event.setCancelled(true); //just to make sure
                nmsEntity.setMot(0.0, 0.0, 0.0);
            }
        }

        if (nmsEntity instanceof EntityVillagerAbstract && nmsDamager instanceof CustomEntityZombieVillager) { /**up to 60 max health (80 after 12 attacks), zombie villagers gain 3 max health and health when hitting a villager*/
            CustomEntityZombieVillager nmsZombieVillager = (CustomEntityZombieVillager)nmsDamager;
            LivingEntity bukkitDamager = ((LivingEntity)nmsDamager.getBukkitEntity());

            nmsZombieVillager.attacks++; /**zombie villagers' attack counts increase when attacking villagers as well*/

            if (bukkitDamager.getMaxHealth() <= (nmsZombieVillager.attacks < 12 ? 57.0 : 77.0)) {
                bukkitDamager.setMaxHealth(bukkitDamager.getMaxHealth() + 3.0);
                bukkitDamager.setHealth(bukkitDamager.getHealth() + 3.0F);
            }
        }
    }
}
