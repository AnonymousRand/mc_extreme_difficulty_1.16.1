package AnonymousRand.ExtremeDifficultyPlugin.customGoals;

import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.PathfinderTargetCondition;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class CustomPathfinderTargetCondition extends PathfinderTargetCondition { //used to be called EntityPredicate in Bukkit/non-Spigot source code 1.16.1
    public static final CustomPathfinderTargetCondition a = new CustomPathfinderTargetCondition();
    private double b = -1.0D;
    private boolean c;
    private boolean d;
    private boolean e;
    private boolean f;
    private boolean g = true;
    private Predicate<EntityLiving> h;

    public CustomPathfinderTargetCondition a(double d0) {
        this.b = d0;
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

                if (this.b > 0.0D) {
                    double d0 = this.g ? entityliving1.A(entityliving) : 1.0D;
                    double d1 = this.b * d0;
                    double d2 = entityliving.g(entityliving1.locX(), entityliving1.locY(), entityliving1.locZ());

                    if (d2 > d1 * d1) {
                        return false;
                    }
                }
            }

            return true;
        }
    }
}
