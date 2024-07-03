package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
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

    public static void removePathfinderGoals(EntityInsentient entity) {
        ArrayList<PathfinderGoal> goalsToRemove = new ArrayList<>();

        try {
            switch (entity.getBukkitEntity().getType()) { // need to do this instead of just taking the goals out of the custom entity's target selector because for some reason the custom entity's target selector's Field d doesn't have the super (vanilla) ones
                case BEE:
                    goalsToRemove = getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityBee)entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityBee)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    break;
                case BLAZE:
                    goalsToRemove = getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityBlaze)entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityBlaze)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    break;
                case ENDERMITE:
                    goalsToRemove = getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityEndermite)entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityEndermite)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    break;
                case HOGLIN:
                    goalsToRemove = getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityHoglin)entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityHoglin)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    break;
                case HUSK:
                    goalsToRemove = getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityZombieHusk)entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityZombieHusk)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    break;
                case MAGMA_CUBE:
                    goalsToRemove = getPathfinderGoals((Set<?>) goalSet.get(((CustomEntitySlimeMagmaCube)entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntitySlimeMagmaCube)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    break;
                case PILLAGER:
                    goalsToRemove = getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityPillager)entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityPillager)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    break;
                case RABBIT:
                    goalsToRemove = getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityRabbit)entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityRabbit)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    break;
                case RAVAGER:
                    goalsToRemove = getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityRavager)entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityRavager)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    break;
                case SILVERFISH:
                    goalsToRemove = getPathfinderGoals((Set<?>) goalSet.get(((CustomEntitySilverfish)entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntitySilverfish)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    break;
                case SLIME:
                    goalsToRemove = getPathfinderGoals((Set<?>) goalSet.get(((CustomEntitySlime)entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntitySlime)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    break;
                case VEX:
                    goalsToRemove = getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityVex)entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityVex)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    break;
                case VINDICATOR:
                    goalsToRemove = getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityVindicator)entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityVindicator)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    break;
                case WITCH:
                    goalsToRemove = getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityWitch)entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityWitch)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    break;
                case WITHER_SKELETON:
                    goalsToRemove = getPathfinderGoals((Set<?>) goalSet.get(((CustomEntitySkeletonWither)entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntitySkeletonWither)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    break;
                case ZOGLIN:
                    goalsToRemove = getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityZoglin)entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityZoglin)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    break;
                case ZOMBIE:
                    if (entity instanceof CustomEntityZombieThor) {
                        goalsToRemove = getPathfinderGoals((Set) goalSet.get(((CustomEntityZombieThor) entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                        goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityZombieThor)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    } else if (entity instanceof CustomEntityZombieSuper) {
                        goalsToRemove = getPathfinderGoals((Set) goalSet.get(((CustomEntityZombieSuper) entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                        goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityZombieSuper)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    } else {
                        goalsToRemove = getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityZombie)entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                        goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityZombie)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    }
                    break;
                case ZOMBIE_VILLAGER:
                    goalsToRemove = getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityZombieVillager)entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityZombieVillager)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    break;
                case ZOMBIFIED_PIGLIN:
                    goalsToRemove = getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityZombiePig)entity).vanillaTargetSelector), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set<?>) goalSet.get(((CustomEntityZombiePig)entity).vanillaTargetSelector), PathfinderGoalNearestAttackableTarget.class));
                    break;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (goalsToRemove.size() > 0) {
            for (PathfinderGoal goal : goalsToRemove) { // but somehow removing vanilla goals from custom target selectors still works
                if (!(goal instanceof CustomPathfinderGoalHurtByTarget) && !(goal instanceof CustomPathfinderGoalNearestAttackableTarget)) {
                    entity.targetSelector.a(goal); // remove goal
                }
            }

            if (entity instanceof EntityCreature) {
                entity.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget((EntityCreature)entity)); /** custom goal that prevents mobs from retaliating against other mobs in case the mob damage event doesn't register and cancel the damage */
            }
        }
    }

    public static void updateMobFollowRange(EntityInsentient entityInsentient) {
        try {
            for (PathfinderGoal goal : getPathfinderGoals((Set<?>) goalSet.get(entityInsentient.targetSelector), CustomPathfinderGoalNearestAttackableTarget.class)) {
                ((CustomPathfinderGoalNearestAttackableTarget<?>) goal).updateFollowRange();
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}