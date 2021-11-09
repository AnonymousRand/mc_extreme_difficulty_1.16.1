package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.util.Vector;

public class CustomEntityArrowKnockback extends CustomEntityArrow {

    public CustomEntityArrowKnockback(World world) {
        super(world);
    }

    @Override
    protected void a(MovingObjectPositionEntity movingobjectpositionentity) {
        super.a(movingobjectpositionentity);

        Entity entity = movingobjectpositionentity.getEntity();

        if (entity instanceof EntityPlayer) {
            ((EntityPlayer)entity).setMot(new Vec3D(this.getMot().getX() * 2.0, -this.getMot().getY(), this.getMot().getZ() * 2.0));
        }
    }
}
