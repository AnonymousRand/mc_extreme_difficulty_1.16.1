package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.bukkitrunnables.UtilLightningStorm;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class LightningStrikeListeners implements Listener {
    
    private final JavaPlugin plugin;
    public static boolean storm;
    private final Random random = new Random();

    public LightningStrikeListeners(JavaPlugin plugin) {
        this.plugin = plugin;
        storm = false;
    }

    @EventHandler
    public void lightningStrike(LightningStrikeEvent event) {
        LightningStrike bukkitLightning = event.getLightning();
        World bukkitWorld = event.getWorld();
        net.minecraft.server.v1_16_R1.World nmsWorld = ((CraftWorld)bukkitWorld).getHandle();
        Location loc = bukkitLightning.getLocation().add(0.0, -1.0, 0.0);
        Location entityLoc;

        if (loc.getBlock().getType() == Material.WATER) { /**lightning damage radiates through water up to a 42 by 42 box (max dist about 60 blocks), with lower damage the further from the strike from about 1-4.5*/
            for (Entity entity : bukkitWorld.getNearbyEntities(bukkitLightning.getBoundingBox().expand(42.0, 128.0, 42.0))) {
                if (!(entity instanceof Player) && !(entity instanceof Fish)) {
                    continue;
                }

                entityLoc = entity.getLocation();

                if (entityLoc.getBlock().getType() == Material.WATER) {
                    ((LivingEntity)entity).damage(4.5 - (loc.distance(entityLoc) / 17.0));
                }
            }
        }

        if (!storm && random.nextDouble() < 0.1) { /**non-storm lightning has a 10% chance to summon a lightning storm in a 100 block radius area centered on the initial lightning strike*/
            storm = true;
            new UtilLightningStorm(nmsWorld, bukkitWorld, loc, random.nextInt(25) + 35).runTaskTimer(this.plugin, 0L, random.nextInt(5) + 3);
        }
    }

    @EventHandler
    public void lightningSpawned(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.LIGHTNING) {
            LightningStrike bukkitLightning = (LightningStrike)event.getEntity();
            World bukkitWorld = bukkitLightning.getWorld();
            net.minecraft.server.v1_16_R1.World nmsWorld = ((CraftWorld)bukkitWorld).getHandle();
            Location loc = bukkitLightning.getLocation();
            Location entityLoc;

            if (loc.getBlock().getType() == Material.WATER) { /**lightning damage radiates through water up to a 42 by 42 box (max dist about 60 blocks), with lower damage the further from the strike from about 1-4.5*/
                for (Entity entity : bukkitWorld.getNearbyEntities(bukkitLightning.getBoundingBox().expand(42.0, 128.0, 42.0))) {
                    if (!(entity instanceof Player) && !(entity instanceof Fish)) {
                        continue;
                    }

                    entityLoc = entity.getLocation();

                    if (entityLoc.getBlock().getType() == Material.WATER) {
                        ((LivingEntity)entity).damage(4.5 - (loc.distance(entityLoc) / 17.0));
                    }
                }
            }

            if (!storm && random.nextDouble() < 0.1) { /**non-storm lightning has a 10% chance to summon a lightning storm in a 100 block radius area centered on the initial lightning strike*/
                storm = true;
                new UtilLightningStorm(nmsWorld, bukkitWorld, loc, random.nextInt(25) + 35).runTaskTimer(this.plugin, 0L, random.nextInt(5) + 3);
            }
        }
    }
}
