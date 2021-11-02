package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntitySkeleton;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntitySkeletonStray;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.Vec3D;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class CustomEntityArrowSpawnMob extends CustomEntityArrow {

    private EntityLiving entityToSpawn;

    public CustomEntityArrowSpawnMob(World world, Vector a, @Nullable ProjectileSource source, EntityLiving entityLiving) {
        super(world);
        this.entityToSpawn = entityLiving;

        double x = a.getX(), y = a.getY(), z = a.getZ(); //similar code from projectile's shoot method; passing in a vec3d directly to this constructor doesn't seem to get the correct vector so must convert vector to vec3d instead
        Vec3D v = (new Vec3D(x, y, z).d());
        this.setMot(v);

        if (source instanceof Entity) {
            this.setShooter(((CraftEntity)source).getHandle());
        }

        if (this.getShooter() instanceof CustomEntitySkeleton || this.getShooter() instanceof CustomEntitySkeletonStray) {
            this.setDamage(1.5); /**skeletons and strays do slightly less damage*/
        }
    }

    @Override
    public void die() {
        super.die();

        if (this.entityToSpawn != null) {
            new SpawnLivingEntity(this.getWorld(), this.entityToSpawn, 1, null, new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ()), true).run();
            this.entityToSpawn = null;
        }
    }
}
