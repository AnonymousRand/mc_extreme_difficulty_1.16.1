package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntityChicken;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntityChickenAggressive;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntityGhast;
import net.minecraft.server.v1_16_R1.*;
import net.minecraft.server.v1_16_R1.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

import static org.bukkit.entity.EntityType.*;

public class MobDeathListeners implements Listener {

    Random rand = new Random();

    @EventHandler
    public void mobDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        EntityType entityType = event.getEntityType();
        Location loc = event.getEntity().getLocation();

        switch (entityType) {
            case CHICKEN: /**chickens drop 20 eggs on death*/
                entity.getWorld().dropItem(entity.getLocation(), new ItemStack(Material.EGG, 20));

                if (((CraftLivingEntity)entity).getHandle() instanceof CustomEntityChickenAggressive) {
                    if (rand.nextDouble() < 0.2) { /**aggressive chickens have a 20% chance to explode into 12-18 chickens*/
                        CustomEntityChicken newChicken;
                        Chicken chicken = (Chicken)entity;

                        for (int i = rand.nextInt(7); i <= 18; i++) {
                            newChicken = new CustomEntityChicken(((CraftWorld)chicken.getWorld()).getHandle());
                            newChicken.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                            ((CraftWorld)chicken.getWorld()).getHandle().addEntity(newChicken, CreatureSpawnEvent.SpawnReason.NATURAL);
                        }
                    }
                }
                break;
            case COW: /**cows explode with power 2 upon death*/
                entity.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 2.0f, false);
                break;
            case MUSHROOM_COW: /**mooshrooms explode with power 15 upon death*/
                entity.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 15.0f, false);
                break;
            case SPIDER: /**spiders lay down cobwebs that last 10 seconds upon death in a 5 by 5 cube around itself*/
                Location loc2;
                EntitySpider spider = (EntitySpider)(((CraftLivingEntity)entity).getHandle());

                for (int x = -2; x <= 2; x++) {
                    for (int y = -2; y <= 2; y++) {
                        for (int z = -2; z <= 2; z++) {
                            loc2 = new Location(spider.getWorld().getWorld(), Math.floor(spider.locX()) + x, Math.floor(spider.locY()) + y, Math.floor(spider.locZ()) + z);
                            if (loc2.getBlock().getType() == Material.AIR) {
                                loc2.getBlock().setType(Material.COBWEB);
                                Bukkit.getPluginManager().callEvent(new BlockPlaceEvent(loc2.getBlock(), loc2.getBlock().getState(), null, null, null, false, null)); //fire event that would otherwise not be fired so that the cobweb block can be broken after 10 seconds
                            }
                        }
                    }
                }
                break;
            case WANDERING_TRADER: /**wandering traders spawn 10 evokers and illusioners upon death*/ //todo: change to custom mobs
                WanderingTrader trader = (WanderingTrader)entity;
                EntityIllagerIllusioner illusioner;
                EntityEvoker evoker;

                for (int i = 0; i < 10; i++) {
                    illusioner = new EntityIllagerIllusioner(EntityTypes.ILLUSIONER, ((CraftWorld)trader.getWorld()).getHandle());
                    evoker = new EntityEvoker(EntityTypes.EVOKER, ((CraftWorld)trader.getWorld()).getHandle());

                    illusioner.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)trader.getWorld()).getHandle().addEntity(illusioner, CreatureSpawnEvent.SpawnReason.NATURAL);

                    evoker.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)trader.getWorld()).getHandle().addEntity(evoker, CreatureSpawnEvent.SpawnReason.NATURAL);
                }
                break;
        }
    }
}
