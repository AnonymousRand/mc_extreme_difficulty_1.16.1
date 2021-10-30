package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
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
            case BAT: /**bats explode with power 0.5 upon death*/
                bukkitWorld.createExplosion(loc.getX(), loc.getY(), loc.getZ(), 0.5f, false);
                break;
            case CHICKEN: /**chickens drop 20 eggs on death*/
                bukkitWorld.dropItem(loc, new ItemStack(Material.EGG, 20));

                if (nmsEntity instanceof CustomEntityChickenAggressive && !(nmsEntity instanceof CustomEntityChickenAggressiveExploding)) {
                    if (this.random.nextDouble() < 0.2) { /**aggressive chickens have a 20% chance to explode into 12-18 chickens*/
                        CustomEntityChicken newChicken;

                        for (int i = random.nextInt(7); i <= 18; i++) {
                            newChicken = new CustomEntityChicken(nmsWorld);
                            newChicken.setPosition(loc.getX(), loc.getY(), loc.getZ());
                            nmsWorld.addEntity(newChicken, CreatureSpawnEvent.SpawnReason.NATURAL);
                        }
                    }
                }
                break;
            case COW: /**cows explode with power 2 upon death*/
                bukkitWorld.createExplosion(loc.getX(), loc.getY(), loc.getZ(), 2.0f, false);
                break;
            case ENDERMAN:
                if (event.getDrops().size() != 0) {
                    if (this.random.nextDouble() < 0.98) { /**enderman have a 1% chance to drop a pearl without looting, and only slightly more with looting*/
                        event.getDrops().clear();
                    }
                }
                break;
            case MUSHROOM_COW: /**mooshrooms explode with power 15 upon death*/
                bukkitWorld.createExplosion(loc.getX(), loc.getY(), loc.getZ(), 15.0f, false);
                break;
            case SPIDER: /**spiders lay down cobwebs that last 10 seconds upon death in a 3 by 3 cube around itself*/
                Location loc2;
                EntitySpider spider = (EntitySpider)(nmsEntity);

                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
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
                WanderingTrader trader = (WanderingTrader)bukkitEntity;
                EntityIllagerIllusioner illusioner;
                CustomEntityEvoker evoker;

                for (int i = 0; i < 10; i++) {
                    illusioner = new EntityIllagerIllusioner(EntityTypes.ILLUSIONER, nmsWorld);
                    evoker = new CustomEntityEvoker(nmsWorld, this.plugin);

                    illusioner.setPosition(loc.getX(), loc.getY(), loc.getZ());
                    nmsWorld.addEntity(illusioner, CreatureSpawnEvent.SpawnReason.NATURAL);

                    evoker.setPosition(loc.getX(), loc.getY(), loc.getZ());
                    nmsWorld.addEntity(evoker, CreatureSpawnEvent.SpawnReason.NATURAL);
                }
                break;
        }
    }
}
