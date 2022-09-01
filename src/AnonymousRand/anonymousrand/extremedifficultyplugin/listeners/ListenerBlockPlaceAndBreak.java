package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R1.util.CraftNamespacedKey;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class ListenerBlockPlaceAndBreak implements Listener {

    private static final Random random = new Random();

    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        Block bukkitBlock = event.getBlock();
        World nmsWorld = ((CraftWorld)event.getBlock().getWorld()).getHandle();
        Location loc = bukkitBlock.getLocation();
        Material type = bukkitBlock.getType();

        if (event.getPlayer() != null) {
            Player bukkitPlayer = event.getPlayer();

            if (bukkitBlock.getLocation().getY() >= 129.0) { /** can't build above y level 128 in all dimensions to prevent towering up etc. to avoid mobs */
                event.setCancelled(true);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + bukkitPlayer.getName() + " \"You have reached the build height limit of 128 blocks :tf:\"");
                return;
            }

            switch (type) {
                case BEACON, BLAST_FURNACE, CAMPFIRE, CAULDRON,  END_ROD, FURNACE, GLOWSTONE, JACK_O_LANTERN, LANTERN, REDSTONE_LAMP, REDSTONE_ORE, REDSTONE_TORCH, RESPAWN_ANCHOR, SEA_LANTERN, SEA_PICKLE, SHROOMLIGHT, SMOKER, SOUL_CAMPFIRE, SOUL_LANTERN, SOUL_TORCH, TORCH -> {
                    if (bukkitPlayer.getWorld().getEnvironment() == org.bukkit.World.Environment.THE_END) { /** blocks that produce a lot of light can't be placed in the end to prevent exploiting them to deactivate spawners */
                        event.setCancelled(true);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + bukkitPlayer.getName() + " \"You can't place such bright blocks in the end\"");
                    }
                }
                case CONDUIT -> new RunnableConduitSummonMobs(nmsWorld, loc, 20).runTaskTimer(StaticPlugin.plugin, 0L, 100L); /** conduits spawn pufferfish and drowned every 5 seconds for 100 seconds */
                case PISTON, STICKY_PISTON -> { /** pistons and sticky pistons can't be placed */
                    event.setCancelled(true);
                }
            }
        } else {
            if (type == Material.COBWEB || type == Material.SOUL_SOIL) { /** spider-placed cobwebs and wither skeleton-placed soul soil are deleted after 2.5 seconds */
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> bukkitBlock.setType(Material.AIR), 50); // async thread is used so that the game doesn't pause completely for 2.5 seconds
            }
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        EntityPlayer nmsPlayer = ((CraftPlayer)event.getPlayer()).getHandle();
        World nmsWorld = nmsPlayer.getWorld();
        Block bukkitBlock = event.getBlock();
        Location loc = bukkitBlock.getLocation();
        Material type = bukkitBlock.getType();

        switch (type) {
            case ANCIENT_DEBRIS -> { /** breaking ancient debris spawns 6 silverfish and 2 bats */
                new SpawnEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 6, null, loc, true);
                new SpawnEntity(nmsWorld, new CustomEntityBat(nmsWorld), 2, null, loc, false);
            }
            case ANVIL, CHIPPED_ANVIL, DAMAGED_ANVIL, DEAD_BUSH, SMITHING_TABLE, TORCH ->  /** anvils, dead bushes, smithing tables, and torches explode when broken but don't break blocks */
                nmsWorld.createExplosion(null, loc.getX(), loc.getY(), loc.getZ(), 2.0F, false, Explosion.Effect.NONE);
            case BARREL, CHEST, CHEST_MINECART, DISPENSER, DROPPER, ENDER_CHEST, GOLD_BLOCK, GOLD_ORE, HOPPER, HOPPER_MINECART, NETHER_GOLD_ORE, SHULKER_BOX, TRAPPED_CHEST -> { /** breaking these blocks causes piglins within 40 blocks horizontally to go into a frenzy for 15 seconds */
                nmsWorld.getEntities(nmsPlayer, nmsPlayer.getBoundingBox().grow(40.0, 128.0, 40.0), entity -> entity instanceof CustomEntityPiglin).forEach(entity -> {
                    ((CustomEntityPiglin)entity).veryAngryTicks += 300;
                });

                if (type == Material.NETHER_GOLD_ORE) { /** breaking nether gold ore has a 80% chance to cause a random block within a 5 by 5 by 5 radius to turn into lava */
                    if (random.nextDouble() < 0.8) {
                        (new Location(bukkitBlock.getWorld(), loc.getX() + random.nextInt(5) - 2, loc.getY() + random.nextInt(5) - 2, loc.getZ() + random.nextInt(5) - 2)).getBlock().setType(Material.LAVA);
                    }
                }
            }
            case BOOKSHELF, COBBLESTONE_STAIRS, CONDUIT, CRACKED_STONE_BRICKS, IRON_BARS,  MOSSY_STONE_BRICKS, STONE_BRICKS, STONE_BRICK_SLAB, STONE_BRICK_STAIRS -> /** breaking these blocks (all found in strongholds besides conduits) causes silverfish to spawn */
                new SpawnEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), type == Material.CONDUIT ? 50 : 1, CreatureSpawnEvent.SpawnReason.INFECTION, loc, true); /** breaking a spawner spawns 5 silverfish and breaking a conduit spawns 50 */
            case COAL_ORE -> /** breaking coal ore has a 10% chance to spawn a silverfish */
                new SpawnEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), random.nextDouble() < 0.1 ? 1 : 0, null, loc, true);
            case DIAMOND_ORE -> { /** breaking diamond ore spawns 3 silverfish and a bat */
                new SpawnEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 3, null, loc, true);
                new SpawnEntity(nmsWorld, new CustomEntityBat(nmsWorld), 1, null, loc, false);
            }
            case SPAWNER -> { /** breaking a spawner spawns 5 mobs of its spawned type */
                EntityTypes<? extends Entity> types = IRegistry.ENTITY_TYPE.get(CraftNamespacedKey.toMinecraft(((CreatureSpawner)bukkitBlock.getState()).getSpawnedType().getKey())); // gets the spawned entity type from the spawner and converts it into nms
                new SpawnEntity(nmsWorld, types.a(nmsWorld), 5, CreatureSpawnEvent.SpawnReason.SPAWNER, loc, true);
            }
        }
    }

    @EventHandler
    public void blockExplodeAndBreakBlock(BlockExplodeEvent event) { /** explosions do not cause blocks to drop */
        event.setYield(0.0F);

        World nmsWorld = ((CraftWorld)event.getBlock().getWorld()).getHandle();
        Location loc;
        Material type;

        for (Block block : event.blockList()) {
            loc = block.getLocation();
            type = block.getType();

            switch (type) {
                case BOOKSHELF, COBBLESTONE_STAIRS, CRACKED_STONE_BRICKS, IRON_BARS,  MOSSY_STONE_BRICKS, STONE_BRICKS, STONE_BRICK_SLAB, STONE_BRICK_STAIRS -> /** breaking these blocks (all found in strongholds besides conduits) causes a silverfish to spawn */
                    new SpawnEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 1, CreatureSpawnEvent.SpawnReason.INFECTION, loc, true);
                case DEAD_BUSH, TORCH -> /** dead bushes and torches explode when broken but don't break blocks */
                    nmsWorld.createExplosion(null, loc.getX(), loc.getY(), loc.getZ(), 2.0F, false, Explosion.Effect.NONE);
            }
        }
    }

    @EventHandler
    public void entityExplodeAndBreakBlock(EntityExplodeEvent event) { /** explosions do not cause blocks to drop */
        event.setYield(0.0F);

        World nmsWorld = ((CraftWorld)event.getEntity().getWorld()).getHandle();
        Location loc;
        Material type;

        for (Block block : event.blockList()) {
            loc = block.getLocation();
            type = block.getType();

            switch (type) {
                case BOOKSHELF, COBBLESTONE_STAIRS, CRACKED_STONE_BRICKS, IRON_BARS, MOSSY_STONE_BRICKS, STONE_BRICKS, STONE_BRICK_SLAB, STONE_BRICK_STAIRS -> /** breaking these blocks (all found in strongholds besides conduits) causes a silverfish to spawn */
                    new SpawnEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 1, CreatureSpawnEvent.SpawnReason.INFECTION, loc, true);
                case DEAD_BUSH, TORCH -> /** dead bushes and torches explode when broken but don't break blocks */
                    nmsWorld.createExplosion(null, loc.getX(), loc.getY(), loc.getZ(), 2.0F, false, Explosion.Effect.NONE);
                case SPAWNER -> {
                    if (event.getEntity() instanceof EnderDragon) { /** ender dragons can't break spawners */
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    static class RunnableConduitSummonMobs extends BukkitRunnable {

        private final World nmsWorld;
        private final Location loc;
        private int cycles;
        private final int maxCycles;

        public RunnableConduitSummonMobs(World nmsWorld, Location loc, int maxCycles) {
            this.nmsWorld = nmsWorld;
            this.loc = loc;
            this.cycles = 0;
            this.maxCycles = maxCycles;
        }

        @Override
        public void run() {
            if (++this.cycles > this.maxCycles) {
                this.cancel();
                return;
            }

            new SpawnEntity(this.nmsWorld, new CustomEntityDrowned(this.nmsWorld), 1, null, this.loc, true);
            new SpawnEntity(this.nmsWorld, new CustomEntityPufferfish(this.nmsWorld), 1, null, this.loc, true);
        }
    }

    @EventHandler
    public void dragonBreakBlocks(EntityChangeBlockEvent event) {

    }
}
