package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.CustomEntityChickenAggressive;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.CustomEntityEnderDragon;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.CustomEntityIronGolem;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.CustomEntityZombieVillager;
import net.minecraft.server.v1_16_R1.Entity;
import net.minecraft.server.v1_16_R1.EntityArrow;
import net.minecraft.server.v1_16_R1.EntityPlayer;
import net.minecraft.server.v1_16_R1.EntityVillagerAbstract;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import static org.bukkit.entity.EntityType.*;

public class ListenerMobDamage implements Listener {

    @EventHandler
    public void entityDamage(EntityDamageEvent event) {
        EntityType bukkitEntityType = event.getEntityType();
        EntityDamageEvent.DamageCause cause = event.getCause();
        Entity nmsEntity = ((CraftEntity)event.getEntity()).getHandle();
        // todo easier to negate this?
        boolean checkCause = cause.equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) || cause.equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) || cause.equals(EntityDamageEvent.DamageCause.LAVA) || cause.equals(EntityDamageEvent.DamageCause.FALL) || cause.equals(EntityDamageEvent.DamageCause.LIGHTNING) || cause.equals(EntityDamageEvent.DamageCause.SUFFOCATION) || cause.equals(EntityDamageEvent.DamageCause.CONTACT) || cause.equals(EntityDamageEvent.DamageCause.DROWNING) || cause.equals(EntityDamageEvent.DamageCause.DRAGON_BREATH) || cause.equals(EntityDamageEvent.DamageCause.FALLING_BLOCK) || cause.equals(EntityDamageEvent.DamageCause.FIRE) || cause.equals(EntityDamageEvent.DamageCause.FIRE_TICK) || cause.equals(EntityDamageEvent.DamageCause.MAGIC) || cause.equals(EntityDamageEvent.DamageCause.POISON) || cause.equals(EntityDamageEvent.DamageCause.CRAMMING);

        /* all non-player mobs take no damage from these sources */
        if (checkCause) {
            if (bukkitEntityType != PLAYER && bukkitEntityType != ENDER_DRAGON && bukkitEntityType != WITHER) {
                event.setCancelled(true);
                return;
            // todo comment on how not canceling event might be for comedic red flash with heal?
            } else if (bukkitEntityType == ENDER_DRAGON || bukkitEntityType == WITHER) { /* ender dragon and wither gain max health and health equal to 20% of the damage dealt by these causes */
                LivingEntity livingEntity = (LivingEntity) event.getEntity();
                livingEntity.setMaxHealth(livingEntity.getMaxHealth() + event.getDamage() * 0.2);
                livingEntity.setHealth(livingEntity.getHealth() + event.getDamage() * 0.2);
                event.setDamage(0.0);
                return;
            }
        }

        if (bukkitEntityType != IRON_GOLEM && bukkitEntityType != PLAYER) { /* golems within 60 blocks of damaged entity get a 5% stat boost */
            nmsEntity.getWorld().getEntities(nmsEntity, nmsEntity.getBoundingBox().grow(60.0, 60.0, 60.0), entity -> entity instanceof CustomEntityIronGolem).forEach(entity -> ((CustomEntityIronGolem)entity).increaseStatsMultiply(1.05));
        }
    }

    @EventHandler
    public void entityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity nmsEntity = ((CraftEntity)event.getEntity()).getHandle();
        Entity nmsDamager = ((CraftEntity)event.getDamager()).getHandle();

        if (nmsDamager instanceof EntityArrow) {
            if (((EntityArrow)nmsDamager).getShooter() instanceof EntityPlayer) { /* player-shot arrows still do damage */
                return;
            }
        }

        // todo exclude villager too?
        if (!(nmsEntity instanceof EntityPlayer) && !(nmsEntity instanceof CustomEntityChickenAggressive) && !(nmsEntity instanceof EntityVillagerAbstract) && !(nmsDamager instanceof EntityPlayer) && !(nmsDamager instanceof CustomEntityChickenAggressive)) { /* hostile mobs can't damage each other except aggressive chickens and villagers/traders */ // gettype doesn't seem to work so I'm using instanceof
            event.setCancelled(true);
        }

        if (nmsDamager instanceof CustomEntityEnderDragon) { // just to make sure
            if (!(nmsEntity instanceof EntityPlayer)) { /* ender dragon can't fling non-player mobs */
                event.setCancelled(true);
                nmsEntity.setMot(0.0, 0.0, 0.0);
            }
        }

        if (nmsEntity instanceof EntityVillagerAbstract && nmsDamager instanceof CustomEntityZombieVillager) { /* up to 60 max health (80 after 12 attacks), zombie villagers gain 3 max health and health when hitting a villager */
            CustomEntityZombieVillager nmsZombieVillager = (CustomEntityZombieVillager)nmsDamager;
            LivingEntity bukkitDamager = ((LivingEntity) nmsDamager.getBukkitEntity());

            nmsZombieVillager.increaseAttacks(1); /* zombie villagers' attack counts increase when attacking villagers as well */

            if (bukkitDamager.getMaxHealth() <= (nmsZombieVillager.getAttacks() < 12 ? 57.0 : 77.0)) {
                bukkitDamager.setMaxHealth(bukkitDamager.getMaxHealth() + 3.0);
                bukkitDamager.setHealth(bukkitDamager.getHealth() + 3.0F);
            }
        }
    }
}
