package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityDragonFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CustomEntityEnderDragon extends EntityEnderDragon implements ICustomMob {

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
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, false)); /**for the custom fireball goal; uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public void a(EntityEnderCrystal entityendercrystal, BlockPosition blockposition, DamageSource damagesource) { //oncrystaldestroyed
        super.a(entityendercrystal, blockposition, damagesource);

        if (entityendercrystal == this.currentEnderCrystal) { /**blowing up the end crystal that the dragon is currently healing from does not damage the dragon*/
            this.heal(10.0F);
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
        private final World nmsWorld;
        private static final Random random = new Random();

        public PathfinderGoalDragonFireball(CustomEntityEnderDragon dragon) {
            this.dragon = dragon;
            this.nmsWorld = dragon.getWorld();
        }

        @Override
        public boolean a() {
            return this.dragon.getGoalTarget() != null;
        }

        @Override
        public boolean b() {
            return this.a();
        }

        @Override
        public void e() {
            try {
                if (this.dragon.ticksLived % (40 + 8 * ((EnderDragonBattle)CustomEntityEnderDragon.dragonBattle.get(this.dragon)).c()) == 0) {
                    EntityLiving entityLiving = this.dragon.getGoalTarget();
                    double rand = random.nextDouble();

                    double x = entityLiving.locX() - this.dragon.locX();
                    double y = entityLiving.e(0.5D) - this.dragon.e(0.5D);
                    double z = entityLiving.locZ() - this.dragon.locZ();

                    if (rand < 0.5) { /**dragon shoots a fireball every (40 + 8 * numberOfAliveCrystals) ticks, with a 50% chance to shoot a custom normal fireball, 25% chance to shoot a power 2 ghast fireball, 10% chance to shoot a super fireball, 9% chance to shoot an arrow barrage, and 6% chance to shoot 2 rings of fireballs*/
                        CustomEntityDragonFireball newFireball = new CustomEntityDragonFireball(this.nmsWorld, this.dragon, x, y, z);
                        newFireball.setPosition(this.dragon.locX(), this.dragon.locY(), this.dragon.locZ());
                        this.nmsWorld.addEntity(newFireball);
                    } else if (rand < 0.75) {

                    } else if (rand < 0.85) {

                    } else if (rand < 0.94) {

                    } else {

                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}