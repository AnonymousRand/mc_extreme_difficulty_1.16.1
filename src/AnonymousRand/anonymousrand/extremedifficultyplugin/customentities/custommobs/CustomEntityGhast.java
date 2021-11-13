package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityLargeFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalBreakBlocksAround;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalCobwebMoveFaster;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableFireballsInAllDirections;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.Random;

public class CustomEntityGhast extends EntityGhast implements ICommonCustomMethods {

    public static JavaPlugin plugin;
    public int attacks;
    private boolean a15, deathFireballs;

    public CustomEntityGhast(World world) {
        super(EntityTypes.GHAST, world);
        this.a(PathType.LAVA, 0.0F); /**no longer avoids lava*/
        this.a(PathType.DAMAGE_FIRE, 0.0F); /**no longer avoids fire*/
        this.attacks = 0;
        this.a15 = false;
        this.deathFireballs = false;
    }

    @Override
    protected void initPathfinder() {
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 80, 2, 2, 2, 0, false)); /**custom goal that breaks blocks around the mob periodically*/
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(5, new CustomEntityGhast.PathfinderGoalGhastIdleMove(this));
        this.goalSelector.a(7, new CustomEntityGhast.PathfinderGoalGhastMoveTowardsTarget(this));
        this.goalSelector.a(7, new CustomEntityGhast.CustomPathfinderGoalGhastAttackTarget(this)); /**uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal)*/
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable(damagesource)) {
            return false;
        } else if (damagesource.j() instanceof EntityLargeFireball && damagesource.getEntity() instanceof EntityHuman) { /**rebounded fireballs do not do damage*/
            return false;
        } else {
            return super.damageEntity(damagesource, f);
        }
    }

    public double getFollowRange() { /**ghasts have 80 block detection range (setting attribute doesn't work)*/
        return 80.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 15 && !this.a15) { /**after 15 attacks, ghasts get 16 max health and health*/
            this.a15 = true;
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(16.0);
            this.setHealth(16.0F);
        }

        if (this.getHealth() <= 0.0 && !this.deathFireballs) { //do this here instead of in die() so that the fireballs don't have to wait until the death animation finishes playing to start firing
            this.deathFireballs = true;
            new RunnableFireballsInAllDirections(this, 0.4, 2).runTaskTimer(plugin, 0L, this.attacks < 50 ? 20L : 40L); /**when killed, ghasts summon 100 power 1 fireballs in all directions, or wither skulls instead after 50 attacks*/
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

                int k = this.getEntityType().e().g() + 32; /**random despawn distance increased to 64 blocks*/
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

    static class CustomPathfinderGoalGhastAttackTarget extends PathfinderGoal {

        private final CustomEntityGhast ghast;
        public int a, attackNum;

        public CustomPathfinderGoalGhastAttackTarget(CustomEntityGhast entityghast) {
            this.ghast = entityghast;
            this.attackNum = 0;
        }

        @Override
        public boolean a() {
            return this.ghast.getGoalTarget() != null;
        }

        @Override
        public void c() {
            this.a = 0;
        }

        @Override
        public void d() {
            this.ghast.t(false);
        }

        @Override
        public void e() {
            EntityLiving entityliving = this.ghast.getGoalTarget();

            if (this.ghast.d(entityliving.getPositionVector()) < 6400.0D) { /**removed line of sight requirement for ghast attack, and too much vertical distance no longer stops the ghast from firing*/
                World world = this.ghast.world;

                ++this.a;
                if (this.a == 10 && !this.ghast.isSilent()) {
                    world.a((EntityHuman)null, 1015, this.ghast.getChunkCoordinates(), 0);
                }

                this.ghast.t(this.a > 2); //shooting animation only plays for 2 ticks

                if (this.a == 5) { /**shoots a fireball every 5 ticks*/
                    if (++this.attackNum == 6) { //attacks only count every 1.5 seconds, or 6 shots
                        this.ghast.attacks++;
                        this.attackNum = 0;
                    }

                    Vec3D vec3d = this.ghast.f(1.0F);
                    double d2 = entityliving.locX() - (this.ghast.locX() + vec3d.x * 4.0D);
                    double d3 = entityliving.e(0.5D) - (0.5D + this.ghast.e(0.5D));
                    double d4 = entityliving.locZ() - (this.ghast.locZ() + vec3d.z * 4.0D);

                    if (!this.ghast.isSilent()) {
                        world.a((EntityHuman)null, 1016, this.ghast.getChunkCoordinates(), 0);
                    }

                    CustomEntityLargeFireball entitylargefireball = new CustomEntityLargeFireball(world, this.ghast, d2, d3, d4, this.ghast.attacks < 50 ? this.ghast.getPower() : (this.ghast.attacks - 50) % 8 == 0 ? 3 : this.ghast.getPower()); /**after 50 attacks, the ghast shoots a power 3 fireball every 12 seconds*/
                    entitylargefireball.setPosition(this.ghast.locX() + vec3d.x * 4.0D, this.ghast.e(0.5D) + 0.5D, entitylargefireball.locZ() + vec3d.z * 4.0D);
                    world.addEntity(entitylargefireball);

                    if (this.ghast.attacks >= 30 && (this.ghast.attacks - 30) % 6 == 0) { /**after 30 attacks, the ghast shoots a ring of power 1 fireballs every 9 seconds*/
                        new RunnableFireballsInAllDirections(this.ghast, 0.4, 1).runTaskTimer(this.ghast.plugin, 0L, 20L);
                    }

                    this.a = 0;
                }
            } else if (this.a > 0) {
                --this.a;
            }
        }
    }

    static class PathfinderGoalGhastMoveTowardsTarget extends PathfinderGoal {

        private final EntityGhast a;

        public PathfinderGoalGhastMoveTowardsTarget(EntityGhast entityghast) {
            this.a = entityghast;
            this.a(EnumSet.of(PathfinderGoal.Type.LOOK));
        }

        @Override
        public boolean a() {
            return true;
        }

        @Override
        public void e() {
            if (this.a.getGoalTarget() == null) {
                Vec3D vec3d = this.a.getMot();

                this.a.yaw = -((float) MathHelper.d(vec3d.x, vec3d.z)) * 57.295776F;
                this.a.aH = this.a.yaw;
            } else {
                EntityLiving entityliving = this.a.getGoalTarget();

                if (entityliving.h((Entity) this.a) < 4096.0D) {
                    double d1 = entityliving.locX() - this.a.locX();
                    double d2 = entityliving.locZ() - this.a.locZ();

                    this.a.yaw = -((float) MathHelper.d(d1, d2)) * 57.295776F;
                    this.a.aH = this.a.yaw;
                }
            }

        }
    }

    static class PathfinderGoalGhastIdleMove extends PathfinderGoal {

        private final EntityGhast a;

        public PathfinderGoalGhastIdleMove(EntityGhast entityghast) {
            this.a = entityghast;
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE));
        }

        @Override
        public boolean a() {
            ControllerMove controllermove = this.a.getControllerMove();

            if (!controllermove.b()) {
                return true;
            } else {
                double d0 = controllermove.d() - this.a.locX();
                double d1 = controllermove.e() - this.a.locY();
                double d2 = controllermove.f() - this.a.locZ();
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                return d3 < 1.0D || d3 > 3600.0D;
            }
        }

        @Override
        public boolean b() {
            return false;
        }

        @Override
        public void c() {
            Random random = this.a.getRandom();
            double d0 = this.a.locX() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double d1 = this.a.locY() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double d2 = this.a.locZ() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);

            this.a.getControllerMove().a(d0, d1, d2, 1.0D);
        }
    }
}
