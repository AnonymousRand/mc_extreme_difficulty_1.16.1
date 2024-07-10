package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableBreakBlocks;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.PathfinderGoal;
import org.bukkit.entity.LivingEntity;

public class NewPathfinderGoalBreakBlockLookingAt extends PathfinderGoal {

    private final EntityInsentient entity;

    public NewPathfinderGoalBreakBlockLookingAt(EntityInsentient entity) {
        this.entity = entity;
    }

    @Override
    public boolean a() {
        return this.entity.getGoalTarget() != null;
    }

    @Override
    public boolean b() {
        return this.a();
    }

    @Override
    public void e() {
        if (this.entity.ticksLived % 80 == 0) {
            new RunnableBreakBlocks(((LivingEntity) this.entity.getBukkitEntity()).getTargetBlock(null, 40).getLocation(), this.entity.getWorld().getWorld(), 0, 0, 0, 0, false).run();
        }
    }
}