package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.util.EntityFilter;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;

import java.util.EnumSet;
import java.util.Random;

// Entire class rewritten instead of inherited from PathfinderGoalMeleeAttack in order to apply our own logic
// without being too hacky or needing too much reflection (everything's private :/)
public class CustomPathfinderGoalMeleeAttack<T extends EntityInsentient & ICustomHostile> extends PathfinderGoal {

    protected final T goalOwner;
    protected int attackCooldown;
    protected int remainingAttackCooldown;
    protected double minAttackReach;

    public CustomPathfinderGoalMeleeAttack(T goalOwner) {
        // default minimum attack reach is 2.0 blocks to help baby zombies out
        this(goalOwner, 20, 2.0);
    }

    public CustomPathfinderGoalMeleeAttack(
            T goalOwner,
            int attackCooldown,
            double minAttackReach) {

        this.goalOwner = goalOwner;
        this.attackCooldown = attackCooldown;
        this.remainingAttackCooldown = attackCooldown;
        this.minAttackReach = minAttackReach;
    }

    @Override
    public boolean a() {
        return EntityFilter.BASE.test(this.goalOwner.getGoalTarget());
    }
    
    @Override
    public boolean b() {
        return this.a();
    }

    @Override
    public void c() {
        this.goalOwner.setAggressive(true);
    }

    @Override
    public void d() {
        this.goalOwner.setAggressive(false);
    }

    @Override
    public void e() {
        this.remainingAttackCooldown--;

        EntityLiving goalTarget = this.goalOwner.getGoalTarget();
        if (goalTarget == null) {
            return;
        }

        if (this.remainingAttackCooldown <= 0
                && NMSUtil.distSq(this.goalOwner, goalTarget, false) <= this.getAttackReachSq(goalTarget)) {
            this.remainingAttackCooldown = this.attackCooldown;
            this.goalOwner.swingHand(EnumHand.MAIN_HAND);
            this.goalOwner.attackEntity(goalTarget);
        }
    }

    protected double getAttackReachSq(EntityLiving goalTarget) {
        return Math.max(Math.pow(2.0F * this.goalOwner.getWidth(), 2) + goalTarget.getWidth(),
                this.minAttackReach);
    }

    public void setAttackCooldown(int attackCooldown) {
        this.attackCooldown = attackCooldown;
    }

    public void setMinAttackReach(double minAttackReach) {
        this.minAttackReach = minAttackReach;
    }
}
