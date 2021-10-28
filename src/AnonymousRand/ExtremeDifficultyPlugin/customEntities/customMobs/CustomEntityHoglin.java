package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.*;
import com.google.common.collect.ImmutableList;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Method;

public class CustomEntityHoglin extends EntityHoglin {

    public int attacks;
    private boolean a10, a32, a42, a70;

    public CustomEntityHoglin(World world) {
        super(EntityTypes.HOGLIN, world);
        this.a(PathType.LAVA, 0.0F); //no longer avoids lava
        this.attacks = 0;
        this.a10 = false;
        this.a32 = false;
        this.a42 = false;
        this.a70 = false;
    }

    @Override
    protected void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobweb(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack(this, 1.0D, true)); /**custom melee attack goal continues attacking even when line of sight is broken*/
        this.goalSelector.a(2, new NewPathfinderGoalBreakSpecificBlocksAround(this, 20, 5, 1, 5, 1, false)); /**custom goal that breaks repellant blocks around the mob periodically*/
        this.goalSelector.a(3, new NewPathfinderGoalBreakBlocksAround(this, 40, 1, 1, 1, 1, false)); /**custom goal that breaks blocks around the mob periodically*/
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    public double getFollowRange() {
        return this.attacks < 12 ? 40.0 : 60.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 10 && !this.a10) { /**after 10 attacks, hoglins get regen 2*/
            this.a10 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
        }

        if (this.attacks == 32 && !this.a32) { /**after 32 attacks, hoglins get regen 3*/
            this.a32 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));
        }

        if (this.attacks == 42 && !this.a42) { /**after 42 attacks, hoglins summon a baby hoglin*/
            this.a42 = true;
            CustomEntityHoglin newHoglin = new CustomEntityHoglin(this.getWorld());
            newHoglin.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
            newHoglin.a(true);
            this.getWorld().addEntity(newHoglin, CreatureSpawnEvent.SpawnReason.NATURAL);
        }

        if (this.attacks == 70 && !this.a70) { /**after 70 attacks, hoglins summon 2 baby hoglins*/
            this.a70 = true;
            CustomEntityHoglin newHoglin;

            for (int i = 0; i < 2; i++) {
                newHoglin = new CustomEntityHoglin(this.getWorld());
                newHoglin.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
                newHoglin.a(true);
                this.getWorld().addEntity(newHoglin, CreatureSpawnEvent.SpawnReason.NATURAL);
            }
        }

        if (this.ticksLived == 10) { /**hoglins move 75% faster, do 2 damage (6 for babies), and have extra knockback*/
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.7);
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(this.isBaby() ? 6.0 : 2.0);
            this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(3.0);
        }

        if (this.ticksLived % (random.nextInt(100) + 50) == 10) { /**hoglins have 40 block detection range (setting attribute doesn't work) (60 after 12 attacks)*/
            EntityPlayer player = this.getWorld().a(EntityPlayer.class, new CustomPathfinderTargetCondition(), this, this.locX(), this.locY(), this.locZ(), this.getBoundingBox().grow(this.getFollowRange(), 128.0, this.getFollowRange())); //get closest player within bounding box
            if (player != null && !player.isInvulnerable() && this.getGoalTarget() == null) {
                this.setGoalTarget(player);
            }

            if (this.getGoalTarget() != null) {
                EntityLiving target = this.getGoalTarget();

                if (!(target instanceof EntityPlayer) || target.isInvulnerable() || this.d(target.getPositionVector()) > Math.pow(this.getFollowRange(), 2)) { /**mobs only target players (in case mob damage listener doesn't register)*/
                    this.setGoalTarget(null);
                }
            }
        }
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityhuman = this.world.findNearbyPlayer(this, -1.0D);

            if (entityhuman != null) {
                double d0 = Math.pow(entityhuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityhuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); //mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleepingdouble d0 = entityhuman.h(this);
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (d0 > (double)j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 8; /**random despawn distance increased to 40 blocks*/
                int l = k * k;

                if (this.ticksFarFromPlayer > 600 && this.random.nextInt(800) == 0 && d0 > (double)l && this.isTypeNotPersistent(d0)) {
                    this.die();
                } else if (d0 < (double)l) {
                    this.ticksFarFromPlayer = 0;
                }
            }

        } else {
            this.ticksFarFromPlayer = 0;
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
