package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.projectiles;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class CustomEntityArrow extends EntityArrow {

    public CustomEntityArrow(World world, Vector a, byte pierce, @Nullable ProjectileSource source) { // "a" parameter is for matching this new arrow's movement vector with the old arrow's when it replaces the old arrow; "pierce" parameter makes sure that the new arrow has the same pierce level as the old one, "source" parameter makes sure that the new arrow has the same shooter as the old one
        super(EntityTypes.ARROW, world);

        double x = a.getX(), y = a.getY(), z = a.getZ(); // similar code from projectile's shoot method; passing in a vec3d directly to this constructor doesn't seem to get the correct vector so must convert vector to vec3d instead
        Vec3D v = (new Vec3D(x, y, z).d());
        this.setMot(v);
        this.setPierceLevel(pierce);

        if (source instanceof Entity) {
            this.setShooter(((CraftEntity)source).getHandle());
        }
    }

    public CustomEntityArrow(World world) {
        super(EntityTypes.ARROW, world);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived >= 80) { /* arrows despawn after 4 seconds to reduce lag and make them harder to pick up */
            this.die();
        }
    }

    @Override
    protected ItemStack getItemStack()
    {
        return new ItemStack(Items.ARROW); /* tipped arrows are now nonexistent */
    }
}
