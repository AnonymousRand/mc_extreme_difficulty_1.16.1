package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityEnderCrystal;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.Random;

public class ListenerMobSpawnAndReplaceWithCustom implements Listener {

    public static double phantomSize;
    private static final Random random = new Random();

    public ListenerMobSpawnAndReplaceWithCustom() {
        phantomSize = 0.0;
    }

    @EventHandler
    public void mobSpawn(CreatureSpawnEvent event) { // replace mobs with custom mobs when they spawn in
        LivingEntity bukkitEntity = event.getEntity();
        EntityLiving nmsEntity = ((CraftLivingEntity)event.getEntity()).getHandle();
        World nmsWorld = nmsEntity.getWorld();

        if (!(nmsEntity instanceof CustomEntityBat ||
                nmsEntity instanceof CustomEntityBee ||
                nmsEntity instanceof CustomEntityBlaze ||
                nmsEntity instanceof CustomEntitySpiderCave ||
                nmsEntity instanceof CustomEntityChicken ||
                nmsEntity instanceof CustomEntityChickenAggressive ||
                nmsEntity instanceof CustomEntityCow ||
                nmsEntity instanceof CustomEntityCreeper ||
                nmsEntity instanceof CustomEntityDrowned ||
                nmsEntity instanceof CustomEntityEnderDragon ||
                nmsEntity instanceof CustomEntityEnderman ||
                nmsEntity instanceof CustomEntityEndermite ||
                nmsEntity instanceof CustomEntityEvoker ||
                nmsEntity instanceof CustomEntityGhast ||
                nmsEntity instanceof CustomEntityGuardian ||
                nmsEntity instanceof CustomEntityGuardianElder ||
                nmsEntity instanceof CustomEntityHoglin ||
                nmsEntity instanceof EntityHorseSkeleton ||
                nmsEntity instanceof CustomEntityIllusioner ||
                nmsEntity instanceof CustomEntityIronGolem ||
                nmsEntity instanceof CustomEntitySlimeMagmaCube ||
                nmsEntity instanceof CustomEntityLlama ||
                nmsEntity instanceof CustomEntityLlamaTrader ||
                nmsEntity instanceof CustomEntityMushroomCow ||
                nmsEntity instanceof CustomEntityPhantom ||
                nmsEntity instanceof CustomEntityPig ||
                nmsEntity instanceof CustomEntityPiglin ||
                nmsEntity instanceof CustomEntityPillager ||
                nmsEntity instanceof CustomEntityPufferfish ||
                nmsEntity instanceof CustomEntityRabbit ||
                nmsEntity instanceof CustomEntityRavager ||
                nmsEntity instanceof CustomEntitySheep ||
                nmsEntity instanceof CustomEntitySheepAggressive ||
                nmsEntity instanceof CustomEntityShulker ||
                nmsEntity instanceof CustomEntitySilverfish ||
                nmsEntity instanceof CustomEntitySkeleton ||
                nmsEntity instanceof CustomEntitySkeletonStray ||
                nmsEntity instanceof CustomEntitySkeletonWither ||
                nmsEntity instanceof CustomEntitySlime ||
                nmsEntity instanceof CustomEntitySpider ||
                nmsEntity instanceof CustomEntityStrider ||
                nmsEntity instanceof CustomEntityVex ||
                nmsEntity instanceof EntityVillager ||
                nmsEntity instanceof CustomEntityVindicator ||
                nmsEntity instanceof CustomEntityWitch ||
                nmsEntity instanceof CustomEntityWither ||
                nmsEntity instanceof CustomEntityWolf ||
                nmsEntity instanceof CustomEntityZoglin ||
                nmsEntity instanceof CustomEntityZombie ||
                nmsEntity instanceof CustomEntityZombieHusk ||
                nmsEntity instanceof CustomEntityZombieSuper ||
                nmsEntity instanceof CustomEntityZombieThor ||
                nmsEntity instanceof CustomEntityZombieVillager ||
                nmsEntity instanceof CustomEntityZombiePig)) { // to prevent stack overflow when the new replacement mobs are spawned, causing this event to fire again and again

            CreatureSpawnEvent.SpawnReason spawnReason = event.getSpawnReason();

            switch (event.getEntityType()) {
                case BAT:
                    if (spawnReason != CreatureSpawnEvent.SpawnReason.DROWNED) {
                        new SpawnEntity(nmsWorld, new CustomEntityBat(nmsWorld), 1, null, bukkitEntity, null, true, false);
                    }
                    break;
                case BEE:
                    new SpawnEntity(nmsWorld, new CustomEntityBee(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case BLAZE:
                    if (spawnReason != CreatureSpawnEvent.SpawnReason.SPAWNER && random.nextDouble() < 0.1) { /** blazes not spawned from spawners have a 10% chance to spawn as a magma cube instead */
                        CustomEntitySlimeMagmaCube newMagmaCube = new CustomEntitySlimeMagmaCube(nmsWorld);
                        int i = random.nextInt(3) + 2;  /** all magma cubes spawn two "sizes" larger from sizes 4, 8, and 16 compared to 1, 2, and 4 only */
                        int j = 1 << i;
                        newMagmaCube.setSize(j, true);
                        new SpawnEntity(nmsWorld, newMagmaCube, 1, CreatureSpawnEvent.SpawnReason.SPAWNER, bukkitEntity, null, true, true);
                        return;
                    } else if (spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER && random.nextDouble() < 0.1) { /** blaze spawners have a 10% chance to spawn a wither skeleton instead */
                        new SpawnEntity(nmsWorld, new CustomEntitySkeletonWither(nmsWorld), 1, CreatureSpawnEvent.SpawnReason.SPAWNER, bukkitEntity, null, true, true);
                        return;
                    }

                    new SpawnEntity(nmsWorld, new CustomEntityBlaze(nmsWorld), 1, null, bukkitEntity, null, true, false);
                    break;
                case CAVE_SPIDER:
                    if (spawnReason != CreatureSpawnEvent.SpawnReason.DROWNED) {
                        new SpawnEntity(nmsWorld, new CustomEntitySpiderCave(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    }
                    break;
                case CHICKEN:
                    new SpawnEntity(nmsWorld, new CustomEntityChicken(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case COD:
                case PUFFERFISH:
                case SALMON:
                case SQUID:
                case TROPICAL_FISH:
                    new SpawnEntity(nmsWorld, new CustomEntityPufferfish(nmsWorld), 4, null, bukkitEntity, null, false, true); /** all fish spawn in an additional 4 pufferfish */
                    break;
                case COW:
                    new SpawnEntity(nmsWorld, new CustomEntityCow(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case CREEPER:
                    new SpawnEntity(nmsWorld, new CustomEntityCreeper(nmsWorld, 15), 1, null, bukkitEntity, null, true, true);
                    break;
                case DONKEY:
                case HORSE:
                case MULE: /** donkeys, horses and mules spawn in as a skeleton horse trap instead; becuase of this listener, the skeletons and horses are separate, not rider-passenger */
                    EntityHorseSkeleton newSkeletonHorse = new EntityHorseSkeleton(EntityTypes.SKELETON_HORSE, nmsWorld);
                    newSkeletonHorse.t(true);
                    new SpawnEntity(nmsWorld, newSkeletonHorse, 1, null, bukkitEntity, null, true, true);
                    break;
                case DROWNED:
                    new SpawnEntity(nmsWorld, new CustomEntityDrowned(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case ELDER_GUARDIAN:
                    new SpawnEntity(nmsWorld, new CustomEntityGuardianElder(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case ENDERMAN:
                    new SpawnEntity(nmsWorld, new CustomEntityEnderman(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case ENDERMITE:
                    new SpawnEntity(nmsWorld, new CustomEntityEndermite(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case EVOKER:
                    new SpawnEntity(nmsWorld, new CustomEntityEvoker(nmsWorld), 1, spawnReason, bukkitEntity, null, spawnReason != CreatureSpawnEvent.SpawnReason.RAID, true); /** raids spawn double of every mob, a normal one and a custom one to increase difficulty and make sure that the raid doesn't immediately finish (raids have a Stream to store its spawned mobs, and removing them for the replacement process removes them from the Stream as if they had been killed) */
                    break;
                case GHAST:
                    new SpawnEntity(nmsWorld, new CustomEntityGhast(nmsWorld), 1, null, bukkitEntity, null, true, false);
                    break;
                case GUARDIAN:
                    new SpawnEntity(nmsWorld, new CustomEntityGuardian(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case HOGLIN:
                    new SpawnEntity(nmsWorld, new CustomEntityHoglin(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case HUSK:
                    new SpawnEntity(nmsWorld, new CustomEntityZombieHusk(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case MAGMA_CUBE:
                    if (spawnReason == CreatureSpawnEvent.SpawnReason.NATURAL && random.nextDouble() < 0.3) { /** naturally-spawning magma cubes have a 10% chance to spawn as a shulker and a 20% chance to spawn as a strider instead */
                        if (random.nextDouble() < 0.666666666) {
                            new SpawnEntity(nmsWorld, new CustomEntityStrider(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        } else {
                            new SpawnEntity(nmsWorld, new CustomEntityShulker(nmsWorld), 1, null, bukkitEntity, null, true, false);
                        }

                        return;
                    }

                    int i = random.nextInt(3) + 2;  /** all magma cubes spawn two "sizes" larger from sizes 4, 8, and 16 compared to 1, 2, and 4 only */
                    int j = 1 << i;
                    int orgSize = ((EntityMagmaCube)nmsEntity).getSize();

                    if (spawnReason != CreatureSpawnEvent.SpawnReason.SLIME_SPLIT && j >= 2) {
                        new SpawnEntity(nmsWorld, new CustomEntitySlimeMagmaCube(nmsWorld, j), 1, null, bukkitEntity, null, true, true);
                    } else if (spawnReason == CreatureSpawnEvent.SpawnReason.SLIME_SPLIT && orgSize >= 2) {
                        new SpawnEntity(nmsWorld, new CustomEntitySlimeMagmaCube(nmsWorld, orgSize), 1, null, bukkitEntity, null, true, true);
                    } else { /** magma cubes can't exist at size 1 to prevent lag from too many cubes */
                        bukkitEntity.remove();
                    }
                    break;
                case IRON_GOLEM:
                    new SpawnEntity(nmsWorld, new CustomEntityIronGolem(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case ILLUSIONER:
                    new SpawnEntity(nmsWorld, new CustomEntityIllusioner(nmsWorld), 1, spawnReason, bukkitEntity, null, spawnReason != CreatureSpawnEvent.SpawnReason.RAID, true); /** raids spawn double of every mob, a normal one and a custom one to increase difficulty and make sure that the raid doesn't immediately finish (raids have a Stream to store its spawned mobs, and removing them for the replacement process removes them from the Stream as if they had been killed) */
                    break;
                case LLAMA:
                    new SpawnEntity(nmsWorld, new CustomEntityLlama(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case MUSHROOM_COW:
                    new SpawnEntity(nmsWorld, new CustomEntityMushroomCow(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case PHANTOM:
                    int rand = spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER ? 1 : random.nextInt(3) + 10;
                    new SpawnEntity(nmsWorld, (int)phantomSize, new CustomEntityPhantom(nmsWorld, (int)phantomSize), rand, null, bukkitEntity, null, true, false); /** when phantoms spawn naturally at night, they spawn random 10-12 at a time (10-24 on easy, 10-36 on medium, 10-48 on hard) */
                    break;
                case PIG:
                    new SpawnEntity(nmsWorld, new CustomEntityPig(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case PIGLIN:
                    new SpawnEntity(nmsWorld, new CustomEntityPiglin(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case PILLAGER:
                    new SpawnEntity(nmsWorld, new CustomEntityPillager(nmsWorld), 1, spawnReason, bukkitEntity, null, spawnReason != CreatureSpawnEvent.SpawnReason.RAID, true); /** raids spawn double of every mob, a normal one and a custom one to increase difficulty and make sure that the raid doesn't immediately finish (raids have a Stream to store its spawned mobs, and removing them for the replacement process removes them from the Stream as if they had been killed) */
                    break;
                case RABBIT:
                    new SpawnEntity(nmsWorld, new CustomEntityRabbit(nmsWorld), 5, null, bukkitEntity, null, true, true); /** rabbits are spawned in as 5 killer bunnies instead */
                    break;
                case RAVAGER:
                    new SpawnEntity(nmsWorld, new CustomEntityRavager(nmsWorld), 1, spawnReason, bukkitEntity, null, spawnReason != CreatureSpawnEvent.SpawnReason.RAID, true); /** raids spawn double of every mob, a normal one and a custom one to increase difficulty and make sure that the raid doesn't immediately finish (raids have a Stream to store its spawned mobs, and removing them for the replacement process removes them from the Stream as if they had been killed) */
                    break;
                case SHEEP: /** pink sheep are spawned in as 2 aggressive pink sheep instead */
                    if (((Sheep)bukkitEntity).getColor() == DyeColor.PINK) {
                        new SpawnEntity(nmsWorld, new CustomEntitySheepAggressive(nmsWorld), 2, null, bukkitEntity, null, true, true);
                    } else {
                        new SpawnEntity(nmsWorld, new CustomEntitySheep(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    }
                    break;
                case SHULKER:
                    new SpawnEntity(nmsWorld, new CustomEntityShulker(nmsWorld), 1, null, bukkitEntity, null, true, false);
                    break;
                case SILVERFISH:
                    new SpawnEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case SKELETON:
                    if (spawnReason != CreatureSpawnEvent.SpawnReason.DROWNED) {
                        new SpawnEntity(nmsWorld, new CustomEntitySkeleton(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    }
                    break;
                case SLIME:
                    i = random.nextInt(3) + 1;  /** all slimes spawn one "size" larger from sizes 2, 4, and 8 compared to 1, 2, and 4 only */
                    j = 1 << i;

                    if (spawnReason != CreatureSpawnEvent.SpawnReason.SLIME_SPLIT) {
                        new SpawnEntity(nmsWorld, new CustomEntitySlime(nmsWorld, j), 1, null, bukkitEntity, null, true, true);
                    } else {
                        new SpawnEntity(nmsWorld, new CustomEntitySlime(nmsWorld, ((EntitySlime)nmsEntity).getSize()), 1, null, bukkitEntity, null, true, true);
                    }

                    break;
                case SPIDER:
                    new SpawnEntity(nmsWorld, new CustomEntitySpider(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case STRAY:
                    new SpawnEntity(nmsWorld, new CustomEntitySkeletonStray(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case STRIDER:
                    new SpawnEntity(nmsWorld, new CustomEntityStrider(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case TRADER_LLAMA:
                    new SpawnEntity(nmsWorld, new CustomEntityLlamaTrader(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case VEX:
                    new SpawnEntity(nmsWorld, new CustomEntityVex(nmsWorld), 1, null, bukkitEntity, null, true, false);
                    break;
                case VINDICATOR:
                    new SpawnEntity(nmsWorld, new CustomEntityVindicator(nmsWorld), 1, spawnReason, bukkitEntity, null, spawnReason != CreatureSpawnEvent.SpawnReason.RAID, true); /** raids spawn double of every mob, a normal one and a custom one to increase difficulty and make sure that the raid doesn't immediately finish (raids have a Stream to store its spawned mobs, and removing them for the replacement process removes them from the Stream as if they had been killed) */
                    break;
                case WITCH:
                    new SpawnEntity(nmsWorld, new CustomEntityWitch(nmsWorld), 1, spawnReason, bukkitEntity, null, spawnReason != CreatureSpawnEvent.SpawnReason.RAID, true); /** raids spawn double of every mob, a normal one and a custom one to increase difficulty and make sure that the raid doesn't immediately finish (raids have a Stream to store its spawned mobs, and removing them for the replacement process removes them from the Stream as if they had been killed) */
                    break;
                case WITHER:
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> new SpawnEntity(nmsWorld, new CustomEntityWither(nmsWorld), 1, null, bukkitEntity, null, true, true), 1); // delay by 1 tick so that soul sand/soil and skulls are deleted
                    break;
                case WITHER_SKELETON:
                    if (spawnReason != CreatureSpawnEvent.SpawnReason.SPAWNER && random.nextDouble() < 0.1) { /** wither skeletons not spawned from spawners have a 10% chance to spawn as a magma cube instead */
                        CustomEntitySlimeMagmaCube newMagmaCube = new CustomEntitySlimeMagmaCube(nmsWorld);
                        i = random.nextInt(3) + 2;  /** all magma cubes spawn two "sizes" larger from sizes 4, 8, and 16 compared to 1, 2, and 4 only */
                        j = 1 << i;
                        newMagmaCube.setSize(j, true);
                        new SpawnEntity(nmsWorld, newMagmaCube, 1, CreatureSpawnEvent.SpawnReason.SPAWNER, bukkitEntity, null, true, true);
                        return;
                    }

                    new SpawnEntity(nmsWorld, new CustomEntitySkeletonWither(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case WOLF:
                    new SpawnEntity(nmsWorld, new CustomEntityWolf(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case ZOGLIN:
                    new SpawnEntity(nmsWorld, new CustomEntityZoglin(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case ZOMBIE:
                    if (spawnReason != CreatureSpawnEvent.SpawnReason.BEEHIVE) {
                        new SpawnEntity(nmsWorld, new CustomEntityZombie(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    }
                    break;
                case ZOMBIE_VILLAGER:
                    new SpawnEntity(nmsWorld, new CustomEntityZombieVillager(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
                case ZOMBIFIED_PIGLIN:
                    new SpawnEntity(nmsWorld, new CustomEntityZombiePig(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    break;
            }
        }
    }

    @EventHandler
    public void chunkLoad(ChunkLoadEvent event) {
        for (Entity bukkitEntity : event.getChunk().getEntities()) {
            if (!(bukkitEntity instanceof LivingEntity || bukkitEntity instanceof EnderCrystal)) {
                return;
            } else {
                net.minecraft.server.v1_16_R1.Entity nmsEntity = ((CraftEntity)bukkitEntity).getHandle();
                EntityType bukkitEntityType = bukkitEntity.getType();
                World nmsWorld = nmsEntity.getWorld();

                if (!(nmsEntity instanceof EntityBat ||
                        nmsEntity instanceof CustomEntityBee ||
                        nmsEntity instanceof CustomEntityBlaze ||
                        nmsEntity instanceof EntityCaveSpider ||
                        nmsEntity instanceof CustomEntityChicken ||
                        nmsEntity instanceof CustomEntityChickenAggressive ||
                        nmsEntity instanceof CustomEntityCow ||
                        nmsEntity instanceof CustomEntityCreeper ||
                        nmsEntity instanceof CustomEntityEnderCrystal ||
                        nmsEntity instanceof CustomEntityEnderDragon ||
                        nmsEntity instanceof CustomEntityEnderman ||
                        nmsEntity instanceof CustomEntityEndermite ||
                        nmsEntity instanceof CustomEntityEvoker ||
                        nmsEntity instanceof CustomEntityGhast ||
                        nmsEntity instanceof CustomEntityGuardian ||
                        nmsEntity instanceof CustomEntityGuardianElder ||
                        nmsEntity instanceof CustomEntityHoglin ||
                        nmsEntity instanceof EntityHorseSkeleton ||
                        nmsEntity instanceof CustomEntityIllusioner ||
                        nmsEntity instanceof CustomEntityIronGolem ||
                        nmsEntity instanceof CustomEntityLlama ||
                        nmsEntity instanceof CustomEntityLlamaTrader ||
                        nmsEntity instanceof CustomEntityMushroomCow ||
                        nmsEntity instanceof CustomEntityPhantom ||
                        nmsEntity instanceof CustomEntityPig ||
                        nmsEntity instanceof CustomEntityPiglin ||
                        nmsEntity instanceof CustomEntityPillager ||
                        nmsEntity instanceof CustomEntityPufferfish ||
                        nmsEntity instanceof CustomEntityRabbit ||
                        nmsEntity instanceof CustomEntityRavager ||
                        nmsEntity instanceof CustomEntitySheep ||
                        nmsEntity instanceof CustomEntitySheepAggressive ||
                        nmsEntity instanceof CustomEntityShulker ||
                        nmsEntity instanceof CustomEntitySilverfish ||
                        nmsEntity instanceof CustomEntitySkeleton ||
                        nmsEntity instanceof CustomEntitySkeletonStray ||
                        nmsEntity instanceof CustomEntitySkeletonWither ||
                        nmsEntity instanceof EntitySlime ||
                        nmsEntity instanceof CustomEntitySpider ||
                        nmsEntity instanceof CustomEntityStrider ||
                        nmsEntity instanceof CustomEntityVex ||
                        nmsEntity instanceof EntityVillager ||
                        nmsEntity instanceof CustomEntityVindicator ||
                        nmsEntity instanceof CustomEntityWitch ||
                        nmsEntity instanceof CustomEntityWither ||
                        nmsEntity instanceof CustomEntityWolf ||
                        nmsEntity instanceof CustomEntityZoglin ||
                        nmsEntity instanceof CustomEntityZombie ||
                        nmsEntity instanceof CustomEntityZombieHusk ||
                        nmsEntity instanceof CustomEntityZombieSuper ||
                        nmsEntity instanceof CustomEntityZombieThor ||
                        nmsEntity instanceof CustomEntityZombieVillager ||
                        nmsEntity instanceof CustomEntityZombiePig)) { // to prevent stack overflow when the new replacement mobs are spawned, causing this event to fire again and again

                    switch (bukkitEntityType) {
                        case BEE:
                            new SpawnEntity(nmsWorld, new CustomEntityBee(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case BLAZE:
                            new SpawnEntity(nmsWorld, new CustomEntityBlaze(nmsWorld), 1, null, bukkitEntity, null, true, false);
                            break;
                        case CHICKEN:
                            new SpawnEntity(nmsWorld, new CustomEntityChicken(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case COD:
                        case PUFFERFISH:
                        case SALMON:
                        case SQUID:
                        case TROPICAL_FISH:
                            new SpawnEntity(nmsWorld, new CustomEntityPufferfish(nmsWorld), 4, null, bukkitEntity, null, false, true);
                            break;
                        case COW:
                            new SpawnEntity(nmsWorld, new CustomEntityCow(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case CREEPER:
                            new SpawnEntity(nmsWorld, new CustomEntityCreeper(nmsWorld, 15), 1, null, bukkitEntity, null, true, true);
                            break;
                        case DONKEY:
                        case HORSE:
                        case MULE:
                            EntityHorseSkeleton newSkeletonHorse = new EntityHorseSkeleton(EntityTypes.SKELETON_HORSE, nmsWorld);
                            newSkeletonHorse.t(true);
                            new SpawnEntity(nmsWorld, newSkeletonHorse, 1, null, bukkitEntity, null, true, true);
                            break;
                        case DROWNED:
                            new SpawnEntity(nmsWorld, new CustomEntityDrowned(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case ELDER_GUARDIAN:
                            new SpawnEntity(nmsWorld, new CustomEntityGuardianElder(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case ENDER_CRYSTAL:
                            Location bukkitLoc = bukkitEntity.getLocation();

                            CustomEntityEnderCrystal newCrystal = new CustomEntityEnderCrystal(nmsWorld);
                            newCrystal.setPosition(bukkitLoc.getX(), bukkitLoc.getY() + 15, bukkitLoc.getZ()); /** end crystals are now spawned 15 blocks higher */
                            nmsWorld.addEntity(newCrystal);
                            bukkitEntity.remove();
                            break;
                        case ENDERMAN:
                            new SpawnEntity(nmsWorld, new CustomEntityEnderman(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case ENDERMITE:
                            new SpawnEntity(nmsWorld, new CustomEntityEndermite(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case EVOKER:
                            new SpawnEntity(nmsWorld, new CustomEntityEvoker(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case GHAST:
                            new SpawnEntity(nmsWorld, new CustomEntityGhast(nmsWorld), 1, null, bukkitEntity, null, true, false);
                            break;
                        case GUARDIAN:
                            new SpawnEntity(nmsWorld, new CustomEntityGuardian(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case HOGLIN:
                            new SpawnEntity(nmsWorld, new CustomEntityHoglin(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case HUSK:
                            new SpawnEntity(nmsWorld, new CustomEntityZombieHusk(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case ILLUSIONER:
                            new SpawnEntity(nmsWorld, new CustomEntityIllusioner(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case IRON_GOLEM:
                            new SpawnEntity(nmsWorld, new CustomEntityIronGolem(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case LLAMA:
                            new SpawnEntity(nmsWorld, new CustomEntityLlama(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case MUSHROOM_COW:
                            new SpawnEntity(nmsWorld, new CustomEntityMushroomCow(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case PIG:
                            new SpawnEntity(nmsWorld, new CustomEntityPig(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case PIGLIN:
                            new SpawnEntity(nmsWorld, new CustomEntityPiglin(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case PILLAGER:
                            new SpawnEntity(nmsWorld, new CustomEntityPillager(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case RABBIT: /** rabbits are spawned in as 5 killer bunnies instead */
                            if (((EntityRabbit) nmsEntity).getRabbitType() == 99) { // to avoid infinite duplication when joining world
                                return;
                            }

                            new SpawnEntity(nmsWorld, new CustomEntityRabbit(nmsWorld), 5, null, bukkitEntity, null, true, true);
                            break;
                        case RAVAGER:
                            new SpawnEntity(nmsWorld, new CustomEntityRavager(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case SHEEP:
                            if (((Sheep) bukkitEntity).getColor() == DyeColor.PINK) {
                                new SpawnEntity(nmsWorld, new CustomEntitySheepAggressive(nmsWorld), 2, null, bukkitEntity, null, true, true);
                            } else {
                                new SpawnEntity(nmsWorld, new CustomEntitySheep(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            }
                            break;
                        case SHULKER:
                            new SpawnEntity(nmsWorld, new CustomEntityShulker(nmsWorld), 1, null, bukkitEntity, null, true, false);
                            break;
                        case SILVERFISH:
                            new SpawnEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case SPIDER:
                            new SpawnEntity(nmsWorld, new CustomEntitySpider(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case STRAY:
                            new SpawnEntity(nmsWorld, new CustomEntitySkeletonStray(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case STRIDER:
                            new SpawnEntity(nmsWorld, new CustomEntityStrider(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case TRADER_LLAMA:
                            new SpawnEntity(nmsWorld, new CustomEntityLlamaTrader(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case VEX:
                            new SpawnEntity(nmsWorld, new CustomEntityVex(nmsWorld), 1, null, bukkitEntity, null, true, false);
                            break;
                        case VINDICATOR:
                            new SpawnEntity(nmsWorld, new CustomEntityVindicator(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case WITCH:
                            new SpawnEntity(nmsWorld, new CustomEntityWitch(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case WITHER:
                            new SpawnEntity(nmsWorld, new CustomEntityWither(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case WITHER_SKELETON:
                            new SpawnEntity(nmsWorld, new CustomEntitySkeletonWither(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case WOLF:
                            new SpawnEntity(nmsWorld, new CustomEntityWolf(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case ZOGLIN:
                            new SpawnEntity(nmsWorld, new CustomEntityZoglin(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case ZOMBIE_VILLAGER:
                            new SpawnEntity(nmsWorld, new CustomEntityZombieVillager(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                        case ZOMBIFIED_PIGLIN:
                            new SpawnEntity(nmsWorld, new CustomEntityZombiePig(nmsWorld), 1, null, bukkitEntity, null, true, true);
                            break;
                    }
                }
            }
        }
    }
}
