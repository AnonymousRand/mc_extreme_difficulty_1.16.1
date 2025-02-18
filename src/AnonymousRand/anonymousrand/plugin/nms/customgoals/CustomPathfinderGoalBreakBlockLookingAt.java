package AnonymousRand.anonymousrand.plugin.nms.customgoals;

import AnonymousRand.anonymousrand.plugin.util.bukkitrunnables.RunnableBreakBlocks;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.PathfinderGoal;
import org.bukkit.entity.LivingEntity;

public class CustomPathfinderGoalBreakBlockLookingAt extends PathfinderGoal {

    private final EntityInsentient goalOwner;

    public CustomPathfinderGoalBreakBlockLookingAt(EntityInsentient goalOwner) {
        this.goalOwner = goalOwner;
    }

    @Override
    public boolean a() {
        return this.goalOwner.getGoalTarget() != null;
    }

    @Override
    public boolean b() {
        return this.a();
    }

    @Override
    public void e() {
        if (this.goalOwner.ticksLived % 80 == 0) {
            new RunnableBreakBlocks(((LivingEntity) this.goalOwner.getBukkitEntity()).getTargetBlock(null, 40).getLocation(), this.goalOwner.getWorld().getWorld(), 0, 0, 0, 0, false).run();
        }
    }
}
