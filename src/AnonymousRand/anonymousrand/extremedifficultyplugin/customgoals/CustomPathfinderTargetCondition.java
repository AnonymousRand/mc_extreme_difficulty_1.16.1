package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.PathfinderTargetCondition;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.function.Predicate;

public class CustomPathfinderTargetCondition extends PathfinderTargetCondition { // used to be called targetPredicate in Bukkit/non-Spigot source code 1.16.1
    public static final CustomPathfinderTargetCondition a = new CustomPathfinderTargetCondition();
    private double detectionRange = -1.0D;
    private boolean c;
    private boolean d;
    private boolean f;
    private Predicate<EntityLiving> targetPredicate;
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
        } else if (this.targetPredicate != null && !this.targetPredicate.test(target)) { // test passing targetPredicate
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

                // test within range
                if (this.detectionRange > 0.0D) { /* skulls and invis potions no longer do anything against detection range */ // todo test
                    double entityDistSq = attacker.g(target.locX(), target.locY(), target.locZ());

                    if (entityDistSq > this.detectionRange * this.detectionRange) {
                        return false;
                    }
                }
            }

            return true;
        }
    }


    @Override
    public CustomPathfinderTargetCondition a(double detectionRange) {
        this.detectionRange = detectionRange;
        return this;
    }

    @Override
    public CustomPathfinderTargetCondition a(@Nullable Predicate<EntityLiving> predicate) {
        this.targetPredicate = predicate;
        return this;
    }

    @Override
    public CustomPathfinderTargetCondition b() {
        this.d = true;
        return this;
    }
}