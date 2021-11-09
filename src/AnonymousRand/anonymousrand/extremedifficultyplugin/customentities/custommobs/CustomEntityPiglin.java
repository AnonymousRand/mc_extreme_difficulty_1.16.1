package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableMobShootArrowsNormally;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

public class CustomEntityPiglin extends EntityPiglin {

    public int attacks;
    private Field goalTarget;

    public CustomEntityPiglin(World world) {
        super(EntityTypes.PIGLIN, world);
        this.a(PathType.LAVA, 0.0F); /**no longer avoids lava*/
        this.a(PathType.DAMAGE_FIRE, 0.0F); /**no longer avoids fire*/
        double rand = this.random.nextDouble();
        this.setSlot(EnumItemSlot.MAINHAND, rand < 0.45 ? new ItemStack(Items.CROSSBOW) : rand < 0.9 ? new ItemStack(Items.GOLDEN_SWORD) : rand < 0.96 ? new ItemStack(Items.NETHERITE_HOE) : new ItemStack(Items.NETHERITE_SWORD)); /**piglins have a 45% chance to be armed with a crossbow or a sword each, a 6% chance to have a netherite hoe, and a 4% chance to have a netherite sword*/
        Arrays.fill(this.dropChanceHand, 0.0f); /**piglins can't drop the items they are holding*/
        this.attacks = 0;

        try {
            this.goalTarget = EntityInsentient.class.getDeclaredField("goalTarget");
            this.goalTarget.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(0, new CustomEntityPiglin.PathfinderGoalPiglinResetMemory(this)); /**custom goal that removes fear of zombie piglins etc.*/
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public void a(EntityLiving entityliving, float f) { //shoot
        this.attacks++;
        this.setHealth((float)(this.getHealth() + 1.0)); /**piglins heal by 1 every time its attacks increase by 1*/

        if (this.attacks == 1) { /**first attack always shoots knockback arrows*/
            new RunnableMobShootArrowsNormally(this, entityliving, f, 15, 6, 25.0, this.random.nextDouble() < 0.2 ? 1 : 0, false, false);
        } else {
            int rand = this.random.nextInt(4);

            if (rand == 0) { /**shoots 15 arrows at a time with increased inaccuracy to seem like a cone; 20% of arrows shot are piercing 1*/
                new RunnableMobShootArrowsNormally(this, entityliving, f, 15, 1, 25.0, this.random.nextDouble() < 0.2 ? 1 : 0, false, false);
            } else {
                new RunnableMobShootArrowsNormally(this, entityliving, f, 15, 3 + rand, 25.0, this.random.nextDouble() < 0.2 ? 1 : 0, false, false); /**25% chance to shoot normal arrows, 25% chance to shoot arrows that each with a 1.5% chance to spawn a piglin; 25% chance to shoot arrows that give bad status effects, and 25% chance to shoot extreme knockback arrows*/
            }
        }
    }

    @Nullable
    @Override
    public EntityLiving getGoalTarget() { //uses normal EntityInsentient getGoalTarget() method that doesn't use the piglin's memory modules because they were removed along with its brain and behavior goals
        try {
            if ((this.goalTarget.get(this)) != null) {
                return (EntityLiving)this.goalTarget.get(this);
            } else {
                return null;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public double getFollowRange() { /**piglins have 32 block detection range (setting attribute doesn't work)*/
        return 32.0;
    }

    @Override
    public void tick() {
        super.tick();

        this.bA = 0; /**piglins never turn into zombie piglins*/

        if (this.ticksLived == 10) { /**piglins have speed 2 and 60 max health*/
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 1));
            ((LivingEntity)(this.getBukkitEntity())).setMaxHealth(60.0);

            if (this.getItemInMainHand().getItem() == Items.CROSSBOW) {
                this.goalSelector.a(1, new CustomPathfinderGoalCrossbowAttack<>(this, 1.0D, 32.0F)); /**uses the custom goal that attacks even when line of sight is broken; also causes crossbow piglins to attack twice as fast if it has a line of sight; (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal)*/
            } else {
                this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack(this, 1.0D, true)); /**custom melee attack goal continues attacking even when line of sight is broken*/
            }
        }
    }

    static class PathfinderGoalPiglinResetMemory extends PathfinderGoal {

        private final CustomEntityPiglin piglin;
        private final BehaviorController<EntityPiglin> brain;

        public PathfinderGoalPiglinResetMemory(CustomEntityPiglin piglin) {
            this.piglin = piglin;
            this.brain = piglin.getBehaviorController();
        }

        @Override
        public boolean a() {
            return true;
        }

        @Override
        public boolean b() {
            return this.a();
        }

        @Override
        public void e() {
            if (this.brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).isPresent()) { //constantly resets these specific memory modules to empty as these tasks are not supposed to be performed
                if (!(this.brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).get() instanceof EntityPlayer)) { /**piglins no longer retaliate against non-player mobs*/
                    this.brain.setMemory(MemoryModuleType.HURT_BY_ENTITY, Optional.empty());
                }
            }

            if (this.brain.getMemory(MemoryModuleType.ATTACK_TARGET).isPresent()) { /**piglins no longer attack wither skeletons etc.*/
                if (!(this.brain.getMemory(MemoryModuleType.ATTACK_TARGET).get() instanceof EntityPlayer)) {
                    this.brain.setMemory(MemoryModuleType.ATTACK_TARGET, Optional.empty());
                }
            }

            if (this.brain.getMemory(MemoryModuleType.ANGRY_AT).isPresent() && this.piglin.getGoalTarget() != null) { /**piglins no longer attack wither skeletons etc.*/
                if (this.brain.getMemory(MemoryModuleType.ANGRY_AT).get() != this.piglin.getGoalTarget().getUniqueID()) {
                    this.brain.setMemory(MemoryModuleType.ANGRY_AT, Optional.empty());
                }
            }

            this.brain.setMemory(MemoryModuleType.AVOID_TARGET, Optional.empty()); /**piglins no longer run from zombie piglins, soul fire etc., and no longer hunt hoglins*/
            this.brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMSIS, Optional.empty());
            this.brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, Optional.empty());
            this.brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, Optional.empty());
            this.brain.setMemory(MemoryModuleType.NEAREST_REPELLENT, Optional.empty());
        }
    }
}
