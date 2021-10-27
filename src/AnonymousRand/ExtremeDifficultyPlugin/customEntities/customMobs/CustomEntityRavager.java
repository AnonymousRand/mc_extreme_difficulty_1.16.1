package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class CustomEntityRavager extends EntityRavager {

    public int attacks;
    private boolean a20, a60, a90;
    public boolean launchHigh;

    public CustomEntityRavager(World world) {
        super(EntityTypes.RAVAGER, world);
        this.attacks = 0;
        this.a20 = false;
        this.a60 = false;
        this.a90 = false;
        this.launchHigh = false;
    }

    @Override
    protected void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobweb(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack(this, 1.0D, true)); /**custom melee attack goal continues attacking even when line of sight is broken*/
        this.goalSelector.a(2, new NewPathfinderGoalBreakBlocksAround(this, 40, 2, 1, 2, 1, true)); /**custom goal that breaks blocks around the mob periodically*/
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
        this.targetSelector.a(3, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class, false)); /**still uses the default (super), line-of-sight-requiring goal for iron golems*/
    }

    @Override
    protected void f(EntityLiving entityliving) { /**ravager can't get stunned and instead heals when its attack is blocked by a shield*/
        this.heal(this.attacks < 30 ? 20.0f : 30.0f);
    }

    public double getFollowRange() {
        return this.attacks < 20 ? 40.0 : 80.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 20 && !this.a20) { /**after 20 attacks, ravagers gain speed 5 and 1 damage*/
            this.a20 = true;
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(1.0);
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 4));
        }

        if (this.attacks == 60 && !this.a60) { /**after 60 attacks, ravagers get extra knockback*/
            this.a60 = true;
            this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(5.0);
        }

        if (this.attacks == 90 && !this.a90) { /**after 90 attacks, ravagers get regen 3*/
            this.a90 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));
        }

        if (this.ticksLived == 10) { /**ravagers have 400 health and extra knockback, but only 0.25 damage*/
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(0.25);
            this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(3.5);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(400.0);
            this.setHealth(400.0f);
        }

        if (this.ticksLived % 40 == 10) { /**ravagers have 40 block detection range (setting attribute doesn't work) (80 after 20 attacks)*/
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
