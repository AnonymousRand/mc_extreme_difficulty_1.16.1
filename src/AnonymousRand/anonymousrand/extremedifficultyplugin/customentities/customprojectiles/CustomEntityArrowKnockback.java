package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles;

import net.minecraft.server.v1_16_R1.*;

public class CustomEntityArrowKnockback extends CustomEntityArrow {

    public CustomEntityArrowKnockback(World world) {
        super(world);
    }

    @Override
    protected void a(MovingObjectPositionEntity movingobjectpositionentity) {
        super.a(movingobjectpositionentity);

        Entity entity = movingobjectpositionentity.getEntity();

        if (entity instanceof EntityPlayer) { //doesn't work all the time
            ((EntityPlayer)entity).setMot(new Vec3D(this.getMot().getX() * 1.5, -this.getMot().getY(), this.getMot().getZ() * 1.5));
        }
    }
}
