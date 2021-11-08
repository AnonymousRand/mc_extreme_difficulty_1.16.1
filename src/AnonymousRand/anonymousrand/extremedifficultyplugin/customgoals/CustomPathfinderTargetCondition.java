package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.PathfinderTargetCondition;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.function.Predicate;

public class CustomPathfinderTargetCondition extends PathfinderTargetCondition { //used to be called EntityPredicate in Bukkit/non-Spigot source code 1.16.1
    public static final CustomPathfinderTargetCondition a = new CustomPathfinderTargetCondition();
    private double followRange = -1.0D;
    private boolean c;
    private boolean d;
    private boolean e;
    private boolean f;
    private boolean g = true;
    private Predicate<EntityLiving> h;

    private Field c1, d1, e1, f1;

    public CustomPathfinderTargetCondition a(double followRange) {
        this.followRange = followRange;

        try { //get booleans via reflection
            this.c1 = PathfinderTargetCondition.class.getDeclaredField("c");
            this.c1.setAccessible(true);
            this.c = c1.getBoolean(this);
            this.d1 = PathfinderTargetCondition.class.getDeclaredField("d");
            this.d1.setAccessible(true);
            this.d = d1.getBoolean(this);
            this.e1 = PathfinderTargetCondition.class.getDeclaredField("e");
            this.e1.setAccessible(true);
            this.e = e1.getBoolean(this);
            this.f1 = PathfinderTargetCondition.class.getDeclaredField("f");
            this.f1.setAccessible(true);
            this.f = f1.getBoolean(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return this;
    }

    public CustomPathfinderTargetCondition a(@Nullable Predicate<EntityLiving> predicate) {
        this.h = predicate;
        return this;
    }

    @Override
    public boolean a(@Nullable EntityLiving entityliving, EntityLiving entityliving1) { //turn off line of sight requirement for initially finding target player using nearestAttackableTarget goal which uses getClosestEntity/Player in CustomIEntityAccess which uses this function CustomPathfinderTargetCondition.a (canTarget) which no longer requires EntitySenses.a (canSee) to be true
        if (entityliving == entityliving1) {
            return false;
        } else if (entityliving1.isSpectator()) {
            return false;
        } else if (!entityliving1.isAlive()) {
            return false;
        } else if (!this.c && entityliving1.isInvulnerable()) {
            return false;
        } else if (this.h != null && !this.h.test(entityliving1)) {
            return false;
        } else {
            if (entityliving != null) {
                if (!this.f) {
                    if (!entityliving.d(entityliving1)) {
                        return false;
                    }

                    if (!entityliving.a(entityliving1.getEntityType())) {
                        return false;
                    }
                }

                if (!this.d && entityliving.r(entityliving1)) {
                    return false;
                }

                if (this.followRange > 0.0D) { /**skulls and invis potions no longer do anything against detection range*/
                    double entityDistanceSq = entityliving.g(entityliving1.locX(), entityliving1.locY(), entityliving1.locZ());

                    if (entityDistanceSq > this.followRange * this.followRange) {
                        return false;
                    }
                }
            }

            return true;
        }
    }
}
