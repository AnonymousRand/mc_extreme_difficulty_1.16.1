package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import net.minecraft.server.v1_16_R1.*;

import java.lang.reflect.Field;
import java.util.*;

public class NewPathfinderGoalBuffMobs extends PathfinderGoal {

    public EntityInsentient entity;
    private final Class<? extends EntityLiving> targetClass;
    private final HashMap<Integer, ArrayList<MobEffect>> attacksAndEffects;
    private final double rangeRadius;
    private final int attackMin, ticksDelayMin, ticksDelayRandBound;
    private final Random random = new Random();
    private Field attacks;

    public NewPathfinderGoalBuffMobs(EntityInsentient entity, Class<? extends EntityLiving> targetClass, HashMap<Integer, ArrayList<MobEffect>> attacksAndEffects, double rangeRadius, int attacksMin, int ticksDelayMin, int ticksDelayRandBound) {
        this.entity = entity;
        this.targetClass = targetClass;
        this.attacksAndEffects = attacksAndEffects;
        this.rangeRadius = rangeRadius;
        this.attackMin = attacksMin;
        this.ticksDelayMin = ticksDelayMin;
        this.ticksDelayRandBound = ticksDelayRandBound;

        try {
            this.attacks = this.entity.getClass().getDeclaredField("attacks");
            this.attacks.setAccessible(true);
        } catch (NoSuchFieldException e) {}
    }

    @Override
    public boolean a() {
        if (this.entity.ticksLived % (this.random.nextInt(this.ticksDelayRandBound) + this.ticksDelayMin) == 0) {
            try {
                if (this.attacks.getInt(this.entity) >= this.attackMin) {
                    return true;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
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
            List<Entity> nmsEntities = this.entity.getWorld().getEntities(this.entity, this.entity.getBoundingBox().g(this.rangeRadius), this.targetClass::isInstance);
            int attacksLocal = this.attacks.getInt(this.entity);

            for (Entity targetEntity : nmsEntities) {
                if (this.normalGetDistanceSq(this.entity.getPositionVector(), targetEntity.getPositionVector()) > Math.pow(this.rangeRadius, 2)) { //ensures that the entities is in a sphere around the mob and not a cube
                    continue;
                }

                for (Map.Entry<Integer, ArrayList<MobEffect>> entry : this.attacksAndEffects.entrySet()) {
                    if (attacksLocal >= entry.getKey()) { //entry.getKey is the integer in the hashmap entry
                        for (MobEffect effect : entry.getValue()) { //entry.getValue is the arraylist of mobeffects in the hashmap entry
                            ((EntityLiving)targetEntity).addEffect(effect);
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public double normalGetDistanceSq(Vec3D vec3d1, Vec3D vec3d2) {
        double d0 = vec3d2.getX() - vec3d1.getX();
        double d1 = vec3d2.getY() - vec3d1.getY();
        double d2 = vec3d2.getZ() - vec3d1.getZ();

        return d0 * d0 + d1 * d1 + d2 * d2;
    }
}
