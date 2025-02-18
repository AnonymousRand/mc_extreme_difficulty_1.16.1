package AnonymousRand.anonymousrand.plugin.nms.customgoals;

import AnonymousRand.anonymousrand.plugin.util.bukkitrunnables.RunnableBreakBlocks;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.PathfinderGoal;

import java.util.Random;

public class CustomPathfinderGoalBreakBlocksAround extends PathfinderGoal { // custom goal to break blocks around a mob

    public EntityInsentient goalOwner;
    protected final int delayTimer, radX, radY, radZ, offsetY;
    private final boolean removeFluids;
    protected static final Random random = new Random();

    public CustomPathfinderGoalBreakBlocksAround(EntityInsentient goalOwner, int delayTimer, int radX, int radY, int radZ, int offsetY, boolean removeFluids) {
        this.goalOwner = goalOwner;
        this.delayTimer = delayTimer;
        this.radX = radX;
        this.radY = radY;
        this.radZ = radZ;
        this.offsetY = offsetY;
        this.removeFluids = removeFluids;
    }

    @Override
    public boolean a() { // should execute
        return this.goalOwner.getGoalTarget() != null && !this.goalOwner.getGoalTarget().isInvulnerable();
    }

    @Override
    public boolean b() { // should continue executing
        return this.a();
    }

    @Override
    public void e() { // tick
        if (this.goalOwner.ticksLived % this.delayTimer == 0) {
            new RunnableBreakBlocks(this.goalOwner, this.radX, this.radY, this.radZ, this.offsetY, this.removeFluids).run();
        }
    }
}
