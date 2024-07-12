package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.EnumSet;
import java.util.List;

public class CustomPathfinderGoalHurtByTarget<T extends EntityInsentient & ICustomHostile>
        extends CustomPathfinderGoalTarget<T> {

    private boolean doesEntityCallForHelp;
    private Class<?>[] reinforcementClasses;

    public CustomPathfinderGoalHurtByTarget(T goalOwner, Class<?>... reinforcementClasses) {

        this(goalOwner, goalOwner.ignoresLOS(), goalOwner.ignoresY(), reinforcementClasses);
    }

    public CustomPathfinderGoalHurtByTarget(
            T goalOwner,
            boolean ignoreLOS,
            boolean ignoreY,
            Class<?>... reinforcementClasses) {

        super(goalOwner, ignoreLOS, ignoreY);
        if (reinforcementClasses.length > 0) {
            this.doesEntityCallForHelp = true;
            this.reinforcementClasses = reinforcementClasses;
        }

        this.a(EnumSet.of(Type.TARGET));
    }

    // Mobs only retaliate against players
    @Override
    public boolean a() {
        if (!super.a()) {
            return false;
        }

        // teleport to target if outside detection range/has no line of sight
        double detectionRange = this.getDetectionRange();
        if (NMSUtil.distSq(this.goalOwner, this.potentialTarget, this.ignoreY) > detectionRange * detectionRange
                || !this.goalOwner.getEntitySenses().a(this.potentialTarget)) {
            if (!this.goalOwner.isSilent()) {
                this.goalOwner.playSound(SoundEffects.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
            this.goalOwner.enderTeleportTo(this.potentialTarget.locX(), this.potentialTarget.locY(),
                    this.potentialTarget.locZ());
            if (!this.goalOwner.isSilent()) {
                this.goalOwner.playSound(SoundEffects.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
        }

        return true;
    }

    @Override
    public void c() {
        super.c();

        this.goalOwner.setGoalTarget(this.potentialTarget, EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY, true);
        if (this.doesEntityCallForHelp && this.reinforcementClasses != null) {
            this.callForHelp();
        }
    }

    @Override
    protected EntityLiving findNearestPotentialTarget(boolean allowIgnoreY) {
        return this.goalOwner.getLastDamager();
    }

    // todo test eventually with like piglin or something
    protected void callForHelp() {
        double reinforcementRange = this.getDetectionRange();
        List<EntityInsentient> entitiesInRange = this.goalOwner.world.b(this.goalOwner.getClass(),
                AxisAlignedBB.a(this.goalOwner.getPositionVector()).grow(reinforcementRange, 10.0, reinforcementRange));

        // the original code here had 5 nested do-while loops
        // in unrelated news I'm typing this with one hand as I recover from my stroke
        for (EntityInsentient candidate : entitiesInRange) {
            boolean passedBaseChecks =
                    this.goalOwner != candidate
                    && candidate.getGoalTarget() == null
                    && !candidate.r(this.potentialTarget);
            if (!passedBaseChecks) {
                continue;
            }

            if (this.goalOwner instanceof EntityTameableAnimal && candidate instanceof EntityTameableAnimal) {
                if (((EntityTameableAnimal) this.goalOwner).getOwner().getUniqueID()
                        == ((EntityTameableAnimal) candidate).getOwner().getUniqueID()) {
                    continue;
                }
            }

            boolean isValidReinforcementClass = false;
            for (Class<?> reinforcementClass : this.reinforcementClasses) {
                if (candidate.getClass() == reinforcementClass) {
                    isValidReinforcementClass = true;
                    break;
                }
            }

            if (isValidReinforcementClass) {
                candidate.setGoalTarget(this.potentialTarget, EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY,
                        true);
            }
        }
    }
}
