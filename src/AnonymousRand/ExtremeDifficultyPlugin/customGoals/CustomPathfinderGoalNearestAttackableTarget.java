package AnonymousRand.ExtremeDifficultyPlugin.customGoals;

import net.minecraft.server.v1_16_R1.*;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.function.Predicate;

public class CustomPathfinderGoalNearestAttackableTarget<T extends EntityLiving> extends CustomPathfinderGoalTarget implements CustomIEntityAccess {
    protected final Class<T> targetClass;
    protected final int targetChance;
    protected EntityLiving nearestTarget;
    protected CustomPathfinderTargetCondition targetCondition;

    public CustomPathfinderGoalNearestAttackableTarget(EntityInsentient entityinsentient, Class<T> oclass, boolean checkSight) {
        this(entityinsentient, oclass, checkSight, false);
    }

    public CustomPathfinderGoalNearestAttackableTarget(EntityInsentient entityinsentient, Class<T> oclass, boolean checkSight, boolean nearbyOnly) {
        this(entityinsentient, oclass, 10, checkSight, nearbyOnly, (Predicate) null);
    }

    public CustomPathfinderGoalNearestAttackableTarget(EntityInsentient entityinsentient, Class<T> oclass, int i, boolean flag, boolean flag1, @Nullable Predicate<EntityLiving> predicate) {
        super(entityinsentient, flag, flag1);
        this.targetClass = oclass;
        this.targetChance = i;
        this.a(EnumSet.of(PathfinderGoal.Type.TARGET));
        this.targetCondition = (new CustomPathfinderTargetCondition()).a(this.k()).a(predicate);
    }

    @Override
    public boolean a() {
        if (this.targetChance > 0 && this.e.getRandom().nextInt(this.targetChance) != 0) {
            return false;
        } else {
            this.g();
            return this.nearestTarget != null;
        }
    }

    protected AxisAlignedBB a(double d0) {
        return this.e.getBoundingBox().grow(d0, 4.0D, d0);
    }

    @Override
    public void c() {
        this.e.setGoalTarget(this.nearestTarget);
        super.c();
    }

    public void a(@Nullable EntityLiving entityliving) {
        this.nearestTarget = entityliving;
    }

    protected void g() {
        if (this.targetClass != EntityHuman.class && this.targetClass != EntityPlayer.class) {
            this.nearestTarget = this.e.world.b(this.targetClass, this.targetCondition, this.e, this.e.locX(), this.e.getHeadY(), this.e.locZ(), this.a(this.k()));
        } else {
            this.nearestTarget = this.customFindPlayers(this.targetCondition, this.e, this.e.locX(), this.e.getHeadY(), this.e.locZ()); //passes to custom a() function (findPlayer()) in CustomIEntityAccess which bypasses line of sight requirement to intially find a target player
        }
    }
}
