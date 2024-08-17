package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import net.minecraft.server.v1_16_R1.*;

public class CustomPathfinderGoalMoveFasterInCobweb extends PathfinderGoal {

    public EntityInsentient entity;

    public CustomPathfinderGoalMoveFasterInCobweb(EntityInsentient entity) {
        this.entity = entity;
    }

    @Override
    public boolean a() {
        return this.entity.ticksLived % 5 == 0;
    }

    @Override
    public boolean b() {
        return this.a();
    }

    @Override
    public void e() {
        AxisAlignedBB hitbox = this.entity.getBoundingBox();
        org.bukkit.World bukkitWorld = this.entity.getWorld().getWorld();

        for (int x = (int) hitbox.minX; x <= (int) Math.ceil(hitbox.maxX); x++) {
            for (int y = (int) hitbox.minY; y <= (int) Math.ceil(hitbox.maxY); y++) {
                for (int z = (int) hitbox.minZ; z <= (int) Math.ceil(hitbox.maxZ); z++) {
                    if (bukkitWorld.getBlockAt(x - 1, y, z - 1).getType() == org.bukkit.Material.COBWEB) { // the hitboxes seem to be off so I need x - 1 and z - 1
                        this.entity.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, 6, 10)); /* non-player mobs gain Speed 11 while in a cobweb (approx original speed) */

                        if (this.entity.getGoalTarget() != null) { /* mobs also move faster vertically and horizontally through cobwebs if their goal target is on a different y-level by 1 or more */
                            EntityLiving target = this.entity.getGoalTarget();

                            if (this.entity.locY() - target.locY() > 1) {
                                this.entity.setMot((target.locX() - this.entity.locX()) / 2.0, -40.0, (target.locZ() - this.entity.locZ()) / 2.0);
                            } else if (this.entity.locY() - target.locY() < -1) {
                                this.entity.setMot((target.locX() - this.entity.locX()) / 2.0, 40.0, (target.locZ() - this.entity.locZ()) / 2.0);
                            }
                        }

                        return;
                    }
                }
            }
        }
    }
}
