package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customProjectiles;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class CustomEntityThrownTrident extends EntityThrownTrident {

    public CustomEntityThrownTrident(World world, Vector a, byte pierce, @Nullable ProjectileSource source) {
        super(EntityTypes.TRIDENT, world);

        double x = a.getX(), y = a.getY(), z = a.getZ(); //similar code from projectile's shoot method; passing in a vec3d directly to this constructor doesn't seem to get the correct vector so must convert vector to vec3d instead
        Vec3D v = (new Vec3D(x, y, z).d());
        this.setMot(v);
        this.setPierceLevel(pierce);

        if (source instanceof Entity) {
            this.setShooter(((CraftEntity)source).getHandle());
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived >= 120) { /**tridents despawn after 6 seconds*/
            this.die();
        }
    }

    @Override
    public void die() {
        super.die();

        if (random.nextDouble() < 0.5) {
            this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 0.4f, false, Explosion.Effect.DESTROY); /**tridents have a 50% chance to explode when they die*/
        }
    }
}
