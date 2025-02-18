package AnonymousRand.anonymousrand.plugin.nms.customgoals;

import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.plugin.util.NmsUtil;
import net.minecraft.server.v1_16_R1.*;

import java.util.*;

public class CustomPathfinderGoalBuffMobs<T extends EntityInsentient & IAttackLevelingMob> extends PathfinderGoal {

    public T goalOwner;
    private final Class<? extends EntityLiving> targetClass;
    private final HashMap<Integer, ArrayList<MobEffect>> attacksAndEffects;
    private final double rangeRadius;
    private final int attacksThresh, minTicksDelay, ticksDelayRandBound;
    private static final Random random = new Random();

    public CustomPathfinderGoalBuffMobs(T goalOwner, Class<? extends EntityLiving> targetClass, HashMap<Integer, ArrayList<MobEffect>> attacksAndEffects, double rangeRadius, int attacksThresh, int minTicksDelay, int ticksDelayRandBound) {
        this.goalOwner = goalOwner;
        this.targetClass = targetClass;
        this.attacksAndEffects = attacksAndEffects;
        this.rangeRadius = rangeRadius;
        this.attacksThresh = attacksThresh;
        this.minTicksDelay = minTicksDelay;
        this.ticksDelayRandBound = ticksDelayRandBound;
    }

    @Override
    public boolean a() {
        if (this.goalOwner.ticksLived % (random.nextInt(this.ticksDelayRandBound) + this.minTicksDelay) == 0) {
            if (this.goalOwner.getAttacks() >= this.attacksThresh) {
                return true;
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
        int attacksLocal = this.goalOwner.getAttacks();
        this.goalOwner.getWorld().getEntities(this.goalOwner, this.goalOwner.getBoundingBox().g(this.rangeRadius), this.targetClass::isInstance).forEach(entity -> {
            if (entity instanceof EntityPlayer || NmsUtil.distSq(this.goalOwner, entity, false) > Math.pow(this.rangeRadius, 2)) { // ensures that the entities is in a sphere around the mob and not a cube
                return;
            }

            for (Map.Entry<Integer, ArrayList<MobEffect>> entry : this.attacksAndEffects.entrySet()) {
                if (attacksLocal >= entry.getKey()) { // entry.getKey is the integer in the hashmap entry
                    for (MobEffect effect : entry.getValue()) { // entry.getValue is the arraylist of mobeffects in the hashmap entry
                        ((EntityLiving) entity).addEffect(effect);
                    }
                }
            }
        });
    }
}
