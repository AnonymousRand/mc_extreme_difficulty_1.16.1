package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityZombieThor;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.LightningStorm;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
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
                new LightningStorm(nmsWorld, loc, this.random.nextInt(16) + 40).runTaskTimer(this.plugin, 0L, this.random.nextInt(4) + 2);
            }

            if (!storm && random.nextDouble() < 0.0175) { /**non-storm lightning has a 1.75% chance to summon thor*/
                new SpawnLivingEntity(this.plugin, nmsWorld, new CustomEntityZombieThor(nmsWorld, this.plugin), 1, null, loc, true).run();
            }
        }
    }
}
