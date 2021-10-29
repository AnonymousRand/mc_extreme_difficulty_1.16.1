package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.misc.CustomEntityLightning;
import AnonymousRand.ExtremeDifficultyPlugin.util.CoordsFromHypotenuse;
import net.minecraft.server.v1_16_R1.BlockPosition;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class LightningStrikeListeners implements Listener {
    
    private final JavaPlugin plugin;
    private static boolean storm;
    private final Random rand = new Random();

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

        if (!storm && rand.nextDouble() < 0.1) { /**non-storm lightning has a 10% chance to summon a lightning storm in a 100 block radius area centered on the initial lightning strike*/
            storm = true;
            new LightningStorm(nmsWorld, bukkitWorld, loc, rand.nextInt(20) + 40).runTaskTimer(this.plugin, 0L, rand.nextInt(5) + 3);
            storm = false;
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

            if (!storm && rand.nextDouble() < 0.1) { /**non-storm lightning has a 10% chance to summon a lightning storm in a 100 block radius area centered on the initial lightning strike*/
                storm = true;
                new LightningStorm(nmsWorld, bukkitWorld, loc, rand.nextInt(20) + 40).runTaskTimer(this.plugin, 0L, rand.nextInt(5) + 3);
            }
        }
    }

    static class LightningStorm extends BukkitRunnable {
        private CustomEntityLightning lightning;
        private final net.minecraft.server.v1_16_R1.World nmsWorld;
        private final World bukkitWorld;
        private final Location loc;
        private Location loc2;
        private int cycles, maxCycles;
        private final CoordsFromHypotenuse coordsFromHypotenuse = new CoordsFromHypotenuse();
        private final Random rand = new Random();

        public LightningStorm(net.minecraft.server.v1_16_R1.World nmsWorld, World bukkitWorld, Location loc, int maxCycles) {
            this.nmsWorld = nmsWorld;
            this.bukkitWorld = bukkitWorld;
            this.loc = loc;
            this.cycles = 0;
            this.maxCycles = maxCycles;
        }

        public void run()
        {
            if (++this.cycles < this.maxCycles) {
                this.loc2 = coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(this.loc.getX(), this.loc.getY(), this.loc.getZ()), rand.nextInt(101), this.bukkitWorld.getHighestBlockYAt(this.loc), 361.0);
                this.lightning = new CustomEntityLightning(this.nmsWorld);
                this.lightning.setPosition(this.loc2.getX(), this.loc2.getY(), this.loc2.getZ());
                this.nmsWorld.addEntity(this.lightning);
            } else if (this.cycles - 20 >= this.maxCycles) {
                this.cancel();
                storm = false;
            }
        }
    }
}
