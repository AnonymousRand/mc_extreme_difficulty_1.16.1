package AnonymousRand.ExtremeDifficultyPlugin.customEntities;

import net.minecraft.server.v1_16_R1.*;

public class CustomEntityBoat extends EntityBoat {

    public CustomEntityBoat(World world, double d0, double d1, double d2) {
        super(EntityTypes.BOAT, world);
        this.setPosition(d0, d1, d2);
        this.setMot(Vec3D.a);
        this.lastX = d0;
        this.lastY = d1;
        this.lastZ = d2;
    }

    @Override
    protected boolean q(Entity entity) {
        return this.getPassengers().size() < 2 && !this.a((Tag) TagsFluid.WATER) && entity instanceof EntityPlayer; //only players can get in boats (to prevent trapping troublesome mobs like endermen in boats)
    }
}
