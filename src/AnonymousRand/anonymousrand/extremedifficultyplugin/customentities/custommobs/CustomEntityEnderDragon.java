package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityDragonFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityDragonFireballSuper;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntitySmallFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableMobShootArrowsNormally;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.*;

public class CustomEntityEnderDragon extends EntityEnderDragon implements ICustomMob {

    public ArrayList<Entity> goalTargets = new ArrayList<>();
    public static Field phaseManager, dragonBattle;

    public CustomEntityEnderDragon(World world) {
        super(EntityTypes.ENDER_DRAGON, world);

        try {
            ((DragonControllerManager)phaseManager.get(this)).setControllerPhase(DragonControllerPhase.HOLDING_PATTERN); //make sure it is moving and not perched when spawning
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    static {
        try {
            phaseManager = EntityEnderDragon.class.getDeclaredField("bN");
            phaseManager.setAccessible(true);
            dragonBattle = EntityEnderDragon.class.getDeclaredField("bM");
            dragonBattle.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(1, new CustomEntityEnderDragon.PathfinderGoalDragonFireball(this)); /**custom goal that allows the dragon to constantly shoot fireballs instead of only during the strafe phase*/
        this.targetSelector.a(1, new CustomEntityEnderDragon.PathfinderGoalDragonNearestAttackableTarget(this, EntityCow.class)); /**for the custom fireball goal; uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public void a(EntityEnderCrystal entityendercrystal, BlockPosition blockposition, DamageSource damagesource) { //oncrystaldestroyed
        super.a(entityendercrystal, blockposition, damagesource);

        if (entityendercrystal == this.currentEnderCrystal) { /**blowing up the end crystal that the dragon is currently healing from does not damage the dragon*/
            this.heal(10.0F);
        }

        if (damagesource.getEntity() != null) { /**dragon shoots a super fireball when crystal destroyed and rapidfires 10 normal custom fireballs, then shoots another super fireball*/
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
            this.healFromCrystal(); /**crystals heal the dragon 10 health per half second*/
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
            List<EntityEnderCrystal> list = this.world.a(EntityEnderCrystal.class, this.getBoundingBox().grow(64.0, 128.0, 64.0)); /**dragon has double the horizontal range for finding end crystals*/
            EntityEnderCrystal entityendercrystal = null;
            double d0 = Double.MAX_VALUE;
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                EntityEnderCrystal entityendercrystal1 = (EntityEnderCrystal) iterator.next();
                double d1 = entityendercrystal1.h(this);

                if (d1 < d0) {
                    d0 = d1;
                    entityendercrystal = entityendercrystal1;
                }
            }

            this.currentEnderCrystal = entityendercrystal;
        }
    }

    public double getFollowRange() { /**dragon has 128 block detection range for new constant fireballs*/
        return 128.0;
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
                if (this.dragon.ticksLived % Math.floor((45 + 8 * ((EnderDragonBattle)CustomEntityEnderDragon.dragonBattle.get(this.dragon)).c()) + 110 * Math.log10(Bukkit.getServer().getOnlinePlayers().size() + 1)) == 0) { /**shoots faster when there are less crystals and less players*/
                    new RunnableDragonShootProjectiles(this.dragon);
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
        private double rand, rand2, x, y, z;
        private static final Random random = new Random();

        public RunnableDragonShootProjectiles(CustomEntityEnderDragon dragon) {
            this.dragon = dragon;
            this.nmsWorld = dragon.getWorld();
            this.run();
        }

        @Override
        public void run() {
            this.rand = random.nextDouble();
            this.rand2 = random.nextDouble();

            for (Entity entity : this.dragon.goalTargets) { /**dragon shoots a random projectile/fireball at every player within 128 blocks*/
                if (this.rand2 < 0.5) { /**50% of the time the dragon shoots the same projectile to every player*/
                    this.rand = random.nextDouble();
                }

                if (!entity.isAlive() || entity.getWorld().getWorld().getEnvironment() != org.bukkit.World.Environment.THE_END) {
                    continue;
                }

                this.x = entity.locX() - this.dragon.locX();
                this.y = entity.e(0.5D) - this.dragon.e(0.5D);
                this.z = entity.locZ() - this.dragon.locZ();

                if (rand < 0.55) { /**dragon shoots a fireball every (45 + 8 * numberOfAliveCrystals) ticks, with a 55% chance to shoot a custom normal fireball, 25% chance to shoot a power 2 ghast fireball, 10% chance to shoot a super fireball, and 10% chance to shoot an arrow barrage*/
                    CustomEntityDragonFireball newFireball = new CustomEntityDragonFireball(this.nmsWorld, this.dragon, this.x, this.y, this.z, true);
                    newFireball.setPosition(this.dragon.locX(), this.dragon.locY(), this.dragon.locZ());
                    this.nmsWorld.addEntity(newFireball);
                } else if (rand < 0.8) {
                    CustomEntitySmallFireball newFireball = new CustomEntitySmallFireball(this.nmsWorld, this.dragon, this.x, this.y, this.z);
                    newFireball.setPosition(this.dragon.locX(), this.dragon.locY(), this.dragon.locZ());
                    this.nmsWorld.addEntity(newFireball);
                } else if (rand < 0.9) {
                    CustomEntityDragonFireballSuper newFireball = new CustomEntityDragonFireballSuper(this.nmsWorld, this.dragon, this.x, this.y, this.z, true);
                    newFireball.setPosition(this.dragon.locX(), this.dragon.locY(), this.dragon.locZ());
                    this.nmsWorld.addEntity(newFireball);
                } else {
                    new RunnableMobShootArrowsNormally(this.dragon, (EntityLiving)entity, 12, 1, 30.0, 2, true, true, (int)Math.ceil(10 * Math.pow(0.9, Bukkit.getServer().getOnlinePlayers().size() + 6))).runTaskTimer(StaticPlugin.plugin, 0L, 4L); /**2 pierce; less cycles with more players to reduce lag*/
                }
            }
        }
    }

    static class RunnableDragonRapidShootFireballs extends BukkitRunnable {

        private final CustomEntityEnderDragon dragon;
        private final World nmsWorld;
        private Entity targetEntity;
        private int cycles;
        private final int maxCycles;
        private CustomEntityDragonFireball newFireball;
        private CustomEntityDragonFireballSuper newFireballSuper;
        private double x, y, z;

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

            this.x = this.targetEntity.locX() - this.dragon.locX();
            this.y = this.targetEntity.e(0.5D) - this.dragon.e(0.5D);
            this.z = this.targetEntity.locZ() - this.dragon.locZ();


            if (this.cycles == 1 || this.cycles == this.maxCycles) {
                this.newFireballSuper = new CustomEntityDragonFireballSuper(this.nmsWorld, this.dragon, this.x, this.y, this.z, false);
                this.newFireballSuper.setPosition(this.dragon.locX(), this.dragon.locY(), this.dragon.locZ());
                this.nmsWorld.addEntity(this.newFireballSuper);
            } else {
                this.newFireball = new CustomEntityDragonFireball(this.nmsWorld, this.dragon, this.x, this.y, this.z, false);
                this.newFireball.setPosition(this.dragon.locX(), this.dragon.locY(), this.dragon.locZ());
                this.nmsWorld.addEntity(this.newFireball);
            }
        }
    }
}