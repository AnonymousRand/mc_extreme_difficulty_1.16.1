package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;

public class EntitySpawnAndReplaceWithCustomListeners implements Listener {

    @EventHandler
    public void creatureSpawn(CreatureSpawnEvent event) { //replace mobs with custom mobs
        Location loc = event.getEntity().getLocation();

        if (!(((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntityBee || ((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntityBlaze ||((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntityHusk || ((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntitySkeleton || ((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntitySpider || ((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntityZombie)) { //to prevent stack overflow when the new replacement mobs are spawned, causing this event to fire again and again
            switch (event.getEntityType()) {
                case BEE:
                    Bee bee = (Bee)event.getEntity();
                    CustomEntityBee newBee = new CustomEntityBee(((CraftWorld)bee.getWorld()).getHandle()); //bukkit.world vs net.minecraft.server world (spigot nms world): since loc and event listeners are a bukkit thing, it uses the bukkit world, but the customeentityskeleton uses nms. Must cast the bukkit world to craftworld world first, then use craftworld's getHandle() to get an nms world
                    newBee.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)bee.getWorld()).getHandle().addEntity(newBee, CreatureSpawnEvent.SpawnReason.NATURAL); //again, must do the casting process as addEntity is only found in an nms world
                    bee.remove();
                    break;
                case BLAZE:
                    Blaze blaze = (Blaze)event.getEntity();
                    CustomEntityBlaze newBlaze = new CustomEntityBlaze(((CraftWorld)blaze.getWorld()).getHandle());
                    newBlaze.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)blaze.getWorld()).getHandle().addEntity(newBlaze, CreatureSpawnEvent.SpawnReason.NATURAL);
                    blaze.remove();
                    break;
                case HUSK:
                    Husk husk = (Husk)event.getEntity();
                    CustomEntityHusk newHusk = new CustomEntityHusk(((CraftWorld)husk.getWorld()).getHandle());
                    newHusk.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)husk.getWorld()).getHandle().addEntity(newHusk, CreatureSpawnEvent.SpawnReason.NATURAL);
                    husk.remove();
                    break;
                case SKELETON:
                    Skeleton skeleton = (Skeleton)event.getEntity();
                    CustomEntitySkeleton newSkeleton = new CustomEntitySkeleton(((CraftWorld)skeleton.getWorld()).getHandle());
                    newSkeleton.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)skeleton.getWorld()).getHandle().addEntity(newSkeleton, CreatureSpawnEvent.SpawnReason.NATURAL);
                    skeleton.remove();
                    break;
                case SPIDER:
                    Spider spider = (Spider)event.getEntity();
                    CustomEntitySpider newSpider = new CustomEntitySpider(((CraftWorld)spider.getWorld()).getHandle());
                    newSpider.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)spider.getWorld()).getHandle().addEntity(newSpider, CreatureSpawnEvent.SpawnReason.NATURAL);
                    spider.remove();
                    break;
                case ZOMBIE:
                    Zombie zombie = (Zombie)event.getEntity();
                    CustomEntityZombie newZombie = new CustomEntityZombie(((CraftWorld)zombie.getWorld()).getHandle());
                    newZombie.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)zombie.getWorld()).getHandle().addEntity(newZombie, CreatureSpawnEvent.SpawnReason.NATURAL);
                    zombie.remove();
                    break;
            }
        }
    }
}
