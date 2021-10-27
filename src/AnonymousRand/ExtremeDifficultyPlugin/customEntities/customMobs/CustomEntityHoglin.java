package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockPlaceEvent;

import java.lang.reflect.Method;

public class CustomEntityHoglin extends EntityHoglin {

    public int attacks;

    public CustomEntityHoglin(World world) {
        super(EntityTypes.HOGLIN, world);
        this.a(PathType.LAVA, 0.0F); //no longer avoids lava
        this.attacks = 0;
    }

    @Override
    protected void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobweb(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack(this, 1.0D, true)); /**custom melee attack goal continues attacking even when line of sight is broken*/
        this.goalSelector.a(2, new NewPathfinderGoalBreakBlocksAround(this, 40, 1, 1, 1, 1, false)); /**custom goal that breaks blocks around the mob periodically*/
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    public double getFollowRange() {
        return this.attacks < 12 ? 40.0 : 60.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived == 10) { /**hoglins do 3 damage (8 for babies), and extra knockback*/
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.6);
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(this.isBaby() ? 8.0 : 3.0);
            this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(3.0);
        }

        if (this.ticksLived % 40 == 10) { /**hoglins have 40 block detection range (setting attribute doesn't work) (60 after 12 attacks)*/
            EntityPlayer player = this.getWorld().a(EntityPlayer.class, new CustomPathfinderTargetCondition(), this, this.locX(), this.locY(), this.locZ(), this.getBoundingBox().grow(this.getFollowRange(), 128.0, this.getFollowRange())); //get closest player within bounding box
            if (player != null && !player.isInvulnerable() && this.getGoalTarget() == null) {
                this.setGoalTarget(player);
            }

            if (this.getGoalTarget() != null) {
                EntityLiving target = this.getGoalTarget();

                if (target.isInvulnerable() || this.d(target.getPositionVector()) > Math.pow(this.getFollowRange(), 2)) {
                    this.setGoalTarget(null);
                }
            }
        }

        Location thisLoc = new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ());
        if (thisLoc.getBlock().getType() == org.bukkit.Material.AIR) { /**hoglins lay down power 1 tnt on itself as long as it is inside an air block*/
            //todo: custom tnt
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
