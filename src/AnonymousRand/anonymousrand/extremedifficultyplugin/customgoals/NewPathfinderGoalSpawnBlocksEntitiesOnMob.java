package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityZombieHusk;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityAreaEffectCloud;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;

public class NewPathfinderGoalSpawnBlocksEntitiesOnMob extends PathfinderGoal {

    private final EntityInsentient entity;
    private final Material material;
    private final Entity entityToBeSpawned;
    private final int delay;
    private Location loc;

    public NewPathfinderGoalSpawnBlocksEntitiesOnMob(EntityInsentient entity, Material material, int delay) {
        this.entity = entity;
        this.material = material;
        this.entityToBeSpawned = null;
        this.delay = delay;
    }

    public NewPathfinderGoalSpawnBlocksEntitiesOnMob(EntityInsentient entity, Entity entityToBeSpawned, int delay) {
        this.entity = entity;
        this.material = null;
        this.entityToBeSpawned = entityToBeSpawned;
        this.delay = delay;
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
        if (this.entity.ticksLived % this.delay == 10) {
            this.loc = new Location(this.entity.getWorld().getWorld(), this.entity.locX(), this.entity.locY(), this.entity.locZ());

            if (this.material != null) {
                if (this.loc.getBlock().getType() == org.bukkit.Material.AIR) { /**spiders lay down cobwebs that last 4 seconds on itself as long as it is inside an air block*/ //cobwebs also indirectly prevent players from shooting arrows onto the spider as the arrows are blocked by the web hitbox
                    this.loc.getBlock().setType(this.material);

                    if (this.material == Material.COBWEB) {
                        Bukkit.getPluginManager().callEvent(new BlockPlaceEvent(this.loc.getBlock(), this.loc.getBlock().getState(), null, null, null, false, null)); //fire event that would otherwise not be fired so that the cobweb block can be broken after 4 seconds
                    }
                }
            } else if (this.entityToBeSpawned != null) {
                this.entityToBeSpawned.setPosition(this.entity.locX(), this.entity.locY(), this.entity.locZ());
                this.entity.getWorld().addEntity(this.entityToBeSpawned);
            }
        }
    }
}
