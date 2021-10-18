package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntityPiglin;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntitySkeleton;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntityZombie;
import net.minecraft.server.v1_16_R1.EntityPlayer;
import net.minecraft.server.v1_16_R1.EntitySmallFireball;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEgg;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
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

    @EventHandler
    public void playerDamage(EntityDamageEvent event) {
        if (event.getEntityType() == PLAYER && event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE) && blazeHit.getOrDefault(((Player)event.getEntity()), false)) { /**blaze fireballs only do 1 damage on impact*/
            blazeHit.replace((Player)event.getEntity(), false);
            event.setDamage(1.0);
        }

        if (event.getEntityType() == PLAYER && event.getCause().equals(EntityDamageEvent.DamageCause.DROWNING)) { /**drowning spawns a pufferfish per damage tick, with 25% chance to also spawn a guardian and a 5% chance to spawn an elder guardian*/
            Player player = (Player)event.getEntity();
            Location loc = player.getLocation();

            if (rand.nextDouble() < 0.25) {
                CustomEntitySkeleton skeleton = new CustomEntitySkeleton(((CraftWorld)player.getWorld()).getHandle()); //todo change to guardian
                skeleton.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                ((CraftWorld)player.getWorld()).getHandle().addEntity(skeleton, CreatureSpawnEvent.SpawnReason.NATURAL);
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
    public void playerDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntityType() == PLAYER) {
            switch (event.getDamager().getType()) {
                case SPIDER:
                    ((Player)event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 0)); /**spiders inflict Slowness 1 for 1 second on hit*/
                    break;
            }
        }
    }

    @EventHandler
    public void playerHitByProjectile(ProjectileHitEvent event) { /**blaze fireballs only do 1 damage on impact*/
        if (event.getHitEntity() instanceof Player && ((CraftEntity)event.getEntity()).getHandle() instanceof EntitySmallFireball) {
            blazeHit.put(((Player)event.getHitEntity()), true);
        }
    }

    @EventHandler
    public void entityAirChange(EntityAirChangeEvent event) {
        if (event.getEntityType() == PLAYER && event.getAmount() > 1) { /**air goes down 3 times as fast*/
            event.setAmount(event.getAmount() - 2);
        }
    }
}
