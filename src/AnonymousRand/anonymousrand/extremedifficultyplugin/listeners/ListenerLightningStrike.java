package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityZombieThor;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableLightningStorm;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableTornado;
import net.minecraft.server.v1_16_R1.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.Random;

public class ListenerLightningStrike implements Listener {
    
    public static boolean storm;
    public static int numberOfThors;
    private static final Random random = new Random();

    @EventHandler
    public void lightningSpawned(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.LIGHTNING) {
            LightningStrike bukkitLightning = (LightningStrike)event.getEntity();
            World bukkitWorld = bukkitLightning.getWorld();
            net.minecraft.server.v1_16_R1.World nmsWorld = ((CraftWorld)bukkitWorld).getHandle();
            Location loc = bukkitLightning.getLocation();
            Location entityLoc;

            if (loc.getBlock().getType() == Material.WATER) { /**lightning damage radiates through water up to a 42 by 42 box (max dist about 60 blocks), with lower damage the further from the strike from about 1-4*/
                for (Entity entity : bukkitWorld.getNearbyEntities(bukkitLightning.getBoundingBox().expand(42.0, 128.0, 42.0))) {
                    if (!(entity instanceof Player) && !(entity instanceof Fish)) {
                        continue;
                    }

                    entityLoc = entity.getLocation();

                    if (entityLoc.getBlock().getType() == Material.WATER) {
                        ((LivingEntity)entity).damage(4 - (loc.distance(entityLoc) / 20.0));
                    }
                }
            }

            if (!storm && random.nextDouble() < 0.025) { /**non-storm lightning has a 2.5% chance to summon a lightning storm in a 100 block radius area centered on the initial lightning strike*/
                new RunnableLightningStorm(nmsWorld, loc, random.nextInt(16) + 40).runTaskTimer(StaticPlugin.plugin, 0L, random.nextInt(3) + 2);
            }

            if (!storm && random.nextDouble() < 0.02 && numberOfThors < Bukkit.getOnlinePlayers().size()) { /**non-storm lightning has a 2% chance to summon thor, up to 1 thor per player*/
                new SpawnEntity(nmsWorld, new CustomEntityZombieThor(nmsWorld), 1, null, loc, true);
                numberOfThors++;
            }
        }
    }
}
