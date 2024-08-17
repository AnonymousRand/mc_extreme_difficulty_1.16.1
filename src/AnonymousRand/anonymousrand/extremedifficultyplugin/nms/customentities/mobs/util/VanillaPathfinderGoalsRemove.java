package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util;

import net.minecraft.server.v1_16_R1.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Set;

public abstract class VanillaPathfinderGoalsRemove {

    private static Field goals;

    /* Get list of goals from original entity (not just running/active goals from `targetSelector.d()`) */
    static {
        try {
            goals = PathfinderGoalSelector.class.getDeclaredField("d");
            goals.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private VanillaPathfinderGoalsRemove() {}

    public static ArrayList<PathfinderGoal> getGoals(
            Set<PathfinderGoalWrapped> pathfinderGoalsWrapped,
            Class<? extends PathfinderGoal> pathfinderGoalClass) {
        ArrayList<PathfinderGoal> goalsFound = new ArrayList<>();
        Field goalField;
        PathfinderGoal goal;

        for (Object pathfinderGoalWrapped : pathfinderGoalsWrapped) {
            try {
                goalField = pathfinderGoalWrapped.getClass().getDeclaredField("a");
                goalField.setAccessible(true);
                goal = (PathfinderGoal) goalField.get(pathfinderGoalWrapped);

                if (pathfinderGoalClass.isInstance(goal)) {
                    goalsFound.add(goal);
                }
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
            }
        }

        return goalsFound;
    }

    /**
     * Removes these vanilla goals as they are always replaced by custom ones:
     * <ul>
     *     <li><code>PathfinderGoalMeleeAttack</code></li>
     *     <li><code>PathfinderGoalArrowAttack</code></li>
     *     <li><code>PathfinderGoalBowShoot</code></li>
     *     <li><code>PathfinderGoalCrossbowAttack</code></li>
     *     <li><code>PathfinderGoalNearestAttackableTarget</code></li>
     *     <li><code>PathfinderGoalHurtByTarget</code></li>
     * </ul>
     * <p></p>
     *
     * This prevents deadlocks with mutexes if the goals happen to be of the same priority, or have
     * <code>shouldExecute()</code> etc. clashes.
     *
     * @param entity the entity to remove the goal from
     */
    public static <T extends EntityInsentient & ICustomHostile & IGoalRemovingMob> void removePathfinderGoals(
            T entity) {
        ArrayList<PathfinderGoal> goalsToRemoveFromGoalSelector = new ArrayList<>();
        ArrayList<PathfinderGoal> goalsToRemoveFromTargetSelector = new ArrayList<>();
        Set<PathfinderGoalWrapped> allGoalsWrapped;

        // need to do this instead of just taking the goals out of the custom entity's target selector because for some
        // reason the custom entity's target selector's Field d doesn't have the super (vanilla) ones // todo test what?
        try {
            allGoalsWrapped = (Set<PathfinderGoalWrapped>) goals.get(entity.getVanillaGoalSelector());
            allGoalsWrapped.addAll((Set<PathfinderGoalWrapped>) goals.get(entity.getVanillaTargetSelector()));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        goalsToRemoveFromGoalSelector.addAll(getGoals(allGoalsWrapped, PathfinderGoalMeleeAttack.class));
        goalsToRemoveFromGoalSelector.addAll(getGoals(allGoalsWrapped, PathfinderGoalArrowAttack.class));
        goalsToRemoveFromGoalSelector.addAll(getGoals(allGoalsWrapped, PathfinderGoalBowShoot.class));
        goalsToRemoveFromGoalSelector.addAll(getGoals(allGoalsWrapped, PathfinderGoalCrossbowAttack.class));
        goalsToRemoveFromTargetSelector.addAll(getGoals(allGoalsWrapped, PathfinderGoalNearestAttackableTarget.class));
        goalsToRemoveFromTargetSelector.addAll(getGoals(allGoalsWrapped, PathfinderGoalHurtByTarget.class));

        // but somehow removing vanilla goals from custom target selectors still works
        for (PathfinderGoal goal : goalsToRemoveFromGoalSelector) {
            entity.goalSelector.a(goal); // removeGoal()
        }

        for (PathfinderGoal goal : goalsToRemoveFromTargetSelector) {
            entity.targetSelector.a(goal);
        }
    }
}
