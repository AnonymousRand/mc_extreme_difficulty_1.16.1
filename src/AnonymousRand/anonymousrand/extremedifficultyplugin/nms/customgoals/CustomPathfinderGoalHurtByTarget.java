package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.EnumSet;
import java.util.List;

public class CustomPathfinderGoalHurtByTarget extends CustomPathfinderGoalTarget {

    private boolean doesEntityCallForHelp;
    private Class<?>[] reinforcementClasses;

    public CustomPathfinderGoalHurtByTarget(EntityCreature goalOwner, Class<?>... reinforcementClasses) {
        // needSightToMaintainTarget (checkSight) is always false, meaning we never need sight to continue tracking a target as the goalTarget (checked in shouldContinueExecuting())
        super(goalOwner, false, false);

        if (reinforcementClasses.length > 0) {
            this.doesEntityCallForHelp = true;
            this.reinforcementClasses = reinforcementClasses;
        }

        this.a(EnumSet.of(Type.TARGET));
    }

    @Override
    // Mobs only retaliate against players in survival
    public boolean a() {
        return (this.e.getLastDamager() instanceof EntityPlayer
                && !((EntityPlayer) this.e.getLastDamager()).abilities.isInvulnerable);
    }

    @Override
    public void c() {
        this.e.setGoalTarget(this.e.getLastDamager(), EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY, true);
        this.g = this.e.getGoalTarget();
        if (this.doesEntityCallForHelp && this.reinforcementClasses != null) {
            this.callForHelp();
        }

        super.c();
    }

    protected void callForHelp() {
        double reinforcementRange = this.k();
        List<EntityInsentient> entitiesInRange = this.e.world.b(this.e.getClass(),
                AxisAlignedBB.a(this.e.getPositionVector()).grow(reinforcementRange, 10.0, reinforcementRange));

        // the original code here had 5 nested do-while loops
        // in unrelated news I'm typing this with one hand as I recover from my stroke
        for (EntityInsentient entity : entitiesInRange) {
            boolean doNotConsider =
                    this.e == entity
                    || entity.getGoalTarget() != null
                    || (this.e instanceof EntityTameableAnimal)
                    && ((EntityTameableAnimal) this.e).getOwner() != ((EntityTameableAnimal) entity).getOwner()
                    || entity.r(this.e.getLastDamager());
            if (doNotConsider) {
                continue;
            }

            boolean isValidReinforcementClass = false;
            for (Class<?> reinforcementClass : this.reinforcementClasses) {
                if (entity.getClass() == reinforcementClass) {
                    isValidReinforcementClass = true;
                    break;
                }
            }

            if (isValidReinforcementClass) {
                entity.setGoalTarget(this.e.getLastDamager(), EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY, true);
            }
        }
    }
}