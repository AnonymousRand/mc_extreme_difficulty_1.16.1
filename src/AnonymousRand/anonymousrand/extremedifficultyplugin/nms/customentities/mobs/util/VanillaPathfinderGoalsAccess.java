package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_16_R1.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Set;

public class VanillaPathfinderGoalsAccess {

    private static Field goalSet;

    static {
        try {
            goalSet = PathfinderGoalSelector.class.getDeclaredField("d"); // get list of goals from original entity (not just running/active goals which is obtained with targetSelector.d())
            goalSet.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private VanillaPathfinderGoalsAccess() {} // private constructor = protect against accidental instantiation

    public static ArrayList<PathfinderGoal> getPathfinderGoals(Set<?> goalSet, Class<? extends PathfinderGoal> pathfinderGoalClass) {
        ArrayList<PathfinderGoal> goalsFound = new ArrayList<>();
        Field pathfinderGoalField;
        PathfinderGoal pathfinderGoal;

        for (Object pathfinderGoalWrapped : goalSet) {
            try {
                pathfinderGoalField = pathfinderGoalWrapped.getClass().getDeclaredField("a"); // a is the field that contains the pathfinder goal in the wrapped pathfinder goal object
                pathfinderGoalField.setAccessible(true);
                pathfinderGoal = (PathfinderGoal)pathfinderGoalField.get(pathfinderGoalWrapped);

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
     * Remove vanilla <code>PathfinderGoalHurtByTarget</code> and <code>PathfinderGoalNearestAttackableTarget</code>
     * goals in order to replace them with custom ones.
     *
     * @param entity the entity to remove the goal from
     */
    public static <T extends EntityInsentient & ICustomHostile & IGoalRemovingMob> void removePathfinderGoals(T entity) {
        ArrayList<PathfinderGoal> goalsToRemove = new ArrayList<>();

        // need to do this instead of just taking the goals out of the custom entity's target selector because for some reason the custom entity's target selector's Field d doesn't have the super (vanilla) ones
        try {
            goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(entity.getVanillaTargetSelector()), PathfinderGoalTarget.class));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // but somehow removing vanilla goals from custom target selectors still works
        for (PathfinderGoal goal : goalsToRemove) {
            if (!(goal instanceof CustomPathfinderGoalHurtByTarget) && !(goal instanceof CustomPathfinderGoalNearestAttackableTarget)) {
                entity.targetSelector.a(goal); // removeGoal()
            }
        }
    }
}
