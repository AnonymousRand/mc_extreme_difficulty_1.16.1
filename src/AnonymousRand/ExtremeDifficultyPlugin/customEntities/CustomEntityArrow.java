package AnonymousRand.ExtremeDifficultyPlugin.customEntities;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.util.Vector;

public class CustomEntityArrow extends EntityArrow {

    public CustomEntityArrow(World world, Vector a) { //"a" parameter is for matching this new arrow's movement vector with the old arrow's when it replaces the old arrow
        super(EntityTypes.ARROW, world);
        double x = a.getX(), y = a.getY(), z = a.getZ(); //similar code from projectile's shoot method; passing in a vec3d directly to this constructor doesn't seem to get the correct vector so must convert vector to vec3d instead
        Vec3D v = (new Vec3D(x, y, z).d());
        this.setMot(v);
    }

    @Override
    protected void h() {
        ++this.despawnCounter;
        if (this.despawnCounter >= 60) { //arrows despawn after 3 seconds to reduce lag and make them harder to pick up
            this.die();
        }
    }

    @Override
    protected ItemStack getItemStack()
    {
        return new ItemStack(Items.ARROW); //tipped arrows are now nonexistent
    }
}
