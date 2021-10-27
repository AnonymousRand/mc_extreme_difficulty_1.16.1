package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftRabbit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class MobSpawnAndReplaceWithCustomListeners implements Listener {

    private static JavaPlugin plugin;

    public MobSpawnAndReplaceWithCustomListeners(JavaPlugin plugin2) {
        plugin = plugin2;
    }

    @EventHandler
    public void mobSpawn(CreatureSpawnEvent event) { //replace mobs with custom mobs when they spawn in
        LivingEntity bukkitEntity = event.getEntity();
        EntityLiving nmsEntity = ((CraftLivingEntity)event.getEntity()).getHandle();
        World nmsWorld = nmsEntity.getWorld();
        Location loc = event.getEntity().getLocation();

        if (!(nmsEntity instanceof CustomEntityBat ||
                nmsEntity instanceof CustomEntityBee ||
                nmsEntity instanceof CustomEntityBlaze ||
                nmsEntity instanceof CustomEntitySpiderCave ||
                nmsEntity instanceof CustomEntityChicken ||
                nmsEntity instanceof CustomEntityChickenAggressive ||
                nmsEntity instanceof CustomEntityCow ||
                nmsEntity instanceof CustomEntityCreeper ||
                nmsEntity instanceof CustomEntityDrowned ||
                nmsEntity instanceof CustomEntityEnderman ||
                nmsEntity instanceof CustomEntityEndermite ||
                nmsEntity instanceof CustomEntityEvoker ||
                nmsEntity instanceof CustomEntityGhast ||
                nmsEntity instanceof CustomEntityGuardian ||
                nmsEntity instanceof CustomEntityGuardianElder ||
                nmsEntity instanceof CustomEntityHoglin ||
                nmsEntity instanceof CustomEntityLlama ||
                nmsEntity instanceof CustomEntityLlamaTrader ||
                nmsEntity instanceof CustomEntityMushroomCow ||
                nmsEntity instanceof CustomEntityRabbit ||
                nmsEntity instanceof CustomEntityRavager ||
                nmsEntity instanceof CustomEntitySheepAggressive ||
                nmsEntity instanceof CustomEntitySilverfish ||
                nmsEntity instanceof CustomEntitySkeleton ||
                nmsEntity instanceof CustomEntitySkeletonStray ||
                nmsEntity instanceof CustomEntitySpider ||
                nmsEntity instanceof CustomEntityZombie ||
                nmsEntity instanceof CustomEntityZombieHusk)) { //to prevent stack overflow when the new replacement mobs are spawned, causing this event to fire again and again

            org.bukkit.inventory.ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
            boots.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 3); /**most mobs spawn with depth strider 6 to avoid loopholes such as using water flow to keep them back*/
            boots.addUnsafeEnchantment(Enchantment.DURABILITY, 255);

            switch (event.getEntityType()) {
                case BLAZE:
                    Blaze blaze = (Blaze)bukkitEntity;
                    CustomEntityBlaze newBlaze = new CustomEntityBlaze(nmsWorld);
                    newBlaze.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    nmsWorld.addEntity(newBlaze, CreatureSpawnEvent.SpawnReason.NATURAL);
                    blaze.remove();
                    break;
                case CAVE_SPIDER:
                    if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.DROWNED) {
                        CaveSpider caveSpider = (CaveSpider)bukkitEntity;
                        CustomEntitySpiderCave newCaveSpider = new CustomEntitySpiderCave(nmsWorld);
                        newCaveSpider.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                        ((LivingEntity)newCaveSpider.getBukkitEntity()).getEquipment().setBoots(boots);
                        nmsWorld.addEntity(newCaveSpider, CreatureSpawnEvent.SpawnReason.NATURAL);
                        caveSpider.remove();
                    }
                    break;
                case CHICKEN:
                    Chicken chicken = (Chicken)bukkitEntity;
                    CustomEntityChicken newChicken = new CustomEntityChicken(nmsWorld);
                    newChicken.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((LivingEntity)newChicken.getBukkitEntity()).getEquipment().setBoots(boots);
                    nmsWorld.addEntity(newChicken, CreatureSpawnEvent.SpawnReason.NATURAL);
                    chicken.remove();
                    break;
                case COW:
                    Cow cow = (Cow)bukkitEntity;
                    CustomEntityCow newCow = new CustomEntityCow(nmsWorld);
                    newCow.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((LivingEntity)newCow.getBukkitEntity()).getEquipment().setBoots(boots);
                    nmsWorld.addEntity(newCow, CreatureSpawnEvent.SpawnReason.NATURAL);
                    cow.remove();
                    break;
                case CREEPER:
                    Creeper creeper = (Creeper)bukkitEntity;
                    CustomEntityCreeper newCreeper = new CustomEntityCreeper(nmsWorld, 15);
                    newCreeper.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((LivingEntity)newCreeper.getBukkitEntity()).getEquipment().setBoots(boots);
                    nmsWorld.addEntity(newCreeper, CreatureSpawnEvent.SpawnReason.NATURAL);
                    creeper.remove();
                    break;
                case DROWNED:
                    Drowned drowned = (Drowned)bukkitEntity;
                    CustomEntityDrowned newDrowned = new CustomEntityDrowned(nmsWorld);
                    newDrowned.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((LivingEntity)newDrowned.getBukkitEntity()).getEquipment().setBoots(boots);
                    nmsWorld.addEntity(newDrowned, CreatureSpawnEvent.SpawnReason.NATURAL);
                    drowned.remove();
                    break;
                case ELDER_GUARDIAN:
                    ElderGuardian elderGuardian = (ElderGuardian)bukkitEntity;
                    CustomEntityGuardianElder newElderGuardian = new CustomEntityGuardianElder(nmsWorld);
                    newElderGuardian.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    nmsWorld.addEntity(newElderGuardian, CreatureSpawnEvent.SpawnReason.NATURAL);
                    elderGuardian.remove();
                    break;
                case ENDERMAN:
                    Enderman enderman = (Enderman)bukkitEntity;
                    CustomEntityEnderman newEnderman = new CustomEntityEnderman(nmsWorld);
                    newEnderman.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((LivingEntity)newEnderman.getBukkitEntity()).getEquipment().setBoots(boots);
                    nmsWorld.addEntity(newEnderman, CreatureSpawnEvent.SpawnReason.NATURAL);
                    enderman.remove();
                    break;
                case ENDERMITE:
                    Endermite endermite = (Endermite)bukkitEntity;
                    CustomEntityEndermite newEndermite = new CustomEntityEndermite(nmsWorld);
                    newEndermite.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((LivingEntity)newEndermite.getBukkitEntity()).getEquipment().setBoots(boots);
                    nmsWorld.addEntity(newEndermite, CreatureSpawnEvent.SpawnReason.NATURAL);
                    endermite.remove();
                    break;
                case EVOKER:
                    Evoker evoker = (Evoker)bukkitEntity;
                    CustomEntityEvoker newEvoker = new CustomEntityEvoker(nmsWorld, plugin);
                    newEvoker.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((LivingEntity)newEvoker.getBukkitEntity()).getEquipment().setBoots(boots);
                    nmsWorld.addEntity(newEvoker, CreatureSpawnEvent.SpawnReason.NATURAL);
                    evoker.remove();
                    break;
                case GHAST:
                    Ghast ghast = (Ghast)bukkitEntity;
                    CustomEntityGhast newGhast = new CustomEntityGhast(nmsWorld, plugin);
                    newGhast.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    nmsWorld.addEntity(newGhast, CreatureSpawnEvent.SpawnReason.NATURAL);
                    ghast.remove();
                    break;
                case GUARDIAN:
                    Guardian guardian = (Guardian)bukkitEntity;
                    CustomEntityGuardian newGuardian = new CustomEntityGuardian(nmsWorld);
                    newGuardian.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    nmsWorld.addEntity(newGuardian, CreatureSpawnEvent.SpawnReason.NATURAL);
                    guardian.remove();
                    break;
                case HOGLIN:
                    Hoglin hoglin = (Hoglin)bukkitEntity;
                    CustomEntityHoglin newHoglin = new CustomEntityHoglin(nmsWorld);
                    newHoglin.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    nmsWorld.addEntity(newHoglin, CreatureSpawnEvent.SpawnReason.NATURAL);
                    hoglin.remove();
                    break;
                case HUSK:
                    Husk husk = (Husk)bukkitEntity;
                    CustomEntityZombieHusk newHusk = new CustomEntityZombieHusk(nmsWorld);
                    newHusk.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((LivingEntity)newHusk.getBukkitEntity()).getEquipment().setBoots(boots);
                    nmsWorld.addEntity(newHusk, CreatureSpawnEvent.SpawnReason.NATURAL);
                    husk.remove();
                    break;
                case LLAMA:
                    Llama llama = (Llama)bukkitEntity;
                    CustomEntityLlama newLlama = new CustomEntityLlama(nmsWorld);
                    newLlama.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((LivingEntity)newLlama.getBukkitEntity()).getEquipment().setBoots(boots);
                    nmsWorld.addEntity(newLlama, CreatureSpawnEvent.SpawnReason.NATURAL);
                    llama.remove();
                    break;
                case MUSHROOM_COW:
                    MushroomCow mooshroom = (MushroomCow)bukkitEntity;
                    CustomEntityMushroomCow newMooshroom = new CustomEntityMushroomCow(nmsWorld);
                    newMooshroom.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((LivingEntity)newMooshroom.getBukkitEntity()).getEquipment().setBoots(boots);
                    nmsWorld.addEntity(newMooshroom, CreatureSpawnEvent.SpawnReason.NATURAL);
                    mooshroom.remove();
                    break;
                case RABBIT:
                    Rabbit rabbit = (Rabbit)bukkitEntity;
                    CustomEntityRabbit newRabbit;

                    for (int i = 0; i < 6; i++) { /**all rabbits are spawned in as 6 killer bunnies instead*/
                        newRabbit = new CustomEntityRabbit(nmsWorld);
                        newRabbit.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                        newRabbit.setRabbitType(99);
                        ((LivingEntity)newRabbit.getBukkitEntity()).getEquipment().setBoots(boots);
                        nmsWorld.addEntity(newRabbit, CreatureSpawnEvent.SpawnReason.NATURAL);
                        newRabbit.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 1)); /**changing attributes don't work on rabbits so killer bunnies have speed 2 and jump boost 1*/
                        newRabbit.addEffect(new MobEffect(MobEffects.JUMP, Integer.MAX_VALUE, 1));
                    }
                    rabbit.remove();
                    break;
                case RAVAGER:
                    Ravager ravager = (Ravager)bukkitEntity;
                    CustomEntityRavager newRavager = new CustomEntityRavager(nmsWorld);
                    newRavager.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((LivingEntity)newRavager.getBukkitEntity()).getEquipment().setBoots(boots);
                    nmsWorld.addEntity(newRavager, CreatureSpawnEvent.SpawnReason.NATURAL);
                    newRavager.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 2)); /**changing attributes don't work on ravagers so ravagers have speed 3*/
                    ravager.remove();
                    break;
                case SHEEP:
                    Sheep sheep = (Sheep)bukkitEntity;

                    if (sheep.getColor() == DyeColor.PINK) {
                        CustomEntitySheepAggressive newSheep = new CustomEntitySheepAggressive(nmsWorld, plugin);
                        newSheep.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                        ((LivingEntity)newSheep.getBukkitEntity()).getEquipment().setBoots(boots);
                        newSheep.setColor(EnumColor.PINK);
                        nmsWorld.addEntity(newSheep, CreatureSpawnEvent.SpawnReason.NATURAL);
                        sheep.remove();
                    }
                    break;
                case SILVERFISH:
                    Silverfish silverfish = (Silverfish)bukkitEntity;
                    CustomEntitySilverfish newSilverfish = new CustomEntitySilverfish(nmsWorld);
                    newSilverfish.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((LivingEntity)newSilverfish.getBukkitEntity()).getEquipment().setBoots(boots);
                    nmsWorld.addEntity(newSilverfish, CreatureSpawnEvent.SpawnReason.NATURAL);
                    silverfish.remove();
                    break;
                case SKELETON:
                    if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.DROWNED) {
                        Skeleton skeleton = (Skeleton)bukkitEntity;
                        CustomEntitySkeleton newSkeleton = new CustomEntitySkeleton(nmsWorld, plugin);
                        newSkeleton.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                        ((LivingEntity)newSkeleton.getBukkitEntity()).getEquipment().setBoots(boots);
                        nmsWorld.addEntity(newSkeleton, CreatureSpawnEvent.SpawnReason.NATURAL);
                        skeleton.remove();
                    }
                    break;
                case SPIDER:
                    Spider spider = (Spider)bukkitEntity;
                    CustomEntitySpider newSpider = new CustomEntitySpider(nmsWorld);
                    newSpider.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((LivingEntity)newSpider.getBukkitEntity()).getEquipment().setBoots(boots);
                    nmsWorld.addEntity(newSpider, CreatureSpawnEvent.SpawnReason.NATURAL);
                    spider.remove();
                    break;
                case STRAY:
                    Stray stray = (Stray)bukkitEntity;
                    CustomEntitySkeletonStray newStray = new CustomEntitySkeletonStray(nmsWorld);
                    newStray.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((LivingEntity)newStray.getBukkitEntity()).getEquipment().setBoots(boots);
                    nmsWorld.addEntity(newStray, CreatureSpawnEvent.SpawnReason.NATURAL);
                    stray.remove();
                    break;
                case TRADER_LLAMA:
                    TraderLlama traderLlama = (TraderLlama)bukkitEntity;
                    CustomEntityLlamaTrader newTraderLlama = new CustomEntityLlamaTrader(nmsWorld);
                    newTraderLlama.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((LivingEntity)newTraderLlama.getBukkitEntity()).getEquipment().setBoots(boots);
                    nmsWorld.addEntity(newTraderLlama, CreatureSpawnEvent.SpawnReason.NATURAL);
                    traderLlama.remove();
                    break;
                case ZOMBIE:
                    Zombie zombie = (Zombie)bukkitEntity;
                    CustomEntityZombie newZombie = new CustomEntityZombie(nmsWorld);
                    newZombie.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    ((LivingEntity)newZombie.getBukkitEntity()).getEquipment().setBoots(boots);
                    nmsWorld.addEntity(newZombie, CreatureSpawnEvent.SpawnReason.NATURAL);
                    zombie.remove();
                    break;
            }
        }
    }

    @EventHandler
    public void chunkLoad(ChunkLoadEvent event) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() //delay by 10 ticks to pick up all the mobs that weren't detected by creature spawn event
        {
            public void run()
            {
                for (Entity bukkitEntity : event.getChunk().getEntities()) {
                    if (!(bukkitEntity instanceof LivingEntity)) {
                        return;
                    } else {
                        EntityLiving nmsEntity = ((CraftLivingEntity)bukkitEntity).getHandle();
                        EntityType type = bukkitEntity.getType();
                        World nmsWorld = nmsEntity.getWorld();
                        Location loc = bukkitEntity.getLocation();

                        if (!(nmsEntity instanceof CustomEntityBat ||
                                nmsEntity instanceof CustomEntityBee ||
                                nmsEntity instanceof CustomEntityBlaze ||
                                nmsEntity instanceof EntityCaveSpider ||
                                nmsEntity instanceof CustomEntityChicken ||
                                nmsEntity instanceof CustomEntityChickenAggressive ||
                                nmsEntity instanceof CustomEntityCow ||
                                nmsEntity instanceof CustomEntityCreeper ||
                                nmsEntity instanceof CustomEntityDrowned ||
                                nmsEntity instanceof CustomEntityEnderman ||
                                nmsEntity instanceof CustomEntityEndermite ||
                                nmsEntity instanceof CustomEntityEvoker ||
                                nmsEntity instanceof CustomEntityGhast ||
                                nmsEntity instanceof CustomEntityGuardian ||
                                nmsEntity instanceof CustomEntityGuardianElder ||
                                nmsEntity instanceof CustomEntityHoglin ||
                                nmsEntity instanceof CustomEntityLlama ||
                                nmsEntity instanceof CustomEntityLlamaTrader ||
                                nmsEntity instanceof CustomEntityMushroomCow ||
                                nmsEntity instanceof CustomEntityRabbit ||
                                nmsEntity instanceof CustomEntityRavager ||
                                nmsEntity instanceof CustomEntitySheepAggressive ||
                                nmsEntity instanceof CustomEntitySilverfish ||
                                nmsEntity instanceof EntitySkeleton ||
                                nmsEntity instanceof CustomEntitySkeletonStray ||
                                nmsEntity instanceof CustomEntitySpider ||
                                nmsEntity instanceof CustomEntityZombie ||
                                nmsEntity instanceof CustomEntityZombieHusk)) { //to prevent stack overflow when the new replacement mobs are spawned, causing this event to fire again and again

                            org.bukkit.inventory.ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
                            boots.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 3); /**most mobs spawn with depth strider 6 to avoid loopholes such as using water flow to keep them back*/
                            boots.addUnsafeEnchantment(Enchantment.DURABILITY, 255);

                            switch (type) {
                                case BLAZE:
                                    Blaze blaze = (Blaze)bukkitEntity;
                                    CustomEntityBlaze newBlaze = new CustomEntityBlaze(nmsWorld);
                                    newBlaze.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    nmsWorld.addEntity(newBlaze, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    blaze.remove();
                                    break;
                                case CHICKEN:
                                    Chicken chicken = (Chicken)bukkitEntity;
                                    CustomEntityChicken newChicken = new CustomEntityChicken(nmsWorld);
                                    newChicken.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    ((LivingEntity)newChicken.getBukkitEntity()).getEquipment().setBoots(boots);
                                    nmsWorld.addEntity(newChicken, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    chicken.remove();
                                    break;
                                case COW:
                                    Cow cow = (Cow)bukkitEntity;
                                    CustomEntityCow newCow = new CustomEntityCow(nmsWorld);
                                    newCow.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    ((LivingEntity)newCow.getBukkitEntity()).getEquipment().setBoots(boots);
                                    nmsWorld.addEntity(newCow, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    cow.remove();
                                    break;
                                case CREEPER:
                                    Creeper creeper = (Creeper)bukkitEntity;
                                    CustomEntityCreeper newCreeper = new CustomEntityCreeper(nmsWorld, 15);
                                    newCreeper.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    ((LivingEntity)newCreeper.getBukkitEntity()).getEquipment().setBoots(boots);
                                    nmsWorld.addEntity(newCreeper, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    creeper.remove();
                                    break;
                                case DROWNED:
                                    Drowned drowned = (Drowned)bukkitEntity;
                                    CustomEntityDrowned newDrowned = new CustomEntityDrowned(nmsWorld);
                                    newDrowned.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    ((LivingEntity)newDrowned.getBukkitEntity()).getEquipment().setBoots(boots);
                                    nmsWorld.addEntity(newDrowned, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    drowned.remove();
                                    break;
                                case ELDER_GUARDIAN:
                                    ElderGuardian elderGuardian = (ElderGuardian)bukkitEntity;
                                    CustomEntityGuardianElder newElderGuardian = new CustomEntityGuardianElder(nmsWorld);
                                    newElderGuardian.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    nmsWorld.addEntity(newElderGuardian, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    elderGuardian.remove();
                                    break;
                                case ENDERMAN:
                                    Enderman enderman = (Enderman)bukkitEntity;
                                    CustomEntityEnderman newEnderman = new CustomEntityEnderman(nmsWorld);
                                    newEnderman.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    ((LivingEntity)newEnderman.getBukkitEntity()).getEquipment().setBoots(boots);
                                    nmsWorld.addEntity(newEnderman, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    enderman.remove();
                                    break;
                                case ENDERMITE:
                                    Endermite endermite = (Endermite)bukkitEntity;
                                    CustomEntityEndermite newEndermite = new CustomEntityEndermite(nmsWorld);
                                    newEndermite.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    ((LivingEntity)newEndermite.getBukkitEntity()).getEquipment().setBoots(boots);
                                    nmsWorld.addEntity(newEndermite, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    endermite.remove();
                                    break;
                                case EVOKER:
                                    Evoker evoker = (Evoker)bukkitEntity;
                                    CustomEntityEvoker newEvoker = new CustomEntityEvoker(nmsWorld, plugin);
                                    newEvoker.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    ((LivingEntity)newEvoker.getBukkitEntity()).getEquipment().setBoots(boots);
                                    nmsWorld.addEntity(newEvoker, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    evoker.remove();
                                    break;
                                case GHAST:
                                    Ghast ghast = (Ghast)bukkitEntity;
                                    CustomEntityGhast newGhast = new CustomEntityGhast(nmsWorld, plugin);
                                    newGhast.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    nmsWorld.addEntity(newGhast, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    ghast.remove();
                                    break;
                                case GUARDIAN:
                                    Guardian guardian = (Guardian)bukkitEntity;
                                    CustomEntityGuardian newGuardian = new CustomEntityGuardian(nmsWorld);
                                    newGuardian.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    nmsWorld.addEntity(newGuardian, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    guardian.remove();
                                    break;
                                case HOGLIN:
                                    Hoglin hoglin = (Hoglin)bukkitEntity;
                                    CustomEntityHoglin newHoglin = new CustomEntityHoglin(nmsWorld);
                                    newHoglin.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    nmsWorld.addEntity(newHoglin, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    hoglin.remove();
                                    break;
                                case HUSK:
                                    Husk husk = (Husk)bukkitEntity;
                                    CustomEntityZombieHusk newHusk = new CustomEntityZombieHusk(nmsWorld);
                                    newHusk.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    ((LivingEntity)newHusk.getBukkitEntity()).getEquipment().setBoots(boots);
                                    nmsWorld.addEntity(newHusk, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    husk.remove();
                                    break;
                                case LLAMA:
                                    Llama llama = (Llama)bukkitEntity;
                                    CustomEntityLlama newLlama = new CustomEntityLlama(nmsWorld);
                                    newLlama.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    ((LivingEntity)newLlama.getBukkitEntity()).getEquipment().setBoots(boots);
                                    nmsWorld.addEntity(newLlama, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    llama.remove();
                                    break;
                                case MUSHROOM_COW:
                                    MushroomCow mooshroom = (MushroomCow)bukkitEntity;
                                    CustomEntityMushroomCow newMooshroom = new CustomEntityMushroomCow(nmsWorld);
                                    newMooshroom.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    ((LivingEntity)newMooshroom.getBukkitEntity()).getEquipment().setBoots(boots);
                                    nmsWorld.addEntity(newMooshroom, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    mooshroom.remove();
                                    break;
                                case RABBIT:
                                    Rabbit rabbit = (Rabbit)bukkitEntity;
                                    CustomEntityRabbit newRabbit;

                                    if (((CraftRabbit)rabbit).getHandle().getRabbitType() == 99) { //to avoid infinte duplication when joining world
                                        return;
                                    }

                                    for (int i = 0; i < 6; i++) { /**all rabbits are spawned in as 6 killer bunnies instead*/
                                        newRabbit = new CustomEntityRabbit(nmsWorld);
                                        newRabbit.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                        newRabbit.setRabbitType(99);
                                        ((LivingEntity)newRabbit.getBukkitEntity()).getEquipment().setBoots(boots);
                                        nmsWorld.addEntity(newRabbit, CreatureSpawnEvent.SpawnReason.NATURAL);
                                        newRabbit.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 1)); /**changing attributes don't work on rabbits so killer bunnies have speed 2 and jump boost 1*/
                                        newRabbit.addEffect(new MobEffect(MobEffects.JUMP, Integer.MAX_VALUE, 1));
                                    }
                                    rabbit.remove();
                                    break;
                                case RAVAGER:
                                    Ravager ravager = (Ravager)bukkitEntity;
                                    CustomEntityRavager newRavager = new CustomEntityRavager(nmsWorld);
                                    newRavager.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    ((LivingEntity)newRavager.getBukkitEntity()).getEquipment().setBoots(boots);
                                    nmsWorld.addEntity(newRavager, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    newRavager.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 2)); /**changing attributes don't work on ravagers so ravagers have speed 3*/
                                    ravager.remove();
                                    break;
                                case SHEEP:
                                    Sheep sheep = (Sheep)bukkitEntity;

                                    if (sheep.getColor() == DyeColor.PINK) {
                                        CustomEntitySheepAggressive newSheep = new CustomEntitySheepAggressive(nmsWorld, plugin);
                                        newSheep.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                        ((LivingEntity)newSheep.getBukkitEntity()).getEquipment().setBoots(boots);
                                        newSheep.setColor(EnumColor.PINK);
                                        nmsWorld.addEntity(newSheep, CreatureSpawnEvent.SpawnReason.NATURAL);
                                        sheep.remove();
                                    }
                                    break;
                                case SILVERFISH:
                                    Silverfish silverfish = (Silverfish)bukkitEntity;
                                    CustomEntitySilverfish newSilverfish = new CustomEntitySilverfish(nmsWorld);
                                    newSilverfish.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    ((LivingEntity)newSilverfish.getBukkitEntity()).getEquipment().setBoots(boots);
                                    nmsWorld.addEntity(newSilverfish, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    silverfish.remove();
                                    break;
                                case SPIDER:
                                    Spider spider = (Spider)bukkitEntity;
                                    CustomEntitySpider newSpider = new CustomEntitySpider(nmsWorld);
                                    newSpider.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    ((LivingEntity)newSpider.getBukkitEntity()).getEquipment().setBoots(boots);
                                    nmsWorld.addEntity(newSpider, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    spider.remove();
                                    break;
                                case STRAY:
                                    Stray stray = (Stray)bukkitEntity;
                                    CustomEntitySkeletonStray newStray = new CustomEntitySkeletonStray(nmsWorld);
                                    newStray.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    ((LivingEntity)newStray.getBukkitEntity()).getEquipment().setBoots(boots);
                                    nmsWorld.addEntity(newStray, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    stray.remove();
                                    break;
                                case TRADER_LLAMA:
                                    TraderLlama traderLlama = (TraderLlama)bukkitEntity;
                                    CustomEntityLlamaTrader newTraderLlama = new CustomEntityLlamaTrader(nmsWorld);
                                    newTraderLlama.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    ((LivingEntity)newTraderLlama.getBukkitEntity()).getEquipment().setBoots(boots);
                                    nmsWorld.addEntity(newTraderLlama, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    traderLlama.remove();
                                    break;
                                case ZOMBIE:
                                    Zombie zombie = (Zombie)bukkitEntity;
                                    CustomEntityZombie newZombie = new CustomEntityZombie(nmsWorld);
                                    newZombie.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                                    ((LivingEntity)newZombie.getBukkitEntity()).getEquipment().setBoots(boots);
                                    nmsWorld.addEntity(newZombie, CreatureSpawnEvent.SpawnReason.NATURAL);
                                    zombie.remove();
                                    break;
                            }
                        }
                    }
                }
            }
        }, 10L);
    }
}
