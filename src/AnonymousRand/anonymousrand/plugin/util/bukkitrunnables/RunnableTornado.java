package AnonymousRand.anonymousrand.plugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.plugin.ExtremeDifficultyPlugin;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.CustomEntityEnderDragon;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.CustomEntityWither;
import AnonymousRand.anonymousrand.plugin.listeners.ListenerLightningStrike;
import AnonymousRand.anonymousrand.plugin.util.CustomMathHelper;
import AnonymousRand.anonymousrand.plugin.util.Predicates;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class RunnableTornado extends BukkitRunnable {

    private final World nmsWorld;
    private final org.bukkit.World bukkitWorld;
    private final BlockPosition pos;
    private final Location bukkitLoc;
    private final double radius;
    private int cycleCount;
    private final int maxCycleCount;
    private final ArrayList<Entity> nmsEntitiesAll = new ArrayList<>();
    private final ArrayList<Block> bukkitBlocksTemp = new ArrayList<>();
    private final HashMap<FallingBlock, Integer> fallingBlocksAll = new HashMap<>();
    private final HashMap<Entity, Boolean> playerBreakBlocks = new HashMap<>();
    private static final Random random = new Random();

    public RunnableTornado(World nmsWorld, BlockPosition pos, double radius, int maxCycleCount) {
        this.nmsWorld = nmsWorld;
        this.bukkitWorld = nmsWorld.getWorld();
        this.pos = pos;
        this.bukkitLoc = new Location(this.bukkitWorld, this.pos.getX(), this.pos.getY() + 1.0, this.pos.getZ());
        this.radius = radius;
        this.maxCycleCount = maxCycleCount;
        ListenerLightningStrike.storm = true;
    }

    @Override
    public void run() {
        this.cycleCount++;
        if (this.cycleCount > this.maxCycleCount) {
            this.cancel();
            ListenerLightningStrike.storm = false;

            for (Entity nmsEntity : this.nmsEntitiesAll) {
                nmsEntity.noclip = false;
            }

            for (FallingBlock fallingBlock : this.fallingBlocksAll.keySet()) {
                fallingBlock.setGravity(true);
            }

            for (Map.Entry<Entity, Boolean> entry : this.playerBreakBlocks.entrySet()) {
                entry.setValue(false);
            }

            return;
        }
        for (Map.Entry<FallingBlock, Integer> fallingBlockEntry : this.fallingBlocksAll.entrySet()) { // continue the spinning motion of falling blocks every tick
            fallingBlockEntry.getKey().setVelocity(CustomMathHelper.spiralVector(13.0 + fallingBlockEntry.getValue() / 15.0, this.cycleCount - fallingBlockEntry.getValue(), 0.65 + random.nextDouble() * 0.8).multiply(0.25));
        }

        if (this.cycleCount % 7 == 0) { // pull in entities every 7 ticks
            List<Entity> nmsEntitiesTemp = this.nmsWorld.getEntities(null, new AxisAlignedBB(this.pos).g(this.radius), entity -> ((entity instanceof EntityLiving || entity instanceof EntityEnderPearl) && !(entity instanceof CustomEntityEnderDragon || entity instanceof CustomEntityWither)));
            this.nmsEntitiesAll.addAll(nmsEntitiesTemp);

            for (Entity nmsEntity : nmsEntitiesTemp) {
                nmsEntity.noclip = true;

                if (nmsEntity instanceof EntityPlayer && (!this.playerBreakBlocks.containsKey(nmsEntity) || !this.playerBreakBlocks.get(nmsEntity))) {
                    this.playerBreakBlocks.put(nmsEntity, true);
                    new RunnableBreakBlocks(nmsEntity, 1, 1, 1, 1, false, false, this.maxCycleCount - this.cycleCount).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 1L); // because a player's noclip is instantly turned off if not in spectator
                }

                nmsEntity.getBukkitEntity().setVelocity(new Vector((this.pos.getX() - nmsEntity.locX()) / 15.0, (this.pos.getY() - nmsEntity.locY()) / 15.0, (this.pos.getZ() - nmsEntity.locZ()) / 15.0));
            }
        }

        if (this.cycleCount % 2 == 0) {
            this.bukkitWorld.strikeLightningEffect(this.bukkitLoc); // strike lightning every 2 ticks

            this.bukkitBlocksTemp.clear();
            Location randomLoc;
            for (int i = 0; i < random.nextInt(2) + 5; i++) { // pick up 5-6 new blocks every 2 ticks
                randomLoc = new Location(this.bukkitWorld, this.pos.getX() + random.nextGaussian() * this.radius * 0.2, 0.0, this.pos.getZ() + random.nextGaussian() * this.radius * 0.2);
                this.bukkitBlocksTemp.add(this.bukkitWorld.getHighestBlockAt(randomLoc));
            }

            Vector vec = CustomMathHelper.spiralVector(13.0, this.cycleCount, 0.6 + random.nextDouble() * 0.75).multiply(0.25);
            randomLoc = new Location(this.bukkitWorld, this.bukkitLoc.getX() + 13.0, this.bukkitLoc.getY(), this.bukkitLoc.getZ()); // so that after its first "toss" by the tornado, it will be perfectly centered on it
            for (Block bukkitBlock : this.bukkitBlocksTemp) { // turn those new blocks into falling blocks
                org.bukkit.Material bukkitMaterial = bukkitBlock.getType();

                if (Predicates.blockBreakableDefault.test(bukkitMaterial) && Predicates.notBedrock.test(bukkitMaterial) && Predicates.notFluid.test(bukkitMaterial) && Predicates.notHardBlocks.test((bukkitMaterial))) { // as long as it isn't one of these blocks
                    FallingBlock fallingBlock = this.bukkitWorld.spawnFallingBlock(randomLoc, bukkitBlock.getBlockData());
                    fallingBlock.setGravity(false);
                    fallingBlock.setVelocity(vec);
                    this.fallingBlocksAll.put(fallingBlock, this.cycleCount);
                    bukkitBlock.setType(org.bukkit.Material.AIR);
                } else if (!Predicates.notHardBlocks.test(bukkitMaterial)) { // 50% chance to pick up these blocks
                    if (random.nextDouble() < 0.5) {
                        FallingBlock fallingBlock = this.bukkitWorld.spawnFallingBlock(randomLoc, bukkitBlock.getBlockData());
                        fallingBlock.setGravity(false);
                        fallingBlock.setVelocity(vec);
                        this.fallingBlocksAll.put(fallingBlock, this.cycleCount);
                        bukkitBlock.setType(org.bukkit.Material.AIR);
                    }
                }
            }

            bukkitBlocksTemp.clear();
        }
    }
}
