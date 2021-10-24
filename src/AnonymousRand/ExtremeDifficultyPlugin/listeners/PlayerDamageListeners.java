package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.*;
import net.minecraft.server.v1_16_R1.EntitySmallFireball;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Random;

import static org.bukkit.entity.EntityType.*;

public class PlayerDamageListeners implements Listener {

    protected Random rand = new Random();
    private static HashMap<Player, Boolean> blazeHit = new HashMap<>();
    private static HashMap<Player, Boolean> llamaHit = new HashMap<>();

    @EventHandler
    public void playerDamage(EntityDamageEvent event) {
        if (event.getEntityType() == PLAYER && event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
            if (blazeHit.getOrDefault(((Player)event.getEntity()), false)) { /**blaze fireballs only do 1 damage on impact*/
                blazeHit.replace((Player)event.getEntity(), false);
                event.setDamage(1.0);
            } else if (llamaHit.getOrDefault(((Player)event.getEntity()), false)) { /**llama spit does 15 damage on impact*/
                event.setDamage(15.0);
            }
        }

        if (event.getEntityType() == PLAYER && event.getCause().equals(EntityDamageEvent.DamageCause.DROWNING)) { /**drowning spawns a pufferfish per damage tick, with 25% chance to also spawn a guardian and a 5% chance to spawn an elder guardian*/
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
    }

    @EventHandler
    public void playerDamageByEntity(EntityDamageByEntityEvent event) { //change mob damage effects etc. if it is hard to do in game
        if (event.getEntityType() == PLAYER) {
            Player player = (Player)event.getEntity();

            switch (event.getDamager().getType()) {
                case CAVE_SPIDER:
                    CustomEntitySpiderCave caveSpider = (CustomEntitySpiderCave)(((CraftEntity)event.getDamager()).getHandle());
                    caveSpider.attacks++; //increase attack count by 1
                    break;
                case ENDERMAN:
                    CustomEntityEnderman enderman = (CustomEntityEnderman)(((CraftEntity)event.getDamager()).getHandle());
                    enderman.attacks++;
                    break;
                case ENDERMITE:
                    CustomEntityEndermite endermite = (CustomEntityEndermite)(((CraftEntity)event.getDamager()).getHandle());
                    endermite.attacks++;
                    break;
                case SILVERFISH:
                    CustomEntitySilverfish silverfish = (CustomEntitySilverfish)(((CraftEntity)event.getDamager()).getHandle());
                    silverfish.attacks++;

                    if (silverfish.attacks > 80 && rand.nextDouble() < 0.25) { /**silverfish hava a 25% chance to duplicate when hitting a player after 80 attacks*/
                        CustomEntitySilverfish newSilverfish = new CustomEntitySilverfish(silverfish.getWorld());
                        newSilverfish.setPositionRotation(silverfish.locX(), silverfish.locY(), silverfish.locZ(), silverfish.yaw, silverfish.pitch);
                        silverfish.getWorld().addEntity(newSilverfish, CreatureSpawnEvent.SpawnReason.NATURAL);
                    }
                    break;
                case SPIDER:
                    CustomEntitySpider spider = (CustomEntitySpider)(((CraftEntity)event.getDamager()).getHandle());
                    spider.attacks++;

                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 0)); /**spiders inflict slowness 1 for 1 second on hit*/

                    if ((spider).attacks >= 30) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 25, 0)); /**spiders inflict poison 1 for 2 damage ticks on hit if it has attacked more than 30 times*/
                    }
                    break;
            }
        }
    }

    @EventHandler
    public void playerHitByProjectile(ProjectileHitEvent event) {
        if (event.getHitEntity() instanceof Player) {
            if (((CraftEntity)event.getEntity()).getHandle() instanceof EntitySmallFireball) { /**blaze fireballs only do 1 damage on impact*/
                blazeHit.put(((Player)event.getHitEntity()), true);
            } else if (event.getEntity() instanceof LlamaSpit) { /**llama spit does 15 damage on impact*/
                llamaHit.put((Player)event.getHitEntity(), true);
            }
        }
    }

    @EventHandler
    public void entityAirChange(EntityAirChangeEvent event) {
        if (event.getEntityType() == PLAYER && event.getAmount() > 1) { /**player air goes down 3 times as fast*/
            event.setAmount(event.getAmount() - 2);
        }
    }
}
