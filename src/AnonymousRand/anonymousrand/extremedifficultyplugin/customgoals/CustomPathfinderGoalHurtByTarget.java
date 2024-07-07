package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.event.entity.EntityTargetEvent;

public class CustomPathfinderGoalHurtByTarget extends PathfinderGoalHurtByTarget {

    public CustomPathfinderGoalHurtByTarget(EntityCreature entityCreature, Class<?>... aclass) {
        super(entityCreature, aclass);
    }

    @Override
    public boolean a() {
        super.a();
// todo test if we dont need the super
        if (this.e.getLastDamager() instanceof EntityPlayer) { // only retaliates against players in survival
            return !((EntityPlayer) this.e.getLastDamager()).abilities.isInvulnerable;
        }

        return false;
    }

    @Override // setGoalTarget()
    protected void a(EntityInsentient entityInsentient, EntityLiving entityLiving) { // some mobs like bees use this method instead
        if (entityLiving instanceof EntityPlayer) {
            entityInsentient.setGoalTarget(entityLiving, EntityTargetEvent.TargetReason.TARGET_ATTACKED_NEARBY_ENTITY, true);
        }
    }

    @Override
    public CustomPathfinderGoalHurtByTarget a(Class<?>... aclass) {
        super.a(aclass);
        return this;
    }
}