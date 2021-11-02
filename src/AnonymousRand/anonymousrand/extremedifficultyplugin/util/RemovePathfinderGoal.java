package AnonymousRand.anonymousrand.extremedifficultyplugin.util;

import net.minecraft.server.v1_16_R1.Path;
import net.minecraft.server.v1_16_R1.PathfinderGoal;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class RemovePathfinderGoal {

    public static ArrayList<PathfinderGoal> removePathfinderGoal(Set<?> goalSet, Class<? extends PathfinderGoal> pathfinderGoalClass) {
        ArrayList<PathfinderGoal> goalsToRemove = new ArrayList<>();
        PathfinderGoal pathfinderGoal;

        for (Object pathfinderGoalWrapped : goalSet) {
            try {
                Field pathfinderGoalField = pathfinderGoalWrapped.getClass().getDeclaredField("a"); //a is the field that contains the pathfinder goal in the wrapped pathfinder goal object
                pathfinderGoalField.setAccessible(true);
                pathfinderGoal = (PathfinderGoal)pathfinderGoalField.get(pathfinderGoalWrapped);

                if(pathfinderGoalClass.isInstance(pathfinderGoal)) {
                    goalsToRemove.add(pathfinderGoal);
                }
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
            }
        }

        return goalsToRemove;
    }
}
