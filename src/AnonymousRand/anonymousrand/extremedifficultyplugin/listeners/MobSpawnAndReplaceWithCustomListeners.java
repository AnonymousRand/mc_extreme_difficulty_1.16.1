package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import net.minecraft.server.v1_16_R1.*;
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
    private final Random random = new Random();

    public MobSpawnAndReplaceWithCustomListeners(JavaPlugin plugin) {
        this.plugin = plugin;
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
                nmsEntity instanceof CustomEntityZoglin ||
                nmsEntity instanceof CustomEntityZombie ||
                nmsEntity instanceof CustomEntityZombieHusk)) { //to prevent stack overflow when the new replacement mobs are spawned, causing this event to fire again and again

            CreatureSpawnEvent.SpawnReason spawnReason = event.getSpawnReason();

            switch (event.getEntityType()) {
                case BAT -> {
                    if (spawnReason != CreatureSpawnEvent.SpawnReason.DROWNED) {
                        new SpawnLivingEntity(nmsWorld, new CustomEntityBat(nmsWorld), 1, null, bukkitEntity, null, true, false).run();
                    }
                }
                case BEE -> new SpawnLivingEntity(nmsWorld, new CustomEntityBee(nmsWorld), 1, null, bukkitEntity, null, true, false).run();
                case BLAZE -> new SpawnLivingEntity(nmsWorld, new CustomEntityBlaze(nmsWorld), 1, null, bukkitEntity, null, true, false).run();
                case CAVE_SPIDER -> {
                    if (spawnReason != CreatureSpawnEvent.SpawnReason.DROWNED) {
                        new SpawnLivingEntity(nmsWorld, new CustomEntitySpiderCave(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                    }
                }
                case CHICKEN -> new SpawnLivingEntity(nmsWorld, new CustomEntityChicken(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                case COW -> new SpawnLivingEntity(nmsWorld, new CustomEntityCow(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                case CREEPER -> new SpawnLivingEntity(nmsWorld, new CustomEntityCreeper(nmsWorld, 15), 1, null, bukkitEntity, null, true, true).run();
                case DROWNED -> new SpawnLivingEntity(nmsWorld, new CustomEntityDrowned(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                case ELDER_GUARDIAN -> new SpawnLivingEntity(nmsWorld, new CustomEntityGuardianElder(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                case ENDERMAN -> new SpawnLivingEntity(nmsWorld, new CustomEntityEnderman(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                case ENDERMITE -> new SpawnLivingEntity(nmsWorld, new CustomEntityEndermite(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                case EVOKER -> new SpawnLivingEntity(this.plugin, nmsWorld, new CustomEntityEvoker(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, true).run();
                case GHAST -> new SpawnLivingEntity(this.plugin, nmsWorld, new CustomEntityGhast(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, false).run();
                case GUARDIAN -> new SpawnLivingEntity(nmsWorld, new CustomEntityGuardian(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                case HOGLIN -> new SpawnLivingEntity(nmsWorld, new CustomEntityHoglin(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, true).run();
                case HUSK -> new SpawnLivingEntity(nmsWorld, new CustomEntityZombieHusk(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                case LLAMA -> new SpawnLivingEntity(nmsWorld, new CustomEntityLlama(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                case MUSHROOM_COW -> new SpawnLivingEntity(nmsWorld, new CustomEntityMushroomCow(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                case RABBIT -> new SpawnLivingEntity(nmsWorld, new CustomEntityRabbit(nmsWorld), random.nextInt(6) + 5, null, bukkitEntity, null, true, true).run(); /**rabbits are spawned in as 5-10 killer bunnies instead*/
                case RAVAGER -> new SpawnLivingEntity(nmsWorld, new CustomEntityRavager(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                case SHEEP -> {
                    if (((Sheep)bukkitEntity).getColor() == DyeColor.PINK) {
                        new SpawnLivingEntity(this.plugin, nmsWorld, new CustomEntitySheepAggressive(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, true).run();
                    }
                }
                case SILVERFISH -> new SpawnLivingEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                case SKELETON -> {
                    if (spawnReason != CreatureSpawnEvent.SpawnReason.DROWNED) {
                        new SpawnLivingEntity(nmsWorld, new CustomEntitySkeleton(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, true).run();
                    }
                }
                case SPIDER -> new SpawnLivingEntity(nmsWorld, new CustomEntitySpider(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                case STRAY -> new SpawnLivingEntity(nmsWorld, new CustomEntitySkeletonStray(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                case TRADER_LLAMA -> new SpawnLivingEntity(nmsWorld, new CustomEntityLlamaTrader(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                case ZOGLIN -> new SpawnLivingEntity(nmsWorld, new CustomEntityZoglin(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, true).run();
                case ZOMBIE -> {
                    if (spawnReason != CreatureSpawnEvent.SpawnReason.BEEHIVE) {
                        new SpawnLivingEntity(nmsWorld, new CustomEntityZombie(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, true).run();
                    }
                }
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
                        nmsEntity instanceof CustomEntityZoglin ||
                        nmsEntity instanceof EntityZombie)) { //to prevent stack overflow when the new replacement mobs are spawned, causing this event to fire again and again

                    switch (type) {
                        case BEE -> new SpawnLivingEntity(nmsWorld, new CustomEntityBee(nmsWorld), 1, null, bukkitEntity, null, true, false).run();
                        case BLAZE -> new SpawnLivingEntity(nmsWorld, new CustomEntityBlaze(nmsWorld), 1, null, bukkitEntity, null, true, false).run();
                        case CHICKEN -> new SpawnLivingEntity(nmsWorld, new CustomEntityChicken(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                        case COW -> new SpawnLivingEntity(nmsWorld, new CustomEntityCow(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                        case CREEPER -> new SpawnLivingEntity(nmsWorld, new CustomEntityCreeper(nmsWorld,15), 1, null, bukkitEntity, null, true, true).run();
                        case DROWNED -> new SpawnLivingEntity(nmsWorld, new CustomEntityDrowned(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                        case ELDER_GUARDIAN -> new SpawnLivingEntity(nmsWorld, new CustomEntityGuardianElder(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                        case ENDERMAN -> new SpawnLivingEntity(nmsWorld, new CustomEntityEnderman(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                        case ENDERMITE -> new SpawnLivingEntity(nmsWorld, new CustomEntityEndermite(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                        case EVOKER -> new SpawnLivingEntity(this.plugin, nmsWorld, new CustomEntityEvoker(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, true).run();
                        case GHAST -> new SpawnLivingEntity(this.plugin, nmsWorld, new CustomEntityGhast(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, false).run();
                        case GUARDIAN -> new SpawnLivingEntity(nmsWorld, new CustomEntityGuardian(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                        case HOGLIN -> new SpawnLivingEntity(nmsWorld, new CustomEntityHoglin(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, true).run();
                        case HUSK -> new SpawnLivingEntity(nmsWorld, new CustomEntityZombieHusk(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                        case LLAMA -> new SpawnLivingEntity(nmsWorld, new CustomEntityLlama(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                        case MUSHROOM_COW -> new SpawnLivingEntity(nmsWorld, new CustomEntityMushroomCow(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                        case RABBIT -> { /**rabbits are spawned in as 5-10 killer bunnies instead*/
                            if (((EntityRabbit)nmsEntity).getRabbitType() == 99) { //to avoid infinte duplication when joining world
                                return;
                            }

                            new SpawnLivingEntity(nmsWorld, new CustomEntityRabbit(nmsWorld), random.nextInt(6) + 5, null, bukkitEntity, null, true, true).run();
                        }
                        case RAVAGER -> new SpawnLivingEntity(nmsWorld, new CustomEntityRavager(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                        case SHEEP -> {
                            if (((Sheep)bukkitEntity).getColor() == DyeColor.PINK) {
                                new SpawnLivingEntity(this.plugin, nmsWorld, new CustomEntitySheepAggressive(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, true).run();
                            }
                        }
                        case SILVERFISH -> new SpawnLivingEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                        case SPIDER -> new SpawnLivingEntity(nmsWorld, new CustomEntitySpider(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                        case STRAY -> new SpawnLivingEntity(nmsWorld, new CustomEntitySkeletonStray(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                        case TRADER_LLAMA -> new SpawnLivingEntity(nmsWorld, new CustomEntityLlamaTrader(nmsWorld), 1, null, bukkitEntity, null, true, true).run();
                        case ZOGLIN -> new SpawnLivingEntity(nmsWorld, new CustomEntityZoglin(nmsWorld, this.plugin), 1, null, bukkitEntity, null, true, true).run();
                    }
                }
            }
        }
    }
}
