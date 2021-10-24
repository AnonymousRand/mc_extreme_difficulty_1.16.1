package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.CustomEntityLightning;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customProjectiles.CustomEntityLargeFireball;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderTargetCondition;
import AnonymousRand.ExtremeDifficultyPlugin.util.CoordsFromHypotenuse;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.EnumSet;
import java.util.Random;

public class CustomEntityGhast extends EntityGhast {

    public int attacks;

    public CustomEntityGhast(World world) {
        super(EntityTypes.GHAST, world);
        this.attacks = 0;
    }

    @Override
    protected void initPathfinder() {
        this.goalSelector.a(5, new CustomEntityGhast.PathfinderGoalGhastIdleMove(this));
        this.goalSelector.a(7, new CustomEntityGhast.PathfinderGoalGhastMoveTowardsTarget(this));
        this.goalSelector.a(7, new CustomEntityGhast.CustomPathfinderGoalGhastAttackTarget(this)); /**uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal)*/
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
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

    @Override
    public void tick() {
        super.tick();

        Location thisLoc = new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ());
        Location thisLoc2 = new Location(this.getWorld().getWorld(), this.locX(), this.locY() + 1.0, this.locZ());
        if (thisLoc.getBlock().getType() == org.bukkit.Material.COBWEB || thisLoc2.getBlock().getType() == org.bukkit.Material.COBWEB) { /**non-player mobs gain Speed 11 while in a cobweb (approx original speed)*/
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, 2, 10));
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

                if (d0 > (double) j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 32; /**random despawn distance increased to 64 blocks*/
                int l = k * k;

                if (this.ticksFarFromPlayer > 600 && this.random.nextInt(800) == 0 && d0 > (double) l && this.isTypeNotPersistent(d0)) {
                    this.die();
                } else if (d0 < (double) l) {
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
        public int a;

        public CustomPathfinderGoalGhastAttackTarget(CustomEntityGhast entityghast) {
            this.ghast = entityghast;
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

            if (this.ghast.d(entityliving.getPositionVector()) < 4096.0D) { /**removed line of sight requirement for ghast attack, and too much vertical distance no longer stops the ghast from firing*/
                World world = this.ghast.world;

                ++this.a;
                if (this.a == 10 && !this.ghast.isSilent()) {
                    world.a((EntityHuman)null, 1015, this.ghast.getChunkCoordinates(), 0);
                }

                if (this.a == 2) { /**shoots a fireball every tick*/
                    Vec3D vec3d = this.ghast.f(1.0F);
                    double d2 = entityliving.locX() - (this.ghast.locX() + vec3d.x * 4.0D);
                    double d3 = entityliving.e(0.5D) - (0.5D + this.ghast.e(0.5D));
                    double d4 = entityliving.locZ() - (this.ghast.locZ() + vec3d.z * 4.0D);

                    if (!this.ghast.isSilent()) {
                        world.a((EntityHuman)null, 1016, this.ghast.getChunkCoordinates(), 0);
                    }

                    CustomEntityLargeFireball entitylargefireball = new CustomEntityLargeFireball(world, this.ghast, d2, d3, d4, this.ghast.getPower());
                    entitylargefireball.setPosition(this.ghast.locX() + vec3d.x * 4.0D, this.ghast.e(0.5D) + 0.5D, entitylargefireball.locZ() + vec3d.z * 4.0D);
                    world.addEntity(entitylargefireball);
                    this.a = 0;
                }
            } else if (this.a > 0) {
                --this.a;
            }

            this.ghast.t(true); //shooting animation always plays
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
                double d0 = 64.0D;

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
            double d0 = this.a.locX() + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double d1 = this.a.locY() + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double d2 = this.a.locZ() + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);

            this.a.getControllerMove().a(d0, d1, d2, 1.0D);
        }
    }
}
