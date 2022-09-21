package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.ICustomMob;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;

import java.util.EnumSet;

public class NewPathfinderGoalSlimeMeleeAttack extends NewPathfinderGoalPassiveMeleeAttack {
    public NewPathfinderGoalSlimeMeleeAttack(EntitySlime entity, double d0, boolean flag) {
        super(entity, d0, flag);
    }

    @Override
    protected void a(EntityLiving entityliving, double d0) {
        double d1 = this.a(entityliving);

        if (d0 <= d1 && this.i <= 0) {
            if (this.entity instanceof ICustomMob) {
                if (((ICustomMob)this.entity).getNormalDistanceSq(this.entity.getPositionVector(), entityliving.getPositionVector()) > d1 && random.nextDouble() < 0.875) { /** mobs can only successfully hit you occasionally when they are very distant vertically */
                    return;
                }
            }

            this.g();
            this.entity.swingHand(EnumHand.MAIN_HAND);
            this.entity.attackEntity(entityliving);
        }
    }

    @Override
    protected double a(EntityLiving entityliving) {
        return Math.max(Math.log(this.entity.getWidth()) * 4.0F * Math.log(this.entity.getWidth()) * 4.0F + Math.log(this.entity.getWidth()), 9.0);
    }
}
