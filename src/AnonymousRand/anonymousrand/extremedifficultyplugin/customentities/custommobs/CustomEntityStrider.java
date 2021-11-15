package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalSpawnBlocksEntitiesOnMob;
import net.minecraft.server.v1_16_R1.EntityStrider;
import net.minecraft.server.v1_16_R1.EntityTypes;
import net.minecraft.server.v1_16_R1.Explosion;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.Material;

public class CustomEntityStrider extends EntityStrider {

    public CustomEntityStrider(World world) {
        super(EntityTypes.STRIDER, world);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.hasSinglePlayerPassenger() && random.nextDouble() < 0.0006) { /**if they have a rider, striders have a 0.06% chance per tick to explode*/
            this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 2.5F, false, Explosion.Effect.DESTROY);
            this.die();
        }
    }
}
