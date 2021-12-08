package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityEnderDragon;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityWither;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityWitherMini;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityEnderCrystal;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityLightning;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CustomMathHelper;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableBreakBlocks;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableMobRain;
import net.minecraft.server.v1_16_R1.BlockPosition;
import net.minecraft.server.v1_16_R1.Items;
import net.minecraft.server.v1_16_R1.Vec3D;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class ListenerDragonFight implements Listener {

    private static ArrayList<Block> spawnerBlocks = new ArrayList<>();
    public static int ticksAfterDragonDeath;

    @EventHandler
    public void dragonSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof EnderDragon && (!(((CraftEntity)event.getEntity()).getHandle() instanceof CustomEntityEnderDragon))) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "forceload add -47 -47 48 48"); //to make sure all the mob spawners can generate
            Bukkit.broadcastMessage("You've made it this far. I hope you're ready.");
            org.bukkit.World bukkitWorld = event.getEntity().getWorld();
            World nmsWorld = ((CraftWorld)bukkitWorld).getHandle();

            new SpawnEntity(nmsWorld, new CustomEntityEnderDragon(nmsWorld, event.getEntity().getUniqueId()), 1, null, event.getEntity(), null, true, false);

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> { /**obsidian pillars now have mob spawners on top of them, and each mob spawner generates with a 3 by 3 area of barrier blocks below it so that the mobs always have something to spawn on*/
                for (int i = 1; i < 11; i++) {
                    new RunnableGenerateEndCrystalSpawners(bukkitWorld, i).run();
                    new RunnableGenerateCenterSpawners(bukkitWorld, i).run();
                }
            }, 100);

            ticksAfterDragonDeath = 0;
        }
    }

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
    public void dragonDeath(EntityDeathEvent event) {
        if(event.getEntity() instanceof EnderDragon) {
            Bukkit.broadcastMessage("Impressive...most impressive.");
            Bukkit.broadcastMessage("But you must survive for at least 30 more seconds before you can enter the end portal");
            CustomEntityEnderDragon nmsEntity = (CustomEntityEnderDragon)((CraftEnderDragon)event.getEntity()).getHandle();
            new RunnablePreventExitPortalGeneration(nmsEntity).runTaskTimer(StaticPlugin.plugin, 80L, 1L);
            new RunnableOnDragonDeath(nmsEntity).runTaskTimer(StaticPlugin.plugin, 0L, 1L);
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

            this.spawnCount *= Math.min((1.0 + Math.floor(Bukkit.getServer().getOnlinePlayers().size() / 4.0) * 0.25), 1.5); /**spawns 25% more mobs on average per 4 players, up to 50% more*/
            this.spawnerBlock = new Location(this.bukkitWorld, this.highestBlock.getX(), this.highestBlock.getY() + 1, this.highestBlock.getZ()).getBlock();
            this.spawnerBlock.setType(Material.SPAWNER);
            this.spawner = ((CreatureSpawner)this.spawnerBlock.getState());
            this.spawner.setSpawnedType(this.entityType);
            this.spawner.setSpawnCount(this.spawnCount);
            this.spawner.setSpawnRange(1);
            this.spawner.setMaxSpawnDelay(500); /**max spawn delay reduced to 25 seconds*/
            this.spawner.setMaxNearbyEntities(40);
            this.spawner.update();
            new RunnableSpawnerBreakBlocksAbove(this.spawnerBlock, 1, this.yRadiusForBreakingBlocks).runTaskTimer(StaticPlugin.plugin, 0L, 20L); /**every second, mob spawners break blocks above them that could be used to prevent the spawning of mobs*/
            spawnerBlocks.add(this.spawnerBlock);

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
                    this.minSpawnDelay = 120;
                    this.maxSpawnDelay = 180;
                    this.yRadiusForBreakingBlocks = 2;
                }
                case 2 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, -12.0, 0.0, 0.0));
                    this.entityType = EntityType.SPIDER;
                    this.minSpawnDelay = 70;
                    this.maxSpawnDelay = 160;
                    this.yRadiusForBreakingBlocks = 1;
                }
                case 3 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, -8.0, 0.0, -8.0));
                    this.entityType = EntityType.GHAST;
                    this.minSpawnDelay = 160;
                    this.maxSpawnDelay = 220;
                    this.yRadiusForBreakingBlocks = 5;
                }
                case 4 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, 0.0, 0.0, -12.0));
                    this.entityType = EntityType.ILLUSIONER;
                    this.minSpawnDelay = 160;
                    this.maxSpawnDelay = 230;
                    this.yRadiusForBreakingBlocks = 2;
                }
                case 5 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, 8.0, 0.0, 8.0));
                    this.entityType = EntityType.PHANTOM;
                    this.minSpawnDelay = 90;
                    this.maxSpawnDelay = 150;
                    this.yRadiusForBreakingBlocks = 2;
                }
                case 6 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, -8.0, 0.0, 8.0));
                    this.entityType = EntityType.SHULKER;
                    this.minSpawnDelay = 145;
                    this.maxSpawnDelay = 210;
                    this.yRadiusForBreakingBlocks = 2;
                }
                case 7 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, 0.0, 0.0, 12.0));
                    this.entityType = EntityType.BLAZE;
                    this.minSpawnDelay = 95;
                    this.maxSpawnDelay = 160;
                    this.yRadiusForBreakingBlocks = 2;
                }
                case 8 -> {
                    this.highestBlock = this.bukkitWorld.getHighestBlockAt(new Location(bukkitWorld, 8.0, 0.0, -8.0));
                    this.entityType = EntityType.WITCH;
                    this.minSpawnDelay = 160;
                    this.maxSpawnDelay = 230;
                    this.yRadiusForBreakingBlocks = 2;
                }
                default -> {
                    return;
                }
            }

            this.minSpawnDelay /= Math.min((1.0 + Math.floor(Bukkit.getServer().getOnlinePlayers().size() / 4.0) * 0.5), 2.0); /**spawns 50% faster on average per 4 players, up to 100% faster*/
            this.maxSpawnDelay /= Math.min((1.0 + Math.floor(Bukkit.getServer().getOnlinePlayers().size() / 4.0) * 0.5), 2.0);
            this.spawnerBlock = new Location(this.bukkitWorld, this.highestBlock.getX(), this.highestBlock.getY(), this.highestBlock.getZ()).getBlock();
            this.spawnerBlock.setType(Material.SPAWNER);
            this.spawner = ((CreatureSpawner)this.spawnerBlock.getState());
            this.spawner.setSpawnedType(this.entityType);
            this.spawner.setSpawnCount(1);
            this.spawner.setSpawnRange(1);
            this.spawner.setRequiredPlayerRange(12);
            this.spawner.setMinSpawnDelay(this.minSpawnDelay);
            this.spawner.setMaxSpawnDelay(this.maxSpawnDelay);
            this.spawner.setMaxNearbyEntities(40);
            this.spawner.update();
            new RunnableSpawnerBreakBlocksAbove(this.spawnerBlock, 1, this.yRadiusForBreakingBlocks).runTaskTimer(StaticPlugin.plugin, 0L, 20L); /**every second, mob spawners break blocks above them that could be used to prevent the spawning of mobs*/
            spawnerBlocks.add(this.spawnerBlock);

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
        private Block targetBlock;

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
                        this.targetBlock = this.bukkitWorld.getBlockAt(this.spawnerBlock.getX() + x, this.spawnerBlock.getY() + y, this.spawnerBlock.getZ() + z);

                        if (this.targetBlock.getType() != Material.AIR && this.targetBlock.getType() != Material.SPAWNER) {
                            this.targetBlock.setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }

    static class RunnablePreventExitPortalGeneration extends BukkitRunnable {

        private final org.bukkit.World bukkitWorld;
        private int yOfEndPortals;
        private boolean spawnersStillRemaining, endPortalBroken, gameEnd;

        public RunnablePreventExitPortalGeneration(CustomEntityEnderDragon dragon) {
            this.bukkitWorld = dragon.getWorld().getWorld();
            this.spawnersStillRemaining = true;
            this.endPortalBroken = false;
            this.gameEnd = false;
        }

        @Override
        public void run() {
            this.spawnersStillRemaining = false;

            for (Block spawner : spawnerBlocks) {
                if (spawner.getType() == Material.SPAWNER) {
                    this.spawnersStillRemaining = true;
                    break;
                }
            }

            if (this.spawnersStillRemaining && !this.endPortalBroken) { /**end portal only generates when all spawners have been broken*/
                for (int y = 0; y <= 256; y++) {
                    if (this.bukkitWorld.getBlockAt(1, y, 0).getType() == Material.END_PORTAL) {
                        this.endPortalBroken = true;
                        this.yOfEndPortals = y;
                        Bukkit.broadcastMessage("You're almost there...but you must break all the spawners to activate the end portal");

                        for (int x = -2; x <= 2; x++) {
                            for (int z = -2; z <= 2; z++) {
                                this.bukkitWorld.getBlockAt(x, y, z).setType(Material.BEDROCK);
                            }
                        }
                    }
                }
            } else if (!this.spawnersStillRemaining) { //all spawners destroyed
                this.gameEnd = true;

                for (int x = -2; x <= 2; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && z == 0) {
                            continue;
                        }

                        this.bukkitWorld.getBlockAt(x, this.yOfEndPortals, z).setType(Material.END_PORTAL);
                    }
                }

                for (int x = -1; x <= 1; x++) {
                    this.bukkitWorld.getBlockAt(x, this.yOfEndPortals, 2).setType(Material.END_PORTAL);
                    this.bukkitWorld.getBlockAt(x, this.yOfEndPortals, -2).setType(Material.END_PORTAL);
                }
            }

            if (this.gameEnd && ticksAfterDragonDeath > 600) { /**exit portals can only spawn 30 seconds after dragon is killed*/
                this.cancel();
                this.bukkitWorld.setDifficulty(Difficulty.PEACEFUL); /**kill everything when this goal has been achieved*/
                this.printEndText();
            }
        }

        private void printEndText() {
            ItemStack dragonEgg = new ItemStack(Material.DRAGON_EGG);
            ItemMeta meta = dragonEgg.getItemMeta();
            meta.setDisplayName("AnonymousRand/Iseburg");
            dragonEgg.setItemMeta(meta);

            Bukkit.broadcastMessage("Congratulations to:");

            for (Player player : Bukkit.getServer().getOnlinePlayers()) { //separate for loops to prevent the advancements from preventing the names from being read
                player.setItemInHand(dragonEgg);
            }

            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                Bukkit.broadcastMessage(player.getName());
            }

            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("");
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> {
                Bukkit.broadcastMessage("Through pain, through death, through the endless challenges that I have thrown at you......you've come through.");
                Bukkit.broadcastMessage("");
            }, 60);
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> {
                Bukkit.broadcastMessage("This is it. It's over.");
                Bukkit.broadcastMessage("");
            }, 130);
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> {
                Bukkit.broadcastMessage("And...");
                Bukkit.broadcastMessage("");
            }, 215);
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> {
                Bukkit.broadcastMessage("I'm sorry.");
                Bukkit.broadcastMessage("");
            }, 295);
        }
    }

    static class RunnableOnDragonDeath extends BukkitRunnable {

        private final World nmsWorld;
        private final Vec3D dragonPos;

        public RunnableOnDragonDeath(CustomEntityEnderDragon dragon) {
            this.nmsWorld = dragon.getWorld();
            this.dragonPos = dragon.getPositionVector();
        }

        @Override
        public void run() {
            if (++ticksAfterDragonDeath > 600) {
                this.cancel();
                return;
            }

            if (ticksAfterDragonDeath == 1) { //immediately when dragon dies
                for (Player bukkitPlayer : Bukkit.getServer().getOnlinePlayers()) {
                    for (PotionEffect effect : bukkitPlayer.getActivePotionEffects()) { /**remove positive effects from all players*/
                        PotionEffectType type = effect.getType();

                        if (type.equals(PotionEffectType.ABSORPTION) || type.equals(PotionEffectType.CONDUIT_POWER) || type.equals(PotionEffectType.DAMAGE_RESISTANCE) || type.equals(PotionEffectType.DOLPHINS_GRACE) || type.equals(PotionEffectType.FAST_DIGGING) || type.equals(PotionEffectType.FIRE_RESISTANCE) || type.equals(PotionEffectType.HEAL) || type.equals(PotionEffectType.HEALTH_BOOST) || type.equals(PotionEffectType.HERO_OF_THE_VILLAGE) || type.equals(PotionEffectType.INCREASE_DAMAGE) || type.equals(PotionEffectType.INVISIBILITY) || type.equals(PotionEffectType.JUMP) || type.equals(PotionEffectType.LUCK) || type.equals(PotionEffectType.NIGHT_VISION) || type.equals(PotionEffectType.REGENERATION) || type.equals(PotionEffectType.SATURATION) || type.equals(PotionEffectType.SLOW_FALLING) || type.equals(PotionEffectType.SPEED) || type.equals(PotionEffectType.WATER_BREATHING)) {
                            bukkitPlayer.removePotionEffect(type);
                        }
                    }

                    new SpawnEntity(this.nmsWorld, new CustomEntityWitherMini(this.nmsWorld, false), 1, null, bukkitPlayer.getLocation(), false); /**summon a mini wither at every player's location*/
                }

                new SpawnEntity(this.nmsWorld, new CustomEntityLightning(this.nmsWorld, 10.0F), 1, null, new Location(this.nmsWorld.getWorld(), this.dragonPos.getX(), this.dragonPos.getY(), this.dragonPos.getZ()), false); /**summon a power 10 custom lightning strike on dragon's location*/
                new RunnableMobRain(nmsWorld, 130.0, new BlockPosition(0.0, 0.0, 0.0), 45.0, 1).runTaskTimer(StaticPlugin.plugin, 0L, 2L); /**summon mob rain*/
                new RunnableMobRain(nmsWorld, 130.0, new BlockPosition(0.0, 0.0, 0.0), 35.0, 2).runTaskTimer(StaticPlugin.plugin, 300L, 10L);

                if (Bukkit.getServer().getOnlinePlayers().size() > 5) { /**repeat second wave after 20 seconds if more than 5 players online*/
                    new RunnableMobRain(nmsWorld, 130.0, new BlockPosition(0.0, 0.0, 0.0), 35.0, 2).runTaskTimer(StaticPlugin.plugin, 700L, 10L);
                }
            }
        }
    }
}
