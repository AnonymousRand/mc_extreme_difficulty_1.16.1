package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;

import java.util.EnumSet;

// not extending PathfinderGoalArrowAttack as I normally would due to unused private fields like the goal target (obfuscated as this.c)
// todo uncomment once all have been converted
public class CustomPathfinderGoalRangedAttack<T extends EntityInsentient & IRangedEntity
        & ICustomHostile /* & IAttackLevelingMob*/> extends PathfinderGoal {

    protected final T goalOwner;
    protected final double speedTowardsTarget;
    protected int attackCooldown;
    protected int remainingAttackCooldown;
    protected int seeTime;

    public CustomPathfinderGoalRangedAttack(T goalOwner, double speedTowardsTarget, int attackCooldown) {
        this.goalOwner = goalOwner;
        this.speedTowardsTarget = speedTowardsTarget;
        this.attackCooldown = attackCooldown;
        this.remainingAttackCooldown = -1;
        this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
    }

    @Override
    public boolean a() {
        EntityLiving goalTarget = this.goalOwner.getGoalTarget();
        return goalTarget != null && !goalTarget.isSpectator() && !goalTarget.isInvulnerable() && goalTarget.isAlive();
    }

    @Override
    public boolean b() {
        return this.a() || !this.goalOwner.getNavigation().m(); // todo what happens if we just do a()
    }

    @Override
    public void d() {
        this.seeTime = 0;
        this.remainingAttackCooldown = -1;
    }

    @Override
    public void e() {
        this.remainingAttackCooldown--;

        EntityLiving goalTarget = this.goalOwner.getGoalTarget();
        if (goalTarget == null) {
            return;
        }

        this.goalOwner.getControllerLook().a(goalTarget, 30.0F, 30.0F);
        double distSqToGoalTarget = NMSUtil.distSq(this.goalOwner, goalTarget, false);

        // determine whether to repath or to stand still (repaths if can't see and ignoreLOS is false)
        if (this.goalOwner.getEntitySenses().a(goalTarget) || this.goalOwner.ignoresLOS()) {
            this.seeTime++;
        } else {
            this.seeTime = 0;
        }
        Bukkit.broadcastMessage("see time: " + this.seeTime);
        if (this.seeTime >= 5) {
            this.goalOwner.getNavigation().o();                                    // clearPath() todo stands still?
        } else {
            this.goalOwner.getNavigation().a(goalTarget, this.speedTowardsTarget); // tryMoveTo()
        }

        // attack
        if (this.remainingAttackCooldown <= 0) {
            this.remainingAttackCooldown = this.attackCooldown;
            // this.entity.increaseAttacks(1); // todo uncomment once all have been converted
            float distanceFactor = (float) MathHelper.a(
                    MathHelper.sqrt(distSqToGoalTarget) / this.goalOwner.getDetectionRange(), 0.1, 1.0);
            this.goalOwner.a(goalTarget, distanceFactor); // shoot()
        }
    }
}
