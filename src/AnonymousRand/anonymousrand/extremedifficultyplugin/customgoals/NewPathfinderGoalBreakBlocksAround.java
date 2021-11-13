package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableBreakBlocks;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class NewPathfinderGoalBreakBlocksAround extends PathfinderGoal { //custom goal to break blocks around a mob

    public EntityInsentient entity;
    protected final int delayTimer, radX, radY, radZ, yOffset;
    private final boolean removeFluids;
    protected static final Random random = new Random();

    public NewPathfinderGoalBreakBlocksAround(EntityInsentient entity, int delayTimer, int radX, int radY, int radZ, int yOffset, boolean removeFluids) {
        this.entity = entity;
        this.delayTimer = delayTimer;
        this.radX = radX;
        this.radY = radY;
        this.radZ = radZ;
        this.yOffset = yOffset;
        this.removeFluids = removeFluids;
    }

    @Override
    public boolean a() { //should execute
        return this.entity.getGoalTarget() != null && !this.entity.getGoalTarget().isInvulnerable();
    }

    @Override
    public boolean b() { //should continue executing
        return this.a();
    }

    @Override
    public void e() { //tick
        if (this.entity.ticksLived % this.delayTimer == 0) {
            new RunnableBreakBlocks(this.entity, this.radX, this.radY, this.radZ, this.yOffset, this.removeFluids).run();
        }
    }
}
