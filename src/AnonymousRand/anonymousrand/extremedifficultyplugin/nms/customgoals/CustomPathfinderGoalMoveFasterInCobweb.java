package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import net.minecraft.server.v1_16_R1.*;

public class CustomPathfinderGoalMoveFasterInCobweb extends PathfinderGoal {

    public EntityInsentient goalOwner;

    public CustomPathfinderGoalMoveFasterInCobweb(EntityInsentient goalOwner) {
        this.goalOwner = goalOwner;
    }

    @Override
    public boolean a() {
        return this.goalOwner.ticksLived % 5 == 0;
    }

    @Override
    public boolean b() {
        return this.a();
    }

    @Override
    public void e() {
        AxisAlignedBB hitbox = this.goalOwner.getBoundingBox();
        org.bukkit.World bukkitWorld = this.goalOwner.getWorld().getWorld();

        for (int x = (int) hitbox.minX; x <= (int) Math.ceil(hitbox.maxX); x++) {
            for (int y = (int) hitbox.minY; y <= (int) Math.ceil(hitbox.maxY); y++) {
                for (int z = (int) hitbox.minZ; z <= (int) Math.ceil(hitbox.maxZ); z++) {
                    if (bukkitWorld.getBlockAt(x - 1, y, z - 1).getType() == org.bukkit.Material.COBWEB) { // the hitboxes seem to be off so I need x - 1 and z - 1
                        this.goalOwner.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, 6, 10)); /* non-player mobs gain Speed 11 while in a cobweb (approx original speed) */

                        if (this.goalOwner.getGoalTarget() != null) { /* mobs also move faster vertically and horizontally through cobwebs if their attack target is on a different y-level by 1 or more */
                            EntityLiving attackTarget = this.goalOwner.getGoalTarget();

                            if (this.goalOwner.locY() - attackTarget.locY() > 1) {
                                this.goalOwner.setMot((attackTarget.locX() - this.goalOwner.locX()) / 2.0, -40.0, (attackTarget.locZ() - this.goalOwner.locZ()) / 2.0);
                            } else if (this.goalOwner.locY() - attackTarget.locY() < -1) {
                                this.goalOwner.setMot((attackTarget.locX() - this.goalOwner.locX()) / 2.0, 40.0, (attackTarget.locZ() - this.goalOwner.locZ()) / 2.0);
                            }
                        }

                        return;
                    }
                }
            }
        }
    }
}
