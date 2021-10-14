package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.CustomEntityArrow;
import net.minecraft.server.v1_16_R1.Blocks;
import net.minecraft.server.v1_16_R1.EntityArrow;
import net.minecraft.server.v1_16_R1.EntityPillager;
import net.minecraft.server.v1_16_R1.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.Random;

public class ProjectileListeners implements Listener {

    @EventHandler
    public void projectileLaunch(ProjectileLaunchEvent event) { //replace arrows when shot with custom arrows
        if (event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow)event.getEntity();
            Location loc = arrow.getLocation();
            CustomEntityArrow newArrow = new CustomEntityArrow(((CraftWorld)arrow.getWorld()).getHandle(), arrow.getVelocity(), (byte)arrow.getPierceLevel(), arrow.getShooter());
            newArrow.setPosition(loc.getX(), loc.getY(), loc.getZ());
            ((CraftWorld)arrow.getWorld()).getHandle().addEntity(newArrow);
            arrow.remove();
        }
    }

    @EventHandler
    public void projectileHit(ProjectileHitEvent event) { //arrows when shot by an entity other than a player has a 20% chance to destroy the block that it hits without dropping anything
        if (event.getHitBlock() != null && event.getEntity() instanceof AbstractArrow && !(event.getEntity().getShooter() instanceof CraftPlayer)) {
            if (event.getHitBlock().getType() != Material.BEDROCK && event.getHitBlock().getType() != Material.END_GATEWAY && event.getHitBlock().getType() != Material.END_PORTAL && event.getHitBlock().getType() != Material.END_PORTAL_FRAME && event.getHitBlock().getType() != Material.NETHER_PORTAL && event.getHitBlock().getType() != Material.OBSIDIAN && event.getHitBlock().getType() != Material.CRYING_OBSIDIAN && event.getHitBlock().getType() != Material.COMMAND_BLOCK  && event.getHitBlock().getType() != Material.COMMAND_BLOCK_MINECART && event.getHitBlock().getType() != Material.STRUCTURE_BLOCK && event.getHitBlock().getType() != Material.JIGSAW && event.getHitBlock().getType() != Material.BARRIER && event.getHitBlock().getType() != Material.END_STONE && event.getHitBlock().getType() != Material.SPAWNER) { //as long as it isn't one of these blocks
                Random rand = new Random();
                if (rand.nextDouble() <= 0.2) {
                    event.getHitBlock().setType(Material.AIR); //set the block as air instead of breaking it as there is no way to break it directly without it dropping
                    event.getEntity().remove();
                }
            }
        }
    }
}