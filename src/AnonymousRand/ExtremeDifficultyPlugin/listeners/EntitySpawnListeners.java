package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.CustomEntityHusk;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.CustomEntitySkeleton;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.CustomEntityZombie;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.plugin.java.JavaPlugin;

public class EntitySpawnListeners implements Listener {

    @EventHandler
    public void creatureSpawn(CreatureSpawnEvent event) { //replace mobs with custom mobs
        Location loc = event.getEntity().getLocation();

        if (!(((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntitySkeleton || ((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntityHusk || ((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntityZombie)) { //to prevent stack overflow when the new replacement mobs are spawned, causing this event to fire again and again
            switch (event.getEntityType()) {
                case HUSK:
                    Husk husk = (Husk)event.getEntity();
                    CustomEntityHusk newHusk = new CustomEntityHusk(((CraftWorld) husk.getWorld()).getHandle()); //bukkit.world vs net.minecraft.server world (spigot nms world): since loc and event listeners are a bukkit thing, it uses the bukkit world, but the customeentityskeleton uses nms. Must cast the bukkit world to craftworld world first, then use craftworld's getHandle() to get an nms world
                    newHusk.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)husk.getWorld()).getHandle().addEntity(newHusk, CreatureSpawnEvent.SpawnReason.NATURAL); //again, must do the casting process as addEntity is only found in an nms world
                    husk.remove();
                    break;
                case SKELETON:
                    Skeleton skeleton = (Skeleton)event.getEntity();
                    CustomEntitySkeleton newSkeleton = new CustomEntitySkeleton(((CraftWorld) skeleton.getWorld()).getHandle());
                    newSkeleton.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)skeleton.getWorld()).getHandle().addEntity(newSkeleton, CreatureSpawnEvent.SpawnReason.NATURAL);
                    skeleton.remove();
                    break;
                case ZOMBIE:
                    Zombie zombie = (Zombie)event.getEntity();
                    CustomEntityZombie newZombie = new CustomEntityZombie(((CraftWorld) zombie.getWorld()).getHandle());
                    newZombie.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)zombie.getWorld()).getHandle().addEntity(newZombie, CreatureSpawnEvent.SpawnReason.NATURAL);
                    zombie.remove();
                    break;
            }
        }
    }
}
