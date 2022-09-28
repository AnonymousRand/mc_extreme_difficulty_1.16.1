package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import net.minecraft.server.v1_16_R1.*;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

public class CustomPathfinderGoalNearestAttackableTarget<T extends EntityLiving> extends CustomPathfinderGoalTarget implements CustomIEntityAccess {
    protected final Class<T> targetClass;
    protected final int targetChance;
    public EntityLiving nearestTarget;
    protected CustomPathfinderTargetCondition targetCondition;

    public CustomPathfinderGoalNearestAttackableTarget(EntityInsentient entityInsentient, Class<T> oclass, boolean checkSight) {
        this(entityInsentient, oclass, checkSight, false);
    }

    public CustomPathfinderGoalNearestAttackableTarget(EntityInsentient entityInsentient, Class<T> oclass, boolean checkSight, boolean nearbyOnly) {
        this(entityInsentient, oclass, 10, checkSight, nearbyOnly, null);
    }

    public CustomPathfinderGoalNearestAttackableTarget(EntityInsentient entityInsentient, Class<T> oclass, int i, boolean flag, boolean flag1, @Nullable Predicate<EntityLiving> predicate) {
        super(entityInsentient, flag, flag1);
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

    @Override
    public void c() {
        if (this.nearestTarget != null && this.e.getGoalTarget() == null) {
            this.e.setGoalTarget(this.nearestTarget); // must have this to work; keeps giving null pointer warnings for some reason
        }
    }

    protected AxisAlignedBB a(double d0) {
        return this.e.getBoundingBox().grow(d0, 4.0D, d0);
    }

    public void a(@Nullable EntityLiving entityLiving) {
        this.nearestTarget = entityLiving;
    }

    protected void g() {
        if (this.targetClass != EntityHuman.class && this.targetClass != EntityPlayer.class) {
            this.nearestTarget = this.e.world.b(this.targetClass, this.targetCondition, this.e, this.e.locX(), this.e.getHeadY(), this.e.locZ(), this.a(this.k()));
        } else {
            this.nearestTarget = this.customFindPlayers(this.targetCondition, this.e, this.e.locX(), this.e.getHeadY(), this.e.locZ()); // passes to custom a() function (findPlayer()) in CustomIEntityAccess which bypasses line of sight requirement to intially find a target player
        }
    }
}
