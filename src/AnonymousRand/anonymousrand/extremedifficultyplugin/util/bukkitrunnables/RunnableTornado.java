package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityEnderDragon;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityWither;
import AnonymousRand.anonymousrand.extremedifficultyplugin.listeners.ListenerLightningStrike;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CustomMathHelper;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
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
    private final Location loc;
    private final double radius;
    private int cycles;
    private final int maxCycles;
    private List<Entity> nmsEntitiesTemp;
    private ArrayList<Entity> nmsEntitiesAll = new ArrayList<>();
    private ArrayList<Block> bukkitBlocksTemp = new ArrayList<>();
    private org.bukkit.Material type;
    private HashMap<FallingBlock, Integer> fallingBlocksAll = new HashMap<>();
    private HashMap<Entity, Boolean> playerBreakBlocks = new HashMap<>();
    private Vector vec;
    private Location randomLoc;
    private static final Random random = new Random();

    public RunnableTornado(World nmsWorld, BlockPosition pos, double radius, int maxCycles) {
        this.nmsWorld = nmsWorld;
        this.bukkitWorld = nmsWorld.getWorld();
        this.pos = pos;
        this.loc = new Location(this.bukkitWorld, this.pos.getX(), this.pos.getY() + 1.0, this.pos.getZ());
        this.radius = radius;
        this.maxCycles = maxCycles;
        ListenerLightningStrike.storm = true;
    }

    @Override
    public void run() {
        if (++this.cycles <= this.maxCycles) { //continue the spinning motion of falling blocks every tick
            for (Map.Entry<FallingBlock, Integer> fallingBlockEntry : this.fallingBlocksAll.entrySet()) {
                fallingBlockEntry.getKey().setVelocity(CustomMathHelper.spiralVector(13.0 + fallingBlockEntry.getValue() / 20.0, this.cycles - fallingBlockEntry.getValue(), 0.6 + random.nextDouble() * 0.75).multiply(0.25));
            }

            if (this.cycles % 7 == 0) { //pull in entities every 7 ticks
                this.nmsEntitiesTemp = this.nmsWorld.getEntities(null, new AxisAlignedBB(this.pos).g(this.radius), entity -> ((entity instanceof EntityLiving || entity instanceof EntityEnderPearl) && !(entity instanceof CustomEntityEnderDragon || entity instanceof CustomEntityWither)));
                this.nmsEntitiesAll.addAll(this.nmsEntitiesTemp);

                for (Entity nmsEntity : this.nmsEntitiesTemp) {
                    nmsEntity.noclip = true;

                    if (nmsEntity instanceof EntityPlayer && (!this.playerBreakBlocks.containsKey(nmsEntity) || !this.playerBreakBlocks.get(nmsEntity))) {
                        this.playerBreakBlocks.put(nmsEntity, true);
                        new RunnableBreakBlocks(nmsEntity, 1, 1, 1, 1, false, this.maxCycles - this.cycles).runTaskTimer(StaticPlugin.plugin, 0L, 1L); //because a player's noclip is instantly turned off if not in spectator
                    }

                    nmsEntity.getBukkitEntity().setVelocity(new Vector((this.pos.getX() - nmsEntity.locX()) / 15.0, (this.pos.getY() - nmsEntity.locY()) / 15.0, (this.pos.getZ() - nmsEntity.locZ()) / 15.0));
                }
            }

            if (this.cycles % 2 == 0) {
                this.bukkitWorld.strikeLightningEffect(this.loc); //strike lightning every 2 ticks

                this.bukkitBlocksTemp.clear();
                for (int i = 0; i < random.nextInt(2) + 5; i++) { //pick up 5-6 new blocks every 2 ticks
                    this.randomLoc = new Location(this.bukkitWorld, this.pos.getX() + random.nextGaussian() * this.radius * 0.2, 0.0, this.pos.getZ() + random.nextGaussian() * this.radius * 0.2);
                    this.bukkitBlocksTemp.add(this.bukkitWorld.getHighestBlockAt(this.randomLoc));
                }

                this.vec = CustomMathHelper.spiralVector(13.0, this.cycles, 0.6 + random.nextDouble() * 0.75).multiply(0.25);
                this.randomLoc = new Location(this.bukkitWorld, this.loc.getX() + 13.0, this.loc.getY(), this.loc.getZ()); //so that after its first "toss" by the tornado, it will be perfectly centered on it
                for (Block bukkitBlock : this.bukkitBlocksTemp) { //turn those new blocks into falling blocks
                    type = bukkitBlock.getType();

                    if (type != org.bukkit.Material.AIR && type != org.bukkit.Material.BEDROCK && type != org.bukkit.Material.END_GATEWAY && type != org.bukkit.Material.END_PORTAL && type != org.bukkit.Material.END_PORTAL_FRAME && type != org.bukkit.Material.NETHER_PORTAL && type != org.bukkit.Material.COMMAND_BLOCK && type != org.bukkit.Material.COMMAND_BLOCK_MINECART && type != org.bukkit.Material.STRUCTURE_BLOCK && type != org.bukkit.Material.JIGSAW && type != org.bukkit.Material.BARRIER && type != org.bukkit.Material.SPAWNER && type != org.bukkit.Material.WATER && type != org.bukkit.Material.LAVA && type != org.bukkit.Material.OBSIDIAN && type != org.bukkit.Material.CRYING_OBSIDIAN && type != org.bukkit.Material.RESPAWN_ANCHOR && type != org.bukkit.Material.ANCIENT_DEBRIS && type != org.bukkit.Material.NETHERITE_BLOCK) { //as long as it isn't one of these blocks
                        FallingBlock fallingBlock = this.bukkitWorld.spawnFallingBlock(this.randomLoc, bukkitBlock.getBlockData());
                        fallingBlock.setGravity(false);
                        fallingBlock.setVelocity(this.vec);
                        this.fallingBlocksAll.put(fallingBlock, this.cycles);
                        bukkitBlock.setType(org.bukkit.Material.AIR);
                    } else if (type == org.bukkit.Material.OBSIDIAN || type == org.bukkit.Material.CRYING_OBSIDIAN || type == org.bukkit.Material.RESPAWN_ANCHOR || type == org.bukkit.Material.ANCIENT_DEBRIS || type == org.bukkit.Material.NETHERITE_BLOCK) { //50% chance to pick up these blocks
                        if (random.nextDouble() < 0.5) {
                            FallingBlock fallingBlock = this.bukkitWorld.spawnFallingBlock(this.randomLoc, bukkitBlock.getBlockData());
                            fallingBlock.setGravity(false);
                            fallingBlock.setVelocity(this.vec);
                            this.fallingBlocksAll.put(fallingBlock, this.cycles);
                            bukkitBlock.setType(org.bukkit.Material.AIR);
                        }
                    }
                }
            }
        } else if (this.cycles - 5 >= this.maxCycles) {
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
        }
    }
}
