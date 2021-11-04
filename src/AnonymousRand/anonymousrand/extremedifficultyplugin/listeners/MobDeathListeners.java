package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.SpiderSummonCobwebBlock;
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

    private final JavaPlugin plugin;
    private final Random random = new Random();

    public MobDeathListeners(JavaPlugin plugin) {
        this.plugin = plugin;
    }

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
            case CHICKEN -> { /**chickens drop 20 eggs on death*/
                bukkitWorld.dropItem(loc, new ItemStack(Material.EGG, 20));

                if (nmsEntity instanceof CustomEntityChickenAggressive && !(nmsEntity instanceof CustomEntityChickenAggressiveExploding)) {
                    if (this.random.nextDouble() < 0.2) { /**aggressive chickens have a 20% chance to explode into 12-18 normal chickens*/
                        new SpawnLivingEntity(nmsWorld, new CustomEntityChicken(nmsWorld), this.random.nextInt(7) + 12, null, null, nmsEntity, false, true).run();
                    }
                } else if (nmsEntity instanceof CustomEntityChickenAggressiveExploding) { /**aggressive exploding chickens explode with power 1 after 20 seconds or when killed*/
                    bukkitWorld.createExplosion(loc.getX(), loc.getY(), loc.getZ(), 1.0F, false);
                }

            }
            case COW -> bukkitWorld.createExplosion(loc.getX(), loc.getY(), loc.getZ(), 2.0F, false); /**cows explode with power 2 when killed*/
            case ENDERMAN -> { /**enderman have a 0.5% chance to drop a pearl without looting, and only slightly more with looting*/
                if (event.getDrops().size() != 0) {
                    if (this.random.nextDouble() < 0.99) {
                        event.getDrops().clear();
                    }
                }
            }
            case MUSHROOM_COW -> bukkitWorld.createExplosion(loc.getX(), loc.getY(), loc.getZ(), 15.0F, false); /**mooshrooms explode with power 15 when killed*/
            case SPIDER -> { /**spiders lay down cobwebs that last 10 seconds when killed in a 3 by 3 cube around itself*/
                EntitySpider spider = (EntitySpider)(nmsEntity);
                new SpiderSummonCobwebBlock(spider).run();
            }
            case WANDERING_TRADER -> { /**wandering traders spawn 5 evokers and illusioners when killed*/
                new SpawnLivingEntity(this.plugin, nmsWorld, new CustomEntityEvoker(nmsWorld, this.plugin), 5, null, null, nmsEntity, false, true).run();
            }//todo: change to custom mobs
        }
    }
}
