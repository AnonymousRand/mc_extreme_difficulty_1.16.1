package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableBreakBlocks;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.PathfinderGoal;

import java.util.Random;

public class CustomPathfinderGoalBreakBlocksAround extends PathfinderGoal { // custom goal to break blocks around a mob

    public EntityInsentient entity;
    protected final int delayTimer, radX, radY, radZ, offsetY;
    private final boolean removeFluids;
    protected static final Random random = new Random();

    public CustomPathfinderGoalBreakBlocksAround(EntityInsentient entity, int delayTimer, int radX, int radY, int radZ, int offsetY, boolean removeFluids) {
        this.entity = entity;
        this.delayTimer = delayTimer;
        this.radX = radX;
        this.radY = radY;
        this.radZ = radZ;
        this.offsetY = offsetY;
        this.removeFluids = removeFluids;
    }

    @Override
    public boolean a() { // should execute
        return this.entity.getGoalTarget() != null && !this.entity.getGoalTarget().isInvulnerable();
    }

    @Override
    public boolean b() { // should continue executing
        return this.a();
    }

    @Override
    public void e() { // tick
        if (this.entity.ticksLived % this.delayTimer == 0) {
            new RunnableBreakBlocks(this.entity, this.radX, this.radY, this.radZ, this.offsetY, this.removeFluids).run();
        }
    }
}
