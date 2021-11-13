package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityAreaEffectCloud;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityLightning;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CustomMathHelper;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableLightningStorm;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableSpiderSilverfishSummonMaterialBlock;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class MobDeathListeners implements Listener {

    public static JavaPlugin plugin;
    private static final Random random = new Random();

    @EventHandler
    public void mobDeath(EntityDeathEvent event) {
        LivingEntity bukkitEntity = event.getEntity();
        EntityLiving nmsEntity = ((CraftLivingEntity)bukkitEntity).getHandle();
        EntityType bukkitEntityType = event.getEntityType();
        org.bukkit.World bukkitWorld = bukkitEntity.getWorld();
        World nmsWorld = ((CraftWorld)bukkitWorld).getHandle();
        Location loc = event.getEntity().getLocation();

        switch (bukkitEntityType) { //stuff that happens when mobs die regardless of attack count
            case BAT -> bukkitWorld.createExplosion(loc.getX(), loc.getY(), loc.getZ(), 0.5F, false); /**bats explode with power 0.5 when killed*/
            case CHICKEN -> { /**chickens drop 20 eggs when killed*/
                bukkitWorld.dropItem(loc, new ItemStack(Material.EGG, 20));

                if (nmsEntity instanceof CustomEntityChickenAggressive && !(nmsEntity instanceof CustomEntityChickenAggressiveExploding)) {
                    if (random.nextDouble() < 0.2) { /**aggressive chickens have a 20% chance to explode into 12-18 normal chickens*/
                        new SpawnLivingEntity(nmsWorld, new CustomEntityChicken(nmsWorld), random.nextInt(7) + 12, null, null, nmsEntity, false, true);
                    }
                } else if (nmsEntity instanceof CustomEntityChickenAggressiveExploding) { /**aggressive exploding chickens explode with power 1 after 20 seconds or when killed*/
                    bukkitWorld.createExplosion(loc.getX(), loc.getY(), loc.getZ(), 1.0F, false);
                }

            }
            case COW -> bukkitWorld.createExplosion(loc.getX(), loc.getY(), loc.getZ(), 2.0F, false); /**cows explode with power 2 when killed*/
            case ENDERMAN -> { /**enderman have a 0.5% chance to drop a pearl without looting, and only slightly more with looting*/
                if (event.getDrops().size() != 0) {
                    if (random.nextDouble() < 0.99) {
                        event.getDrops().clear();
                    }
                }
            }
            case IRON_GOLEM -> new SpawnLivingEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 15, null, null, nmsEntity, false, true); /**iron golems summon 15 silverfish when killed*/
            case MUSHROOM_COW -> bukkitWorld.createExplosion(loc.getX(), loc.getY(), loc.getZ(), 15.0F, false); /**mooshrooms explode with power 15 when killed*/
            case PIG -> {
                if (random.nextDouble() < 0.14) {
                    if (random.nextDouble() < 0.285714286) { /**pigs have a 4% chance to summon a hoglin on death and a 10% chance to summon a piglin on death*/
                        new SpawnLivingEntity(nmsWorld, new CustomEntityHoglin(nmsWorld), 1, null, bukkitEntity, null, false, true);
                    } else {
                        new SpawnLivingEntity(nmsWorld, new CustomEntityPiglin(nmsWorld), 1, null, bukkitEntity, null, false, true);
                    }
                }
            }
            case SPIDER -> { /**spiders lay down cobwebs that last 10 seconds when killed in a 3 by 3 cube around itself*/
                EntitySpider spider = (EntitySpider)(nmsEntity);
                new RunnableSpiderSilverfishSummonMaterialBlock(spider, org.bukkit.Material.COBWEB, 1);
            }
            case WANDERING_TRADER -> { /**wandering traders spawn 2 evokers and illusioners when killed*/
                new SpawnLivingEntity(nmsWorld, new CustomEntityEvoker(nmsWorld), 2, null, null, nmsEntity, false, true);
                new SpawnLivingEntity(nmsWorld, new CustomEntityIllagerIllusioner(nmsWorld), 2, null, null, nmsEntity, false, true);
            }
            case ZOMBIE -> {
                if (nmsEntity instanceof CustomEntityZombieThor) { /**thors create a massive lightning storm and 2 rings of vanilla and custom lightning around itself when killed*/
                    new RunnableLightningStorm(nmsWorld, loc, random.nextInt(21) + 50).runTaskTimer(plugin, 0L, random.nextInt(4) + 2);
                    Location loc2;

                    for (int i = 0; i < 8; i++) {
                        loc2 = CustomMathHelper.coordsFromHypotenuseAndAngle(bukkitWorld, new BlockPosition(loc.getX(), loc.getY(), loc.getZ()), 3.0, loc.getY(), i * 45.0);
                        bukkitWorld.strikeLightning(loc2);
                    }

                    CustomEntityLightning newLightning;
                    for (int i = 0; i < 20; i++) {
                        loc2 = CustomMathHelper.coordsFromHypotenuseAndAngle(bukkitWorld, new BlockPosition(loc.getX(), loc.getY(), loc.getZ()), 10.0, loc.getY(), i * 18.0);

                        newLightning = new CustomEntityLightning(nmsWorld);
                        newLightning.setPosition(loc2.getX(), loc2.getY(), loc2.getZ());
                        nmsWorld.addEntity(newLightning);
                    }
                } else if (nmsEntity instanceof CustomEntityZombieSuper) {
                    PlayerDeathAndRespawnListeners.superZombies.remove(nmsEntity);
                } else { /**zombies summon an area effect cloud when killed*/
                    CustomEntityAreaEffectCloud newAEC = new CustomEntityAreaEffectCloud(nmsWorld, 2.0F, 100, 30);
                    newAEC.addEffect(new MobEffect(MobEffects.HARM, 0));

                    try {
                        newAEC.setColor(PotionUtil.a(PotionUtil.a((PotionRegistry)newAEC.potionRegistry.get(newAEC), newAEC.effects)));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    newAEC.setPosition(nmsEntity.locX(), nmsEntity.locY(), nmsEntity.locZ());
                    nmsWorld.addEntity(newAEC);
                }
            }
        }
    }
}
