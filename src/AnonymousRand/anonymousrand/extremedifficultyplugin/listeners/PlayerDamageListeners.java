package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Random;

import static org.bukkit.entity.EntityType.*;

public class PlayerDamageListeners implements Listener {

    private final JavaPlugin plugin;
    private final Random random = new Random();

    public PlayerDamageListeners(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void playerDamageByEntity(EntityDamageByEntityEvent event) { //change mob damage effects and attack counts etc. if it is hard to do in their custom entity classes
        if (event.getEntityType() == PLAYER) {
            Player bukkitPlayer = (Player)event.getEntity();
            Entity nmsDamager = ((CraftEntity)event.getDamager()).getHandle();
            World nmsWorld = nmsDamager.getWorld();

            switch (event.getDamager().getType()) {
                case BAT -> ((CustomEntityBat)nmsDamager).attacks++; //increase attack count by 1
                case CAVE_SPIDER -> ((CustomEntitySpiderCave)nmsDamager).attacks++;
                case CHICKEN -> ((CustomEntityChickenAggressive)nmsDamager).attacks++;
                case ENDERMAN -> ((CustomEntityEnderman)nmsDamager).attacks++;
                case ENDERMITE -> ((CustomEntityEndermite)nmsDamager).attacks++;
                case HOGLIN -> {
                    CustomEntityHoglin hoglin = (CustomEntityHoglin)nmsDamager;
                    hoglin.attacks++;

                    if (hoglin.attacks >= 25 && hoglin.attacks < 59 && (hoglin.attacks - 25) % 3 == 0) { /**from 25 to 59 attacks, hoglins gain 4 max health every 3 attacks*/
                        hoglin.addEffect(new MobEffect(MobEffects.HEALTH_BOOST, Integer.MAX_VALUE, (hoglin.attacks - 25) / 3));
                    } else if (hoglin.attacks >= 59 && hoglin.attacks < 70) { /**from 59 to 70 attacks, hoglin loses 4 max health but gains 1.2 damage per attack*/
                        int amp = hoglin.getEffect(MobEffects.HEALTH_BOOST).getAmplifier();
                        hoglin.removeEffect(MobEffects.HEALTH_BOOST);
                        hoglin.addEffect(new MobEffect(MobEffects.HEALTH_BOOST, Integer.MAX_VALUE, amp - 1));

                        hoglin.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(hoglin.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue() + 1.2);
                    }

                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {
                            bukkitPlayer.setVelocity(new Vector(0.0, 0.8 * (hoglin.isBaby() ? 2.0 : 1.0), 0.0));  /**hoglins launch players into air, doubled if baby*/
                        }
                    }, 2L);
                }
                case RABBIT -> ((CustomEntityRabbit)nmsDamager).attacks++;
                case RAVAGER -> {
                    CustomEntityRavager ravager = (CustomEntityRavager)nmsDamager;
                    ravager.attacks++;

                    if (ravager.launchHigh) {
                        Bukkit.broadcastMessage("You really thought you could get away with that?");
                        ravager.launchHigh = false;
                        event.setDamage(6.0);
                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                            public void run() {
                                bukkitPlayer.setVelocity(new Vector(0.0, 1.0, 0.0));
                            }
                        }, 2L);
                    }

                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {
                            if (Math.abs(bukkitPlayer.getVelocity().getX()) < 0.14 && Math.abs(bukkitPlayer.getVelocity().getY()) < 0.14 && Math.abs(bukkitPlayer.getVelocity().getZ()) < 0.14) { /**if the player has not moved much after 5 ticks (meaning it did not get knockbacked enough), the next attack the player will be flung high into the air if they are jumping and damage will be increased to 6*/
                                ravager.launchHigh = true;
                            }
                        }
                    }, 5L);
                }
                case SHEEP -> {
                    CustomEntitySheepAggressive sheep = (CustomEntitySheepAggressive)nmsDamager;
                    sheep.attacks++;

                    if (sheep.launchHigh) {
                        Bukkit.broadcastMessage("You really thought you could get away with that?");
                        sheep.launchHigh = false;
                        event.setDamage(9.0);
                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                            public void run() {
                                bukkitPlayer.setVelocity(new Vector(0.0, 1.5, 0.0));
                            }
                        }, 2L);
                    }

                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {
                            if (Math.abs(bukkitPlayer.getVelocity().getX()) < 0.14 && Math.abs(bukkitPlayer.getVelocity().getY()) < 0.14 && Math.abs(bukkitPlayer.getVelocity().getZ()) < 0.14) { /**if the player has not moved much after 5 ticks (meaning it did not get knockbacked enough), the next attack the player will be flung high into the air if they are jumping and damage will be increased to 9*/
                                sheep.launchHigh = true;
                            }
                        }
                    }, 5L);
                }
                case SILVERFISH -> {
                    CustomEntitySilverfish silverfish = (CustomEntitySilverfish)nmsDamager;
                    silverfish.attacks++;

                    if (silverfish.attacks > 60 && random.nextDouble() < 0.2) { /**silverfish hava a 20% chance to duplicate when hitting a player after 60 attacks*/
                        new SpawnLivingEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 1, null, null, silverfish, false, true).run();
                    }
                }
                case SPIDER -> {
                    CustomEntitySpider spider = (CustomEntitySpider)nmsDamager;
                    spider.attacks++;
                    bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 0)); /**spiders inflict slowness 1 for 1.5 secondS on hit*/

                    if ((spider).attacks >= 25) {
                        bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 25, 0)); /**spiders inflict poison 1 for 2 damage ticks on hit if it has attacked more than 25 times*/
                    }
                }
                case ZOGLIN -> {
                    CustomEntityZoglin zoglin = (CustomEntityZoglin)nmsDamager;
                    zoglin.attacks++;

                    if (zoglin.attacks >= 40) {
                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                            public void run() {
                                bukkitPlayer.setVelocity(new Vector(0.0, 0.75, 0.0)); /**after 40 attacks, zoglins throw players into the air when it hits the player*/
                            }
                        }, 2L);
                    }
                }
                case ZOMBIE -> {
                    org.bukkit.World bukkitWorld = bukkitPlayer.getWorld();

                    if (nmsDamager instanceof CustomEntityZombieThor) {
                        bukkitWorld.strikeLightning(bukkitPlayer.getLocation());
                    } else {
                        CustomEntityZombie zombie = (CustomEntityZombie)nmsDamager;
                        zombie.attacks++;

                        if (zombie.attacks >= 35) { /**after 35 attacks, zombies summon vanilla lightning on the player when it hits the player*/
                            bukkitWorld.strikeLightning(bukkitPlayer.getLocation());
                        }
                    }
                }
                case ZOMBIE_VILLAGER -> ((CustomEntityZombieVillager)nmsDamager).attacks++;
            }
        }
    }

    @EventHandler
    public void playerDamage(EntityDamageEvent event) {
        EntityDamageEvent.DamageCause cause = event.getCause();
        
        if (event.getEntityType() == PLAYER) {
            EntityPlayer nmsPlayer = (EntityPlayer)((CraftEntity) event.getEntity()).getHandle();

            if (cause.equals(EntityDamageEvent.DamageCause.DROWNING)) { /**drowning spawns a pufferfish per damage tick, with 20% chance to also spawn a guardian and a 2.5% chance to spawn an elder guardian*/
                World nmsWorld = nmsPlayer.getWorld();

                if (this.random.nextDouble() < 0.2) {
                    new SpawnLivingEntity(nmsWorld, new CustomEntityGuardian(nmsWorld), 1, null, null, nmsPlayer, false, true).run();
                } else if (this.random.nextDouble() < 0.025) {
                    new SpawnLivingEntity(nmsWorld, new CustomEntityGuardianElder(nmsWorld), 1, null, null, nmsPlayer, false, true).run();
                } else {
                    //todo pufferfish
                }
            }

            if (cause.equals(EntityDamageEvent.DamageCause.CONTACT)) {
                event.setDamage(15.0); /**cactus does 15 damage per tick*/
            }

            if (cause.equals(EntityDamageEvent.DamageCause.LIGHTNING)) {
                event.setDamage(1.5); /**lightning only does 1.5 damage instead of 5*/
            }
        }
    }

    @EventHandler
    public void entityAirChange(EntityAirChangeEvent event) {
        if (event.getEntityType() == PLAYER && event.getAmount() >= 2) { /**player air goes down 2 times as fast*/
            event.setAmount(event.getAmount() - 1);
        }
    }
}
