package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.*;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customProjectiles.*;
import net.minecraft.server.v1_16_R1.*;
import net.minecraft.server.v1_16_R1.Entity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Random;

public class ProjectileListeners implements Listener {

    Random rand = new Random();

    @EventHandler
    public void projectileLaunch(ProjectileLaunchEvent event) { //replace arrows and small fireballs when shot with custom arrows/small fireballs
        Projectile projectile = event.getEntity();
        Entity nmsProjectile = ((CraftEntity)projectile).getHandle();
        Entity nmsShooter = ((CraftEntity)projectile.getShooter()).getHandle();
        World nmsWorld = ((CraftWorld)projectile.getWorld()).getHandle();

        if (projectile instanceof Arrow && !(nmsProjectile instanceof CustomEntityArrowSpawnMob) && !(nmsProjectile instanceof CustomEntityArrowExploding)) {
            Arrow arrow = (Arrow)projectile;
            Location loc = arrow.getLocation();

            if (!(nmsShooter instanceof CustomEntitySkeletonStray)) {
                if (nmsShooter instanceof CustomEntitySkeleton) {
                    if (((CustomEntitySkeleton)nmsShooter).spawnExplodingArrow) { //replace skeleton arrows with exploding ones
                        CustomEntityArrowExploding newArrow = new CustomEntityArrowExploding(nmsWorld, arrow.getVelocity(), (byte)0, arrow.getShooter(), 1.0f);
                        newArrow.setPosition(loc.getX(), loc.getY(), loc.getZ());
                        nmsWorld.addEntity(newArrow);
                        arrow.remove();
                        return;
                    }
                }

                CustomEntityArrow newArrow = new CustomEntityArrow(nmsWorld, arrow.getVelocity(), (byte)arrow.getPierceLevel(), arrow.getShooter());
                newArrow.setPosition(loc.getX(), loc.getY(), loc.getZ());
                nmsWorld.addEntity(newArrow);
                arrow.remove();

                if (nmsProjectile.isBurning()) { //carries over burning arrows
                    newArrow.setOnFire(50);
                }

                if (nmsProjectile.isNoGravity()) { //carries over no gravity
                    newArrow.setNoGravity(true);
                }
            } else {
                if (!(((CustomEntitySkeletonStray)nmsShooter).spawnMob) && !(((CustomEntitySkeletonStray)nmsShooter).spawnExplodingArrow)) { //normal replace
                    CustomEntityArrow newArrow = new CustomEntityArrow(nmsWorld, arrow.getVelocity(), (byte)arrow.getPierceLevel(), arrow.getShooter());
                    newArrow.setPosition(loc.getX(), loc.getY(), loc.getZ());
                    nmsWorld.addEntity(newArrow);
                    arrow.remove();

                    if (nmsProjectile.isBurning()) { //carries over burning arrows
                        newArrow.setOnFire(50);
                    }

                    if (nmsProjectile.isNoGravity()) { //carries over no gravity
                        newArrow.setNoGravity(true);
                    }
                } else if (((CustomEntitySkeletonStray)nmsShooter).spawnMob){ //replace stray arrows with the mob spawning ones
                    Random rand = new Random();
                    double rand2 = rand.nextDouble();
                    CustomEntityArrowSpawnMob newArrow;

                    //todo: replace with custom mobs
                    if (rand2 < 0.25) {
                        newArrow = new CustomEntityArrowSpawnMob(nmsWorld, arrow.getVelocity(), arrow.getShooter(), new CustomEntityCreeper(nmsWorld, 40));
                    } else if (rand2 < 0.5) {
                        newArrow = new CustomEntityArrowSpawnMob(nmsWorld, arrow.getVelocity(), arrow.getShooter(), new EntityVex(EntityTypes.VEX, nmsWorld));
                    } else if (rand2 < 0.75) {
                        newArrow = new CustomEntityArrowSpawnMob(nmsWorld, arrow.getVelocity(), arrow.getShooter(), new EntityRabbit(EntityTypes.RABBIT, nmsWorld));
                    } else {
                        newArrow = new CustomEntityArrowSpawnMob(nmsWorld, arrow.getVelocity(), arrow.getShooter(), new CustomEntitySilverfish(nmsWorld));
                    }

                    newArrow.setPosition(loc.getX(), loc.getY(), loc.getZ());
                    nmsWorld.addEntity(newArrow);
                    arrow.remove();
                } else { //replace stray arrows with exploding arrows
                    CustomEntityArrowExploding newArrow = new CustomEntityArrowExploding(nmsWorld, arrow.getVelocity(), (byte)0, arrow.getShooter(), 1.0f);
                    newArrow.setPosition(loc.getX(), loc.getY(), loc.getZ());
                    nmsWorld.addEntity(newArrow);
                    newArrow.setNoGravity(true); /**this arrow does not lose y-level*/
                    arrow.remove();
                }
            }
        }

        if (projectile instanceof Trident && !(nmsProjectile instanceof CustomEntityThrownTrident)) {
            Trident trident = (Trident)projectile;
            Location loc = trident.getLocation();
            CustomEntityThrownTrident newTrident = new CustomEntityThrownTrident(((CraftWorld)trident.getWorld()).getHandle(), trident.getVelocity(), (byte)trident.getPierceLevel(), trident.getShooter());
            newTrident.setPosition(loc.getX(), loc.getY(), loc.getZ());
            ((CraftWorld)trident.getWorld()).getHandle().addEntity(newTrident);
            trident.remove();

            if (nmsShooter instanceof CustomEntityDrowned) {
                if (((CustomEntityDrowned)nmsShooter).attacks >= 30) {
                    if (rand.nextDouble() < (((CustomEntityDrowned)nmsShooter).attacks < 60 ? 0.1 : 0.333333333)) { /**tridents have a 0%, 10% or 33% chance to not lose y level depending on attack count*/
                        newTrident.setNoGravity(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void projectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        ProjectileSource bukkitShooter = projectile.getShooter();
        Entity nmsProjectile = ((CraftEntity)projectile).getHandle();
        Entity nmsShooter = ((CraftEntity)bukkitShooter).getHandle();

        if (event.getHitBlock() != null) {
            Block hitBlock = event.getHitBlock();
            if (nmsProjectile instanceof CustomEntityArrow && !(bukkitShooter instanceof CraftPlayer)) { /**arrows when shot by an entity other than a player has a 20% chance to destroy the block that it hits without dropping anything*/
                if (hitBlock.getType() != Material.BEDROCK && hitBlock.getType() != Material.END_GATEWAY && hitBlock.getType() != Material.END_PORTAL && hitBlock.getType() != Material.END_PORTAL_FRAME && hitBlock.getType() != Material.NETHER_PORTAL && hitBlock.getType() != Material.OBSIDIAN && hitBlock.getType() != Material.CRYING_OBSIDIAN && hitBlock.getType() != Material.COMMAND_BLOCK  && hitBlock.getType() != Material.COMMAND_BLOCK_MINECART && hitBlock.getType() != Material.STRUCTURE_BLOCK && hitBlock.getType() != Material.JIGSAW && hitBlock.getType() != Material.BARRIER && hitBlock.getType() != Material.END_STONE && hitBlock.getType() != Material.SPAWNER && hitBlock.getType() != Material.COBWEB) { //as long as it isn't one of these blocks
                    Random rand = new Random();
                    if (rand.nextDouble() <= 0.2) {
                        hitBlock.setType(Material.AIR); //set the block as air instead of breaking it as there is no way to break it directly without it dropping
                        nmsProjectile.die();
                    }
                }
            }

            if (nmsProjectile instanceof CustomEntityThrownTrident) { /**tridents when shot by an entity other than a player has a 10% chance to destroy the block that it hits without dropping anything*/
                if (hitBlock.getType() != Material.BEDROCK && hitBlock.getType() != Material.END_GATEWAY && hitBlock.getType() != Material.END_PORTAL && hitBlock.getType() != Material.END_PORTAL_FRAME && hitBlock.getType() != Material.NETHER_PORTAL && hitBlock.getType() != Material.OBSIDIAN && hitBlock.getType() != Material.CRYING_OBSIDIAN && hitBlock.getType() != Material.COMMAND_BLOCK  && hitBlock.getType() != Material.COMMAND_BLOCK_MINECART && hitBlock.getType() != Material.STRUCTURE_BLOCK && hitBlock.getType() != Material.JIGSAW && hitBlock.getType() != Material.BARRIER && hitBlock.getType() != Material.END_STONE && hitBlock.getType() != Material.SPAWNER && hitBlock.getType() != Material.COBWEB) { //as long as it isn't one of these blocks
                    Random rand = new Random();
                    if (rand.nextDouble() <= 0.1) {
                        hitBlock.setType(Material.AIR);
                    }
                }

                nmsProjectile.die(); /**tridents despawn and die on block hit*/
            }
        }

        if (event.getHitEntity() != null && nmsProjectile instanceof CustomEntityArrow) {
            if (((CustomEntityArrow)nmsProjectile).getPierceLevel() == 0) { /**0 pierce arrows despawn on entity impact*/
                nmsProjectile.die();
            }

            if (event.getHitEntity() instanceof CraftPlayer && nmsShooter instanceof EntitySkeletonStray && !(nmsProjectile instanceof CustomEntityArrowExploding) && !(nmsProjectile instanceof CustomEntityArrowSpawnMob)) { //normal arrows shot by strays inflict slowness as custom strays do not have the slowness method properly applied
                ((LivingEntity)event.getHitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 600, 0));
            }
        }

        if (nmsProjectile instanceof CustomEntityArrowExploding) { /**meteor rain arrows explode on any impact and despawn*/
            nmsProjectile.die();
        }

        if (nmsProjectile instanceof CustomEntityArrowSpawnMob) { /**spawn mob on any impact and despawn*/
            nmsProjectile.die();
        }
    }
}