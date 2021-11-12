package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.extremedifficultyplugin.listeners.LightningStrikeListeners;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RunnableTornado extends BukkitRunnable {

    private final World nmsWorld;
    private final org.bukkit.World bukkitWorld;
    private final BlockPosition pos;
    private final Location loc;
    private final double radius;
    private int cycles;
    private final int maxCycles;
    private List<Entity> nmsEntities;
    private ArrayList<Entity> nmsEntitiesAll = new ArrayList<>();
    private ArrayList<Block> bukkitBlocks = new ArrayList<>();
    private org.bukkit.Material type;
    private ArrayList<FallingBlock> fallingBlocksAll = new ArrayList<>();
    private double phi;
    private double x;
    private double z;
    private final Random random = new Random();

    public RunnableTornado(World nmsWorld, BlockPosition pos, double radius, int maxCycles) {
        this.nmsWorld = nmsWorld;
        this.bukkitWorld = nmsWorld.getWorld();
        this.pos = pos;
        this.loc = new Location(this.bukkitWorld, this.pos.getX(), this.pos.getY(), this.pos.getZ());
        this.radius = radius;
        this.maxCycles = maxCycles;
        this.phi = 0;
        LightningStrikeListeners.storm = true;
    }

    @Override
    public void run() {
        if (++this.cycles <= this.maxCycles) {
            if (this.cycles % 3 == 0) {
                this.bukkitWorld.strikeLightningEffect(this.loc);

                this.nmsEntities = this.nmsWorld.getEntities(null, new AxisAlignedBB(this.pos).g(this.radius), entity -> entity instanceof EntityLiving);
                this.nmsEntitiesAll.addAll(this.nmsEntities);
                for (Entity nmsEntity : this.nmsEntities) {
                    nmsEntity.noclip = true;
                    nmsEntity.getBukkitEntity().setVelocity(new Vector((this.pos.getX() - nmsEntity.locX()) / 8.0, (this.pos.getY() - nmsEntity.locY()) / 8.0, (this.pos.getZ() - nmsEntity.locZ()) / 8.0));
                }
            }

            this.bukkitBlocks.clear();
            for (int i = 0; i < this.random.nextInt(2) + 4; i++) {
                this.bukkitBlocks.add(new Location(this.bukkitWorld, this.pos.getX() + this.random.nextGaussian() * this.radius * 0.25, this.pos.getY() + this.random.nextGaussian() * this.radius * 0.25, this.pos.getZ() + this.random.nextGaussian() * this.radius * 0.25).getBlock());
            }

            this.phi += Math.PI / 8.0; //todo: not spiraling
            for (Block bukkitBlock : this.bukkitBlocks) {
                type = bukkitBlock.getType();

                if (type != org.bukkit.Material.BEDROCK && type != org.bukkit.Material.END_GATEWAY && type != org.bukkit.Material.END_PORTAL && type != org.bukkit.Material.END_PORTAL_FRAME && type != org.bukkit.Material.NETHER_PORTAL && type != org.bukkit.Material.COMMAND_BLOCK && type != org.bukkit.Material.COMMAND_BLOCK_MINECART && type != org.bukkit.Material.STRUCTURE_BLOCK && type != org.bukkit.Material.JIGSAW && type != org.bukkit.Material.BARRIER && type != org.bukkit.Material.SPAWNER) {
                    for (double t = 0; t <= 2.0 * Math.PI; t += Math.PI / 8.0){
                        for (double i = 0; i < 2; i++){
                            this.x = 0.3 * (2.0 * Math.PI - t) * 0.5 * Math.cos(t + this.phi + i * Math.PI);
                            this.z = 0.3 * (2 * Math.PI - t) * 0.5 * Math.sin(t + this.phi + i * Math.PI);
                        }
                    }

                    FallingBlock fallingBlock = this.bukkitWorld.spawnFallingBlock(bukkitBlock.getLocation(), bukkitBlock.getBlockData());
                    fallingBlock.setGravity(false);
                    fallingBlock.setVelocity(new Vector(this.x, 0.5 + this.random.nextDouble() / 5.0, this.z));
                    this.fallingBlocksAll.add(fallingBlock);
                    bukkitBlock.setType(org.bukkit.Material.AIR);
                }
            }
        } else if (this.cycles - 5 >= this.maxCycles) {
            this.cancel();
            LightningStrikeListeners.storm = false;

            for (Entity nmsEntity : this.nmsEntitiesAll) {
                nmsEntity.noclip = false;
            }

            for (FallingBlock fallingBlock : this.fallingBlocksAll) {
                fallingBlock.setGravity(true);
            }
        }
    }
}
