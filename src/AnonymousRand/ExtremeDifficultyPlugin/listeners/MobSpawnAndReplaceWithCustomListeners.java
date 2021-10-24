package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.*;
import net.minecraft.server.v1_16_R1.Entity;
import net.minecraft.server.v1_16_R1.MobEffect;
import net.minecraft.server.v1_16_R1.MobEffects;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.plugin.java.JavaPlugin;

public class MobSpawnAndReplaceWithCustomListeners implements Listener {

    private JavaPlugin plugin;

    public MobSpawnAndReplaceWithCustomListeners(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void creatureSpawn(CreatureSpawnEvent event) { //replace mobs with custom mobs
        Location loc = event.getEntity().getLocation();

        Entity entity = ((CraftLivingEntity)event.getEntity()).getHandle();

        if (!(entity instanceof CustomEntityBat ||
                entity instanceof CustomEntityBee ||
                entity instanceof CustomEntityBlaze ||
                entity instanceof CustomEntitySpiderCave ||
                entity instanceof CustomEntityChicken ||
                entity instanceof CustomEntityChickenAggressive ||
                entity instanceof CustomEntityCow ||
                entity instanceof CustomEntityCreeper ||
                entity instanceof CustomEntityDrowned ||
                entity instanceof CustomEntityEnderman ||
                entity instanceof CustomEntityEndermite ||
                entity instanceof CustomEntityEvoker ||
                entity instanceof CustomEntityGhast ||
                entity instanceof CustomEntityGuardian ||
                entity instanceof CustomEntityGuardianElder ||
                entity instanceof CustomEntityHusk ||
                entity instanceof CustomEntityLlama ||
                entity instanceof CustomEntityLlamaTrader ||
                entity instanceof CustomEntityMushroomCow ||
                entity instanceof CustomEntityRabbit ||
                entity instanceof CustomEntityRavager ||
                entity instanceof CustomEntitySkeleton ||
                entity instanceof CustomEntitySilverfish ||
                entity instanceof CustomEntitySkeletonStray ||
                entity instanceof CustomEntitySpider ||
                entity instanceof CustomEntityZombie)) { //to prevent stack overflow when the new replacement mobs are spawned, causing this event to fire again and again

            switch (event.getEntityType()) {
                case BAT:
                    if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.DROWNED) { //only if they are spawned in by this event and not by duplicating bats etc
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
                    if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.DROWNED) {
                        CaveSpider caveSpider = (CaveSpider)event.getEntity();
                        CustomEntitySpiderCave newCaveSpider = new CustomEntitySpiderCave(((CraftWorld)caveSpider.getWorld()).getHandle());
                        newCaveSpider.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                        ((CraftWorld)caveSpider.getWorld()).getHandle().addEntity(newCaveSpider, CreatureSpawnEvent.SpawnReason.NATURAL);
                        caveSpider.remove();
                    }
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
                case ELDER_GUARDIAN:
                    ElderGuardian elderGuardian = (ElderGuardian)event.getEntity();
                    CustomEntityGuardianElder newElderGuardian = new CustomEntityGuardianElder(((CraftWorld)elderGuardian.getWorld()).getHandle());
                    newElderGuardian.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)elderGuardian.getWorld()).getHandle().addEntity(newElderGuardian, CreatureSpawnEvent.SpawnReason.NATURAL);
                    elderGuardian.remove();
                    break;
                case ENDERMAN:
                    Enderman enderman = (Enderman)event.getEntity();
                    CustomEntityEnderman newEnderman = new CustomEntityEnderman(((CraftWorld)enderman.getWorld()).getHandle());
                    newEnderman.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)enderman.getWorld()).getHandle().addEntity(newEnderman, CreatureSpawnEvent.SpawnReason.NATURAL);
                    enderman.remove();
                    break;
                case ENDERMITE:
                    Endermite endermite = (Endermite)event.getEntity();
                    CustomEntityEndermite newEndermite = new CustomEntityEndermite(((CraftWorld)endermite.getWorld()).getHandle());
                    newEndermite.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)endermite.getWorld()).getHandle().addEntity(newEndermite, CreatureSpawnEvent.SpawnReason.NATURAL);
                    endermite.remove();
                    break;
                case EVOKER:
                    Evoker evoker = (Evoker)event.getEntity();
                    CustomEntityEvoker newEvoker = new CustomEntityEvoker(((CraftWorld)evoker.getWorld()).getHandle(), this.plugin);
                    newEvoker.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)evoker.getWorld()).getHandle().addEntity(newEvoker, CreatureSpawnEvent.SpawnReason.NATURAL);
                    evoker.remove();
                    break;
                case GHAST:
                    Ghast ghast = (Ghast)event.getEntity();
                    CustomEntityGhast newGhast = new CustomEntityGhast(((CraftWorld)ghast.getWorld()).getHandle());
                    newGhast.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)ghast.getWorld()).getHandle().addEntity(newGhast, CreatureSpawnEvent.SpawnReason.NATURAL);
                    ghast.remove();
                    break;
                case GUARDIAN:
                    Guardian guardian = (Guardian)event.getEntity();
                    CustomEntityGuardian newGuardian = new CustomEntityGuardian(((CraftWorld)guardian.getWorld()).getHandle());
                    newGuardian.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)guardian.getWorld()).getHandle().addEntity(newGuardian, CreatureSpawnEvent.SpawnReason.NATURAL);
                    guardian.remove();
                    break;
                case HUSK:
                    Husk husk = (Husk)event.getEntity();
                    CustomEntityHusk newHusk = new CustomEntityHusk(((CraftWorld)husk.getWorld()).getHandle());
                    newHusk.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)husk.getWorld()).getHandle().addEntity(newHusk, CreatureSpawnEvent.SpawnReason.NATURAL);
                    husk.remove();
                    break;
                case LLAMA:
                    Llama llama = (Llama)event.getEntity();
                    CustomEntityLlama newLlama = new CustomEntityLlama(((CraftWorld)llama.getWorld()).getHandle());
                    newLlama.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)llama.getWorld()).getHandle().addEntity(newLlama, CreatureSpawnEvent.SpawnReason.NATURAL);
                    llama.remove();
                    break;
                case TRADER_LLAMA:
                    TraderLlama traderLlama = (TraderLlama)event.getEntity();
                    CustomEntityLlamaTrader newTraderLlama = new CustomEntityLlamaTrader(((CraftWorld)traderLlama.getWorld()).getHandle());
                    newTraderLlama.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)traderLlama.getWorld()).getHandle().addEntity(newTraderLlama, CreatureSpawnEvent.SpawnReason.NATURAL);
                    traderLlama.remove();
                    break;
                case MUSHROOM_COW:
                    MushroomCow mooshroom = (MushroomCow)event.getEntity();
                    CustomEntityMushroomCow newMooshroom = new CustomEntityMushroomCow(((CraftWorld)mooshroom.getWorld()).getHandle());
                    newMooshroom.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)mooshroom.getWorld()).getHandle().addEntity(newMooshroom, CreatureSpawnEvent.SpawnReason.NATURAL);
                    mooshroom.remove();
                    break;
                case RABBIT:
                    Rabbit rabbit = (Rabbit)event.getEntity();
                    CustomEntityRabbit newRabbit;

                    for (int i = 0; i < 5; i++) { /**all rabbits are spawned in as 5 killer bunnies instead*/
                        newRabbit = new CustomEntityRabbit(((CraftWorld)rabbit.getWorld()).getHandle());
                        newRabbit.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                        ((CraftWorld)rabbit.getWorld()).getHandle().addEntity(newRabbit, CreatureSpawnEvent.SpawnReason.NATURAL);
                        newRabbit.setRabbitType(99);
                    }
                    rabbit.remove();
                    break;
                case RAVAGER:
                    Ravager ravager = (Ravager)event.getEntity();
                    CustomEntityRavager newRavager = new CustomEntityRavager(((CraftWorld)ravager.getWorld()).getHandle());
                    newRavager.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)ravager.getWorld()).getHandle().addEntity(newRavager, CreatureSpawnEvent.SpawnReason.NATURAL);
                    newRavager.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 1)); /**changing attributes don't work on ravagers so ravagers have speed 2*/
                    ravager.remove();
                    break;
                case SKELETON:
                    if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.DROWNED) {
                        Skeleton skeleton = (Skeleton)event.getEntity();
                        CustomEntitySkeleton newSkeleton = new CustomEntitySkeleton(((CraftWorld)skeleton.getWorld()).getHandle(), this.plugin);
                        newSkeleton.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                        ((CraftWorld)skeleton.getWorld()).getHandle().addEntity(newSkeleton, CreatureSpawnEvent.SpawnReason.NATURAL);
                        skeleton.remove();
                    }
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
                case STRAY:
                    Stray stray = (Stray)event.getEntity();
                    CustomEntitySkeletonStray newStray = new CustomEntitySkeletonStray(((CraftWorld)stray.getWorld()).getHandle());
                    newStray.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((CraftWorld)stray.getWorld()).getHandle().addEntity(newStray, CreatureSpawnEvent.SpawnReason.NATURAL);
                    stray.remove();
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
