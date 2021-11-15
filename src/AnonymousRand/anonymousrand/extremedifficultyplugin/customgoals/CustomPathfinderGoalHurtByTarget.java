package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.event.entity.EntityTargetEvent;

public class CustomPathfinderGoalHurtByTarget extends PathfinderGoalHurtByTarget {

    public CustomPathfinderGoalHurtByTarget(EntityCreature entitycreature, Class<?>... aclass) {
        super(entitycreature, aclass);
    }

    @Override
    public boolean a() {
        super.a();

        if (this.e.getLastDamager() instanceof EntityPlayer) { //only retaliates against players in survival
            return !((EntityPlayer)this.e.getLastDamager()).abilities.isInvulnerable;
        }

        return false;
    }

    @Override
    protected void a(EntityInsentient entityinsentient, EntityLiving entityliving) { //some mobs like bees use this method instead
        if (entityliving instanceof EntityPlayer) {
            entityinsentient.setGoalTarget(entityliving, EntityTargetEvent.TargetReason.TARGET_ATTACKED_NEARBY_ENTITY, true);
        }
    }

    @Override
    public CustomPathfinderGoalHurtByTarget a(Class<?>... aclass) {
        super.a(aclass);
        return this;
    }
}
