package AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.CustomMobs;

import AnonymousRand.ExtremeDifficultyPlugin.CustomGoals.*;
import AnonymousRand.ExtremeDifficultyPlugin.util.SpawnLivingEntity;
import net.minecraft.server.v1_16_R1.*;

public class CustomEntityHoglin extends EntityHoglin {

    public PathfinderGoalSelector targetSelectorVanilla;
    public int attacks;
    private boolean a10, a32, a42, a70;

    public CustomEntityHoglin(World world) {
        super(EntityTypes.HOGLIN, world);
        this.targetSelectorVanilla = super.targetSelector;
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
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 40, 1, 1, 1, 1, false)); /**custom goal that breaks blocks around the mob periodically*/
        this.goalSelector.a(0, new NewPathfinderGoalCobweb(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack(this, 1.0D, true)); /**custom melee attack goal continues attacking even when line of sight is broken*/
        this.goalSelector.a(1, new NewPathfinderGoalBreakRepellentBlocksAround(this, 20, 5, 1, 5, 1, false)); /**custom goal that breaks repellant blocks around the mob periodically*/
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public void die() {
        super.die();

        if (this.random.nextDouble() < (this.attacks < 70 ? 0.3 : 1.0)) { /**hoglins have a 30% chance to spawn a zoglin after death (100% chance after 70 attacks)*/
            new SpawnLivingEntity(this.getWorld(), new CustomEntityZoglin(this.getWorld()), 1, null, null, this, false, true).run();
        }
    }

    public double getFollowRange() { /**hoglins have 40 block detection range (setting attribute doesn't work) (64 after 10 attacks)*/
        return this.attacks < 10 ? 40.0 : 64.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 10 && !this.a10) { /**after 10 attacks, hoglins get regen 2*/
            this.a10 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
            this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, true)); /**updates attack range; only happens if/when the mob has a target*/
        }

        if (this.attacks == 32 && !this.a32) { /**after 32 attacks, hoglins get regen 3*/
            this.a32 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));
        }

        if (this.attacks == 42 && !this.a42) { /**after 42 attacks, hoglins summon a baby hoglin*/
            this.a42 = true;
            CustomEntityHoglin newHoglin = new CustomEntityHoglin(this.getWorld());
            new SpawnLivingEntity(this.getWorld(), newHoglin, 1, null, null, this, false, true).run();
            newHoglin.a(true);
        }

        if (this.attacks == 70 && !this.a70) { /**after 70 attacks, hoglins summon 2 baby hoglins*/
            this.a70 = true;
            CustomEntityHoglin newHoglin = new CustomEntityHoglin(this.getWorld());
            new SpawnLivingEntity(this.getWorld(), newHoglin, 2, null, null, this, false, true).run();
            newHoglin.a(true);
        }

        if (this.ticksLived == 10) { /**hoglins move 75% faster (125% faster for babies), do 2 damage (6 for babies), and have extra knockback*/
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(this.isBaby() ? 0.9 : 0.7);
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(this.isBaby() ? 6.0 : 2.0);
            this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(2.5);
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
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityhuman = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (entityhuman != null) {
                double d0 = Math.pow(entityhuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityhuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); /**mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleepingdouble d0 = entityhuman.h(this);*/
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
