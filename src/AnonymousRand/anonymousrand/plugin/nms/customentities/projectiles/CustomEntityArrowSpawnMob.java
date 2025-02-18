package AnonymousRand.anonymousrand.plugin.nms.customentities.projectiles;

import AnonymousRand.anonymousrand.plugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.Location;

public class CustomEntityArrowSpawnMob extends CustomEntityArrow {

    private EntityLiving entityToSpawn;

    public CustomEntityArrowSpawnMob(World world, EntityLiving entityToSpawn) {
        super(world);
        this.entityToSpawn = entityToSpawn;
    }

    @Override
    public void die() {
        super.die();

        if (this.entityToSpawn != null) {
            new SpawnEntity(this.world, this.entityToSpawn, 1, null, new Location(this.world.getWorld(), this.locX(), this.locY(), this.locZ()), true);
            this.entityToSpawn = null;
        }
    }
}
