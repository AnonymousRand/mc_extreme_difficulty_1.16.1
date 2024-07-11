package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.EntitySlime;
import net.minecraft.server.v1_16_R1.EnumHand;

// todo test this rewriting works
public class NewPathfinderGoalSlimeMeleeAttack<T extends EntitySlime & ICustomHostile>
        extends CustomPathfinderGoalMeleeAttack<T> {

    public NewPathfinderGoalSlimeMeleeAttack(T slime) {
        super(slime);
    }

    /* Slimes have attack reach that scales logarithmically with its size, capped at 9.0 */
    @Override // todo test min instead of max, and cleaner math (wtf is this?)
    protected double getAttackReachSq(EntityLiving goalTarget) {
        double base =
                Math.log(Math.pow(4.0F * this.goalOwner.getWidth(), 2) + Math.log(this.goalOwner.getWidth()));
        return Math.min(Math.max(base, this.minAttackReach), 9.0);
    }
}
