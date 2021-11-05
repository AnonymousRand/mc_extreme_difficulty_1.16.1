package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityLargeFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntitySmallFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalCobwebMoveFaster;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

import java.util.EnumSet;

public class CustomEntityBlaze extends EntityBlaze {

    public PathfinderGoalSelector targetSelectorVanilla;
    public int attacks;
    private int rapidFireTracker;

    private static final DataWatcherObject<Byte> d = DataWatcher.a(CustomEntityBlaze.class, DataWatcherRegistry.a);

    public CustomEntityBlaze(World world) {
        super(EntityTypes.BLAZE, world);
        this.targetSelectorVanilla = super.targetSelector;
        this.a(PathType.WATER, 0.0F); /**no longer avoids water*/
        this.a(PathType.LAVA, 8.0F);
        this.a(PathType.DANGER_FIRE, 0.0F);
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.attacks = 0;
        this.rapidFireTracker = 0;
    }

    @Override
    protected void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(3, new CustomEntityBlaze.CustomPathfinderGoalBlazeFireball(this));
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public boolean dN() { /**no longer damaged by water*/
        return false;
    } /**no longer damaged by water*/

    public double getFollowRange() { /**blazes have 48 block detection range (setting attribute doesn't work)*/
        return 48.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.ticksLived == 10) { /**blazes have 6 health*/
            this.setHealth(6.0F);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(6.0);
        }
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityhuman = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (entityhuman != null) {
                double d0 = Math.pow(entityhuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityhuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); /**mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleeping*/
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

    @Override
    public int bL() { //getMaxFallHeight
        if (this.getGoalTarget() == null) {
            return 3;
        } else {
            int i = (int)(this.getHealth() * 20.0); /**mobs are willing to take 20 times the fall distance (same damage) to reach and do not stop taking falls if it is at less than 33% health*/

            return i + 3;
        }
    }

    public static class CustomPathfinderGoalBlazeFireball extends PathfinderGoal { //new attack goal

        private final CustomEntityBlaze blaze;
        private int b;
        private int c;
        private int d;

        public CustomPathfinderGoalBlazeFireball(CustomEntityBlaze entityblaze) {
            this.blaze = entityblaze;
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        }

        @Override
        public boolean a() {
            EntityLiving entityliving = this.blaze.getGoalTarget();
            return entityliving != null && entityliving.isAlive() && this.blaze.d(entityliving);
        }

        @Override
        public void c() {
            this.b = 0;
        }

        @Override
        public void d() {
            this.d = 0;
        }

        @Override
        public void e() {
            --this.c;
            EntityLiving entityliving = this.blaze.getGoalTarget();

            if (entityliving != null) { /**attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via nearestAttackableTarget goal)*/
                ++this.d;

                double d0 = this.blaze.h((Entity) entityliving);

                if (d0 < 4.0D) {
                    if (this.c <= 0) {
                        this.c = 80; /**only melees every 4 seconds*/
                        this.blaze.attackEntity(entityliving);
                        this.blaze.getWorld().createExplosion(this.blaze, entityliving.locX(), entityliving.locY(), entityliving.locZ(), 0.4F, false, Explosion.Effect.DESTROY); /**melee attack creates a power 0.4 explosion on player's location*/
                    }

                    this.blaze.getControllerMove().a(entityliving.locX(), entityliving.locY(), entityliving.locZ(), 1.0D);
                } else if (d0 < this.g() * this.g()) {
                    double d1 = entityliving.locX() - this.blaze.locX();
                    double d2 = entityliving.e(0.5D) - this.blaze.e(0.5D);
                    double d3 = entityliving.locZ() - this.blaze.locZ();

                    if (this.c <= 0) {
                        ++this.b;
                        if (this.b == 1) {
                            this.c = 0; /**no pause between each volley; shoots constantly*/
                        } else if (this.b <= 4) {
                            if (this.blaze.rapidFireTracker == 0) {
                                this.c = 2; /**tripled attack speed*/
                            } else {
                                this.c = 1; /**6x attack speed during rapidfire*/
                            }
                        } else {
                            this.c = 0; /**no pause between each volley; shoots constantly*/
                            this.b = 0;
                        }

                        if (this.b > 1) {
                            float f = MathHelper.c(MathHelper.sqrt(d0)) * 0.5F;
                            World world = this.blaze.world;

                            if (!this.blaze.isSilent()) {
                                world.a((EntityHuman)null, 1018, this.blaze.getChunkCoordinates(), 0);
                            }

                            if (this.blaze.rapidFireTracker <= 0 && this.blaze.attacks < 400) {
                                if (this.blaze.attacks % 160 == 0 && this.blaze.attacks != 0) { /**every 160 shots, the blaze shoots a fireball with explosion power 2*/
                                    CustomEntityLargeFireball entityLargeFireball = new CustomEntityLargeFireball(world, this.blaze, d1, d2, d3, 2);
                                    entityLargeFireball.setPosition(entityLargeFireball.locX(), this.blaze.e(0.5D) + 0.5D, entityLargeFireball.locZ());
                                    world.addEntity(entityLargeFireball);
                                    this.blaze.attacks++;
                                } else if (this.blaze.attacks % 60 == 0 && this.blaze.attacks != 0) { /**every 60 shots, the blaze shoots a fireball with explosion power 1*/
                                    CustomEntityLargeFireball entityLargeFireball = new CustomEntityLargeFireball(world, this.blaze, d1, d2, d3, 1);
                                    entityLargeFireball.setPosition(entityLargeFireball.locX(), this.blaze.e(0.5D) + 0.5D, entityLargeFireball.locZ());
                                    world.addEntity(entityLargeFireball);
                                    this.blaze.attacks++;
                                } else {
                                    CustomEntitySmallFireball entitySmallFireball = new CustomEntitySmallFireball(world, this.blaze, d1, d2, d3); /**blaze has no inaccuracy*/
                                    entitySmallFireball.setPosition(entitySmallFireball.locX(), this.blaze.e(0.5D) + 0.5D, entitySmallFireball.locZ());
                                    world.addEntity(entitySmallFireball);
                                    this.blaze.attacks++;
                                }

                                if (this.blaze.attacks % 15 == 0) { /**every 15 shots, the blaze shoots a ring of fireballs*/
                                    CustomEntitySmallFireball entitySmallFireball;

                                    for (double x = -1.0; x <= 1.0; x += 0.4) {
                                        for (double y = -1.0; y <= 1.0; y += 0.4) {
                                            for (double z = -1.0; z <= 1.0; z += 0.4) {
                                                entitySmallFireball = new CustomEntitySmallFireball(world, this.blaze, x, y, z);
                                                entitySmallFireball.setPosition(entitySmallFireball.locX(), this.blaze.e(0.5D) + 0.5D, entitySmallFireball.locZ());
                                                world.addEntity(entitySmallFireball);
                                            }
                                        }
                                    }
                                }

                            } else { /**rapid fire phase for 50 shots after 400 normal shots*/
                                if (this.blaze.attacks >= 400) { /**first entering rapid fire phase*/
                                    this.blaze.attacks = 0;
                                    this.blaze.rapidFireTracker = 50;
                                } else {
                                    for (int i = 0; i < 8; i++) { /**shoots 8 fireballs at a time during this phase*/
                                        CustomEntitySmallFireball entitySmallFireball = new CustomEntitySmallFireball(world, this.blaze, d1 + this.blaze.getRandom().nextGaussian() * (double)f * 0.5, d2, d3 + this.blaze.getRandom().nextGaussian() * (double)f * 0.5); /**blaze has 0.5x default inaccuracy in this phase*/

                                        entitySmallFireball.setPosition(entitySmallFireball.locX(), this.blaze.e(0.5D) + 0.5D, entitySmallFireball.locZ());
                                        world.addEntity(entitySmallFireball);
                                    }

                                    CustomEntitySmallFireball entitySmallFireball;

                                    if (this.blaze.rapidFireTracker == 1) { /**shoots a large fireball with explosion power 4 when this phase ends*/
                                        CustomEntityLargeFireball entityLargeFireball = new CustomEntityLargeFireball(world, this.blaze, d1, d2, d3, 4);
                                        entityLargeFireball.setPosition(entityLargeFireball.locX(), this.blaze.e(0.5D) + 0.5D, entityLargeFireball.locZ());
                                        world.addEntity(entityLargeFireball);

                                        for (double x = -1.0; x <= 1.0; x += 0.3) {
                                            for (double y = -1.0; y <= 1.0; y += 0.3) {
                                                for (double z = -1.0; z <= 1.0; z += 0.3) {
                                                    entitySmallFireball = new CustomEntitySmallFireball(world, this.blaze, x, y, z);
                                                    entitySmallFireball.setPosition(entitySmallFireball.locX(), this.blaze.e(0.5D) + 0.5D, entitySmallFireball.locZ());
                                                    world.addEntity(entitySmallFireball);
                                                }
                                            }
                                        }
                                    } else if (this.blaze.rapidFireTracker % 17 == 0) { /**every 17 shots, the blaze shoots a ring of fireballs*/
                                        for (double x = -1.0; x <= 1.0; x += 0.4) {
                                            for (double y = -1.0; y <= 1.0; y += 0.4) {
                                                for (double z = -1.0; z <= 1.0; z += 0.4) {
                                                    entitySmallFireball = new CustomEntitySmallFireball(world, this.blaze, x, y, z);
                                                    entitySmallFireball.setPosition(entitySmallFireball.locX(), this.blaze.e(0.5D) + 0.5D, entitySmallFireball.locZ());
                                                    world.addEntity(entitySmallFireball);
                                                }
                                            }
                                        }
                                    }

                                    this.blaze.rapidFireTracker--;
                                }
                            }
                        }
                    }

                    this.blaze.getControllerLook().a(entityliving, 10.0F, 10.0F);
                } else if (this.d < 5) {
                    this.blaze.getControllerMove().a(entityliving.locX(), entityliving.locY(), entityliving.locZ(), 1.0D);
                }

                super.e();
            }
        }

        private double g() {
            return this.blaze.b(GenericAttributes.FOLLOW_RANGE);
        }
    }
}