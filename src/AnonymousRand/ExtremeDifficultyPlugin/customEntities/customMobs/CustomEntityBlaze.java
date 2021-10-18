package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customProjectiles.CustomEntityLargeFireball;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_16_R1.*;

import java.util.EnumSet;

public class CustomEntityBlaze extends EntityBlaze {

    private static final DataWatcherObject<Byte> d = DataWatcher.a(CustomEntityBlaze.class, DataWatcherRegistry.a);
    private int shotTracker;
    private int rapidFireTracker;

    public CustomEntityBlaze(World world) {
        super(EntityTypes.BLAZE, world);
        this.a(PathType.WATER, 0.0F); /**no longer avoids water*/
        this.a(PathType.LAVA, 8.0F);
        this.a(PathType.DANGER_FIRE, 0.0F);
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.shotTracker = 0;
        this.rapidFireTracker = 0;
    }

    @Override
    protected void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(3, new CustomPathfinderGoalBlazeFireball(this));
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public void tick() {
        super.tick();
        if (this.ticksLived == 10) { /**blazes have 6 health*/
            this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(6.0);
        }
    }

    @Override
    public boolean dN() { /**no longer damaged by water*/
        return false;
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityhuman = this.world.findNearbyPlayer(this, -1.0D);

            if (entityhuman != null) {
                double d0 = Math.pow(entityhuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityhuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); /**mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleeping*/
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (d0 > (double) j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 8; /**random despawn distance increased to 40 blocks*/
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
        double d3 = this.locX() - d0; /**for determining distance to entities, y-level does not matter, eg. mob follow range*/
        double d5 = this.locZ() - d2;

        return d3 * d3 + d5 * d5;
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /**for determining distance to entities, y-level does not matter, eg. mob follow range*/
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }

    static class CustomPathfinderGoalBlazeFireball extends PathfinderGoal { //new attack goal

        private final CustomEntityBlaze a;
        private int b;
        private int c;
        private int d;

        public CustomPathfinderGoalBlazeFireball(CustomEntityBlaze entityblaze) {
            this.a = entityblaze;
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        }

        @Override
        public boolean a() {
            EntityLiving entityliving = this.a.getGoalTarget();

            return entityliving != null && entityliving.isAlive() && this.a.d(entityliving);
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
            EntityLiving entityliving = this.a.getGoalTarget();

            if (entityliving != null) { /**attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via nearestAttackableTarget goal)*/
                ++this.d;

                double d0 = this.a.h((Entity) entityliving);

                if (d0 < 4.0D) {
                    if (this.c <= 0) {
                        this.c = 80; /**only melees every 4 seconds*/
                        this.a.attackEntity(entityliving);
                        this.a.getWorld().createExplosion(this.a, entityliving.locX(), entityliving.locY(), entityliving.locZ(), 0.4f, false, Explosion.Effect.DESTROY); /**melee attack creates a power 0.4 explosion on player's location*/
                    }

                    this.a.getControllerMove().a(entityliving.locX(), entityliving.locY(), entityliving.locZ(), 1.0D);
                } else if (d0 < this.g() * this.g()) {
                    double d1 = entityliving.locX() - this.a.locX();
                    double d2 = entityliving.e(0.5D) - this.a.e(0.5D);
                    double d3 = entityliving.locZ() - this.a.locZ();

                    if (this.c <= 0) {
                        ++this.b;
                        if (this.b == 1) {
                            this.c = 0; /**no pause between each volley; shoots constantly*/
                        } else if (this.b <= 4) {
                            if (this.a.rapidFireTracker == 0) {
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
                            World world = this.a.world;

                            if (!this.a.isSilent()) {
                                world.a((EntityHuman) null, 1018, this.a.getChunkCoordinates(), 0);
                            }

                            if (this.a.rapidFireTracker <= 0 && this.a.shotTracker < 50) {
                                if (this.a.shotTracker % 160 == 0 && this.a.shotTracker != 0) { /**every 160 shots, the blaze shoots a fireball with explosion power 2*/
                                    CustomEntityLargeFireball entitylargefireball = new CustomEntityLargeFireball(world, this.a, d1, d2, d3, 2);
                                    entitylargefireball.setPosition(entitylargefireball.locX(), this.a.e(0.5D) + 0.5D, entitylargefireball.locZ());
                                    world.addEntity(entitylargefireball);
                                    this.a.shotTracker++;
                                } else if (this.a.shotTracker % 60 == 0 && this.a.shotTracker != 0) { /**every 60 shots, the blaze shoots a fireball with explosion power 1*/
                                    EntityLargeFireball entitylargefireball = new EntityLargeFireball(world, this.a, d1, d2, d3);
                                    entitylargefireball.setPosition(entitylargefireball.locX(), this.a.e(0.5D) + 0.5D, entitylargefireball.locZ());
                                    world.addEntity(entitylargefireball);
                                    this.a.shotTracker++;
                                } else {
                                    EntitySmallFireball entitysmallfireball = new EntitySmallFireball(world, this.a, d1, d2, d3); /**blaze has no inaccuracy*/
                                    entitysmallfireball.setPosition(entitysmallfireball.locX(), this.a.e(0.5D) + 0.5D, entitysmallfireball.locZ());
                                    world.addEntity(entitysmallfireball);
                                    this.a.shotTracker++;
                                }

                                if (this.a.shotTracker % 15 == 0) {
                                    EntitySmallFireball entitysmallfireball;

                                    for (double x = -1.0; x <= 1.0; x += 0.4) {
                                        for (double y = -1.0; y <= 1.0; y += 0.4) {
                                            for (double z = -1.0; z <= 1.0; z += 0.4) {
                                                entitysmallfireball = new EntitySmallFireball(world, this.a, x, y, z);
                                                entitysmallfireball.setPosition(entitysmallfireball.locX(), this.a.e(0.5D) + 0.5D, entitysmallfireball.locZ());
                                                world.addEntity(entitysmallfireball);
                                            }
                                        }
                                    }
                                }

                            } else { /**rapid fire phase for 50 shots after 350 normal shots*/
                                if (this.a.shotTracker >= 50) { /**first entering rapid fire phase*/
                                    this.a.shotTracker = 0;
                                    this.a.rapidFireTracker = 50;
                                } else {
                                    for (int i = 0; i < 8; i++) { /**shoots 8 fireballs at a time during this phase*/
                                        EntitySmallFireball entitysmallfireball = new EntitySmallFireball(world, this.a, d1 + this.a.getRandom().nextGaussian() * (double)f * 0.5, d2, d3 + this.a.getRandom().nextGaussian() * (double)f * 0.5); /**blaze has 0.5x default inaccuracy in this phase*/

                                        entitysmallfireball.setPosition(entitysmallfireball.locX(), this.a.e(0.5D) + 0.5D, entitysmallfireball.locZ());
                                        world.addEntity(entitysmallfireball);
                                    }

                                    EntitySmallFireball entitysmallfireball;

                                    if (this.a.rapidFireTracker == 1) { /**shoots a large fireball with explosion power 4 when this phase ends*/
                                        CustomEntityLargeFireball entitylargefireball = new CustomEntityLargeFireball(world, this.a, d1, d2, d3, 4);
                                        entitylargefireball.setPosition(entitylargefireball.locX(), this.a.e(0.5D) + 0.5D, entitylargefireball.locZ());
                                        world.addEntity(entitylargefireball);

                                        for (double x = -1.0; x <= 1.0; x += 0.3) {
                                            for (double y = -1.0; y <= 1.0; y += 0.3) {
                                                for (double z = -1.0; z <= 1.0; z += 0.3) {
                                                    entitysmallfireball = new EntitySmallFireball(world, this.a, x, y, z);
                                                    entitysmallfireball.setPosition(entitysmallfireball.locX(), this.a.e(0.5D) + 0.5D, entitysmallfireball.locZ());
                                                    world.addEntity(entitysmallfireball);
                                                }
                                            }
                                        }
                                    } else if (this.a.rapidFireTracker % 17 == 0){
                                        for (double x = -1.0; x <= 1.0; x += 0.4) {
                                            for (double y = -1.0; y <= 1.0; y += 0.4) {
                                                for (double z = -1.0; z <= 1.0; z += 0.4) {
                                                    entitysmallfireball = new EntitySmallFireball(world, this.a, x, y, z);
                                                    entitysmallfireball.setPosition(entitysmallfireball.locX(), this.a.e(0.5D) + 0.5D, entitysmallfireball.locZ());
                                                    world.addEntity(entitysmallfireball);
                                                }
                                            }
                                        }
                                    }

                                    this.a.rapidFireTracker--;
                                }
                            }
                        }
                    }

                    this.a.getControllerLook().a(entityliving, 10.0F, 10.0F);
                } else if (this.d < 5) {
                    this.a.getControllerMove().a(entityliving.locX(), entityliving.locY(), entityliving.locZ(), 1.0D);
                }

                super.e();
            }
        }

        private double g() {
            return this.a.b(GenericAttributes.FOLLOW_RANGE);
        }
    }
}