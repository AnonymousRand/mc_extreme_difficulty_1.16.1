package AnonymousRand.anonymousrand.plugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.plugin.nms.customentities.misc.CustomEntityAreaEffectCloud;
import AnonymousRand.anonymousrand.plugin.util.Predicates;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;

public class RunnableConstantlySpawnBlocksEntities extends BukkitRunnable {

    private final EntityLiving runnableOwner;
    private final Material material;
    private final Entity firstEntityToSpawn;
    private Entity entityToBeSpawned;
    private final int xRadius, yRadius, zRadius;
    private final double offsetY;
    private final boolean terraform;
    private final World nmsWorld;
    protected int cycleCount;
    protected final int maxCycleCount;
    protected Block bukkitBlock;
    protected Material bukkitMaterial;

    public RunnableConstantlySpawnBlocksEntities(EntityLiving runnableOwner, @Nullable Material material, @Nullable Entity firstEntityToSpawn, int xRadius, int yRadius, int zRadius, double offsetY, boolean terraform) {
        this.runnableOwner = runnableOwner;
        this.material = material;
        this.firstEntityToSpawn = firstEntityToSpawn;
        this.xRadius = xRadius;
        this.yRadius = yRadius;
        this.zRadius = zRadius;
        this.offsetY = offsetY;
        this.terraform = terraform;
        this.nmsWorld = runnableOwner.getWorld();
        this.cycleCount = 0;
        this.maxCycleCount = 1;
    }

    public RunnableConstantlySpawnBlocksEntities(EntityLiving runnableOwner, @Nullable Material material, @Nullable Entity firstEntityToSpawn, int xRadius, int yRadius, int zRadius, double offsetY, boolean terraform, int maxCycleCount) {
        this.runnableOwner = runnableOwner;
        this.material = material;
        this.firstEntityToSpawn = firstEntityToSpawn;
        this.xRadius = xRadius;
        this.yRadius = yRadius;
        this.zRadius = zRadius;
        this.offsetY = offsetY;
        this.terraform = terraform;
        this.nmsWorld = runnableOwner.getWorld();
        this.cycleCount = 0;
        this.maxCycleCount = maxCycleCount;
    }

    @Override
    public void run() {
        this.cycleCount++;
        if (this.cycleCount > this.maxCycleCount) {
            this.cancel();
            return;
        }

        for (int x = -this.xRadius; x <= this.xRadius; x++) {
            for (int y = -this.yRadius; y <= this.yRadius; y++) {
                for (int z = -this.zRadius; z <= this.zRadius; z++) {
                    Location bukkitLoc = new Location(this.nmsWorld.getWorld(), this.runnableOwner.locX() + x, this.runnableOwner.locY() + this.offsetY + y, this.runnableOwner.locZ() + z);

                    if (this.material != null) {
                        this.bukkitBlock = bukkitLoc.getBlock();
                        this.bukkitMaterial = this.bukkitBlock.getType();

                        if (this.terraform) {
                            if (this.bukkitMaterial != this.material && Predicates.blockBreakableDefault.test(this.bukkitMaterial) && Predicates.notBedrock.test(this.bukkitMaterial) && Predicates.notHardBlocks.test(this.bukkitMaterial) && Predicates.notFireOrWitherRose.test(this.bukkitMaterial) && Predicates.notFluid.test(this.bukkitMaterial)) { // as long as it isn't one of these blocks
                                this.bukkitBlock.setType(this.material);

                                if (this.material == Material.COBWEB) {
                                    Bukkit.getPluginManager().callEvent(new BlockPlaceEvent(this.bukkitBlock, this.bukkitBlock.getState(), null, null, null, false, null)); // fire event that would otherwise not be fired so that the cobweb block can be broken after 2.5 seconds
                                }
                            }
                        } else {
                            if (this.bukkitMaterial == Material.AIR) {
                                this.bukkitBlock.setType(this.material);

                                if (this.material == Material.COBWEB || this.material == Material.SOUL_SOIL) {
                                    Bukkit.getPluginManager().callEvent(new BlockPlaceEvent(this.bukkitBlock, this.bukkitBlock.getState(), null, null, null, false, null)); // fire event that would otherwise not be fired so that the cobweb or soul soil block can be broken after 2.5 seconds
                                }
                            }
                        }
                    } else if (this.firstEntityToSpawn != null) {
                        if (this.firstEntityToSpawn instanceof CustomEntityAreaEffectCloud) {
                            CustomEntityAreaEffectCloud newAEC = (CustomEntityAreaEffectCloud) this.firstEntityToSpawn;

                            try {
                                this.entityToBeSpawned = this.firstEntityToSpawn.getClass().getDeclaredConstructor(World.class, float.class, int.class, int.class).newInstance(this.nmsWorld, newAEC.getRadius(), newAEC.getDuration(), newAEC.waitTime);
                            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                                e.printStackTrace();
                            }

                            for (MobEffect effect : newAEC.effects) {
                                ((CustomEntityAreaEffectCloud) this.entityToBeSpawned).addEffect(effect);
                            }
                        } else if (this.firstEntityToSpawn instanceof EntityTNTPrimed) {
                            try {
                                this.entityToBeSpawned = this.firstEntityToSpawn.getClass().getDeclaredConstructor(EntityTypes.class, World.class).newInstance(EntityTypes.TNT, this.nmsWorld);
                            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        if (this.entityToBeSpawned != null) {
                            this.entityToBeSpawned.setPosition(bukkitLoc.getX(), bukkitLoc.getY(), bukkitLoc.getZ());
                            this.nmsWorld.addEntity(this.entityToBeSpawned);
                        }
                    }
                }
            }
        }
    }
}
