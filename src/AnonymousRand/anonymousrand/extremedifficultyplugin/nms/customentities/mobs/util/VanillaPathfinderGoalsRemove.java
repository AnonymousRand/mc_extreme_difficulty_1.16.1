package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Set;

public abstract class VanillaPathfinderGoalsRemove {

    private static Field goals;

    static {
        try {
            goals = PathfinderGoalSelector.class.getDeclaredField("d"); // get list of goals from original entity (not just running/active goals which is obtained with targetSelector.d())
            goals.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private VanillaPathfinderGoalsRemove() {} // private constructor = protect against accidental instantiation

    public static ArrayList<PathfinderGoal> getPathfinderGoals(
            Set<PathfinderGoalWrapped> pathfinderGoalsWrapped,
            Class<? extends PathfinderGoal> pathfinderGoalClass) {
        ArrayList<PathfinderGoal> goalsFound = new ArrayList<>();
        Field pathfinderGoalField;
        PathfinderGoal pathfinderGoal;

        for (Object pathfinderGoalWrapped : pathfinderGoalsWrapped) {
            try {
                pathfinderGoalField = pathfinderGoalWrapped.getClass().getDeclaredField("a"); // a is the field that contains the pathfinder goal in the wrapped pathfinder goal object
                pathfinderGoalField.setAccessible(true);
                pathfinderGoal = (PathfinderGoal) pathfinderGoalField.get(pathfinderGoalWrapped);

                if (pathfinderGoalClass.isInstance(pathfinderGoal)) {
                    goalsFound.add(pathfinderGoal);
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
    public static <T extends EntityInsentient & ICustomHostile & IGoalRemovingMob> void removePathfinderGoals(T entity) {
        ArrayList<PathfinderGoal> goalsToRemoveGoalSelector = new ArrayList<>();
        ArrayList<PathfinderGoal> goalsToRemoveTargetSelector = new ArrayList<>();
        Set<PathfinderGoalWrapped> allPathfinderGoalsWrapped;

        // need to do this instead of just taking the goals out of the custom entity's target selector because for some reason the custom entity's target selector's Field d doesn't have the super (vanilla) ones
        try {
            allPathfinderGoalsWrapped = (Set<PathfinderGoalWrapped>) goals.get(entity.getVanillaGoalSelector());
            allPathfinderGoalsWrapped.addAll((Set<PathfinderGoalWrapped>) goals.get(entity.getVanillaTargetSelector()));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        goalsToRemoveGoalSelector.addAll(getPathfinderGoals(allPathfinderGoalsWrapped, PathfinderGoalMeleeAttack.class));
        goalsToRemoveGoalSelector.addAll(getPathfinderGoals(allPathfinderGoalsWrapped, PathfinderGoalArrowAttack.class));
        goalsToRemoveGoalSelector.addAll(getPathfinderGoals(allPathfinderGoalsWrapped, PathfinderGoalBowShoot.class));
        goalsToRemoveGoalSelector.addAll(getPathfinderGoals(allPathfinderGoalsWrapped, PathfinderGoalCrossbowAttack.class));
        goalsToRemoveTargetSelector.addAll(getPathfinderGoals(allPathfinderGoalsWrapped, PathfinderGoalNearestAttackableTarget.class));
        goalsToRemoveTargetSelector.addAll(getPathfinderGoals(allPathfinderGoalsWrapped, PathfinderGoalHurtByTarget.class));

        // but somehow removing vanilla goals from custom target selectors still works
        for (PathfinderGoal goal : goalsToRemoveGoalSelector) {
            entity.goalSelector.a(goal); // removeGoal()
        }

        for (PathfinderGoal goal : goalsToRemoveTargetSelector) {
            entity.targetSelector.a(goal);
        }
    }
}
