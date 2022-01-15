package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.*;
import net.minecraft.server.v1_16_R1.*;
import net.minecraft.server.v1_16_R1.Entity;
import org.bukkit.Bukkit;
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
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.Random;

public class ListenerProjectile implements Listener {

    private static final Random random = new Random();

    @EventHandler
    public void projectileLaunch(ProjectileLaunchEvent event) { //replace arrows when shot with custom arrows
        Projectile bukkitProjectile = event.getEntity();
        Entity nmsProjectile = ((CraftEntity)bukkitProjectile).getHandle();

        if (nmsProjectile instanceof CustomEntityArrow || nmsProjectile instanceof CustomEntityDragonFireball || nmsProjectile instanceof EntityEgg || nmsProjectile instanceof CustomEntityLlamaSpit || nmsProjectile instanceof CustomEntitySmallFireball || nmsProjectile instanceof CustomEntityLargeFireball || nmsProjectile instanceof CustomEntityThrownTrident) { //reduce lag and null pointers
            return;
        }

        Location loc = bukkitProjectile.getLocation();
        Entity nmsShooter = ((CraftEntity)bukkitProjectile.getShooter()).getHandle();
        World nmsWorld = ((CraftWorld)bukkitProjectile.getWorld()).getHandle();

        if (nmsProjectile instanceof EntityArrow && !(nmsProjectile instanceof EntityThrownTrident)) {
            Arrow bukkitArrow = (Arrow)bukkitProjectile;
            CustomEntityArrow newArrow = new CustomEntityArrow(nmsWorld, bukkitArrow.getVelocity(), (byte)bukkitArrow.getPierceLevel(), bukkitArrow.getShooter());

            if (nmsShooter instanceof EntityPlayer) { /**player-shot arrows have more inaccuracy*/
                newArrow = new CustomEntityArrow(nmsWorld, bukkitArrow.getVelocity().add(new Vector(random.nextDouble() - 0.4, random.nextDouble() - 0.4, random.nextDouble() - 0.4)), (byte)bukkitArrow.getPierceLevel(), bukkitArrow.getShooter());
            }

            if (nmsProjectile.isBurning()) { //carries over burning arrows
                newArrow.setOnFire(nmsProjectile.getFireTicks());
            }

            if (nmsProjectile.isNoGravity()) { //carries over no gravity
                newArrow.setNoGravity(true);
            }

            newArrow.setPosition(loc.getX(), loc.getY(), loc.getZ());
            nmsWorld.addEntity(newArrow);
            bukkitArrow.remove();
        } else if (nmsProjectile instanceof EntityThrownTrident) {
            Trident bukkitTrident = (Trident)bukkitProjectile;
            CustomEntityThrownTrident newTrident = new CustomEntityThrownTrident(nmsWorld, bukkitTrident.getVelocity(), (byte)bukkitTrident.getPierceLevel(), bukkitTrident.getShooter());
            newTrident.setPosition(loc.getX(), loc.getY(), loc.getZ());

            if (nmsShooter instanceof CustomEntityDrowned) {
                if (((CustomEntityDrowned)nmsShooter).attacks >= 30) {
                    if (random.nextDouble() < (((CustomEntityDrowned)nmsShooter).attacks < 70 ? 0.1 : 0.333333333)) { /**tridents have a 0%, 10% or 33% chance to not lose y level depending on attack count*/
                        newTrident.setNoGravity(true);
                    }
                }
            }

            nmsWorld.addEntity(newTrident);
            bukkitTrident.remove();
        }
    }

