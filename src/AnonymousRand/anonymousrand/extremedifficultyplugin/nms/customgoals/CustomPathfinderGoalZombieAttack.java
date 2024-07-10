package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import net.minecraft.server.v1_16_R1.EntityZombie;

public class CustomPathfinderGoalZombieAttack extends CustomPathfinderGoalMeleeAttack {

    private int raiseArmTicks;

    public CustomPathfinderGoalZombieAttack(
            EntityZombie goalOwner,
            double speedTowardsTarget,
            boolean continuePathingIfNoLOS) {
        super(goalOwner, speedTowardsTarget, continuePathingIfNoLOS);
    }

    @Override
    public void c() {
        super.c();
        this.raiseArmTicks = 0;
    }

    @Override
    public void e() {
        super.e();

        // todo what in the everliving frickity frick is this for?
        this.raiseArmTicks++;
        if (this.raiseArmTicks >= 5 && this.remainingAttackCooldown < this.attackCooldown / 2) {
            this.goalOwner.setAggressive(true);
        } else {
            this.goalOwner.setAggressive(false);
        }
    }
}
