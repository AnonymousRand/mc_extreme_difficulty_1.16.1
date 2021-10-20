package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.*;
import org.bukkit.Location;
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

        if (!(((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntityBat ||
                ((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntityBee ||
                ((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntityBlaze ||
                ((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntityCaveSpider ||
                ((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntityChicken ||
                ((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntityChickenAggressive ||
                ((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntityCow ||
                ((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntityCreeper ||
                ((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntityDrowned ||
                ((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntityEndermite ||
                ((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntityHusk ||
                ((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntitySkeleton ||
                ((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntitySilverfish ||
                ((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntitySpider ||
                ((CraftLivingEntity)event.getEntity()).getHandle() instanceof CustomEntityZombie)) { //to prevent stack overflow when the new replacement mobs are spawned, causing this event to fire again and again

            switch (event.getEntityType()) {
                case BAT:
                    if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER) { //only if they are spawned in by this event and not by duplicating bats etc
                        Bat bat = (Bat)event.getEntity();
                        CustomEntityBat newBat = new CustomEntityBat(((CraftWorld)bat.getWorld()).getHandle()); //bukkit.world vs net.minecraft.server world (spigot nms world): since loc and event listeners are a bukkit thing, it uses the bukkit world, but the customeentityskeleton uses nms. Must cast the bukkit world to craftworld world first, then use craftworld's getHandle() to get an nms world
                        newBat.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                        ((CraftWorld)bat.getWorld()).getHandle().addEntity(newBat, CreatureSpawnEvent.SpawnReason.NATURAL); //again, must do the casting process as addEntity is only found in an nms world
                        bat.remove();
                    }
                    break;
                case BEE:
                    Bee bee = (Bee)event.getEntity();
                    CustomEntityBee newBee = new CustomEntityBee(((CraftWorld)bee.getWorld()).getHandle());
                    newBee.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)bee.getWorld()).getHandle().addEntity(newBee, CreatureSpawnEvent.SpawnReason.NATURAL);
                    bee.remove();
                    break;
                case BLAZE:
                    Blaze blaze = (Blaze)event.getEntity();
                    CustomEntityBlaze newBlaze = new CustomEntityBlaze(((CraftWorld)blaze.getWorld()).getHandle());
                    newBlaze.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)blaze.getWorld()).getHandle().addEntity(newBlaze, CreatureSpawnEvent.SpawnReason.NATURAL);
                    blaze.remove();
                    break;
                case CAVE_SPIDER:
                    CaveSpider caveSpider = (CaveSpider)event.getEntity();
                    CustomEntityCaveSpider newCaveSpider = new CustomEntityCaveSpider(((CraftWorld)caveSpider.getWorld()).getHandle());
                    newCaveSpider.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)caveSpider.getWorld()).getHandle().addEntity(newCaveSpider, CreatureSpawnEvent.SpawnReason.NATURAL);
                    caveSpider.remove();
                    break;
                case CHICKEN:
                    Chicken chicken = (Chicken)event.getEntity();
                    CustomEntityChicken newChicken = new CustomEntityChicken(((CraftWorld)chicken.getWorld()).getHandle());
                    newChicken.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)chicken.getWorld()).getHandle().addEntity(newChicken, CreatureSpawnEvent.SpawnReason.NATURAL);
                    chicken.remove();
                    break;
                case COW:
                    Cow cow = (Cow)event.getEntity();
                    CustomEntityCow newCow = new CustomEntityCow(((CraftWorld)cow.getWorld()).getHandle());
                    newCow.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)cow.getWorld()).getHandle().addEntity(newCow, CreatureSpawnEvent.SpawnReason.NATURAL);
                    cow.remove();
                    break;
                case CREEPER:
                    Creeper creeper = (Creeper)event.getEntity();
                    CustomEntityCreeper newCreeper = new CustomEntityCreeper(((CraftWorld)creeper.getWorld()).getHandle(), 15);
                    newCreeper.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)creeper.getWorld()).getHandle().addEntity(newCreeper, CreatureSpawnEvent.SpawnReason.NATURAL);
                    creeper.remove();
                    break;
                case DROWNED:
                    Drowned drowned = (Drowned)event.getEntity();
                    CustomEntityDrowned newDrowned = new CustomEntityDrowned(((CraftWorld)drowned.getWorld()).getHandle());
                    newDrowned.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)drowned.getWorld()).getHandle().addEntity(newDrowned, CreatureSpawnEvent.SpawnReason.NATURAL);
                    drowned.remove();
                    break;
                case ENDERMITE:
                    Endermite endermite = (Endermite)event.getEntity();
                    CustomEntityEndermite newEndermite = new CustomEntityEndermite(((CraftWorld)endermite.getWorld()).getHandle());
                    newEndermite.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)endermite.getWorld()).getHandle().addEntity(newEndermite, CreatureSpawnEvent.SpawnReason.NATURAL);
                    endermite.remove();
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
                case SILVERFISH:
                    Silverfish silverfish = (Silverfish)event.getEntity();
                    CustomEntitySilverfish newSilverfish = new CustomEntitySilverfish(((CraftWorld)silverfish.getWorld()).getHandle());
                    newSilverfish.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)silverfish.getWorld()).getHandle().addEntity(newSilverfish, CreatureSpawnEvent.SpawnReason.NATURAL);
                    silverfish.remove();
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
