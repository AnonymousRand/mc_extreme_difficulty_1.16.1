package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.ExtremeDifficultyPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.projectiles.CustomEntityDragonFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.projectiles.CustomEntityDragonFireballSuper;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.projectiles.CustomEntitySmallFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NmsUtil;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableMobShootArrows;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CustomEntityEnderDragon extends EntityEnderDragon implements ICustomHostile {

    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;
    public ArrayList<Entity> targets = new ArrayList<>();

    public CustomEntityEnderDragon(World world, UUID uuid) {
        super(EntityTypes.ENDER_DRAGON, world);
        this.uniqueID = uuid;                                                                        // to make sure bossbar etc. doesn't break
        this.getDragonControllerManager().setControllerPhase(DragonControllerPhase.HOLDING_PATTERN); // make sure it is moving and not perched when spawning (apparently I have to do this)
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `ICustomHostile`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public double getDetectionRange() {
        /* Ender dragons have 128 block detection range for new constant fireball attack */
        return 128.0;
    }

    public boolean ignoresLOS() {
        return IGNORE_LOS;
    }

    public boolean ignoresY() {
        return IGNORE_Y;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `IAttackLevelingMob`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(1, new CustomEntityEnderDragon.PathfinderGoalFireball(this));                                      /* Constantly shoots fireballs instead of only during the strafe phase */
        this.targetSelector.a(1, new CustomEntityEnderDragon.PathfinderGoalNearestAttackableTarget(this, EntityPlayer.class)); /* Ignores y-level, line of sight, and invis/skulls to initially find a target or maintain it as the target (for the custom fireball goal) */
    }

    @Override // `onCrystalDestroyed()` 
    public void a(EntityEnderCrystal enderCrystal, BlockPosition blockPos, DamageSource damageSource) {
        super.a(enderCrystal, blockPos, damageSource);

        /* Blowing up the end crystal that the ender dragon is currently healing from does not damage the dragon */
        if (enderCrystal == this.currentEnderCrystal) {
            this.heal(10.0F);
        }

        /* When a crystal is destroyed, dragons shoot a super fireball, then rapid fire 10 normal custom fireballs, then
         * shoot another super fireball */
        if (damageSource.getEntity() != null) {
            Entity damageSourceEntity = damageSource.getEntity();
            new RunnableDragonRapidShootFireballs(this, damageSourceEntity, 12).runTaskTimer(
                    ExtremeDifficultyPlugin.plugin, 0L, 4L);
        }
    }

    @Override
    public void movementTick() {
        super.movementTick();

        this.goalSelector.doTick();
        this.targetSelector.doTick();

        if (!this.dk()) {
            this.healFromCrystal();
        }
    }

    private void healFromCrystal() {
        if (this.currentEnderCrystal != null) {
            if (this.currentEnderCrystal.dead) {
                this.currentEnderCrystal = null;
            } else if (this.ticksLived % 10 == 0 && this.getHealth() < this.getMaxHealth()) {
                /* Crystals heal the dragon 10 health per half second instead of 1 */
                this.setHealth(this.getHealth() + 9.0F);
            }
        }

        /* Dragon has +50% horizontal range for finding end crystals */
        if (this.random.nextInt(10) == 0) {
            List<EntityEnderCrystal> enderCrystals = this.world.a(EntityEnderCrystal.class,
                    this.getBoundingBox().g(48.0));
            EntityEnderCrystal nearestEnderCrystal = null;
            double distToNearestCrystal = Double.MAX_VALUE;

            for (EntityEnderCrystal enderCrystal : enderCrystals) {
                double distToCrystal = NmsUtil.distSq(this, enderCrystal, false);

                if (distToCrystal < distToNearestCrystal) {
                    distToNearestCrystal = distToCrystal;
                    nearestEnderCrystal = enderCrystal;
                }
            }

            this.currentEnderCrystal = nearestEnderCrystal;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Nested Classes
    ///////////////////////////////////////////////////////////////////////////////////////////////

    static class PathfinderGoalFireball extends PathfinderGoal {

        private final CustomEntityEnderDragon enderDragon;

        public PathfinderGoalFireball(CustomEntityEnderDragon enderDragon) {
            this.enderDragon = enderDragon;
        }

        @Override
        public boolean a() {
            if (this.enderDragon.targets != null) {
                return !this.enderDragon.targets.isEmpty();
            }

            return false;
        }

        @Override
        public boolean b() {
            return this.a();
        }

        @Override
        public void e() {
            /* Dragon shoots faster when there are fewer crystals left */
            if (this.enderDragon.ticksLived % (150 + 10 * this.enderDragon.getEnderDragonBattle().c()) == 0) {
                new RunnableDragonShootProjectiles(this.enderDragon).run();
            }
        }
    }

    static class PathfinderGoalNearestAttackableTarget extends PathfinderGoal {

        private final CustomEntityEnderDragon enderDragon;
        protected final Class targetClass;

        public PathfinderGoalNearestAttackableTarget(CustomEntityEnderDragon enderDragon, Class targetClass) {
            this.enderDragon = enderDragon;
            this.targetClass = targetClass;
        }

        @Override
        public boolean a() {
            return this.enderDragon.ticksLived % 100 == 0;
        }

        @Override
        public boolean b() {
            return this.a();
        }

        @Override
        public void e() {
            this.enderDragon.targets = new ArrayList<>(this.enderDragon.getWorld().getEntities(this.enderDragon,
                    this.enderDragon.getBoundingBox().g(this.enderDragon.getDetectionRange()),
                    this.targetClass::isInstance));
        }
    }

    static class RunnableDragonShootProjectiles extends BukkitRunnable {

        private final CustomEntityEnderDragon enderDragon;
        private final World nmsWorld;
        private static final Random random = new Random();

        public RunnableDragonShootProjectiles(CustomEntityEnderDragon enderDragon) {
            this.enderDragon = enderDragon;
            this.nmsWorld = enderDragon.getWorld();
        }

        @Override
        public void run() {
            double rand;

            /* Dragons shoot at every player within its detection range */
            for (Entity entity : this.enderDragon.targets) {
                if (!entity.isAlive() || entity.getWorld().getWorld().getEnvironment()
                        != org.bukkit.World.Environment.THE_END || ((EntityPlayer) entity).abilities.isInvulnerable) {
                    continue;
                }

                double x = entity.locX() - this.enderDragon.locX();
                double y = entity.e(0.5) - this.enderDragon.e(0.5);
                double z = entity.locZ() - this.enderDragon.locZ();

                /* 68% chance to shoot a custom normal fireball, 20% chance to shoot a power 2 ghast fireball,
                8% chance to shoot an arrow barrage with 2 pierce arrows, and 4% chance to shoot a super fireball */
                rand = random.nextDouble();
                if (rand < 0.68) {
                    CustomEntityDragonFireball newFireball =
                            new CustomEntityDragonFireball(this.nmsWorld, this.enderDragon, x, y, z, true);
                    newFireball.setPosition(this.enderDragon.locX(), this.enderDragon.locY(), this.enderDragon.locZ());
                    this.nmsWorld.addEntity(newFireball);
                } else if (rand < 0.88) {
                    CustomEntitySmallFireball newFireball =
                            new CustomEntitySmallFireball(this.nmsWorld, this.enderDragon, x, y, z);
                    newFireball.setPosition(this.enderDragon.locX(), this.enderDragon.locY(), this.enderDragon.locZ());
                    this.nmsWorld.addEntity(newFireball);
                } else if (rand < 0.96) {
                    new RunnableMobShootArrows(this.enderDragon, (EntityLiving) entity, 12, 1, 30.0, 2, true, true, 10)
                            .runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 4L);
                } else {
                    CustomEntityDragonFireballSuper newFireball =
                            new CustomEntityDragonFireballSuper(this.nmsWorld, this.enderDragon, x, y, z, true);
                    newFireball.setPosition(this.enderDragon.locX(), this.enderDragon.locY(), this.enderDragon.locZ());
                    this.nmsWorld.addEntity(newFireball);
                }
            }
        }
    }

    static class RunnableDragonRapidShootFireballs extends BukkitRunnable {

        private final CustomEntityEnderDragon enderDragon;
        private final World nmsWorld;
        private final Entity targetEntity;
        private int cycleCount;
        private final int maxCycleCount;

        public RunnableDragonRapidShootFireballs(CustomEntityEnderDragon enderDragon, Entity target, int maxCycleCount) {
            this.enderDragon = enderDragon;
            this.nmsWorld = enderDragon.getWorld();
            this.targetEntity = target;
            this.cycleCount = 0;
            this.maxCycleCount = maxCycleCount;
        }

        @Override
        public void run() {
            this.cycleCount++;
            if (this.cycleCount > this.maxCycleCount) {
                this.cancel();
                return;
            }

            double x = this.targetEntity.locX() - this.enderDragon.locX();
            double y = this.targetEntity.e(0.5) - this.enderDragon.e(0.5);
            double z = this.targetEntity.locZ() - this.enderDragon.locZ();

            if (this.cycleCount == 1 || this.cycleCount == this.maxCycleCount) {
                CustomEntityDragonFireballSuper newFireballSuper =
                        new CustomEntityDragonFireballSuper(this.nmsWorld, this.enderDragon, x, y, z, false);
                newFireballSuper.setPosition(this.enderDragon.locX(), this.enderDragon.locY(), this.enderDragon.locZ());
                this.nmsWorld.addEntity(newFireballSuper);
            } else {
                CustomEntityDragonFireball newFireball =
                        new CustomEntityDragonFireball(this.nmsWorld, this.enderDragon, x, y, z, false);
                newFireball.setPosition(this.enderDragon.locX(), this.enderDragon.locY(), this.enderDragon.locZ());
                this.nmsWorld.addEntity(newFireball);
            }
        }
    }
}
