package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityTNTPrimed;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.RemovePathfinderGoals;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import com.mojang.serialization.Dynamic;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class CustomEntityZoglin extends EntityZoglin implements ICommonCustomMethods {

    public PathfinderGoalSelector targetSelectorVanilla;
    public int attacks;
    private boolean a8, a40;

    public CustomEntityZoglin(World world) {
        super(EntityTypes.ZOGLIN, world);
        this.targetSelectorVanilla = super.targetSelector;
        this.a(PathType.LAVA, 0.0F); /**no longer avoids lava*/
        this.a(PathType.DAMAGE_FIRE, 0.0F); /**no longer avoids fire*/
        this.attacks = 0;
        this.a8 = false;
        this.a40 = false;
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(this.isBaby() ? 0.9 : 0.7); /**zoglins move 75% faster (125% faster for babies) and do 2 damage (4 for babies)*/
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(this.isBaby() ? 4.0 : 2.0);
        RemovePathfinderGoals.removePathfinderGoals(this); //remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(1, new CustomEntityZoglin.PathfinderGoalZoglinAttack(this, 1.0D, true)); /**uses the custom melee attack goal that attacks even when line of sight is broken*/
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0D)); //instead of using behavior-controlled idle actions
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        boolean flag = super.damageEntity(damagesource, f);

        if (this.world.isClientSide) {
            return false;
        } else if (flag && damagesource.getEntity() instanceof EntityLiving) {
            EntityLiving entityliving = (EntityLiving) damagesource.getEntity();

            if (entityliving instanceof EntityPlayer && !BehaviorUtil.a(this, entityliving, 4.0D)) { /**only retaliate against players*/
                this.k(entityliving);
            }

            return flag;
        } else {
            return flag;
        }
    }

    @Override
    protected BehaviorController<?> a(Dynamic<?> dynamic) { //removes all the weird behavior-controlled attack goals and instead use the standard pathfinder goals
        return this.cJ().a(dynamic);
    }

    public double getFollowRange() { /**zoglins have 128 block detection range (setting attribute doesn't work)*/
        return 128.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 8 && !this.a8) { /**after 8 attacks, zoglins gain regen 2*/
            this.a8 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
            this.goalSelector.a(1, new NewPathfinderGoalShootLargeFireballs(this, 100, 1, false)); /**after 8 attacks, zoglins shoot a power 1 ghast fireball every 5 seconds*/
        }

        if (this.attacks == 40 &&!this.a40) { /**after 40 attacks, zoglins gain speed 5*/
            this.a40 = true;
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 4));
        }

        if (this.ticksLived % 6 == 0 && this.getGoalTarget() != null) {
            Location thisLoc = new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ());

            if (thisLoc.getBlock().getType() == org.bukkit.Material.AIR) { /**zoglins create a path of power 1 tnt on itself as long as it is inside an air block*/
                CustomEntityTNTPrimed newTNT = new CustomEntityTNTPrimed(this.getWorld(), 35, (this.attacks >= 15 && this.ticksLived % 100 == 0) ? 2.0F : 1.0F); /**after 15 attacks, zoglins spawn a power 2 tnt instead every 5 seconds*/
                newTNT.setPosition(this.locX(), this.locY(), this.locZ());
                this.getWorld().addEntity(newTNT);
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

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && d0 > (double)l && this.isTypeNotPersistent(d0)) {
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

    @Override
    public int bL() { //getMaxFallHeight
        if (this.getGoalTarget() == null) {
            return 3;
        } else {
            int i = (int)(this.getHealth() * 20.0); /**mobs are willing to take 20 times the fall distance (same damage) to reach and do not stop taking falls if it is at less than 33% health*/

            return i + 3;
        }
    }

    static class PathfinderGoalZoglinAttack extends CustomPathfinderGoalMeleeAttack {

        protected final CustomEntityZoglin zoglin;
        private boolean moveEverywhere;

        public PathfinderGoalZoglinAttack(CustomEntityZoglin zoglin, double speedTowardsTarget, boolean useLongMemory) {
            super(zoglin, speedTowardsTarget, useLongMemory);
            this.zoglin = zoglin;
            this.moveEverywhere = false;
        }

        @Override
        public void e() {
            super.e();

            EntityLiving entityliving = this.zoglin.getGoalTarget();

            if (entityliving == null) {
                return;
            }

            if (this.zoglin.attacks >= 8) {
                if (this.zoglin.attacks == 25 && !this.moveEverywhere) { /**after 25 attacks, zoglins throw players around erratically, often high in the air, for a few seconds before teleporting to the player to continue attacking*/
                    this.moveEverywhere = true;
                    new RunnableZoglinThrowPlayerAround(this.zoglin, entityliving, 12).runTaskTimer(StaticPlugin.plugin, 0L, 5L);
                }
            }
        }
    }

    static class RunnableZoglinThrowPlayerAround extends BukkitRunnable {

        private final CustomEntityZoglin zoglin;
        private final EntityLiving target;
        private int cycles;
        private final int maxCycles;
        private static final Random random = new Random();

        public RunnableZoglinThrowPlayerAround(CustomEntityZoglin zoglin, EntityLiving target, int maxCycles) {
            this.zoglin = zoglin;
            this.target = target;
            this.cycles = 0;
            this.maxCycles = maxCycles;
        }

        @Override
        public void run() {
            if (++this.cycles <= this.maxCycles) {
                LivingEntity bukkitEntity = (LivingEntity)this.target.getBukkitEntity();
                bukkitEntity.setVelocity(new Vector(random.nextDouble() * 6 - random.nextDouble() * 3, random.nextDouble() * 3 - random.nextDouble() * 1.5, random.nextDouble() * 6 - random.nextDouble() * 3));
            } else if (++this.cycles - 32 >= this.maxCycles) {
                this.cancel();
                this.zoglin.setPosition(this.target.locX(), this.target.locY(), this.target.locZ()); //zoglin teleports to player after 8 seconds so that it can continue attacking
            }
        }
    }
}
