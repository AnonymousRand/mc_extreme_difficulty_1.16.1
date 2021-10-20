package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customProjectiles.CustomEntityArrow;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customProjectiles.CustomEntityArrowExploding;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customProjectiles.CustomEntitySmallFireball;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customProjectiles.CustomEntityThrownTrident;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.*;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

import static org.bukkit.entity.EntityType.PLAYER;

public class ProjectileListeners implements Listener {

    @EventHandler
    public void projectileLaunch(ProjectileLaunchEvent event) { //replace arrows and small fireballs when shot with custom arrows/small fireballs
        if (event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow)event.getEntity();
            Location loc = arrow.getLocation();
            CustomEntityArrow newArrow = new CustomEntityArrow(((CraftWorld)arrow.getWorld()).getHandle(), arrow.getVelocity(), (byte)arrow.getPierceLevel(), arrow.getShooter());
            newArrow.setPosition(loc.getX(), loc.getY(), loc.getZ());
            ((CraftWorld)arrow.getWorld()).getHandle().addEntity(newArrow);
            arrow.remove();
        }


        if (event.getEntity() instanceof Trident && !(((CraftEntity)event.getEntity()).getHandle() instanceof CustomEntityThrownTrident)) {
            Trident trident = (Trident)event.getEntity();
            Location loc = trident.getLocation();
            CustomEntityThrownTrident newTrident = new CustomEntityThrownTrident(((CraftWorld)trident.getWorld()).getHandle(), trident.getVelocity(), (byte)trident.getPierceLevel(), trident.getShooter());
            newTrident.setPosition(loc.getX(), loc.getY(), loc.getZ());
            ((CraftWorld)trident.getWorld()).getHandle().addEntity(newTrident);
            trident.remove();
        }
    }

    @EventHandler
    public void projectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();

        if (event.getHitBlock() != null) {

            if (((CraftEntity)projectile).getHandle() instanceof CustomEntityArrow && !(projectile.getShooter() instanceof CraftPlayer)) { /**arrows when shot by an entity other than a player has a 20% chance to destroy the block that it hits without dropping anything*/
                if (event.getHitBlock().getType() != Material.BEDROCK && event.getHitBlock().getType() != Material.END_GATEWAY && event.getHitBlock().getType() != Material.END_PORTAL && event.getHitBlock().getType() != Material.END_PORTAL_FRAME && event.getHitBlock().getType() != Material.NETHER_PORTAL && event.getHitBlock().getType() != Material.OBSIDIAN && event.getHitBlock().getType() != Material.CRYING_OBSIDIAN && event.getHitBlock().getType() != Material.COMMAND_BLOCK  && event.getHitBlock().getType() != Material.COMMAND_BLOCK_MINECART && event.getHitBlock().getType() != Material.STRUCTURE_BLOCK && event.getHitBlock().getType() != Material.JIGSAW && event.getHitBlock().getType() != Material.BARRIER && event.getHitBlock().getType() != Material.END_STONE && event.getHitBlock().getType() != Material.SPAWNER) { //as long as it isn't one of these blocks
                    Random rand = new Random();
                    if (rand.nextDouble() <= 0.2) {
                        event.getHitBlock().setType(Material.AIR); //set the block as air instead of breaking it as there is no way to break it directly without it dropping
                        ((CraftEntity)projectile).getHandle().die();
                    }
                }
            }

            if (((CraftEntity)projectile).getHandle() instanceof CustomEntityThrownTrident) { /**tridents when shot by an entity other than a player has a 10% chance to destroy the block that it hits without dropping anything*/
                if (event.getHitBlock().getType() != Material.BEDROCK && event.getHitBlock().getType() != Material.END_GATEWAY && event.getHitBlock().getType() != Material.END_PORTAL && event.getHitBlock().getType() != Material.END_PORTAL_FRAME && event.getHitBlock().getType() != Material.NETHER_PORTAL && event.getHitBlock().getType() != Material.OBSIDIAN && event.getHitBlock().getType() != Material.CRYING_OBSIDIAN && event.getHitBlock().getType() != Material.COMMAND_BLOCK  && event.getHitBlock().getType() != Material.COMMAND_BLOCK_MINECART && event.getHitBlock().getType() != Material.STRUCTURE_BLOCK && event.getHitBlock().getType() != Material.JIGSAW && event.getHitBlock().getType() != Material.BARRIER && event.getHitBlock().getType() != Material.END_STONE && event.getHitBlock().getType() != Material.SPAWNER) { //as long as it isn't one of these blocks
                    Random rand = new Random();
                    if (rand.nextDouble() <= 0.1) {
                        event.getHitBlock().setType(Material.AIR);
                    }
                }

                ((CraftEntity)projectile).getHandle().die(); /**tridents despawn and die on block hit*/
            }
        }

        if (event.getHitEntity() != null && ((CraftEntity)projectile).getHandle() instanceof CustomEntityArrow) { /**arrows despawn on entity impact*/
            ((CraftEntity)projectile).getHandle().die();
        }

        if (((CraftEntity)projectile).getHandle() instanceof CustomEntityArrowExploding) { /**meteor rain arrows explode on any impact and despawn*/
            ((CraftEntity)projectile).getHandle().die();
        }
    }
}