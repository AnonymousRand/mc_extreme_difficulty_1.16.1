package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityAreaEffectCloud;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;

public class NewPathfinderGoalSpawnBlocksEntitiesOnMob extends PathfinderGoal {

    private final EntityInsentient entity;
    private final Material material;
    private final Entity firstEntityToSpawn;
    private final int delayTimer, xRadius, yRadius, zRadius;
    private final double yOffset;
    private final boolean terraform;

    public NewPathfinderGoalSpawnBlocksEntitiesOnMob(EntityInsentient entity, Material material, int delayTimer) {
        this.entity = entity;
        this.material = material;
        this.firstEntityToSpawn = null;
        this.delayTimer = delayTimer;
        this.xRadius = 0;
        this.yRadius = 0;
        this.zRadius = 0;
        this.yOffset = 0.0;
        this.terraform = false;
    }

    public NewPathfinderGoalSpawnBlocksEntitiesOnMob(EntityInsentient entity, Material material, int delayTimer, int xRadius, int yRadius, int zRadius, double yOffset, boolean terraform) {
        this.entity = entity;
        this.material = material;
        this.firstEntityToSpawn = null;
        this.delayTimer = delayTimer;
        this.xRadius = xRadius;
        this.yRadius = yRadius;
        this.zRadius = zRadius;
        this.yOffset = yOffset;
        this.terraform = terraform;
    }

    public NewPathfinderGoalSpawnBlocksEntitiesOnMob(EntityInsentient entity, Entity entityToBeSpawned, int delayTimer) {
        this.entity = entity;
        this.material = null;
        this.firstEntityToSpawn = entityToBeSpawned;
        this.delayTimer = delayTimer;
        this.xRadius = 0;
        this.yRadius = 0;
        this.zRadius = 0;
        this.yOffset = 0.0;
        this.terraform = false;
    }

    public NewPathfinderGoalSpawnBlocksEntitiesOnMob(EntityInsentient entity, Entity entityToBeSpawned, int delayTimer, int xRadius, int yRadius, int zRadius, double yOffset) {
        this.entity = entity;
        this.material = null;
        this.firstEntityToSpawn = entityToBeSpawned;
        this.delayTimer = delayTimer;
        this.xRadius = xRadius;
        this.yRadius = yRadius;
        this.zRadius = zRadius;
        this.yOffset = yOffset;
        this.terraform = false;
    }

    @Override
    public boolean a() {
        return this.entity.getGoalTarget() != null;
    }

    @Override
    public boolean b() {
        return this.a();
    }

    @Override
    public void e() {
        if ((this.entity.ticksLived - 10) % this.delayTimer == 0) {
            new SpawnBlocksEntities(this.entity, this.material, this.firstEntityToSpawn, this.xRadius, this.yRadius, this.zRadius, this.yOffset, this.terraform).run();
        }
    }

    static class SpawnBlocksEntities extends BukkitRunnable{

        private final EntityInsentient entity;
        private final Material material;
        private final Entity firstEntityToSpawn;
        private Entity entityToBeSpawned;
        private final int xRadius, yRadius, zRadius;
        private final double yOffset;
        private final boolean terraform;
        private final World nmsWorld;
        private CustomEntityAreaEffectCloud AEC;
        private Location loc;

        public SpawnBlocksEntities(EntityInsentient entity, Material material, Entity entityToBeSpawned, int xRadius, int yRadius, int zRadius, double yOffset, boolean terraform) {
            this.entity = entity;
            this.material = material;
            this.firstEntityToSpawn = entityToBeSpawned;
            this.xRadius = xRadius;
            this.yRadius = yRadius;
            this.zRadius = zRadius;
            this.yOffset = yOffset;
            this.terraform = terraform;
            this.nmsWorld = entity.getWorld();
        }

        @Override
        public void run() {
            for (int x = -this.xRadius; x <= this.xRadius; x++) {
                for (int y = -this.yRadius; y <= this.yRadius; y++) {
                    for (int z = -this.zRadius; z <= this.zRadius; z++) {
                        this.loc = new Location(this.nmsWorld.getWorld(), this.entity.locX() + x, this.entity.locY() + this.yOffset + y, this.entity.locZ() + z);

                        if (this.material != null) {
                            if (this.terraform) {
                                Material type = this.loc.getBlock().getType();

                                if (type != Material.AIR && type != Material.BEDROCK && type != Material.END_GATEWAY && type != Material.END_PORTAL && type != Material.END_PORTAL_FRAME && type != Material.NETHER_PORTAL && type != Material.OBSIDIAN && type != Material.CRYING_OBSIDIAN && type != Material.COMMAND_BLOCK && type != Material.COMMAND_BLOCK_MINECART && type != Material.STRUCTURE_BLOCK && type != Material.JIGSAW && type != Material.BARRIER && type != Material.SPAWNER && type != Material.COBWEB) { //as long as it isn't one of these blocks) {
                                    this.loc.getBlock().setType(this.material);

                                    if (this.material == Material.COBWEB) {
                                        Bukkit.getPluginManager().callEvent(new BlockPlaceEvent(this.loc.getBlock(), this.loc.getBlock().getState(), null, null, null, false, null)); //fire event that would otherwise not be fired so that the cobweb block can be broken after 2.5 seconds
                                    }
                                }
                            } else {
                                if (this.loc.getBlock().getType() == org.bukkit.Material.AIR) {
                                    this.loc.getBlock().setType(this.material);

                                    if (this.material == Material.COBWEB) {
                                        Bukkit.getPluginManager().callEvent(new BlockPlaceEvent(this.loc.getBlock(), this.loc.getBlock().getState(), null, null, null, false, null)); //fire event that would otherwise not be fired so that the cobweb block can be broken after 2.5 seconds
                                    }
                                }
                            }
                        } else if (this.firstEntityToSpawn instanceof CustomEntityAreaEffectCloud) {
                            this.AEC = (CustomEntityAreaEffectCloud) this.firstEntityToSpawn;

                            try {
                                this.entityToBeSpawned = this.firstEntityToSpawn.getClass().getDeclaredConstructor(World.class, float.class, int.class, int.class).newInstance(this.nmsWorld, this.AEC.getRadius(), this.AEC.getDuration(), this.AEC.waitTime);
                                ((CustomEntityAreaEffectCloud) this.entityToBeSpawned).setColor(this.AEC.getColor());
                            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                                e.printStackTrace();
                            }

                            if (this.entityToBeSpawned != null) {
                                this.entityToBeSpawned.setPosition(this.loc.getX(), this.loc.getY(), this.loc.getZ());
                                this.nmsWorld.addEntity(this.entityToBeSpawned);
                            }
                        }
                    }
                }
            }
        }
    }
}
