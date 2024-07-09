package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.EntitySlime;
import net.minecraft.server.v1_16_R1.EnumHand;

public class NewPathfinderGoalSlimeMeleeAttack extends NewPathfinderGoalPassiveMeleeAttack {

    public NewPathfinderGoalSlimeMeleeAttack(EntitySlime entity, double d0) {
        super(entity, d0);
    }

    @Override
    protected void a(EntityLiving entityLiving, double d0) {
        double d1 = this.a(entityLiving);

        if (d0 <= d1 && this.i <= 0) {
            if (this.entity instanceof ICustomHostile) {
                if (NMSUtil.distSq(this.entity, entityLiving) > d1 && random.nextDouble() < 0.875) { /* mobs can only successfully hit you occasionally when they are very distant vertically */
                    return;
                }
            }

            this.g();
            this.entity.swingHand(EnumHand.MAIN_HAND);
            this.entity.attackEntity(entityLiving);
        }
    }

    @Override
    protected double a(EntityLiving entityLiving) {
        return Math.max(Math.log(this.entity.getWidth()) * 4.0F * Math.log(this.entity.getWidth()) * 4.0F + Math.log(this.entity.getWidth()), 9.0);
    }
}
