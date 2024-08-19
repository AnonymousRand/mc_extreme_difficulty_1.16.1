package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.ExtremeDifficultyPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R1.util.CraftNamespacedKey;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class ListenerBlockPlaceAndBreak implements Listener {

    private static final Random random = new Random();

    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        Block bukkitBlock = event.getBlock();

        if (event.getPlayer() != null) {
            Player bukkitPlayer = event.getPlayer();

            if (bukkitBlock.getLocation().getY() >= 129.0) { /* can't build above y-level 128 in all dimensions to prevent towering up etc. to avoid mobs */
                event.setCancelled(true);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + bukkitPlayer.getName() + " \"You have reached the build height limit of 128 blocks :tf:\"");
                return;
            }

            Material bukkitMaterial = bukkitBlock.getType();
            switch (bukkitMaterial) {
                case BEACON:
                case BLAST_FURNACE:
                case CAMPFIRE:
                case CAULDRON:
                case  END_ROD:
                case FURNACE:
                case GLOWSTONE:
                case JACK_O_LANTERN:
                case LANTERN:
                case REDSTONE_LAMP:
                case REDSTONE_ORE:
                case REDSTONE_TORCH:
                case RESPAWN_ANCHOR:
                case SEA_LANTERN:
                case SEA_PICKLE:
                case SHROOMLIGHT:
                case SMOKER:
                case SOUL_CAMPFIRE:
                case SOUL_LANTERN:
                case SOUL_TORCH:
                case TORCH:
                    if (bukkitPlayer.getWorld().getEnvironment() == org.bukkit.World.Environment.THE_END) { /* blocks that produce a lot of light can't be placed in the end to prevent exploiting them to deactivate spawners */
                        event.setCancelled(true);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + bukkitPlayer.getName() + " \"You can't place such bright blocks in the end\"");
                    }

                    break;
                case CONDUIT:
                    new RunnableConduitSummonMobs(((CraftWorld)event.getBlock().getWorld()).getHandle(), bukkitBlock.getLocation(), 20).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 100L); /* conduits spawn pufferfish and drowned every 5 seconds for 100 seconds */
                    break;
                case PISTON:
                case STICKY_PISTON:
                    event.setCancelled(true); /* pistons and sticky pistons can't be placed */
                    break;
            }
        } else {
            Material bukkitMaterial = bukkitBlock.getType();
            if (bukkitMaterial == Material.COBWEB || bukkitMaterial == Material.SOUL_SOIL) { /* spider-placed cobwebs and wither skeleton-placed soul soil are deleted after 2.5 seconds */
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(ExtremeDifficultyPlugin.plugin, () -> bukkitBlock.setType(Material.AIR), 50); // async thread is used so that the game doesn't pause completely for 2.5 seconds
            }
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        EntityPlayer nmsPlayer = ((CraftPlayer)event.getPlayer()).getHandle();
        World nmsWorld = nmsPlayer.getWorld();
        Block bukkitBlock = event.getBlock();
        Location bukkitLoc = bukkitBlock.getLocation();
        Material bukkitMaterial = bukkitBlock.getType();

        switch (bukkitMaterial) {
            case ANCIENT_DEBRIS: /* breaking ancient debris spawns 6 silverfish and 2 bats */
                new SpawnEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 6, null, bukkitLoc, true);
                new SpawnEntity(nmsWorld, new CustomEntityBat(nmsWorld), 2, null, bukkitLoc, false);
                break;
            case ANVIL:
            case CHIPPED_ANVIL:
            case DAMAGED_ANVIL:
            case DEAD_BUSH:
            case SMITHING_TABLE:
            case TORCH: /* anvils, dead bushes, smithing tables, and torches explode when broken but don't break blocks */
                nmsWorld.createExplosion(null, bukkitLoc.getX(), bukkitLoc.getY(), bukkitLoc.getZ(), 2.0F, false, Explosion.Effect.NONE);
                break;
            case BARREL:
            case CHEST:
            case CHEST_MINECART:
            case DISPENSER:
            case DROPPER:
            case ENDER_CHEST:
            case GOLD_BLOCK:
            case GOLD_ORE:
            case HOPPER:
            case HOPPER_MINECART:
            case NETHER_GOLD_ORE:
            case SHULKER_BOX:
            case TRAPPED_CHEST: /* breaking these blocks causes piglins within 40 blocks horizontally to go into a frenzy for 15 seconds */
                nmsWorld.getEntities(nmsPlayer, nmsPlayer.getBoundingBox().grow(40.0, 128.0, 40.0), entity -> entity instanceof CustomEntityPiglin).forEach(entity -> ((CustomEntityPiglin)entity).frenzyTicks += 300);

                if (bukkitMaterial == Material.NETHER_GOLD_ORE) { /* breaking nether gold ore has a 80% chance to cause a random block within a 5 by 5 by 5 radius to turn into lava */
                    if (random.nextDouble() < 0.8) {
                        (new Location(bukkitBlock.getWorld(), bukkitLoc.getX() + random.nextInt(5) - 2,   bukkitLoc.getY() + random.nextInt(5) - 2, bukkitLoc.getZ() + random.nextInt(5) - 2)).getBlock().setType(Material.LAVA);
                    }
                }

                break;
            case BOOKSHELF:
            case COBBLESTONE_STAIRS:
            case CONDUIT:
            case CRACKED_STONE_BRICKS:
            case IRON_BARS:
            case  MOSSY_STONE_BRICKS:
            case STONE_BRICKS:
            case STONE_BRICK_SLAB:
            case STONE_BRICK_STAIRS: /* breaking these blocks (all found in strongholds besides conduits) causes silverfish to spawn */
                new SpawnEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), bukkitMaterial == Material.CONDUIT ? 50 : 1, CreatureSpawnEvent.SpawnReason.INFECTION, bukkitLoc, true); /* breaking a spawner spawns 5 silverfish and breaking a conduit spawns 50 */
                break;
            case COAL_ORE: /* breaking coal ore has a 10% chance to spawn a silverfish */
                new SpawnEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), random.nextDouble() < 0.1 ? 1 : 0, null, bukkitLoc, true);
                break;
            case DIAMOND_ORE: /* breaking diamond ore spawns 3 silverfish and a bat */
                new SpawnEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 3, null, bukkitLoc, true);
                new SpawnEntity(nmsWorld, new CustomEntityBat(nmsWorld), 1, null, bukkitLoc, false);
                break;
            case SPAWNER: /* breaking a spawner spawns 5 mobs of its spawned type */
                EntityTypes<? extends Entity> types = IRegistry.ENTITY_TYPE.get(CraftNamespacedKey.toMinecraft(((CreatureSpawner)bukkitBlock.getState()).getSpawnedType().getKey())); // gets the spawned entity type from the spawner and converts it into nms
                new SpawnEntity(nmsWorld, types.a(nmsWorld), 5, CreatureSpawnEvent.SpawnReason.SPAWNER, bukkitLoc, true);
                break;
        }
    }

    @EventHandler
    public void blockExplodeAndBreakBlock(BlockExplodeEvent event) { /* explosions do not cause blocks to drop */
        event.setYield(0.0F);

        World nmsWorld = ((CraftWorld)event.getBlock().getWorld()).getHandle();
        Location bukkitLoc;
        Material bukkitMaterial;

        for (Block block : event.blockList()) {
            bukkitLoc = block.getLocation();
            bukkitMaterial = block.getType();

            switch (bukkitMaterial) {
                case BOOKSHELF:
                case COBBLESTONE_STAIRS:
                case CRACKED_STONE_BRICKS:
                case IRON_BARS:
                case  MOSSY_STONE_BRICKS:
                case STONE_BRICKS:
                case STONE_BRICK_SLAB:
                case STONE_BRICK_STAIRS: /* breaking these blocks (all found in strongholds besides conduits) causes a silverfish to spawn */
                    new SpawnEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 1, CreatureSpawnEvent.SpawnReason.INFECTION, bukkitLoc, true);
                    break;
                case DEAD_BUSH:
                case TORCH: /* dead bushes and torches explode when broken but don't break blocks */
                    nmsWorld.createExplosion(null, bukkitLoc.getX(), bukkitLoc.getY(), bukkitLoc.getZ(), 2.0F, false, Explosion.Effect.NONE);
                    break;
            }
        }
    }

    @EventHandler
    public void entityExplodeAndBreakBlock(EntityExplodeEvent event) { /* explosions do not cause blocks to drop */
        event.setYield(0.0F);

        World nmsWorld = ((CraftWorld)event.getEntity().getWorld()).getHandle();
        Location bukkitLoc;
        Material bukkitMaterial;

        for (Block block : event.blockList()) {
            bukkitLoc = block.getLocation();
            bukkitMaterial = block.getType();

            switch (bukkitMaterial) {
                case BOOKSHELF:
                case COBBLESTONE_STAIRS:
                case CRACKED_STONE_BRICKS:
                case IRON_BARS:
                case MOSSY_STONE_BRICKS:
                case STONE_BRICKS:
                case STONE_BRICK_SLAB:
                case STONE_BRICK_STAIRS: /* breaking these blocks (all found in strongholds besides conduits) causes a silverfish to spawn */
                    new SpawnEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 1, CreatureSpawnEvent.SpawnReason.INFECTION, bukkitLoc, true);
                    break;
                case DEAD_BUSH:
                case TORCH: /* dead bushes and torches explode when broken but don't break blocks */
                    nmsWorld.createExplosion(null, bukkitLoc.getX(), bukkitLoc.getY(), bukkitLoc.getZ(), 2.0F, false, Explosion.Effect.NONE);
                    break;
                case SPAWNER:
                    if (event.getEntity() instanceof EnderDragon) { /* ender dragons can't break spawners */
                        event.setCancelled(true);
                    }
                    break;
            }
        }
    }

    static class RunnableConduitSummonMobs extends BukkitRunnable {

        private final World nmsWorld;
        private final Location bukkitLoc;
        private int cycleCount;
        private final int cycleCountMax;

        public RunnableConduitSummonMobs(World nmsWorld, Location bukkitLoc, int cycleCountMax) {
            this.nmsWorld = nmsWorld;
            this.bukkitLoc = bukkitLoc;
            this.cycleCount = 0;
            this.cycleCountMax = cycleCountMax;
        }

        @Override
        public void run() {
            if (++this.cycleCount > this.cycleCountMax) {
                this.cancel();
                return;
            }

            new SpawnEntity(this.nmsWorld, new CustomEntityDrowned(this.nmsWorld), 1, null, this.bukkitLoc, true);
            new SpawnEntity(this.nmsWorld, new CustomEntityPufferfish(this.nmsWorld), 1, null, this.bukkitLoc, true);
        }
    }
}
