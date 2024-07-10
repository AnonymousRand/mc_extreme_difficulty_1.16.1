package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.EnumSet;
import java.util.List;

public class CustomPathfinderGoalHurtByTarget extends CustomPathfinderGoalTarget {

    private boolean doesEntityCallForHelp;
    private Class<?>[] reinforcementClasses;

    public CustomPathfinderGoalHurtByTarget(
            EntityInsentient goalOwner,
            boolean ignoreLOS,
            boolean ignoreY,
            Class<?>... reinforcementClasses) {

        super(goalOwner, false, ignoreLOS, ignoreY);

        if (reinforcementClasses.length > 0) {
            this.doesEntityCallForHelp = true;
            this.reinforcementClasses = reinforcementClasses;
        }

        this.a(EnumSet.of(Type.TARGET));
    }

    // Mobs only retaliate against players in survival
    @Override
    public boolean a() {
        EntityLiving revengeTarget = this.e.getLastDamager();

        boolean isValidPlayer = revengeTarget instanceof EntityPlayer
                && !((EntityPlayer) revengeTarget).abilities.isInvulnerable;
        if (!isValidPlayer) {
            return false;
        }

        // teleport to target if outside detection range/has no line of sight
        double detectionRange = this.k();
        if (NMSUtil.distSq(this.e, revengeTarget, this.ignoreY) > detectionRange * detectionRange
                || !this.e.getEntitySenses().a(revengeTarget)) {
            if (!this.e.isSilent()) {
                this.e.playSound(SoundEffects.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
            this.e.enderTeleportTo(revengeTarget.locX(), revengeTarget.locY(), revengeTarget.locZ());
            if (!this.e.isSilent()) {
                this.e.playSound(SoundEffects.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
        }

        return true;
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

    // todo test eventually with like piglin or something
    protected void callForHelp() {
        double reinforcementRange = this.k();
        List<EntityInsentient> entitiesInRange = this.e.world.b(this.e.getClass(),
                AxisAlignedBB.a(this.e.getPositionVector()).grow(reinforcementRange, 10.0, reinforcementRange));

        // the original code here had 5 nested do-while loops
        // in unrelated news I'm typing this with one hand as I recover from my stroke
        for (EntityInsentient entity : entitiesInRange) {
            boolean passedBasicChecks =
                    this.e != entity
                    && entity.getGoalTarget() == null
                    && (!(this.e instanceof EntityTameableAnimal)
                        || ((EntityTameableAnimal) this.e).getOwner() == ((EntityTameableAnimal) entity).getOwner())
                    && !entity.r(this.e.getLastDamager());
            if (!passedBasicChecks) {
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
