package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityAreaEffectCloud;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;

import java.lang.reflect.InvocationTargetException;

public class NewPathfinderGoalSpawnBlocksEntitiesOnMob extends PathfinderGoal {

    private final EntityInsentient entity;
    private final Material material;
    private final Entity firstEntityToSpawn;
    private Entity entityToBeSpawned;
    private final int delay, yRadius;
    private final double yOffset;
    private final World nmsWorld;
    private CustomEntityAreaEffectCloud AEC;
    private Location loc;

    public NewPathfinderGoalSpawnBlocksEntitiesOnMob(EntityInsentient entity, Material material, int delay) {
        this.entity = entity;
        this.material = material;
        this.firstEntityToSpawn = null;
        this.delay = delay;
        this.yRadius = 0;
        this.yOffset = 0.0;
        this.nmsWorld = entity.getWorld();
    }

    public NewPathfinderGoalSpawnBlocksEntitiesOnMob(EntityInsentient entity, Entity entityToBeSpawned, int delay) {
        this.entity = entity;
        this.material = null;
        this.firstEntityToSpawn = entityToBeSpawned;
        this.delay = delay;
        this.yRadius = 0;
        this.yOffset = 0.0;
        this.nmsWorld = entity.getWorld();
    }

    public NewPathfinderGoalSpawnBlocksEntitiesOnMob(EntityInsentient entity, Entity entityToBeSpawned, int delay, int yRadius, double yOffset) {
        this.entity = entity;
        this.material = null;
        this.firstEntityToSpawn = entityToBeSpawned;
        this.delay = delay;
        this.yRadius = yRadius;
        this.yOffset = yOffset;
        this.nmsWorld = entity.getWorld();
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
        if ((this.entity.ticksLived - 10) % this.delay == 0) {
            for (int i = -this.yRadius; i <= this.yRadius; i++) {
                this.loc = new Location(this.nmsWorld.getWorld(), this.entity.locX(), this.entity.locY() + this.yOffset + i, this.entity.locZ());

                if (this.material != null) {
                    if (this.loc.getBlock().getType() == org.bukkit.Material.AIR) {
                        this.loc.getBlock().setType(this.material);

                        if (this.material == Material.COBWEB) {
                            Bukkit.getPluginManager().callEvent(new BlockPlaceEvent(this.loc.getBlock(), this.loc.getBlock().getState(), null, null, null, false, null)); //fire event that would otherwise not be fired so that the cobweb block can be broken after 4 seconds
                        }
                    }
                } else if (this.firstEntityToSpawn instanceof CustomEntityAreaEffectCloud) {
                    this.AEC = (CustomEntityAreaEffectCloud)this.firstEntityToSpawn;

                    try {
                        this.entityToBeSpawned = this.firstEntityToSpawn.getClass().getDeclaredConstructor(World.class, float.class, int.class, int.class).newInstance(this.nmsWorld, this.AEC.getRadius(), this.AEC.getDuration(), this.AEC.waitTime);
                        ((CustomEntityAreaEffectCloud)this.entityToBeSpawned).setColor(this.AEC.getColor());
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
