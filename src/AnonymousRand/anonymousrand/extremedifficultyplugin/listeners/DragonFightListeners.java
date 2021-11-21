package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityEnderCrystal;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableGenerateBarrierFountain;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class DragonFightListeners implements Listener {
    @EventHandler
    public void endCrystalSpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof EnderCrystal && !(((CraftEntity) event.getEntity()).getHandle() instanceof CustomEntityEnderCrystal)) {
            World nmsWorld = ((CraftWorld) event.getEntity().getWorld()).getHandle();
            Entity bukkitEntity = event.getEntity();
            Location loc = bukkitEntity.getLocation();

            CustomEntityEnderCrystal newCrystal = new CustomEntityEnderCrystal(nmsWorld);
            newCrystal.setPosition(loc.getX(), loc.getY() + 15, loc.getZ());
            nmsWorld.addEntity(newCrystal);
            bukkitEntity.remove();
        }
    }

    @EventHandler
    public void dragonSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof EnderDragon) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "forceload add -47 -47 48 48"); //to make sure all the mob spawners can generate

            org.bukkit.World bukkitWorld = event.getEntity().getWorld();
            //do the replacement stuff
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> { /**obsidian pillars now have mob spawners on top of them, and each mob spawner generates with a 3 by 3 area of barrier blocks below it so that the mobs always have something to spawn on*/
                new RunnableGenerateBarrierFountain(bukkitWorld); /**center fountain is made of barriers instead of bedrock since wither can now break bedrock*/

                for (int i = 1; i < 11; i++) {
                    new RunnableGenerateEndCrystalSpawners(bukkitWorld, i).run();
                    new RunnableGenerateCenterSpawners(bukkitWorld, i).run();
                }
            }, 100);
        }
    }

    static class RunnableGenerateEndCrystalSpawners extends BukkitRunnable {

        protected Block highestBlock;
        protected final org.bukkit.World bukkitWorld;
        protected final int spawnerNum;
        protected Block spawnerBlock;
        protected CreatureSpawner spawner;
        protected EntityType entityType;
        protected int spawnCount, yRadiusForBreakingBlocks;

        public RunnableGenerateEndCrystalSpawners(org.bukkit.World bukkitWorld, int spawnerNum) {
            this.bukkitWorld = bukkitWorld;
            this.spawnerNum = spawnerNum;
        }

        @Override
        public void run() {
            switch (this.spawnerNum) {
                case 1 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, 42.0, 0.0, 0.0));
                    this.entityType = EntityType.ILLUSIONER;
                    this.spawnCount = 9;
                    this.yRadiusForBreakingBlocks = 2;
                }
                case 2 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, 33.0, 0.0, 24.0));
                    this.entityType = EntityType.CREEPER;
                    this.spawnCount = 28;
                    this.yRadiusForBreakingBlocks = 2;
                }
                case 3 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, 12.0, 0.0, 39.0));
                    this.entityType = EntityType.ENDERMITE;
                    this.spawnCount = 60;
                    this.yRadiusForBreakingBlocks = 1;
                }
                case 4 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, -13.0, 0.0, 39.0));
                    this.entityType = EntityType.STRAY;
                    this.spawnCount = 28;
                    this.yRadiusForBreakingBlocks = 2;
                }
                case 5 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, -34.0, 0.0, 24.0));
                    this.entityType = EntityType.SHULKER;
                    this.spawnCount = 18;
                    this.yRadiusForBreakingBlocks = 2;
                }
                case 6 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, -42.0, 0.0, -1.0));
                    this.entityType = EntityType.WITHER_SKELETON;
                    this.spawnCount = 21;
                    this.yRadiusForBreakingBlocks = 3;
                }
                case 7 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, -34.0, 0.0, -25.0));
                    this.entityType = EntityType.PIGLIN;
                    this.spawnCount = 28;
                    this.yRadiusForBreakingBlocks = 2;
                }
                case 8 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, -13.0, 0.0, -40.0));
                    this.entityType = EntityType.VEX;
                    this.spawnCount = 42;
                    this.yRadiusForBreakingBlocks = 1;
                }
                case 9 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, 12.0, 0.0, -40.0));
                    this.entityType = EntityType.BLAZE;
                    this.spawnCount = 28;
                    this.yRadiusForBreakingBlocks = 2;
                }
                case 10 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, 33.0, 0.0, -25.0));
                    this.entityType = EntityType.WITCH;
                    this.spawnCount = 11;
                    this.yRadiusForBreakingBlocks = 2;
                }
            }

            this.spawnCount *= Math.min((1.0 + Math.floor(Bukkit.getServer().getOnlinePlayers().size() / 4.0) * 0.5), 2.0);
            this.spawnerBlock = new Location(this.bukkitWorld, this.highestBlock.getX(), this.highestBlock.getY() + 1, this.highestBlock.getZ()).getBlock();
            this.spawnerBlock.setType(org.bukkit.Material.SPAWNER);
            this.spawner = ((CreatureSpawner)this.spawnerBlock.getState());
            this.spawner.setSpawnedType(this.entityType);
            this.spawner.setSpawnCount(this.spawnCount);
            this.spawner.setSpawnRange(1);
            this.spawner.setMaxNearbyEntities(25);
            this.spawner.update();
            new RunnableSpawnerBreakBlocksAbove(this.spawnerBlock, 1, this.yRadiusForBreakingBlocks).runTaskTimer(StaticPlugin.plugin, 0L, 20L); /**every second, mob spawners break blocks above them that could be used to prevent the spawning of mobs*/

            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    this.bukkitWorld.getBlockAt(new Location(this.bukkitWorld, this.spawnerBlock.getX() + x, this.spawnerBlock.getY() - 1, this.spawnerBlock.getZ() + z)).setType(org.bukkit.Material.BARRIER);
                }
            }
        }
    }

    static class RunnableGenerateCenterSpawners extends RunnableGenerateEndCrystalSpawners {

        private int minSpawnDelay, maxSpawnDelay;

        public RunnableGenerateCenterSpawners(org.bukkit.World bukkitWorld, int spawnerNum) {
            super(bukkitWorld, spawnerNum);
        }

        @Override
        public void run() {
            switch (this.spawnerNum) {
                case 1 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, 12.0, 0.0, 0.0));
                    this.entityType = EntityType.ZOGLIN;
                    this.minSpawnDelay = 100;
                    this.maxSpawnDelay = 180;
                    this.yRadiusForBreakingBlocks = 2;
                }
                case 2 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, -12.0, 0.0, 0.0));
                    this.entityType = EntityType.SPIDER;
                    this.minSpawnDelay = 55;
                    this.maxSpawnDelay = 160;
                    this.yRadiusForBreakingBlocks = 1;
                }
                case 3 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, -8.0, 0.0, -8.0));
                    this.entityType = EntityType.GHAST;
                    this.minSpawnDelay = 130;
                    this.maxSpawnDelay = 220;
                    this.yRadiusForBreakingBlocks = 5;
                }
                case 4 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, 0.0, 0.0, -12.0));
                    this.entityType = EntityType.ILLUSIONER;
                    this.minSpawnDelay = 130;
                    this.maxSpawnDelay = 220;
                    this.yRadiusForBreakingBlocks = 2;
                }
                case 5 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, 8.0, 0.0, 8.0));
                    this.entityType = EntityType.PHANTOM;
                    this.minSpawnDelay = 70;
                    this.maxSpawnDelay = 150;
                    this.yRadiusForBreakingBlocks = 2;
                }
                case 6 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, -8.0, 0.0, 8.0));
                    this.entityType = EntityType.SHULKER;
                    this.minSpawnDelay = 115;
                    this.maxSpawnDelay = 210;
                    this.yRadiusForBreakingBlocks = 2;
                }
                case 7 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, 0.0, 0.0, 12.0));
                    this.entityType = EntityType.BLAZE;
                    this.minSpawnDelay = 75;
                    this.maxSpawnDelay = 160;
                    this.yRadiusForBreakingBlocks = 2;
                }
                case 8 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, 8.0, 0.0, -8.0));
                    this.entityType = EntityType.WITCH;
                    this.minSpawnDelay = 120;
                    this.maxSpawnDelay = 220;
                    this.yRadiusForBreakingBlocks = 2;
                }
                default -> {
                    return;
                }
            }

            this.minSpawnDelay /= Math.min((1.0 + Math.floor(Bukkit.getServer().getOnlinePlayers().size() / 4.0) * 0.5), 2.0);
            this.maxSpawnDelay /= Math.min((1.0 + Math.floor(Bukkit.getServer().getOnlinePlayers().size() / 4.0) * 0.5), 2.0);
            this.spawnerBlock = new Location(this.bukkitWorld, this.highestBlock.getX(), this.highestBlock.getY(), this.highestBlock.getZ()).getBlock();
            this.spawnerBlock.setType(org.bukkit.Material.SPAWNER);
            this.spawner = ((CreatureSpawner)this.spawnerBlock.getState());
            this.spawner.setSpawnedType(this.entityType);
            this.spawner.setSpawnCount(1);
            this.spawner.setSpawnRange(1);
            this.spawner.setRequiredPlayerRange(12);
            this.spawner.setMinSpawnDelay(this.minSpawnDelay);
            this.spawner.setMaxSpawnDelay(this.maxSpawnDelay);
            this.spawner.setMaxNearbyEntities(35);
            this.spawner.update();
            new RunnableSpawnerBreakBlocksAbove(this.spawnerBlock, 1, this.yRadiusForBreakingBlocks).runTaskTimer(StaticPlugin.plugin, 0L, 20L); /**every second, mob spawners break blocks above them that could be used to prevent the spawning of mobs*/

            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    this.bukkitWorld.getBlockAt(new Location(this.bukkitWorld, this.spawnerBlock.getX() + x, this.spawnerBlock.getY() - 1, this.spawnerBlock.getZ() + z)).setType(org.bukkit.Material.BARRIER);
                }
            }
        }
    }

    static class RunnableSpawnerBreakBlocksAbove extends BukkitRunnable {

        private final Block spawnerBlock;
        private final org.bukkit.World bukkitWorld;
        private final int radius, yRadius;

        public RunnableSpawnerBreakBlocksAbove(Block spawnerBlock, int radius, int yRadius) {
            this.spawnerBlock = spawnerBlock;
            this.bukkitWorld = spawnerBlock.getWorld();
            this.radius = radius;
            this.yRadius = yRadius;
        }

        @Override
        public void run() {
            if (this.spawnerBlock.getType() != Material.SPAWNER) {
                this.cancel();
            }

            for (int x = -this.radius; x <= this.radius; x++) {
                for (int y = 1; y <= this.yRadius; y++) {
                    for (int z = -this.radius; z <= this.radius; z++) {
                        if (this.bukkitWorld.getBlockAt(this.spawnerBlock.getX() + x, this.spawnerBlock.getY() + y, this.spawnerBlock.getZ() + z).getType() != Material.AIR) {
                            this.bukkitWorld.getBlockAt(this.spawnerBlock.getX() + x, this.spawnerBlock.getY() + y, this.spawnerBlock.getZ() + z).setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }
}
