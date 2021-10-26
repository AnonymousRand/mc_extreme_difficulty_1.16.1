package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftProjectile;
import org.bukkit.entity.LlamaSpit;
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

    private JavaPlugin plugin;
    private static HashMap<EntityPlayer, Boolean> blazeHit = new HashMap<>();
    private static HashMap<EntityPlayer, Boolean> ghastHit = new HashMap<>();
    private static HashMap<EntityPlayer, Boolean> llamaHit = new HashMap<>();
    protected Random rand = new Random();

    public PlayerDamageListeners(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void playerDamageByEntity(EntityDamageByEntityEvent event) { //change mob damage effects etc. if it is hard to do in game
        if (event.getEntityType() == PLAYER) {
            Player bukkitPlayer = (Player)event.getEntity();
            Entity nmsDamager = ((CraftEntity)event.getDamager()).getHandle();

            switch (event.getDamager().getType()) {
                case CAVE_SPIDER:
                    ((CustomEntitySpiderCave)(nmsDamager)).attacks++; //increase attack count by 1
                    break;
                case CHICKEN:
                    ((CustomEntityChickenAggressive)(nmsDamager)).attacks++;
                    break;
                case ENDERMAN:
                    ((CustomEntityEnderman)(nmsDamager)).attacks++;
                    break;
                case ENDERMITE:
                    ((CustomEntityEndermite)(nmsDamager)).attacks++;
                    break;
                case RABBIT:
                    ((CustomEntityRabbit)(nmsDamager)).attacks++;
                    break;
                case RAVAGER:
                    CustomEntityRavager ravager = (CustomEntityRavager)(nmsDamager);
                    ravager.attacks++;
                    Vector origin = bukkitPlayer.getVelocity();

                    if (ravager.launchHigh) {
                        Bukkit.broadcastMessage("You really thought you could get away with that?");
                        ravager.launchHigh = false;
                        event.setDamage(10.0);
                        bukkitPlayer.setVelocity(new Vector(0.0, 5.0, 0.0));
                    }

                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable()
                    {
                        public void run()
                        {
                            if (Math.abs(bukkitPlayer.getVelocity().getX()) < 0.15 && Math.abs(bukkitPlayer.getVelocity().getY()) < 0.15 && Math.abs(bukkitPlayer.getVelocity().getZ()) < 0.15) { /**if the player has traveled 3 blocks or less in the span of 5 ticks (meaning it did not get knockbacked enough), the next attack the player will be flung high into the air if they are jumping and damage will be increased to 10*/
                                ravager.launchHigh = true;
                            }
                        }
                    }, 5L);

                    break;
                case SHEEP:
                    ((CustomEntitySheepAggressive)(nmsDamager)).attacks++;
                    break;
                case SILVERFISH:
                    CustomEntitySilverfish silverfish = ((CustomEntitySilverfish)(nmsDamager));
                    silverfish.attacks++;

                    if (silverfish.attacks > 80 && rand.nextDouble() < 0.25) { /**silverfish hava a 25% chance to duplicate when hitting a player after 80 attacks*/
                        CustomEntitySilverfish newSilverfish = new CustomEntitySilverfish(silverfish.getWorld());
                        newSilverfish.setPositionRotation(silverfish.locX(), silverfish.locY(), silverfish.locZ(), silverfish.yaw, silverfish.pitch);
                        silverfish.getWorld().addEntity(newSilverfish, CreatureSpawnEvent.SpawnReason.NATURAL);
                    }
                    break;
                case SPIDER:
                    CustomEntitySpider spider = (CustomEntitySpider)(nmsDamager);
                    spider.attacks++;

                    bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 0)); /**spiders inflict slowness 1 for 1.5 secondS on hit*/

                    if ((spider).attacks >= 35) {
                        bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 25, 0)); /**spiders inflict poison 1 for 2 damage ticks on hit if it has attacked more than 35 times*/
                    }
                    break;
            }
        }
    }

    @EventHandler
    public void playerHitByProjectile(ProjectileHitEvent event) {
        Entity nmsProjectile = ((CraftEntity)event.getEntity()).getHandle();

        if (event.getHitEntity() instanceof Player) {
            EntityPlayer nmsPlayer = (((CraftPlayer)event.getHitEntity()).getHandle());

            if (nmsProjectile instanceof EntitySmallFireball) { /**blaze fireballs only do 1 damage on impact*/
                blazeHit.put(nmsPlayer, true);
            } else if (nmsProjectile instanceof EntityLlamaSpit) { /**llama spit does 15 damage on impact*/
                llamaHit.put(nmsPlayer, true);
            } else if (nmsProjectile instanceof EntityLargeFireball) {
                ghastHit.put(nmsPlayer, true);
            }
        }
    }

    @EventHandler
    public void playerDamage(EntityDamageEvent event) {
        if (event.getEntityType() == PLAYER) {
            if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) { //not perfect in terms of sync but the listeners are usually 0-1 tick apart
                EntityPlayer player = (EntityPlayer) ((CraftEntity) event.getEntity()).getHandle();

                if (llamaHit.getOrDefault(player, false)) { /**llama spit does 14 damage on impact*/
                    llamaHit.replace(player, false);
                    event.setDamage(14.0);
                } else if (blazeHit.getOrDefault(player, false)) { /**blaze fireballs only do 1 damage on impact*/
                    blazeHit.replace(player, false);
                    event.setDamage(1.0);
                } else if (ghastHit.getOrDefault(player, false)) { /**ghast fireballs only do 1 damage on impact*/
                    ghastHit.replace(player, false);
                    event.setDamage(1.0);
                }
            }

            if (event.getCause().equals(EntityDamageEvent.DamageCause.DROWNING)) { /**drowning spawns a pufferfish per damage tick, with 25% chance to also spawn a guardian and a 5% chance to spawn an elder guardian*/
                Player player = (Player)event.getEntity();
                Location loc = player.getLocation();

                if (rand.nextDouble() < 0.25) {
                    CustomEntityGuardian guardian = new CustomEntityGuardian(((CraftWorld)player.getWorld()).getHandle());
                    guardian.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)player.getWorld()).getHandle().addEntity(guardian, CreatureSpawnEvent.SpawnReason.NATURAL);
                } else if (rand.nextDouble() < 0.05) {
                    CustomEntityZombie zombie = new CustomEntityZombie(((CraftWorld)player.getWorld()).getHandle()); //todo change to elder guardian
                    zombie.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)player.getWorld()).getHandle().addEntity(zombie, CreatureSpawnEvent.SpawnReason.NATURAL);
                } else {
                    CustomEntityPiglin piglin = new CustomEntityPiglin(((CraftWorld)player.getWorld()).getHandle()); //todo change to pufferfish
                    piglin.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)player.getWorld()).getHandle().addEntity(piglin, CreatureSpawnEvent.SpawnReason.NATURAL);
                }
            }

            if (event.getCause().equals(EntityDamageEvent.DamageCause.CONTACT)) {
                event.setDamage(20.0); //cactus does 20 damage per tick
            }
        }
    }

    @EventHandler
    public void entityAirChange(EntityAirChangeEvent event) {
        if (event.getEntityType() == PLAYER && event.getAmount() >= 2) { /**player air goes down 3 times as fast*/
            event.setAmount(event.getAmount() - 2);
        }
    }
}
