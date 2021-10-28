package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.*;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customProjectiles.*;
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
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Random;

public class ProjectileListeners implements Listener {

    private Random rand = new Random();

    @EventHandler
    public void projectileLaunch(ProjectileLaunchEvent event) { //replace arrows when shot with custom arrows
        Projectile bukkitProjectile = event.getEntity();
        Entity nmsProjectile = ((CraftEntity)bukkitProjectile).getHandle();
        Entity nmsShooter = ((CraftEntity)bukkitProjectile.getShooter()).getHandle();
        World nmsWorld = ((CraftWorld)bukkitProjectile.getWorld()).getHandle();

        if (bukkitProjectile instanceof Arrow && !(nmsProjectile instanceof CustomEntityArrow)) {
            Arrow bukkitArrow = (Arrow)bukkitProjectile;
            Location loc = bukkitArrow.getLocation();

            if (!(nmsShooter instanceof CustomEntitySkeletonStray)) {
                if (nmsShooter instanceof CustomEntitySkeleton) {
                    if (((CustomEntitySkeleton)nmsShooter).spawnExplodingArrow) { //replace skeleton arrows with exploding ones
                        CustomEntityArrowExploding newArrow = new CustomEntityArrowExploding(nmsWorld, bukkitArrow.getVelocity(), (byte)0, bukkitArrow.getShooter(), 1.0f);
                        newArrow.setPosition(loc.getX(), loc.getY(), loc.getZ());
                        nmsWorld.addEntity(newArrow);
                        bukkitArrow.remove();
                        return;
                    }
                }

                CustomEntityArrow newArrow = new CustomEntityArrow(nmsWorld, bukkitArrow.getVelocity(), (byte)bukkitArrow.getPierceLevel(), bukkitArrow.getShooter());
                newArrow.setPosition(loc.getX(), loc.getY(), loc.getZ());
                nmsWorld.addEntity(newArrow);
                bukkitArrow.remove();

                if (nmsProjectile.isBurning()) { //carries over burning arrows
                    newArrow.setOnFire(50);
                }

                if (nmsProjectile.isNoGravity()) { //carries over no gravity
                    newArrow.setNoGravity(true);
                }
            } else {
                if (!(((CustomEntitySkeletonStray)nmsShooter).spawnMob) && !(((CustomEntitySkeletonStray)nmsShooter).spawnExplodingArrow)) { //normal replace
                    CustomEntityArrow newArrow = new CustomEntityArrow(nmsWorld, bukkitArrow.getVelocity(), (byte)bukkitArrow.getPierceLevel(), bukkitArrow.getShooter());
                    newArrow.setPosition(loc.getX(), loc.getY(), loc.getZ());
                    nmsWorld.addEntity(newArrow);
                    bukkitArrow.remove();

                    if (nmsProjectile.isBurning()) { //carries over burning arrows
                        newArrow.setOnFire(50);
                    }

                    if (nmsProjectile.isNoGravity()) { //carries over no gravity
                        newArrow.setNoGravity(true);
                    }
                } else if (((CustomEntitySkeletonStray)nmsShooter).spawnMob){ //replace stray arrows with the mob spawning ones
                    this.rand = new Random();
                    double rand2 = rand.nextDouble();
                    CustomEntityArrowSpawnMob newArrow;

                    //todo: replace with custom mobs
                    if (rand2 < 0.25) {
                        newArrow = new CustomEntityArrowSpawnMob(nmsWorld, bukkitArrow.getVelocity(), bukkitArrow.getShooter(), new CustomEntityCreeper(nmsWorld, 40));
                    } else if (rand2 < 0.5) {
                        newArrow = new CustomEntityArrowSpawnMob(nmsWorld, bukkitArrow.getVelocity(), bukkitArrow.getShooter(), new EntityVex(EntityTypes.VEX, nmsWorld));
                    } else if (rand2 < 0.75) {
                        newArrow = new CustomEntityArrowSpawnMob(nmsWorld, bukkitArrow.getVelocity(), bukkitArrow.getShooter(), new EntityRabbit(EntityTypes.RABBIT, nmsWorld)); //todo: a single killer rabbit instead (summon custom, set type)
                    } else {
                        newArrow = new CustomEntityArrowSpawnMob(nmsWorld, bukkitArrow.getVelocity(), bukkitArrow.getShooter(), new CustomEntitySilverfish(nmsWorld));
                    }

                    newArrow.setPosition(loc.getX(), loc.getY(), loc.getZ());
                    nmsWorld.addEntity(newArrow);
                    bukkitArrow.remove();
                } else { //replace stray arrows with exploding arrows
                    CustomEntityArrowExploding newArrow = new CustomEntityArrowExploding(nmsWorld, bukkitArrow.getVelocity(), (byte)0, bukkitArrow.getShooter(), 1.0f);
                    newArrow.setPosition(loc.getX(), loc.getY(), loc.getZ());
                    nmsWorld.addEntity(newArrow);
                    newArrow.setNoGravity(true); /**this arrow does not lose y level*/
                    bukkitArrow.remove();
                }
            }
        }

        if (bukkitProjectile instanceof Trident && !(nmsProjectile instanceof CustomEntityThrownTrident)) {
            Trident bukkitTrident = (Trident)bukkitProjectile;
            Location loc = bukkitTrident.getLocation();
            CustomEntityThrownTrident newTrident = new CustomEntityThrownTrident(((CraftWorld)bukkitTrident.getWorld()).getHandle(), bukkitTrident.getVelocity(), (byte)bukkitTrident.getPierceLevel(), bukkitTrident.getShooter());
            newTrident.setPosition(loc.getX(), loc.getY(), loc.getZ());
            ((CraftWorld)bukkitTrident.getWorld()).getHandle().addEntity(newTrident);
            bukkitTrident.remove();

            if (nmsShooter instanceof CustomEntityDrowned) {
                if (((CustomEntityDrowned)nmsShooter).attacks >= 30) {
                    if (rand.nextDouble() < (((CustomEntityDrowned)nmsShooter).attacks < 70 ? 0.1 : 0.333333333)) { /**tridents have a 0%, 10% or 33% chance to not lose y level depending on attack count*/
                        newTrident.setNoGravity(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void projectileHit(ProjectileHitEvent event) {
        Projectile bukkitProjectile = event.getEntity();
        ProjectileSource bukkitShooter = bukkitProjectile.getShooter();
        Entity nmsProjectile = ((CraftEntity)bukkitProjectile).getHandle();
        Entity nmsShooter = ((CraftEntity)bukkitShooter).getHandle();

        if (event.getHitBlock() != null) {
            Block hitBlock = event.getHitBlock();
            Material type = hitBlock.getType();

            boolean checkType = type != Material.BEDROCK && type != Material.END_GATEWAY && type != Material.END_PORTAL && type != Material.END_PORTAL_FRAME && type != Material.NETHER_PORTAL && type != Material.OBSIDIAN && type != Material.CRYING_OBSIDIAN && type != Material.COMMAND_BLOCK && type != Material.COMMAND_BLOCK_MINECART && type != Material.STRUCTURE_BLOCK && type != Material.JIGSAW && type != Material.BARRIER && type != Material.END_STONE && type != Material.SPAWNER && type != Material.COBWEB;

            if (nmsProjectile instanceof CustomEntityArrow && !(bukkitShooter instanceof CraftPlayer)) { /**arrows when shot by an entity other than a player has a 20% chance to destroy the block that it hits without dropping anything*/
                if (checkType) { //as long as it isn't one of these blocks
                    this.rand = new Random();
                    if (rand.nextDouble() <= 0.2) {
                        hitBlock.setType(Material.AIR); //set the block as air instead of breaking it as there is no way to break it directly without it dropping
                    }
                }

                if (type != Material.COBWEB) {
                    nmsProjectile.die(); /**arrows die on block hit as long as it isn't a cobweb*/
                }
            }

            if (nmsProjectile instanceof CustomEntityThrownTrident) { /**tridents when shot by an entity other than a player has a 10% chance to destroy the block that it hits without dropping anything*/
                if (checkType) { //as long as it isn't one of these blocks
                    this.rand = new Random();
                    if (rand.nextDouble() <= 0.1) {
                        hitBlock.setType(Material.AIR);
                    }
                }

                nmsProjectile.die(); /**tridents die on block hit*/
            }
        }

        if (event.getHitEntity() != null) {
            Entity nmsHitEntity = ((CraftEntity)event.getHitEntity()).getHandle();

            if (nmsProjectile instanceof CustomEntityArrow) {
                if (((CustomEntityArrow)nmsProjectile).getPierceLevel() == 0) { /**0 pierce arrows die on entity impact*/
                    nmsProjectile.die();
                }

                if (nmsHitEntity instanceof EntityPlayer && nmsShooter instanceof EntitySkeletonStray && !(nmsProjectile instanceof CustomEntityArrowExploding) && !(nmsProjectile instanceof CustomEntityArrowSpawnMob)) { //normal arrows shot by strays inflict slowness as custom strays do not have the slowness method properly applied
                    ((EntityLiving)nmsHitEntity).addEffect(new MobEffect(MobEffects.SLOWER_MOVEMENT, 600, 0));
                }
            }
        }

        if (nmsProjectile instanceof CustomEntityArrowExploding) { /**meteor rain arrows explode on any impact and die*/
            nmsProjectile.die();
        }

        if (nmsProjectile instanceof CustomEntityArrowSpawnMob) { /**spawn mob arrows spawn their mob on any impact and die*/
            nmsProjectile.die();
        }

        if (nmsProjectile instanceof EntityEnderPearl) { /**ender pearls spawn an endermite on landing*/
            CustomEntityEndermite endermite = new CustomEntityEndermite(nmsProjectile.getWorld());
            endermite.setPositionRotation(nmsProjectile.locX(), nmsProjectile.locY(), nmsProjectile.locZ(), nmsProjectile.yaw, nmsProjectile.pitch);
            nmsProjectile.getWorld().addEntity(endermite, CreatureSpawnEvent.SpawnReason.NATURAL);
        }

        if (nmsProjectile instanceof CustomEntitySmallFireball && nmsShooter instanceof CustomEntityGhast) { /**blaze fireballs when shot by ghasts during their death still explode like large fireballs; the reason I use small fireballs instead is because large fireballs keep hitting each other as they are spawned on the same spot*/
            nmsProjectile.world.createExplosion((Entity)null, nmsProjectile.locX(), nmsProjectile.locY(), nmsProjectile.locZ(), 1.0f, false, Explosion.Effect.DESTROY);
        }
    }
}