package AnonymousRand.ExtremeDifficultyPlugin.customGoals;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

public class NewPathfinderGoalCobweb extends PathfinderGoal {

    protected EntityInsentient entity;

    public NewPathfinderGoalCobweb(EntityInsentient entity) {
        this.entity = entity;
    }

    @Override
    public boolean a() {
        return this.entity.ticksLived % 5 == 0;
    }

    @Override
    public boolean b() {
        return this.entity.ticksLived % 5 == 0;
    }

    @Override
    public void e() {
        AxisAlignedBB hitbox = this.entity.getBoundingBox();
        org.bukkit.World bukkitWorld = this.entity.getWorld().getWorld();

        for (int x = (int)hitbox.minX; x <= (int)Math.ceil(hitbox.maxX); x++) {
            for (int y = (int)hitbox.minY; y <= (int)Math.ceil(hitbox.maxY); y++) {
                for (int z = (int)hitbox.minZ; z <= (int)Math.ceil(hitbox.maxZ); z++) {
                    if (bukkitWorld.getBlockAt(x, y, z).getType() == org.bukkit.Material.COBWEB) { /**non-player mobs gain Speed 11 while in a cobweb (approx original speed)*/
                        this.entity.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, 5, 10));

                        if (this.entity.getGoalTarget() != null) { /**mobs also move faster vertically through cobwebs if their goal target is on a different y-level by 1 or more*/
                            if (this.entity.locY() - this.entity.getGoalTarget().locY() > 1.0) {
                                this.entity.setMot(0.0, -20.0, 0.0);
                            } else if (this.entity.locY() - this.entity.getGoalTarget().locY() < -1.0) {
                                this.entity.setMot(0.0, 20.0, 0.0);
                            }
                        }

                        return;
                    }
                }
            }
        }
    }
}
