package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntityCreeper;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntityRabbit;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntitySilverfish;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntitySkeletonStray;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customProjectiles.*;
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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class ProjectileListeners implements Listener {

    @EventHandler
    public void projectileLaunch(ProjectileLaunchEvent event) { //replace arrows and small fireballs when shot with custom arrows/small fireballs
        Projectile projectile = event.getEntity();

        if (projectile instanceof Arrow && !(((CraftEntity)projectile).getHandle() instanceof CustomEntityArrowSpawnMob) && !(((CraftEntity)projectile).getHandle() instanceof CustomEntityArrowExploding)) {
            Arrow arrow = (Arrow)projectile;
            Location loc = arrow.getLocation();

            if (!(((CraftEntity)projectile.getShooter()).getHandle() instanceof CustomEntitySkeletonStray)) {
                CustomEntityArrow newArrow = new CustomEntityArrow(((CraftWorld)arrow.getWorld()).getHandle(), arrow.getVelocity(), (byte)arrow.getPierceLevel(), arrow.getShooter());
                newArrow.setPosition(loc.getX(), loc.getY(), loc.getZ());
                ((CraftWorld)arrow.getWorld()).getHandle().addEntity(newArrow);

                if (((CraftEntity)projectile).getHandle().isBurning()) { //carries over burning arrows
                    newArrow.setOnFire(50);
                }

                arrow.remove();
            } else {
                if (!(((CustomEntitySkeletonStray)((CraftEntity)(projectile.getShooter())).getHandle()).spawnMob) && !(((CustomEntitySkeletonStray)((CraftEntity)(projectile.getShooter())).getHandle()).spawnFirework)) {
                    CustomEntityArrow newArrow = new CustomEntityArrow(((CraftWorld) arrow.getWorld()).getHandle(), arrow.getVelocity(), (byte) arrow.getPierceLevel(), arrow.getShooter());
                    newArrow.setPosition(loc.getX(), loc.getY(), loc.getZ());
                    ((CraftWorld) arrow.getWorld()).getHandle().addEntity(newArrow);

                    if (((CraftEntity) projectile).getHandle().isBurning()) { //carries over burning arrows
                        newArrow.setOnFire(50);
                    }

                    arrow.remove();
                } else if (((CustomEntitySkeletonStray)((CraftEntity)(projectile.getShooter())).getHandle()).spawnMob){ //replace stray arrows with the mob spawning ones
                    Random rand = new Random();
                    double rand2 = rand.nextDouble();
                    CustomEntityArrowSpawnMob newArrow;

                    //todo: replace with custom mobs
                    if (rand2 < 0.25) {
                        newArrow = new CustomEntityArrowSpawnMob(((CraftWorld)arrow.getWorld()).getHandle(), arrow.getVelocity(), arrow.getShooter(), new CustomEntityCreeper(((CraftWorld)arrow.getWorld()).getHandle(), 40));
                    } else if (rand2 < 0.5) {
                        newArrow = new CustomEntityArrowSpawnMob(((CraftWorld)arrow.getWorld()).getHandle(), arrow.getVelocity(), arrow.getShooter(), new EntityVex(EntityTypes.VEX, ((CraftWorld)arrow.getWorld()).getHandle()));
                    } else if (rand2 < 0.75) {
                        newArrow = new CustomEntityArrowSpawnMob(((CraftWorld)arrow.getWorld()).getHandle(), arrow.getVelocity(), arrow.getShooter(), new EntityRabbit(EntityTypes.RABBIT, ((CraftWorld)arrow.getWorld()).getHandle()));
                    } else {
                        newArrow = new CustomEntityArrowSpawnMob(((CraftWorld)arrow.getWorld()).getHandle(), arrow.getVelocity(), arrow.getShooter(), new CustomEntitySilverfish(((CraftWorld)arrow.getWorld()).getHandle()));
                    }

                    newArrow.setPosition(loc.getX(), loc.getY(), loc.getZ());
                    ((CraftWorld)arrow.getWorld()).getHandle().addEntity(newArrow);
                    arrow.remove();
                } else { //replace stray arrows with exploding arrows
                    CustomEntityArrowExploding newArrow = new CustomEntityArrowExploding(((CraftWorld)arrow.getWorld()).getHandle(), arrow.getVelocity(), (byte)0, arrow.getShooter(), 1.0f);
                    newArrow.setPosition(loc.getX(), loc.getY(), loc.getZ());
                    ((CraftWorld)arrow.getWorld()).getHandle().addEntity(newArrow);
                    arrow.remove();
                }
            }
        }

        if (projectile instanceof Trident && !(((CraftEntity)projectile).getHandle() instanceof CustomEntityThrownTrident)) {
            Trident trident = (Trident)projectile;
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
                if (event.getHitBlock().getType() != Material.BEDROCK && event.getHitBlock().getType() != Material.END_GATEWAY && event.getHitBlock().getType() != Material.END_PORTAL && event.getHitBlock().getType() != Material.END_PORTAL_FRAME && event.getHitBlock().getType() != Material.NETHER_PORTAL && event.getHitBlock().getType() != Material.OBSIDIAN && event.getHitBlock().getType() != Material.CRYING_OBSIDIAN && event.getHitBlock().getType() != Material.COMMAND_BLOCK  && event.getHitBlock().getType() != Material.COMMAND_BLOCK_MINECART && event.getHitBlock().getType() != Material.STRUCTURE_BLOCK && event.getHitBlock().getType() != Material.JIGSAW && event.getHitBlock().getType() != Material.BARRIER && event.getHitBlock().getType() != Material.END_STONE && event.getHitBlock().getType() != Material.SPAWNER && event.getHitBlock().getType() != Material.COBWEB) { //as long as it isn't one of these blocks
                    Random rand = new Random();
                    if (rand.nextDouble() <= 0.2) {
                        event.getHitBlock().setType(Material.AIR); //set the block as air instead of breaking it as there is no way to break it directly without it dropping
                        ((CraftEntity)projectile).getHandle().die();
                    }
                }
            }

            if (((CraftEntity)projectile).getHandle() instanceof CustomEntityThrownTrident) { /**tridents when shot by an entity other than a player has a 10% chance to destroy the block that it hits without dropping anything*/
                if (event.getHitBlock().getType() != Material.BEDROCK && event.getHitBlock().getType() != Material.END_GATEWAY && event.getHitBlock().getType() != Material.END_PORTAL && event.getHitBlock().getType() != Material.END_PORTAL_FRAME && event.getHitBlock().getType() != Material.NETHER_PORTAL && event.getHitBlock().getType() != Material.OBSIDIAN && event.getHitBlock().getType() != Material.CRYING_OBSIDIAN && event.getHitBlock().getType() != Material.COMMAND_BLOCK  && event.getHitBlock().getType() != Material.COMMAND_BLOCK_MINECART && event.getHitBlock().getType() != Material.STRUCTURE_BLOCK && event.getHitBlock().getType() != Material.JIGSAW && event.getHitBlock().getType() != Material.BARRIER && event.getHitBlock().getType() != Material.END_STONE && event.getHitBlock().getType() != Material.SPAWNER && event.getHitBlock().getType() != Material.COBWEB) { //as long as it isn't one of these blocks
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

            if (event.getHitEntity() instanceof CraftPlayer && ((CraftEntity)(projectile.getShooter())).getHandle() instanceof EntitySkeletonStray && !(((CraftEntity)projectile).getHandle() instanceof CustomEntityArrowExploding) && !(((CraftEntity)projectile).getHandle() instanceof CustomEntityArrowSpawnMob)) { /**normal arrows shot by strays inflict slowness as custom strays do not have the slowness method properly applied*/
                ((LivingEntity)event.getHitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 600, 0));
            }
        }

        if (((CraftEntity)projectile).getHandle() instanceof CustomEntityArrowExploding) { /**meteor rain arrows explode on any impact and despawn*/
            ((CraftEntity)projectile).getHandle().die();
        }

        if (((CraftEntity)projectile).getHandle() instanceof CustomEntityArrowSpawnMob) { /**spawn mob on any impact and despawn*/
            ((CraftEntity)projectile).getHandle().die();
        }
    }
}