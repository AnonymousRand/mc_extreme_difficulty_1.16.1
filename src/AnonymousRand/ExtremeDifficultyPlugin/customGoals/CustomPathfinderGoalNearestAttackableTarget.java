package AnonymousRand.ExtremeDifficultyPlugin.customGoals;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class CustomPathfinderGoalNearestAttackableTarget<T extends EntityLiving> extends PathfinderGoalNearestAttackableTarget implements CustomIEntityAccess {
    protected final Class<T> targetClass;
    protected final int b;
    protected CustomPathfinderTargetCondition d;

    public CustomPathfinderGoalNearestAttackableTarget(EntityInsentient entityinsentient, Class<T> oclass, boolean useLineOfSight) {
        this(entityinsentient, oclass, useLineOfSight, false);
    }

    public CustomPathfinderGoalNearestAttackableTarget(EntityInsentient entityinsentient, Class<T> oclass, boolean flag, boolean flag1) {
        this(entityinsentient, oclass, 10, flag, flag1, (Predicate)null);
    }

    public CustomPathfinderGoalNearestAttackableTarget(EntityInsentient entityinsentient, Class<T> oclass, int i, boolean flag, boolean flag1, @Nullable Predicate<EntityLiving> predicate) {
        super(entityinsentient, oclass, i, flag, flag1, predicate);
        this.targetClass = oclass;
        this.b = i;

        this.d = (new CustomPathfinderTargetCondition()).a(getFollowRangeOfGoalOwner()).a(predicate); //register custom follow ranges for mobs
    }

    @Override
    protected void g() {
        if (this.targetClass != EntityHuman.class && this.targetClass != EntityPlayer.class) {
            super.c = this.e.world.b(this.targetClass, this.d, this.e, this.e.locX(), this.e.getHeadY(), this.e.locZ(), this.a(this.k()));
        } else {
            super.c = this.customFindPlayers(this.d, this.e, this.e.locX(), this.e.getHeadY(), this.e.locZ()); //passes to custom a() function (findPlayer()) in CustomIEntityAccess which bypasses line of sight requirement to intially find a target player
        }
    }

    protected double getFollowRangeOfGoalOwner() {
        Entity bukkitEntity = this.e.getBukkitEntity();
        EntityType type = bukkitEntity.getType();

        switch (type) {
            case CREEPER -> {
                return 40.0;
            }
        }

        return this.k();
    }
}
