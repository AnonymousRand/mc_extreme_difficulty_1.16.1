package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.PathfinderTargetCondition;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.function.Predicate;

public class CustomPathfinderTargetCondition extends PathfinderTargetCondition { // used to be called EntityPredicate in Bukkit/non-Spigot source code 1.16.1
    public static final CustomPathfinderTargetCondition a = new CustomPathfinderTargetCondition();
    private double followRange = -1.0D;
    private boolean c;
    private boolean d;
    private boolean f;
    private Predicate<EntityLiving> h;
    private static Field c1, d1, f1;

    public CustomPathfinderTargetCondition() {
        super();

        try {
            c1 = PathfinderTargetCondition.class.getDeclaredField("c");
            c1.setAccessible(true);
            d1 = PathfinderTargetCondition.class.getDeclaredField("d");
            d1.setAccessible(true);
            f1 = PathfinderTargetCondition.class.getDeclaredField("f");
            f1.setAccessible(true);
            this.c = c1.getBoolean(this);
            this.d = d1.getBoolean(this);
            this.f = f1.getBoolean(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean a(@Nullable EntityLiving attacker, EntityLiving target) { // turn off line of sight requirement for initially finding target player using nearestAttackableTarget goal which uses getClosestEntity/Player in CustomIEntityAccess which uses this function CustomPathfinderTargetCondition.a (canTarget) which no longer requires EntitySenses.a (canSee) to be true
        if (attacker == target) {
            return false;
        } else if (target.isSpectator()) {
            return false;
        } else if (!target.isAlive()) {
            return false;
        } else if (!this.c && target.isInvulnerable()) {
            return false;
        } else if (this.h != null && !this.h.test(target)) {
            return false;
        } else {
            if (attacker != null) {
                if (!this.f) {
                    if (!attacker.d(target)) {
                        return false;
                    }

                    if (!attacker.a(target.getEntityType())) {
                        return false;
                    }
                }

                if (!this.d && attacker.r(target)) {
                    return false;
                }

                if (this.followRange > 0.0D) { /* skulls and invis potions no longer do anything against detection range */
                    double entityDistanceSq = attacker.g(target.locX(), target.locY(), target.locZ());

                    if (entityDistanceSq > this.followRange * this.followRange) {
                        return false;
                    }
                }
            }

            return true;
        }
    }


    @Override
    public CustomPathfinderTargetCondition a(double followRange) {
        this.followRange = followRange;
        return this;
    }

    @Override
    public CustomPathfinderTargetCondition a(@Nullable Predicate<EntityLiving> predicate) {
        this.h = predicate;
        return this;
    }

    @Override
    public CustomPathfinderTargetCondition b() {
        this.d = true;
        return this;
    }
}
