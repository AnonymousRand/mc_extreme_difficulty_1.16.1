package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderTargetCondition;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.*;

public class CustomEntityBee extends EntityBee {

    public CustomEntityBee(World world) {
        super(EntityTypes.BEE, world);
        for (Field f : EntityBee.class.getDeclaredFields()) {
            f.setAccessible(true);
        }
    }

    @Override
    public void tick() {
        super.tick();

        /**always aggro at players within 12 blocks*/
        EntityPlayer player = this.getWorld().a(EntityPlayer.class, new CustomPathfinderTargetCondition(), this, this.locX(), this.locY(), this.locZ(), this.getBoundingBox().grow(12.0, 128.0, 12.0));
        if (player != null) {
            this.setGoalTarget(player);
        }

        if (this.ticksLived == 10) { /**bees move 30% faster*/
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.39);
        }

        if (this.ticksLived == 10) { /**bees do 1000 damage*/
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(1000.0);
        }

        if (this.ticksLived == 10) { /**bees have 5 health*/
            this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(5.0);
        }

        if (this.hasStung()) {
            this.setHasStung(false); /**bees don't die from stinging*/

            CustomEntityBee newBee = new CustomEntityBee(this.getWorld()); /**duplicates when stinging*/
            newBee.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
            this.getWorld().addEntity(newBee, CreatureSpawnEvent.SpawnReason.NATURAL);
        }
    }

    @Override
    public double g(double d0, double d1, double d2) {
        double d3 = this.locX() - d0; /**for determining distance to entities, y-level does not matter, eg. mob follow range*/
        double d5 = this.locZ() - d2;

        return d3 * d3 + d5 * d5;
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /**for determining distance to entities, y-level does not matter, eg. mob follow range*/
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }
}
