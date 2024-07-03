package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityZombieThor;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableLightningStorm;
import net.minecraft.server.v1_16_R1.DamageSource;
import net.minecraft.server.v1_16_R1.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LightningStrike;
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
            net.minecraft.server.v1_16_R1.World nmsWorld = ((CraftWorld)bukkitLightning.getWorld()).getHandle();
            Location bukkitLoc = bukkitLightning.getLocation();

            if (!storm && random.nextDouble() < 0.025) { /** non-storm lightning has a 2.5% chance to summon a lightning storm in a 100 block radius area centered on the initial lightning strike */
                new RunnableLightningStorm(nmsWorld, bukkitLoc, random.nextInt(11) + 45).runTaskTimer(StaticPlugin.plugin, 0L, random.nextInt(3) + 2);
            }

            if (!storm && random.nextDouble() < 0.02 && numberOfThors < Bukkit.getOnlinePlayers().size()) { /** non-storm lightning has a 2% chance to summon thor, up to 1 thor per player */
                new SpawnEntity(nmsWorld, new CustomEntityZombieThor(nmsWorld), 1, null, bukkitLoc, true);
                numberOfThors++;
            }
        }
    }
}
