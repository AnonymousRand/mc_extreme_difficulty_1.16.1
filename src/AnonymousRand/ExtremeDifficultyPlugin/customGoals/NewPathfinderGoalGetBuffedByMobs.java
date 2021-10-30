package AnonymousRand.ExtremeDifficultyPlugin.customGoals;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntitySilverfish;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customProjectiles.CustomEntityArrow;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.Random;

public class NewPathfinderGoalGetBuffedByMobs extends PathfinderGoal {

    public EntityInsentient entity;
    private final Random random = new Random();

    public NewPathfinderGoalGetBuffedByMobs(EntityInsentient entity) {
        this.entity = entity;
    }

    @Override
    public boolean a() {
        if (this.entity.hasEffect(MobEffects.HUNGER)) {
            if (this.entity.getEffect(MobEffects.HUNGER).getAmplifier() >= 252 && this.entity.getGoalTarget() != null) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean b() {
        if (this.entity.hasEffect(MobEffects.HUNGER)) {
            if (this.entity.getEffect(MobEffects.HUNGER).getAmplifier() >= 252 && this.entity.getGoalTarget() != null) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void e() {
        int amplifier = this.entity.getEffect(MobEffects.HUNGER).getAmplifier();

        if (this.entity.ticksLived % (amplifier == 252 ? 20 : amplifier == 253 ? 14 : 8) == 0) {
            this.shootArrows();
        }

        if (amplifier > 252 && this.entity.ticksLived % 240 == 0) {
            CustomEntitySilverfish newSilverfish = new CustomEntitySilverfish(this.entity.getWorld());
            newSilverfish.setPosition(this.entity.locX(), this.entity.locY(), this.entity.locZ());
            this.entity.getWorld().addEntity(newSilverfish, CreatureSpawnEvent.SpawnReason.NATURAL);
        }
    }

    public void shootArrows() {
        CustomEntityArrow entityarrow = new CustomEntityArrow(this.entity.getWorld());
        entityarrow.setShooter(this.entity);
        Vec3D pos = this.entity.getPositionVector();
        Vec3D lookDirection = this.entity.getGoalTarget().getPositionVector().d(pos).d(); //d(pos) is subtract, d() is normalize

        double d0 = lookDirection.getX();
        double d1 = lookDirection.getY() + random.nextDouble() * 0.02 + 0.02;
        double d2 = lookDirection.getZ();

        if (this.random.nextDouble() <= 0.05) { /**5% of arrows shot are piercing 1*/
            entityarrow.setPierceLevel((byte)1);
        }

        entityarrow.setPosition(pos.getX() + lookDirection.getX() * 0.5, pos.getY() + 1.8, pos.getZ() + lookDirection.getZ() * 0.5);
        entityarrow.shoot(d0, d1, d2, 1.25F, 0.0f);
        this.entity.playSound(SoundEffects.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
        this.entity.getWorld().addEntity(entityarrow);
    }
}
