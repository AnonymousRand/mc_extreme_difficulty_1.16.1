package AnonymousRand.anonymousrand.plugin.listeners;

import AnonymousRand.anonymousrand.plugin.ExtremeDifficultyPlugin;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.*;
import AnonymousRand.anonymousrand.plugin.nms.customentities.projectiles.CustomEntityWitherSkull;
import AnonymousRand.anonymousrand.plugin.nms.customentities.misc.CustomEntityAreaEffectCloud;
import AnonymousRand.anonymousrand.plugin.nms.customentities.misc.CustomEntityLightning;
import AnonymousRand.anonymousrand.plugin.util.CustomMathHelper;
import AnonymousRand.anonymousrand.plugin.util.bukkitrunnables.RunnableMobRain;
import AnonymousRand.anonymousrand.plugin.util.bukkitrunnables.RunnableLightningStorm;
import AnonymousRand.anonymousrand.plugin.util.bukkitrunnables.RunnableSpawnBlocksAround;
import AnonymousRand.anonymousrand.plugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class ListenerMobDeath implements Listener {

    private static final Random random = new Random();

    @EventHandler
    public void mobDeath(EntityDeathEvent event) {
        LivingEntity bukkitEntity = event.getEntity();
        EntityLiving nmsEntity = ((CraftLivingEntity)bukkitEntity).getHandle();
        EntityType bukkitEntityType = event.getEntityType();
        org.bukkit.World bukkitWorld = bukkitEntity.getWorld();
        World nmsWorld = ((CraftWorld)bukkitWorld).getHandle();
        Location bukkitLoc = event.getEntity().getLocation();

        switch (bukkitEntityType) { // stuff that happens when mobs die regardless of attack count
            case BAT:
                bukkitWorld.createExplosion(bukkitLoc.getX(), bukkitLoc.getY(), bukkitLoc.getZ(), 0.5F, false); /* bats explode with power 0.5 when killed */
                    break;
            case CHICKEN: /* chickens drop 20 eggs when killed */
                bukkitWorld.dropItem(bukkitLoc, new ItemStack(Material.EGG, 20));

                if (nmsEntity instanceof CustomEntityChickenAggressive && !(nmsEntity instanceof CustomEntityChickenAggressiveExploding)) {
                    if (random.nextDouble() < 0.05) { /* aggressive chickens have a 5% chance to explode into 15-20 more chickens */
                        new SpawnEntity(nmsWorld, new CustomEntityChicken(nmsWorld), random.nextInt(6) + 15, null, null, nmsEntity, false, true);
                    }
                } else if (nmsEntity instanceof CustomEntityChickenAggressiveExploding) { /* aggressive exploding chickens explode with power 1 after 20 seconds or when killed */
                    bukkitWorld.createExplosion(bukkitLoc.getX(), bukkitLoc.getY(), bukkitLoc.getZ(), 1.0F, false);
                }

                break;
            case COW:
                bukkitWorld.createExplosion(bukkitLoc.getX(), bukkitLoc.getY(), bukkitLoc.getZ(), 2.0F, false); /* cows explode with power 2 when killed */
                break;
            case ENDERMAN:  /* enderman have a 0.5% chance to drop a pearl without looting, and only slightly more with looting */
                if (event.getDrops().size() != 0) {
                    if (random.nextDouble() < 0.99) {
                        event.getDrops().clear();
                    }
                }
                break;
            case IRON_GOLEM:
                new SpawnEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 10, null, null, nmsEntity, false, true); /* iron golems summon 10 silverfish when killed */
                break;
            case MUSHROOM_COW:
                bukkitWorld.createExplosion(bukkitLoc.getX(), bukkitLoc.getY(), bukkitLoc.getZ(), 10.0F, false); /* mooshrooms explode with power 10 when killed */
                break;
            case PIG:
                if (random.nextDouble() < 0.14) {
                    if (random.nextDouble() < 0.285714286) { /* pigs have a 4% chance to summon a hoglin on death and a 10% chance to summon a piglin on death */
                        new SpawnEntity(nmsWorld, new CustomEntityHoglin(nmsWorld), 1, null, bukkitEntity, null, false, true);
                    } else {
                        new SpawnEntity(nmsWorld, new CustomEntityPiglin(nmsWorld), 1, null, bukkitEntity, null, false, true);
                    }
                }
                break;
            case RAVAGER:  /* ravagers explode with power 4 when killed and give all raiders within 32 blocks horiztonally regen 32 for 1 second */
                bukkitWorld.createExplosion(bukkitLoc.getX(), bukkitLoc.getY(), bukkitLoc.getZ(), 4.0F, true);

                nmsWorld.getEntities(nmsEntity, nmsEntity.getBoundingBox().grow(32.0, 128.0, 32.0), entity -> entity instanceof EntityRaider).forEach(entity -> ((EntityRaider)entity).addEffect(new MobEffect(MobEffects.REGENERATION, 20, 31)));
                break;
            case SPIDER:  /* spiders lay down cobwebs that last 10 seconds in a 3 by 3 cube around itself when killed */
                EntitySpider spider = (EntitySpider)(nmsEntity);
                new RunnableSpawnBlocksAround(spider, org.bukkit.Material.COBWEB, 1).run();
                break;
            case WANDERING_TRADER:  /* wandering traders spawn 2 evokers and illusioners when killed */
                new SpawnEntity(nmsWorld, new CustomEntityEvoker(nmsWorld), 2, null, null, nmsEntity, false, true);
                new SpawnEntity(nmsWorld, new CustomEntityIllusioner(nmsWorld), 2, null, null, nmsEntity, false, true);
                break;
            case WITHER:
                if (nmsEntity instanceof CustomEntityWitherMini) { /* mini withers shoot less blue skulls in all directions and summon 3 wither skeletons when killed */
                    new RunnableWitherDeathSkulls((CustomEntityWither)nmsEntity,15).runTaskTimer(ExtremeDifficultyPlugin.plugin, 30L, 1L);
                    new SpawnEntity(nmsWorld, new CustomEntitySkeletonWither(nmsWorld), 3, null, null, nmsEntity, false, true);
                } else { /* withers also drop 3 eyes of ender when killed and shoot blue skulls in all directions and summon a mob rain */
                    bukkitWorld.dropItem(bukkitLoc, new ItemStack(Material.ENDER_EYE, 3));
                    new RunnableWitherDeathSkulls((CustomEntityWither)nmsEntity,60).runTaskTimer(ExtremeDifficultyPlugin.plugin, 30L, 1L);
                    new RunnableMobRain(nmsEntity, ((CustomEntityWither)nmsEntity).getGoalTarget(), 45.0, 1).runTaskTimer(ExtremeDifficultyPlugin.plugin, 110L, 2L);
                }
                break;
            case WITHER_SKELETON:  /* wither skeletons now have a +8% chance to drop a skull when killed */
                if (random.nextDouble() < 0.08) {
                    bukkitWorld.dropItem(bukkitLoc, new ItemStack(Material.WITHER_SKELETON_SKULL));
                }
                break;
            case ZOMBIE:
                if (nmsEntity instanceof CustomEntityZombieThor) { /* thors create a massive lightning storm and 2 rings of vanilla and custom lightning around itself when killed */
                    new RunnableLightningStorm(nmsWorld, bukkitLoc, random.nextInt(16) + 55).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, random.nextInt(3) + 2);
                    Location bukkitLoc2;

                    for (int i = 0; i < 8; i++) {
                        bukkitLoc2 = CustomMathHelper.coordsFromHypotAndAngle(bukkitWorld, new BlockPosition(bukkitLoc.getX(), bukkitLoc.getY(), bukkitLoc.getZ()), 3.0, bukkitWorld.getHighestBlockYAt(bukkitLoc), i * 45.0);
                        bukkitWorld.strikeLightning(bukkitLoc2);
                    }

                    for (int i = 0; i < 20; i++) {
                        bukkitLoc2 = CustomMathHelper.coordsFromHypotAndAngle(bukkitWorld, new BlockPosition(bukkitLoc.getX(), bukkitLoc.getY(), bukkitLoc.getZ()), 10.0, bukkitWorld.getHighestBlockYAt(bukkitLoc), i * 18.0);
                        new SpawnEntity(nmsWorld, new CustomEntityLightning(nmsWorld), 1, null, bukkitLoc2, false);
                    }
                } else if (nmsEntity instanceof CustomEntityZombieSuper) {
                    ListenerPlayerDeathAndRespawn.superZombies.remove(nmsEntity);
                } else { /* zombies summon an area effect cloud when killed */ // todo of what?
                    CustomEntityAreaEffectCloud newAEC = new CustomEntityAreaEffectCloud(nmsWorld, 2.0F, 80, 40);
                    newAEC.addEffect(new MobEffect(MobEffects.HARM, 0));

                    try {
                        newAEC.setColor(PotionUtil.a(PotionUtil.a((PotionRegistry)newAEC.potionRegistry.get(newAEC), newAEC.effects)));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    newAEC.setPosition(nmsEntity.locX(), nmsEntity.locY(), nmsEntity.locZ());
                    nmsWorld.addEntity(newAEC);
                }
                break;
        }
    }

    static class RunnableWitherDeathSkulls extends BukkitRunnable {

        private final CustomEntityWither wither;
        private final World nmsWorld;
        private int cycleCount;
        private final int maxCycleCount;

        public RunnableWitherDeathSkulls(CustomEntityWither wither, int maxCycleCount) {
            this.wither = wither;
            this.nmsWorld = wither.getWorld();
            this.cycleCount = 0;
            this.maxCycleCount = maxCycleCount;
        }

        @Override
        public void run() {
            if (++this.cycleCount > this.maxCycleCount) {
                this.cancel();
                return;
            }

            CustomEntityWitherSkull entityWitherSkull = new CustomEntityWitherSkull(this.nmsWorld, this.wither, random.nextDouble() * 2.0 - 1.0, random.nextDouble() * 2.0 - 1.0, random.nextDouble() * 2.0 - 1.0);
            entityWitherSkull.setCharged(true);
            entityWitherSkull.setPosition(entityWitherSkull.locX(), this.wither.e(0.5) + 0.5, entityWitherSkull.locZ());
            this.nmsWorld.addEntity(entityWitherSkull);
        }
    }
}
