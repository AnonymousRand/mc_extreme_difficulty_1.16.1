package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.projectiles.CustomEntityLargeFireball;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.PathfinderGoal;
import net.minecraft.server.v1_16_R1.Vec3D;
import org.bukkit.util.Vector;

public class CustomPathfinderGoalShootLargeFireballs extends PathfinderGoal {

    private final EntityInsentient entity;
    private final int attackCooldown, intYield;
    private final boolean summonLightning;

    public CustomPathfinderGoalShootLargeFireballs(EntityInsentient entity, int attackCooldown, int intYield, boolean summonLightning) {
        this.entity = entity;
        this.attackCooldown = attackCooldown;
        this.intYield = intYield;
        this.summonLightning = summonLightning;
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
        if (this.entity.ticksLived % this.attackCooldown == 11) { // todo what is this 11 about
            EntityLiving entityLiving;

            if (this.entity.getGoalTarget() != null) {
                entityLiving = this.entity.getGoalTarget();
            } else {
                return;
            }

            Vec3D vec3d = this.entity.f(1.0F);
            double d2 = entityLiving.locX() - (this.entity.locX() + vec3d.x * 4.0D);
            double d3 = entityLiving.e(0.5D) - (0.5D + this.entity.e(0.5D));
            double d4 = entityLiving.locZ() - (this.entity.locZ() + vec3d.z * 4.0D);

            CustomEntityLargeFireball largeFireball = new CustomEntityLargeFireball(this.entity.getWorld(), this.entity, d2, d3, d4, this.intYield, this.summonLightning);
            largeFireball.setPosition(this.entity.locX() + vec3d.x * 4.0D, this.entity.e(0.5D) + 0.5D, largeFireball.locZ() + vec3d.z * 4.0D);
            this.entity.getWorld().addEntity(largeFireball);
            largeFireball.getBukkitEntity().setVelocity(largeFireball.getBukkitEntity().getVelocity().add(new Vector(d2 / 3.0, d3 / 3.0, d4 / 3.0))); /* fireballs move much faster */
        }
    }
}
