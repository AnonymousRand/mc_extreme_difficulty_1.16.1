package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.PathfinderGoal;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import java.util.Random;

public class NewPathfinderGoalBreakBlockLookingAt extends PathfinderGoal {

    private final EntityInsentient entity;
    private final Random random = new Random();

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
        if (this.entity.ticksLived % 60 == 0) {
            Block block = ((LivingEntity)this.entity.getBukkitEntity()).getTargetBlock(null, 40);
            org.bukkit.Material type = block.getType();

            if (type != org.bukkit.Material.BEDROCK && type != org.bukkit.Material.END_GATEWAY && type != org.bukkit.Material.END_PORTAL && type != org.bukkit.Material.END_PORTAL_FRAME && type != org.bukkit.Material.NETHER_PORTAL && type != org.bukkit.Material.COMMAND_BLOCK  && type != org.bukkit.Material.COMMAND_BLOCK_MINECART && type != org.bukkit.Material.STRUCTURE_BLOCK && type != org.bukkit.Material.JIGSAW && type != org.bukkit.Material.BARRIER && type != org.bukkit.Material.SPAWNER && type != org.bukkit.Material.COBWEB && type != org.bukkit.Material.OBSIDIAN && type != org.bukkit.Material.CRYING_OBSIDIAN && type != org.bukkit.Material.ANCIENT_DEBRIS && type != org.bukkit.Material.NETHERITE_BLOCK && type != org.bukkit.Material.WATER && type != org.bukkit.Material.LAVA) { //as long as it isn't one of these blocks
                block.setType(org.bukkit.Material.AIR);
            } else if (type == org.bukkit.Material.OBSIDIAN || type == org.bukkit.Material.CRYING_OBSIDIAN || type == org.bukkit.Material.ANCIENT_DEBRIS || type == org.bukkit.Material.NETHERITE_BLOCK) { //50% chance to break these blocks
                if (this.random.nextDouble() < 0.5) {
                    block.setType(org.bukkit.Material.AIR);
                }
            }
        }
    }
}