    @EventHandler
    public void projectileHit(ProjectileHitEvent event) {
        Projectile bukkitProjectile = event.getEntity();
        Entity nmsProjectile = ((CraftEntity)bukkitProjectile).getHandle();

        if (nmsProjectile instanceof CustomEntityDragonFireball || nmsProjectile instanceof EntityEgg || nmsProjectile instanceof CustomEntityLargeFireball || nmsProjectile instanceof CustomEntityLlamaSpit) { //reduce lag and null pointers
            return;
        }

        World nmsWorld = nmsProjectile.getWorld();
        ProjectileSource bukkitShooter = bukkitProjectile.getShooter();
        Entity nmsShooter = null;

        if (bukkitShooter != null) {
            nmsShooter = ((CraftEntity)bukkitShooter).getHandle();
        }

        if (nmsProjectile instanceof CustomEntitySmallFireball) { /**blaze fireballs when shot by ghasts and dragons still explode like large fireballs; the reason I use small fireballs instead is because large fireballs keep hitting each other as they are spawned on the same spot*/
            if (nmsShooter instanceof CustomEntityGhast) {
                nmsWorld.createExplosion((Entity)null, nmsProjectile.locX(), nmsProjectile.locY(), nmsProjectile.locZ(), 1.0F, false, Explosion.Effect.DESTROY);
            } else if (nmsShooter instanceof CustomEntityEnderDragon) {
                nmsWorld.createExplosion((Entity)null, nmsProjectile.locX(), nmsProjectile.locY(), nmsProjectile.locZ(), 2.0F, false, Explosion.Effect.DESTROY);
            }

            return;
        }

        if (event.getHitBlock() != null) {
            Block hitBlock = event.getHitBlock();
            Material type = hitBlock.getType();

            if (nmsProjectile instanceof CustomEntityArrow && !(bukkitShooter instanceof Player)) { /**arrows when shot by an entity other than a player has a 20% chance to destroy the block that it hits without dropping anything*/
                if (type != Material.AIR && type != Material.BEDROCK && type != Material.END_GATEWAY && type != Material.END_PORTAL && type != Material.END_PORTAL_FRAME && type != Material.NETHER_PORTAL && type != Material.OBSIDIAN && type != Material.CRYING_OBSIDIAN && type != Material.COMMAND_BLOCK && type != Material.COMMAND_BLOCK_MINECART && type != Material.STRUCTURE_BLOCK && type != Material.JIGSAW && type != Material.BARRIER && type != Material.END_STONE && type != Material.SPAWNER && type != Material.COBWEB) { //as long as it isn't one of these blocks
                    if (random.nextDouble() <= 0.2) {
                        hitBlock.setType(Material.AIR); //set the block as air instead of breaking it as there is no way to break it directly without it dropping
                    }
                }

                if (type != Material.COBWEB) {
                    nmsProjectile.die(); /**arrows die on block hit as long as it isn't a cobweb*/
                    return;
                }
            }

            if (nmsProjectile instanceof CustomEntityThrownTrident) { /**tridents when shot by an entity other than a player has a 10% chance to destroy the block that it hits without dropping anything*/
                if (type != Material.AIR && type != Material.BEDROCK && type != Material.END_GATEWAY && type != Material.END_PORTAL && type != Material.END_PORTAL_FRAME && type != Material.NETHER_PORTAL && type != Material.OBSIDIAN && type != Material.CRYING_OBSIDIAN && type != Material.COMMAND_BLOCK && type != Material.COMMAND_BLOCK_MINECART && type != Material.STRUCTURE_BLOCK && type != Material.JIGSAW && type != Material.BARRIER && type != Material.END_STONE && type != Material.SPAWNER && type != Material.COBWEB) { //as long as it isn't one of these blocks
                    if (random.nextDouble() <= 0.1) {
                        hitBlock.setType(Material.AIR);
                    }
                }

                nmsProjectile.die(); /**tridents die on block hit*/
                return;
            }
        }

        if (event.getHitEntity() != null) {
            Entity nmsHitEntity = ((CraftEntity)event.getHitEntity()).getHandle();

            if (nmsProjectile instanceof CustomEntityArrow) {
                if (((CustomEntityArrow)nmsProjectile).getPierceLevel() == 0) { /**0 pierce arrows die on entity impact*/
                    nmsProjectile.die();
                    return;
                }

                if (nmsHitEntity instanceof EntityPlayer && nmsShooter instanceof EntitySkeletonStray && !(nmsProjectile instanceof CustomEntityArrowExploding) && !(nmsProjectile instanceof CustomEntityArrowSpawnMob)) { /**normal arrows shot by strays inflict slowness 2 for 30 seconds*/
                    ((EntityLiving)nmsHitEntity).addEffect(new MobEffect(MobEffects.SLOWER_MOVEMENT, 600, 1));
                }
            }
        }

        if (nmsProjectile instanceof CustomEntityArrowExploding || nmsProjectile instanceof CustomEntityArrowSpawnMob) { /**meteor rain arrows explode on any impact and die, spawn mob arrows spawn their mob on any impact and die*/
            nmsProjectile.die();
            return;
        }

        if (nmsProjectile instanceof EntityEnderPearl) { /**ender pearls spawn an endermite on landing (with now a 5% chance to spawn 2 due to the original vanilla spawning)*/
            new SpawnEntity(nmsWorld, new CustomEntityEndermite(nmsWorld), 1, null, null, nmsProjectile, false, true);
        }
    }
}