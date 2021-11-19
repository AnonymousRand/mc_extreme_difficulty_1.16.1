package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityPhantom;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityZombieSuper;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import net.minecraft.server.v1_16_R1.EntityPhantom;
import net.minecraft.server.v1_16_R1.EntityTypes;
import net.minecraft.server.v1_16_R1.EntityZombie;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class PlayerDeathAndRespawnListeners implements Listener {

    private static HashMap<Player, Collection<PotionEffect>> collections = new HashMap<>();
    private static HashMap<Player, Integer> respawnCount = new HashMap<>();
    public static ArrayList<EntityZombie> superZombies = new ArrayList<>();

    @EventHandler
    public void playerDeath(PlayerDeathEvent event) {
        Player bukkitPlayer = event.getEntity();

        collections.put(bukkitPlayer, bukkitPlayer.getActivePotionEffects()); /**negative status effects now last after respawning*/

        if (((CraftPlayer)bukkitPlayer).getHandle().getLastDamager() instanceof EntityZombie) { /**when players are killed by a zombie-type mob, a super zombie is spawned at the death location and it will pick up armor, tools etc*/
            World nmsWorld = ((CraftWorld)event.getEntity().getWorld()).getHandle();
            CustomEntityZombieSuper newZombie = new CustomEntityZombieSuper(nmsWorld);
            newZombie.getBukkitEntity().setCustomName("Dinnerbone");
            new SpawnLivingEntity(nmsWorld, newZombie, 1, null, bukkitPlayer.getLocation(), true);
            superZombies.add(newZombie);
        }
    }

    @EventHandler
    public void playerRespawn(PlayerRespawnEvent event) {
        Player bukkitPlayer = event.getPlayer();

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> { //delay by 1 tick or else the server does not re-apply the status effects, thinking that the player doesn't exist yet
            for (PotionEffect e : collections.getOrDefault(bukkitPlayer, Collections.emptyList())) { //only re-applies negative status effects
                if (e.getType().equals(PotionEffectType.SLOW) || e.getType().equals(PotionEffectType.SLOW_DIGGING) || e.getType().equals(PotionEffectType.CONFUSION) || e.getType().equals(PotionEffectType.BLINDNESS) || e.getType().equals(PotionEffectType.HUNGER) || e.getType().equals(PotionEffectType.WEAKNESS) || e.getType().equals(PotionEffectType.POISON) || e.getType().equals(PotionEffectType.WITHER) || e.getType().equals(PotionEffectType.LEVITATION) || e.getType().equals(PotionEffectType.UNLUCK) || e.getType().equals(PotionEffectType.BAD_OMEN)) {
                    bukkitPlayer.addPotionEffect(e);
                }
            }

            respawnCount.put(bukkitPlayer, respawnCount.getOrDefault(bukkitPlayer, 0) + 1);

            if (respawnCount.get(bukkitPlayer) % 2 == 0) { /**create explosion on respawn location every 2 respawns regardless of if they switched beds/anchors*/
                bukkitPlayer.getWorld().createExplosion(event.getRespawnLocation(), 1.5F);
            }

            if (respawnCount.get(bukkitPlayer) % 5 == 0) { /**summon phantoms on respawn location every 5 respawns equal to the number of respawns divided by 5*/
                World nmsWorld = ((CraftWorld)bukkitPlayer.getWorld()).getHandle();
                MobSpawnAndReplaceWithCustomListeners.phantomSize += 0.05 / Bukkit.getServer().getOnlinePlayers().size() * respawnCount.get(bukkitPlayer) / 5;
                new SpawnLivingEntity(nmsWorld, new CustomEntityPhantom(nmsWorld, (int)MobSpawnAndReplaceWithCustomListeners.phantomSize), respawnCount.get(bukkitPlayer) / 5, null, bukkitPlayer.getLocation(), false);
            }

            if (superZombies.size() >= (3 * Bukkit.getServer().getOnlinePlayers().size())) { /**don't have more than 3 super zombies per player in the world at a time to avoid lag*/
                superZombies.get(0).die();
                superZombies.remove(0);
            }
        }, 1L);
    }

    @EventHandler
    public void totemUse(EntityResurrectEvent event) {
        if (event.getEntity() instanceof Player) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> {
                for (PotionEffect effect : event.getEntity().getActivePotionEffects()) { /**totems leave the player at 1 heart without any status effects*/
                    event.getEntity().removePotionEffect(effect.getType());
                }
            }, 4L);
        }
    }
}