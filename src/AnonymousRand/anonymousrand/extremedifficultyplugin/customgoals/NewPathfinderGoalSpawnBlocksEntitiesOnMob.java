package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableConstantlySpawnBlocksEntities;
import net.minecraft.server.v1_16_R1.Entity;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.PathfinderGoal;
import org.bukkit.Material;

public class NewPathfinderGoalSpawnBlocksEntitiesOnMob extends PathfinderGoal {

    protected final EntityInsentient entity;
    protected Material material;
    protected Entity firstEntityToSpawn;
    protected int delayTimer, xRadius, yRadius, zRadius;
    protected double yOffset;
    protected boolean terraform;

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

    public void changeSpawnedEntity(Entity entityToBeSpawned) {
        this.firstEntityToSpawn = entityToBeSpawned;
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
            new RunnableConstantlySpawnBlocksEntities(this.entity, this.material, this.firstEntityToSpawn, this.xRadius, this.yRadius, this.zRadius, this.yOffset, this.terraform).run();
        }
    }
}
