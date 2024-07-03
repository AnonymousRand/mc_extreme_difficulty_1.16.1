package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.util.CustomIEntityAccess;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityTargetEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

// passes to CustomPathfinderGoalNearestAttackableTarget.g()
// which passes to CustomIEntityAccess.customFindPlayer()
// which passes to CustomIEntityAccess.customFindEntity()
// which passes to CustomPathfinderTargetConditions.a()
// which removes line of sight requirement
public class CustomPathfinderGoalNearestAttackableTarget<T extends EntityLiving> extends CustomPathfinderGoalTarget implements CustomIEntityAccess {
    protected final Class<T> targetClass;
    protected final int targetChance;
    public EntityLiving nearestTarget;
    protected CustomPathfinderTargetCondition targetCondition;

    public CustomPathfinderGoalNearestAttackableTarget(EntityInsentient entityInsentient, Class<T> oclass) {
        /** Never needs sight to find target and start attacking */
        this(entityInsentient, oclass, 10, null);
    }

    public CustomPathfinderGoalNearestAttackableTarget(EntityInsentient entityInsentient, Class<T> oclass, int i, @Nullable Predicate<EntityLiving> predicate) {
        super(entityInsentient, false, false);
        this.targetClass = oclass;
        this.targetChance = i;
        this.a(EnumSet.of(PathfinderGoal.Type.TARGET));
        this.targetCondition = (new CustomPathfinderTargetCondition()).a(this.k()).a(predicate);
    }

    public void updateFollowRange() {
        this.targetCondition.a(this.k());
        Bukkit.broadcastMessage("3; new range: " + this.k());
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
            this.e.setGoalTarget(this.nearestTarget, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, false);
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
            this.nearestTarget = this.customFindPlayers(this.targetCondition, this.e, this.e.locX(), this.e.getHeadY(), this.e.locZ()); // passes to custom a() function (findPlayer()) in CustomIEntityAccess which bypasses line of sight requirement to initially find a target player
        }
    }
}
