package AnonymousRand.anonymousrand.plugin.nms.customgoals;

import AnonymousRand.anonymousrand.plugin.nms.customentities.projectiles.CustomEntityLargeFireball;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.PathfinderGoal;
import net.minecraft.server.v1_16_R1.Vec3D;
import org.bukkit.util.Vector;

public class CustomPathfinderGoalShootLargeFireballs extends PathfinderGoal {

    private final EntityInsentient goalOwner;
    private final int attackCooldown, intYield;
    private final boolean summonLightning;

    public CustomPathfinderGoalShootLargeFireballs(EntityInsentient goalOwner, int attackCooldown, int intYield, boolean summonLightning) {
        this.goalOwner = goalOwner;
        this.attackCooldown = attackCooldown;
        this.intYield = intYield;
        this.summonLightning = summonLightning;
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
        if (this.goalOwner.ticksLived % this.attackCooldown == 11) { // todo what is this 11 about
            EntityLiving entityLiving;

            if (this.goalOwner.getGoalTarget() != null) {
                entityLiving = this.goalOwner.getGoalTarget();
            } else {
                return;
            }

            Vec3D vec3d = this.goalOwner.f(1.0F);
            double d2 = entityLiving.locX() - (this.goalOwner.locX() + vec3d.x * 4.0);
            double d3 = entityLiving.e(0.5) - (0.5 + this.goalOwner.e(0.5));
            double d4 = entityLiving.locZ() - (this.goalOwner.locZ() + vec3d.z * 4.0);

            CustomEntityLargeFireball largeFireball = new CustomEntityLargeFireball(this.goalOwner.getWorld(), this.goalOwner, d2, d3, d4, this.intYield, this.summonLightning);
            largeFireball.setPosition(this.goalOwner.locX() + vec3d.x * 4.0, this.goalOwner.e(0.5) + 0.5, largeFireball.locZ() + vec3d.z * 4.0);
            this.goalOwner.getWorld().addEntity(largeFireball);
            largeFireball.getBukkitEntity().setVelocity(largeFireball.getBukkitEntity().getVelocity().add(new Vector(d2 / 3.0, d3 / 3.0, d4 / 3.0))); /* fireballs move much faster */
        }
    }
}
