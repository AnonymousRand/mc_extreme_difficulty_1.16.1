package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;

import java.util.EnumSet;

public class CustomPathfinderGoalRangedCrossbowAttack<T extends EntityMonster & IRangedEntity & ICrossbow> extends PathfinderGoalCrossbowAttack {

    protected final T entity;
    protected CustomPathfinderGoalRangedCrossbowAttack.State crossbowState;
    protected final double speedTowardsTarget;
    protected int attackInterval;
    protected float maxAttackDistSq;
    protected int seeTime;
    protected int updatePathDelay;

    public CustomPathfinderGoalRangedCrossbowAttack(T t0, double speedTowardsTarget, int attackInterval, float maxDistance) {
        super(t0, speedTowardsTarget, maxDistance);
        this.crossbowState = State.UNCHARGED;
        this.entity = t0;
        this.speedTowardsTarget = speedTowardsTarget;
        this.attackInterval = attackInterval;
        this.maxAttackDistSq = maxDistance * maxDistance;
        this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
    }

    private boolean j() {
        return this.crossbowState == State.UNCHARGED;
    }

    @Override
    public void e() {
        EntityLiving attackTarget = this.entity.getGoalTarget();
        if (attackTarget == null) {
            return;
        }

        double distanceToSquared = NMSUtil.distSqIgnoreY(this.entity, attackTarget);
        /* breaking line of sight does not stop the mob from attacking */
        ++this.seeTime;
        boolean flag2 = (distanceToSquared > (double) this.maxAttackDistSq || this.seeTime < 5) && this.crossbowState == State.UNCHARGED;

        if (flag2) {
            --this.updatePathDelay;
            if (this.updatePathDelay <= 0) {
                this.entity.getNavigation().a(attackTarget, this.j() ? this.speedTowardsTarget : this.speedTowardsTarget * 0.5D);
                this.updatePathDelay = CustomPathfinderGoalRangedCrossbowAttack.a.a(this.entity.getRandom());
            }
        } else {
            this.updatePathDelay = 0;
            this.entity.getNavigation().o();
        }

        this.entity.getControllerLook().a(attackTarget, 30.0F, 30.0F);
        if (this.crossbowState == State.UNCHARGED) {
            if (!flag2) {
                this.entity.c(ProjectileHelper.a(this.entity, Items.CROSSBOW));
                this.crossbowState = State.CHARGING;
                (this.entity).b(true);
            }
        } else if (this.crossbowState == State.CHARGING) {
            if (!this.entity.isHandRaised()) {
                this.crossbowState = State.UNCHARGED;
            }

            int ticksUsingItem = this.entity.dZ();

            if (ticksUsingItem >= this.attackInterval - 1) { // attack speed is now controllable with attribute instead of fixed
                                                             // -1 since extra tick for READY_TO_ATTACK (and it's necessary for animation)
                this.entity.releaseActiveItem();
                this.crossbowState = State.READY_TO_ATTACK;
                (this.entity).b(false);
            }
        } else if (this.crossbowState == State.READY_TO_ATTACK) {
            // skip straight past CHARGED since that incurs extra delays
            (this.entity).a(attackTarget, 1.0F); // shoot()
            ItemStack itemstack1 = this.entity.b(ProjectileHelper.a(this.entity, Items.CROSSBOW));

            ItemCrossbow.a(itemstack1, false);
            this.crossbowState = State.UNCHARGED;
        }
    }

    enum State {
        UNCHARGED, CHARGING, READY_TO_ATTACK;
        State() {}
    }
}
