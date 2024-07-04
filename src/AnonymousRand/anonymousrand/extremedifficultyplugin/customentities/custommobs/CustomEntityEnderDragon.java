package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityDragonFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityDragonFireballSuper;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntitySmallFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableMobShootArrows;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CustomEntityEnderDragon extends EntityEnderDragon implements ICustomHostile {

    public ArrayList<Entity> goalTargets = new ArrayList<>();
    public Field phaseManager, dragonBattle;

    public CustomEntityEnderDragon(World world, UUID uuid) {
        super(EntityTypes.ENDER_DRAGON, world);
        this.initCustomHostile();
        this.uniqueID = uuid; // to make sure bossbar etc. doesn't break
    }

    //////////////////////////////////////  ICustomHostile  ///////////////////////////////////////

    public void initCustomHostile() {
        try {
            this.phaseManager = EntityEnderDragon.class.getDeclaredField("bN");
            this.phaseManager.setAccessible(true);
            this.dragonBattle = EntityEnderDragon.class.getDeclaredField("bM");
            this.dragonBattle.setAccessible(true);
            ((DragonControllerManager)this.phaseManager.get(this)).setControllerPhase(DragonControllerPhase.HOLDING_PATTERN); // make sure it is moving and not perched when spawning
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public double getFollowRange() { /** dragon has 128 block detection range for new constant fireballs */
        return 128.0;
    }

    @Override
    public double g(double x, double y, double z) {
        double dist_x = this.locX() - x;
        double dist_z = this.locZ() - z;

        return dist_x * dist_x + dist_z * dist_z;
    }

    @Override
    public double d(Vec3D vec3d) {
        double dist_x = this.locX() - vec3d.x;
        double dist_z = this.locZ() - vec3d.z;

        return dist_x * dist_x + dist_z * dist_z;
    }

    ////////////////////////////////////  IAttackLevelingMob  /////////////////////////////////////
    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(1, new CustomEntityEnderDragon.PathfinderGoalDragonFireball(this)); /** custom goal that allows the dragon to constantly shoot fireballs instead of only during the strafe phase */
        this.targetSelector.a(1, new CustomEntityEnderDragon.PathfinderGoalDragonNearestAttackableTarget(this, EntityPlayer.class)); /** for the custom fireball goal; uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
    }

    @Override
    public void a(EntityEnderCrystal entityEnderCrystal, BlockPosition blockPosition, DamageSource damagesource) { // oncrystaldestroyed
        super.a(entityEnderCrystal, blockPosition, damagesource);

        if (entityEnderCrystal == this.currentEnderCrystal) { /** blowing up the end crystal that the dragon is currently healing from does not damage the dragon */
            this.heal(10.0F);
        }

        if (damagesource.getEntity() != null) { /** dragon shoots a super fireball when crystal destroyed and rapidfires 10 normal custom fireballs, then shoots another super fireball */
            Entity entity = damagesource.getEntity();
            new RunnableDragonRapidShootFireballs(this, entity, 12).runTaskTimer(StaticPlugin.plugin, 0L, 4L);
        }
    }

    @Override
    public void movementTick() {
        super.movementTick();

        this.goalSelector.doTick();
        this.targetSelector.doTick();

        if (!this.dk()) {
            this.healFromCrystal(); /** crystals heal the dragon 10 health per half second */
        }
    }

    private void healFromCrystal() {
        if (this.currentEnderCrystal != null) {
            if (this.currentEnderCrystal.dead) {
                this.currentEnderCrystal = null;
            } else if (this.ticksLived % 10 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.setHealth(this.getHealth() + 9.0F);
            }
        }

        if (this.random.nextInt(10) == 0) {
            List<EntityEnderCrystal> list = this.world.a(EntityEnderCrystal.class, this.getBoundingBox().grow(64.0, 128.0, 64.0)); /** dragon has double the horizontal range for finding end crystals */
            EntityEnderCrystal entityEnderCrystal = null;
            double d0 = Double.MAX_VALUE;

            for (EntityEnderCrystal entityEnderCrystal1 : list) {
                double d1 = entityEnderCrystal1.h(this);

                if (d1 < d0) {
                    d0 = d1;
                    entityEnderCrystal = entityEnderCrystal1;
                }
            }

            this.currentEnderCrystal = entityEnderCrystal;
        }
    }

    static class PathfinderGoalDragonFireball extends PathfinderGoal {

        private final CustomEntityEnderDragon dragon;

        public PathfinderGoalDragonFireball(CustomEntityEnderDragon dragon) {
            this.dragon = dragon;
        }

        @Override
        public boolean a() {
            if (this.dragon.goalTargets != null) {
                return this.dragon.goalTargets.size() > 0;
            }

            return false;
        }

        @Override
        public boolean b() {
            return this.a();
        }

        @Override
        public void e() {
            try {
                if (this.dragon.ticksLived % Math.floor((45 + 9 * ((EnderDragonBattle)this.dragon.dragonBattle.get(this.dragon)).c()) + 200 * Math.log10(Bukkit.getServer().getOnlinePlayers().size() + 1)) == 0) { /** shoots faster when there are fewer crystals and fewer players */
                    new RunnableDragonShootProjectiles(this.dragon).run();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    static class PathfinderGoalDragonNearestAttackableTarget extends PathfinderGoal {

        private final CustomEntityEnderDragon dragon;
        protected final Class targetClass;

        public PathfinderGoalDragonNearestAttackableTarget(CustomEntityEnderDragon dragon, Class targetClass) {
            this.dragon = dragon;
            this.targetClass = targetClass;
        }

        @Override
        public boolean a() {
            return this.dragon.ticksLived % 100 == 0;
        }

        @Override
        public boolean b() {
            return this.a();
        }

        @Override
        public void e() {
            this.dragon.goalTargets = new ArrayList<>(this.dragon.getWorld().getEntities(this.dragon, this.dragon.getBoundingBox().g(this.dragon.getFollowRange()), this.targetClass::isInstance));
        }
    }

    static class RunnableDragonShootProjectiles extends BukkitRunnable {

        private final CustomEntityEnderDragon dragon;
        private final World nmsWorld;
        private static final Random random = new Random();

        public RunnableDragonShootProjectiles(CustomEntityEnderDragon dragon) {
            this.dragon = dragon;
            this.nmsWorld = dragon.getWorld();
        }

        @Override
        public void run() {
            double rand = random.nextDouble();
            double rand2 = random.nextDouble();

            for (Entity entity : this.dragon.goalTargets) { /** dragon shoots a random projectile/fireball at every player within 128 blocks */
                if (rand2 < 0.5) { /** 50% of the time the dragon shoots the same projectile to every player */
                    rand = random.nextDouble();
                }

                if (!entity.isAlive() || entity.getWorld().getWorld().getEnvironment() != org.bukkit.World.Environment.THE_END || ((EntityPlayer)entity).abilities.isInvulnerable) {
                    continue;
                }

                double x = entity.locX() - this.dragon.locX();
                double y = entity.e(0.5D) - this.dragon.e(0.5D);
                double z = entity.locZ() - this.dragon.locZ();

                if (rand < 0.65) { /** dragon shoots a fireball every (45 + 8 * numberOfAliveCrystals) ticks, with a 65% chance to shoot a custom normal fireball, 22.5% chance to shoot a power 2 ghast fireball, 8.5% chance to shoot an arrow barrage, and 4% chance to shoot a super fireball */
                    CustomEntityDragonFireball newFireball = new CustomEntityDragonFireball(this.nmsWorld, this.dragon, x, y, z, true);
                    newFireball.setPosition(this.dragon.locX(), this.dragon.locY(), this.dragon.locZ());
                    this.nmsWorld.addEntity(newFireball);
                } else if (rand < 0.875) {
                    CustomEntitySmallFireball newFireball = new CustomEntitySmallFireball(this.nmsWorld, this.dragon, x, y, z);
                    newFireball.setPosition(this.dragon.locX(), this.dragon.locY(), this.dragon.locZ());
                    this.nmsWorld.addEntity(newFireball);
                } else if (rand < 0.96) {
                    new RunnableMobShootArrows(this.dragon, (EntityLiving)entity, 12, 1, 30.0, 2, true, true, (int)Math.ceil(10 * Math.pow(0.9, Bukkit.getServer().getOnlinePlayers().size() + 6))).runTaskTimer(StaticPlugin.plugin, 0L, 4L); /** 2 pierce; fewer cycles with more players to reduce lag */
                } else {
                    CustomEntityDragonFireballSuper newFireball = new CustomEntityDragonFireballSuper(this.nmsWorld, this.dragon, x, y, z, true);
                    newFireball.setPosition(this.dragon.locX(), this.dragon.locY(), this.dragon.locZ());
                    this.nmsWorld.addEntity(newFireball);
                }
            }
        }
    }

    static class RunnableDragonRapidShootFireballs extends BukkitRunnable {

        private final CustomEntityEnderDragon dragon;
        private final World nmsWorld;
        private final Entity targetEntity;
        private int cycles;
        private final int maxCycles;

        public RunnableDragonRapidShootFireballs(CustomEntityEnderDragon dragon, Entity targetEntity, int maxCycles) {
            this.dragon = dragon;
            this.nmsWorld = dragon.getWorld();
            this.targetEntity = targetEntity;
            this.cycles = 0;
            this.maxCycles = maxCycles;
        }

        @Override
        public void run() {
            if (++this.cycles > this.maxCycles) {
                this.cancel();
                return;
            }

            double x = this.targetEntity.locX() - this.dragon.locX();
            double y = this.targetEntity.e(0.5D) - this.dragon.e(0.5D);
            double z = this.targetEntity.locZ() - this.dragon.locZ();


            if (this.cycles == 1 || this.cycles == this.maxCycles) {
                CustomEntityDragonFireballSuper newFireballSuper = new CustomEntityDragonFireballSuper(this.nmsWorld, this.dragon, x, y, z, false);
                newFireballSuper.setPosition(this.dragon.locX(), this.dragon.locY(), this.dragon.locZ());
                this.nmsWorld.addEntity(newFireballSuper);
            } else {
                CustomEntityDragonFireball newFireball = new CustomEntityDragonFireball(this.nmsWorld, this.dragon, x, y, z, false);
                newFireball.setPosition(this.dragon.locX(), this.dragon.locY(), this.dragon.locZ());
                this.nmsWorld.addEntity(newFireball);
            }
        }
    }
}