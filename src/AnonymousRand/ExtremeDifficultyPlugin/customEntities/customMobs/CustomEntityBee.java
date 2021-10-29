package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderTargetCondition;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.NewPathfinderGoalCobweb;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.*;

public class CustomEntityBee extends EntityBee {

    private boolean firstSting;

    public CustomEntityBee(World world) {
        super(EntityTypes.BEE, world);
        this.firstSting = false;
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobweb(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**bees are always aggro*/
    }

    public double getFollowRange() { /**bees have 16 block detection range (setting attribute doesn't work)*/
        return 16.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived == 10) { /**bees do 1000 damage but only have 5 health*/
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(1000.0);
            this.setHealth(5.0f);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(5.0);
        }

        if (this.hasStung()) {
            this.setHasStung(false); /**bees don't die from stinging*/

            if (!this.firstSting) {
                this.firstSting = true;
                CustomEntityBee newBee = new CustomEntityBee(this.getWorld()); /**duplicates after the first time stinging*/
                newBee.setPosition(this.locX(), this.locY(), this.locZ());
                this.getWorld().addEntity(newBee, CreatureSpawnEvent.SpawnReason.NATURAL);
            }
        }

        if (this.ticksLived % 5 == 2) {
            if (this.getLastDamager() != null) {
                EntityLiving target = this.getLastDamager();

                if (!(target instanceof EntityPlayer)) { /**mobs only target players (in case mob damage listener doesn't register)*/
                    this.setLastDamager(null);
                }
            }
        }
    }

    @Override
    public double g(double d0, double d1, double d2) {
        double d3 = this.locX() - d0; /**for determining distance to entities, y level does not matter, eg. mob follow range, attacking (can hit player no matter the y level)*/
        double d5 = this.locZ() - d2;

        return d3 * d3 + d5 * d5;
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /**for determining distance to entities, y level does not matter, eg. mob follow range, attacking (can hit player no matter the y level)*/
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }
}
