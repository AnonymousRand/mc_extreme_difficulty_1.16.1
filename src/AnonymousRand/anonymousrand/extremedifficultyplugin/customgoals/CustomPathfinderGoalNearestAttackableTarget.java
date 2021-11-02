package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.RemovePathfinderGoal;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import net.minecraft.server.v1_16_R1.*;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;

public class CustomPathfinderGoalNearestAttackableTarget<T extends EntityLiving> extends CustomPathfinderGoalTarget implements CustomIEntityAccess {
    protected final Class<T> targetClass;
    protected final int targetChance;
    public EntityLiving nearestTarget;
    protected CustomPathfinderTargetCondition targetCondition;
    private boolean firstTarget;

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
        this.firstTarget = true;
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

        if (this.firstTarget && this.targetClass.getName().toLowerCase().contains("human")) { //must put the one checking for human entites last in initPathfinder() to avoid modifying the target selector while this method is trying to remove a goal from it; custom version of this goal must also be higher priority than vanilla nearest attackable target goal so that this one always picks up the target first and does the thingy below
            this.firstTarget = false; //remove original hurtByTargetGoal and replace with custom one

            Field goalSet;
            ArrayList<PathfinderGoal> goalsToRemove = new ArrayList<>();

            try {
                goalSet = PathfinderGoalSelector.class.getDeclaredField("d"); //get list of goals from original entity (not just running/active goals)
                goalSet.setAccessible(true);

                switch (this.e.getBukkitEntity().getType()) { //need to do this instead of just taking the goals out of the custom entity's target selector because for some reason the custom entity's target selector's Field d doesn't have the super (vanilla) ones
                    case BEE -> goalsToRemove = (RemovePathfinderGoal.removePathfinderGoal((Set)goalSet.get(((CustomEntityBee)this.e).targetSelectorVanilla), PathfinderGoalHurtByTarget.class));
                    case BLAZE -> goalsToRemove = (RemovePathfinderGoal.removePathfinderGoal((Set)goalSet.get(((CustomEntityBlaze)this.e).targetSelectorVanilla), PathfinderGoalHurtByTarget.class));
                    case ENDERMITE -> goalsToRemove = (RemovePathfinderGoal.removePathfinderGoal((Set)goalSet.get(((CustomEntityEndermite)this.e).targetSelectorVanilla), PathfinderGoalHurtByTarget.class));
                    case HOGLIN -> goalsToRemove = (RemovePathfinderGoal.removePathfinderGoal((Set)goalSet.get(((CustomEntityHoglin)this.e).targetSelectorVanilla), PathfinderGoalHurtByTarget.class));
                    case RABBIT -> goalsToRemove = (RemovePathfinderGoal.removePathfinderGoal((Set)goalSet.get(((CustomEntityRabbit)this.e).targetSelectorVanilla), PathfinderGoalHurtByTarget.class));
                    case RAVAGER -> goalsToRemove = (RemovePathfinderGoal.removePathfinderGoal((Set)goalSet.get(((CustomEntityRavager)this.e).targetSelectorVanilla), PathfinderGoalHurtByTarget.class));
                    case SILVERFISH -> goalsToRemove = (RemovePathfinderGoal.removePathfinderGoal((Set)goalSet.get(((CustomEntitySilverfish)this.e).targetSelectorVanilla), PathfinderGoalHurtByTarget.class));
                    case ZOGLIN -> goalsToRemove = (RemovePathfinderGoal.removePathfinderGoal((Set)goalSet.get(((CustomEntityZoglin)this.e).targetSelectorVanilla), PathfinderGoalHurtByTarget.class));
                    case ZOMBIE -> goalsToRemove = (RemovePathfinderGoal.removePathfinderGoal((Set)goalSet.get(((CustomEntityZombie)this.e).targetSelectorVanilla), PathfinderGoalHurtByTarget.class));
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

            if (goalsToRemove.size() > 0) {
                for (PathfinderGoal goal : goalsToRemove) { //but somehow removing vanilla goals from custom target selectors still works
                    this.e.targetSelector.a(goal); //remove goal
                }

                this.e.targetSelector.a(1, new CustomPathfinderGoalHurtByTarget((EntityCreature)this.e, new Class[0])); /**custom goal that prevents mobs from retaliating against other mobs in case the mob damage event doesn't register and cancel the damage*/
            }
        }

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
