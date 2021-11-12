package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class MobSpawnAndReplaceWithCustomListeners implements Listener {

    private final JavaPlugin plugin;
    public static double phantomSize;
    private final Random random = new Random();

    public MobSpawnAndReplaceWithCustomListeners(JavaPlugin plugin) {
        this.plugin = plugin;
        phantomSize = 0.0;
    }

    @EventHandler
    public void mobSpawn(CreatureSpawnEvent event) { //replace mobs with custom mobs when they spawn in
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
                nmsEntity instanceof CustomEntityEnderman ||
                nmsEntity instanceof CustomEntityEndermite ||
                nmsEntity instanceof CustomEntityEvoker ||
                nmsEntity instanceof CustomEntityGhast ||
                nmsEntity instanceof CustomEntityGuardian ||
                nmsEntity instanceof CustomEntityGuardianElder ||
                nmsEntity instanceof CustomEntityHoglin ||
                nmsEntity instanceof CustomEntityIllagerIllusioner ||
                nmsEntity instanceof CustomEntityIronGolem ||
                nmsEntity instanceof CustomEntitySlimeMagmaCube ||
                nmsEntity instanceof CustomEntityLlama ||
                nmsEntity instanceof CustomEntityLlamaTrader ||
                nmsEntity instanceof CustomEntityMushroomCow ||
                nmsEntity instanceof CustomEntityPhantom ||
                nmsEntity instanceof CustomEntityPig ||
                nmsEntity instanceof CustomEntityPiglin ||
                nmsEntity instanceof CustomEntityPillager ||
                nmsEntity instanceof CustomEntityRabbit ||
                nmsEntity instanceof CustomEntityRavager ||
                nmsEntity instanceof CustomEntitySheepAggressive ||
                nmsEntity instanceof CustomEntitySilverfish ||
                nmsEntity instanceof CustomEntitySkeleton ||
                nmsEntity instanceof CustomEntitySkeletonStray ||
                nmsEntity instanceof CustomEntitySlime ||
                nmsEntity instanceof CustomEntitySpider ||
                nmsEntity instanceof CustomEntityZoglin ||
                nmsEntity instanceof CustomEntityZombie ||
                nmsEntity instanceof CustomEntityZombieHusk ||
                nmsEntity instanceof CustomEntityZombieSuper ||
                nmsEntity instanceof CustomEntityZombieThor ||
                nmsEntity instanceof CustomEntityZombieVillager)) { //to prevent stack overflow when the new replacement mobs are spawned, causing this event to fire again and again

            CreatureSpawnEvent.SpawnReason spawnReason = event.getSpawnReason();

            switch (event.getEntityType()) {
                case BAT -> {
                    if (spawnReason != CreatureSpawnEvent.SpawnReason.DROWNED) {
                        new SpawnLivingEntity(nmsWorld, new CustomEntityBat(nmsWorld), 1, null, bukkitEntity, null, true, false);
                    }
                }
                case BEE -> new SpawnLivingEntity(nmsWorld, new CustomEntityBee(nmsWorld), 1, null, bukkitEntity, null, true, false);
                case BLAZE -> {
                    if (spawnReason != CreatureSpawnEvent.SpawnReason.SPAWNER && this.random.nextDouble() < 0.1) { /**blazes not spawned from spawners have a 10% chance to spawn as a magma cube instead*/
                        CustomEntitySlimeMagmaCube newMagmaCube = new CustomEntitySlimeMagmaCube(nmsWorld); //todo copy to wither skeleton as well
                        int i = this.random.nextInt(3) + 2;  /**all magma cubes spawn two "sizes" larger from sizes 4, 8, and 16 compared to 1, 2, and 4 only*/
                        int j = 1 << i;
                        newMagmaCube.setSize(j, true);
                        new SpawnLivingEntity(nmsWorld, newMagmaCube, 1, CreatureSpawnEvent.SpawnReason.SPAWNER, bukkitEntity, null, true, true);
                        return;
                    }

                    new SpawnLivingEntity(nmsWorld, new CustomEntityBlaze(nmsWorld), 1, null, bukkitEntity, null, true, false);
                }
                case CAVE_SPIDER -> {
                    if (spawnReason != CreatureSpawnEvent.SpawnReason.DROWNED) {
                        new SpawnLivingEntity(nmsWorld, new CustomEntitySpiderCave(nmsWorld), 1, null, bukkitEntity, null, true, true);
                    }
                }
                case CHICKEN -> new SpawnLivingEntity(nmsWorld, new CustomEntityChicken(nmsWorld), 1, null, bukkitEntity, null, true, true);
                case COW -> new SpawnLivingEntity(nmsWorld, new CustomEntityCow(nmsWorld), 1, null, bukkitEntity, null, true, true);
                case CREEPER -> new SpawnLivingEntity(nmsWorld, new CustomEntityCreeper(nmsWorld, 15), 1, null, bukkitEntity, null, true, true);
                case DROWNED -> new SpawnLivingEntity(nmsWorld, new CustomEntityDrowned(nmsWorld), 1, null, bukkitEntity, null, true, true);
                case ELDER_GUARDIAN -> new SpawnLivingEntity(nmsWorld, new CustomEntityGuardianElder(nmsWorld), 1, null, bukkitEntity, null, true, true);
                case ENDERMAN -> new SpawnLivingEntity(nmsWorld, new CustomEntityEnderman(nmsWorld), 1, null, bukkitEntity, null, true, true);
                case ENDERMITE -> new SpawnLivingEntity(nmsWorld, new CustomEntityEndermite(nmsWorld), 1, null, bukkitEntity, null, true, true);
                case EVOKER -> new SpawnLivingEntity(nmsWorld, new CustomEntityEvoker(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, true);
                case GHAST -> new SpawnLivingEntity(nmsWorld, new CustomEntityGhast(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, false);
                case GUARDIAN -> new SpawnLivingEntity(nmsWorld, new CustomEntityGuardian(nmsWorld), 1, null, bukkitEntity, null, true, true);
                case HOGLIN -> new SpawnLivingEntity(nmsWorld, new CustomEntityHoglin(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, true);
                case HUSK -> new SpawnLivingEntity(nmsWorld, new CustomEntityZombieHusk(nmsWorld), 1, null, bukkitEntity, null, true, true);
                case MAGMA_CUBE -> {
                    CustomEntitySlimeMagmaCube newMagmaCube = new CustomEntitySlimeMagmaCube(nmsWorld);

                    if (spawnReason == CreatureSpawnEvent.SpawnReason.NATURAL && this.random.nextDouble() < 0.3) { /**naturally-spawning magma cubes have a 10% chance to spawn as a shulker and a 20% chance to spawn as a strider instead*/
                        bukkitEntity.remove();

                        /*if (this.random.nextDouble() < 0.666666666) { //todo
                            new SpawnLivingEntity();
                        } else {
                            new SpawnLivingEntity();
                        }*/

                        return;
                    }

                    int i = this.random.nextInt(3) + 2;  /**all magma cubes spawn two "sizes" larger from sizes 4, 8, and 16 compared to 1, 2, and 4 only*/
                    int j = 1 << i;

                    if (spawnReason != CreatureSpawnEvent.SpawnReason.SLIME_SPLIT) {
                        newMagmaCube.setSize(j, true);
                    } else {
                        newMagmaCube.setSize(((EntityMagmaCube)nmsEntity).getSize(), true);
                    }

                    if (newMagmaCube.getSize() < 2) { /**magma cubes can't exist at size 1 to prevent lag from too many cubes*/
                        bukkitEntity.remove();
                        return;
                    }

                    new SpawnLivingEntity(nmsWorld, newMagmaCube, 1, null, bukkitEntity, null, true, true);
                }
                case IRON_GOLEM -> new SpawnLivingEntity(nmsWorld, new CustomEntityIronGolem(nmsWorld), 1, null, bukkitEntity, null, true, true);
                case ILLUSIONER -> new SpawnLivingEntity(nmsWorld, new CustomEntityIllagerIllusioner(nmsWorld), 1, null, bukkitEntity, null, true, true);
                case LLAMA -> new SpawnLivingEntity(nmsWorld, new CustomEntityLlama(nmsWorld), 1, null, bukkitEntity, null, true, true);
                case MUSHROOM_COW -> new SpawnLivingEntity(nmsWorld, new CustomEntityMushroomCow(nmsWorld), 1, null, bukkitEntity, null, true, true);
                case PHANTOM -> {
                    int rand = this.random.nextInt(4) + 12;
                    phantomSize += 0.05 / Bukkit.getServer().getOnlinePlayers().size() * rand; /**every custom phantom spawned increases the server-wide size of future phantom spawns by 0.05*/
                    CustomEntityPhantom newPhantom = new CustomEntityPhantom(nmsWorld, (int)phantomSize);
                    new SpawnLivingEntity(nmsWorld, (int)phantomSize, newPhantom, rand, null, bukkitEntity, null, true, false); /**when phantoms spawn naturally at night, they spawn random 12-15 at a time (12-30 on easy, 12-45 on medium, 12-60 on hard)*/
                }
                case PIG -> new SpawnLivingEntity(nmsWorld, new CustomEntityPig(nmsWorld), 1, null, bukkitEntity, null, true, true);
                case PIGLIN -> new SpawnLivingEntity(nmsWorld, new CustomEntityPiglin(nmsWorld), 1, null, bukkitEntity, null, true, true);
                case PILLAGER -> new SpawnLivingEntity(nmsWorld, new CustomEntityPillager(nmsWorld), 1, null, bukkitEntity, null, true, true);
                case RABBIT -> new SpawnLivingEntity(nmsWorld, new CustomEntityRabbit(nmsWorld), random.nextInt(6) + 5, null, bukkitEntity, null, true, true); /**rabbits are spawned in as 5-10 killer bunnies instead*/
                case RAVAGER -> new SpawnLivingEntity(nmsWorld, new CustomEntityRavager(nmsWorld), 1, null, bukkitEntity, null, true, true);
                case SHEEP -> { /**pink sheep are spawned in as 2 aggressive pink sheep instead*/
                    if (((Sheep)bukkitEntity).getColor() == DyeColor.PINK) {
                        new SpawnLivingEntity(nmsWorld, this.plugin, new CustomEntitySheepAggressive(nmsWorld, this.plugin), 2, null, bukkitEntity, null, true, true);
                    }
                }
                case SILVERFISH -> new SpawnLivingEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 1, null, bukkitEntity, null, true, true);
                case SKELETON -> {
                    if (spawnReason != CreatureSpawnEvent.SpawnReason.DROWNED) {
                        new SpawnLivingEntity(nmsWorld, new CustomEntitySkeleton(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, true);
                    }
                }
                case SLIME -> {
                    CustomEntitySlime newSlime = new CustomEntitySlime(nmsWorld);

                    int i = this.random.nextInt(3) + 1;  /**all slimes spawn one "size" larger from sizes 2, 4, and 8 compared to 1, 2, and 4 only*/
                    int j = 1 << i;

                    if (spawnReason != CreatureSpawnEvent.SpawnReason.SLIME_SPLIT) {
                        newSlime.setSize(j, true);
                    } else {
                        newSlime.setSize(((EntitySlime)nmsEntity).getSize(), true);
                    }

                    new SpawnLivingEntity(nmsWorld, newSlime, 1, null, bukkitEntity, null, true, true);
                }
                case SPIDER -> new SpawnLivingEntity(nmsWorld, new CustomEntitySpider(nmsWorld), 1, null, bukkitEntity, null, true, true);
                case STRAY -> new SpawnLivingEntity(nmsWorld, new CustomEntitySkeletonStray(nmsWorld), 1, null, bukkitEntity, null, true, true);
                case TRADER_LLAMA -> new SpawnLivingEntity(nmsWorld, new CustomEntityLlamaTrader(nmsWorld), 1, null, bukkitEntity, null, true, true);
                case ZOGLIN -> new SpawnLivingEntity(nmsWorld, new CustomEntityZoglin(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, true);
                case ZOMBIE -> {
                    if (spawnReason != CreatureSpawnEvent.SpawnReason.BEEHIVE) {
                        new SpawnLivingEntity(nmsWorld, new CustomEntityZombie(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, true);
                    }
                }
                case ZOMBIE_VILLAGER -> new SpawnLivingEntity(nmsWorld, new CustomEntityZombieVillager(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, true);
            }
        }
    }

    @EventHandler
    public void chunkLoad(ChunkLoadEvent event) {
        for (Entity bukkitEntity : event.getChunk().getEntities()) {
            if (!(bukkitEntity instanceof LivingEntity)) {
                return;
            } else {
                EntityLiving nmsEntity = ((CraftLivingEntity)bukkitEntity).getHandle();
                EntityType type = bukkitEntity.getType();
                World nmsWorld = nmsEntity.getWorld();

                if (!(nmsEntity instanceof EntityBat ||
                        nmsEntity instanceof CustomEntityBee ||
                        nmsEntity instanceof CustomEntityBlaze ||
                        nmsEntity instanceof EntityCaveSpider ||
                        nmsEntity instanceof CustomEntityChicken ||
                        nmsEntity instanceof CustomEntityChickenAggressive ||
                        nmsEntity instanceof CustomEntityCow ||
                        nmsEntity instanceof CustomEntityCreeper ||
                        nmsEntity instanceof CustomEntityEnderman ||
                        nmsEntity instanceof CustomEntityEndermite ||
                        nmsEntity instanceof CustomEntityEvoker ||
                        nmsEntity instanceof CustomEntityGhast ||
                        nmsEntity instanceof CustomEntityGuardian ||
                        nmsEntity instanceof CustomEntityGuardianElder ||
                        nmsEntity instanceof CustomEntityHoglin ||
                        nmsEntity instanceof CustomEntityIllagerIllusioner ||
                        nmsEntity instanceof CustomEntityIronGolem ||
                        nmsEntity instanceof CustomEntityLlama ||
                        nmsEntity instanceof CustomEntityLlamaTrader ||
                        nmsEntity instanceof CustomEntityMushroomCow ||
                        nmsEntity instanceof CustomEntityPhantom ||
                        nmsEntity instanceof CustomEntityPig ||
                        nmsEntity instanceof CustomEntityPiglin ||
                        nmsEntity instanceof CustomEntityPillager ||
                        nmsEntity instanceof CustomEntityRabbit ||
                        nmsEntity instanceof CustomEntityRavager ||
                        nmsEntity instanceof CustomEntitySheepAggressive ||
                        nmsEntity instanceof CustomEntitySilverfish ||
                        nmsEntity instanceof EntitySkeleton ||
                        nmsEntity instanceof CustomEntitySkeletonStray ||
                        nmsEntity instanceof EntitySlime ||
                        nmsEntity instanceof CustomEntitySpider ||
                        nmsEntity instanceof CustomEntityZoglin ||
                        nmsEntity instanceof CustomEntityZombie ||
                        nmsEntity instanceof CustomEntityZombieHusk ||
                        nmsEntity instanceof CustomEntityZombieSuper ||
                        nmsEntity instanceof CustomEntityZombieThor ||
                        nmsEntity instanceof CustomEntityZombieVillager)) { //to prevent stack overflow when the new replacement mobs are spawned, causing this event to fire again and again

                    switch (type) {
                        case BEE -> new SpawnLivingEntity(nmsWorld, new CustomEntityBee(nmsWorld), 1, null, bukkitEntity, null, true, false);
                        case BLAZE -> new SpawnLivingEntity(nmsWorld, new CustomEntityBlaze(nmsWorld), 1, null, bukkitEntity, null, true, false);
                        case CHICKEN -> new SpawnLivingEntity(nmsWorld, new CustomEntityChicken(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        case COW -> new SpawnLivingEntity(nmsWorld, new CustomEntityCow(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        case CREEPER -> new SpawnLivingEntity(nmsWorld, new CustomEntityCreeper(nmsWorld,15), 1, null, bukkitEntity, null, true, true);
                        case DROWNED -> new SpawnLivingEntity(nmsWorld, new CustomEntityDrowned(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        case ELDER_GUARDIAN -> new SpawnLivingEntity(nmsWorld, new CustomEntityGuardianElder(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        case ENDERMAN -> new SpawnLivingEntity(nmsWorld, new CustomEntityEnderman(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        case ENDERMITE -> new SpawnLivingEntity(nmsWorld, new CustomEntityEndermite(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        case EVOKER -> new SpawnLivingEntity(nmsWorld, new CustomEntityEvoker(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, true);
                        case GHAST -> new SpawnLivingEntity(nmsWorld, new CustomEntityGhast(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, false);
                        case GUARDIAN -> new SpawnLivingEntity(nmsWorld, new CustomEntityGuardian(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        case HOGLIN -> new SpawnLivingEntity(nmsWorld, new CustomEntityHoglin(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, true);
                        case HUSK -> new SpawnLivingEntity(nmsWorld, new CustomEntityZombieHusk(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        case ILLUSIONER -> new SpawnLivingEntity(nmsWorld, new CustomEntityIllagerIllusioner(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        case IRON_GOLEM -> new SpawnLivingEntity(nmsWorld, new CustomEntityIronGolem(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        case LLAMA -> new SpawnLivingEntity(nmsWorld, new CustomEntityLlama(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        case MUSHROOM_COW -> new SpawnLivingEntity(nmsWorld, new CustomEntityMushroomCow(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        case PHANTOM -> {
                            phantomSize += 0.025 / Bukkit.getServer().getOnlinePlayers().size();
                            CustomEntityPhantom newPhantom = new CustomEntityPhantom(nmsWorld, (int)phantomSize);
                            new SpawnLivingEntity(nmsWorld, newPhantom, this.random.nextInt(4) + 12, null, bukkitEntity, null, true, false); /**when phantoms spawn naturally at night, they spawn random 12-15 at a time (12-30 on easy, 12-45 on medium, 12-60 on hard)*/
                        }
                        case PIG -> new SpawnLivingEntity(nmsWorld, new CustomEntityPig(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        case PIGLIN -> new SpawnLivingEntity(nmsWorld, new CustomEntityPiglin(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        case PILLAGER -> new SpawnLivingEntity(nmsWorld, new CustomEntityPillager(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        case RABBIT -> { /**rabbits are spawned in as 5-10 killer bunnies instead*/
                            if (((EntityRabbit)nmsEntity).getRabbitType() == 99) { //to avoid infinte duplication when joining world
                                return;
                            }

                            new SpawnLivingEntity(nmsWorld, new CustomEntityRabbit(nmsWorld), random.nextInt(6) + 5, null, bukkitEntity, null, true, true);
                        }
                        case RAVAGER -> new SpawnLivingEntity(nmsWorld, new CustomEntityRavager(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        case SHEEP -> { /**pink sheep are spawned in as 2 aggressive pink sheep instead*/
                            if (((Sheep)bukkitEntity).getColor() == DyeColor.PINK) {
                                new SpawnLivingEntity(nmsWorld, this.plugin, new CustomEntitySheepAggressive(nmsWorld, this.plugin), 2, null, bukkitEntity, null, true, true);
                            }
                        }
                        case SILVERFISH -> new SpawnLivingEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        case SPIDER -> new SpawnLivingEntity(nmsWorld, new CustomEntitySpider(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        case STRAY -> new SpawnLivingEntity(nmsWorld, new CustomEntitySkeletonStray(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        case TRADER_LLAMA -> new SpawnLivingEntity(nmsWorld, new CustomEntityLlamaTrader(nmsWorld), 1, null, bukkitEntity, null, true, true);
                        case ZOGLIN -> new SpawnLivingEntity(nmsWorld, new CustomEntityZoglin(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, true);
                        case ZOMBIE_VILLAGER -> new SpawnLivingEntity(nmsWorld, new CustomEntityZombieVillager(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, true);
                    }
                }
            }
        }
    }
}
