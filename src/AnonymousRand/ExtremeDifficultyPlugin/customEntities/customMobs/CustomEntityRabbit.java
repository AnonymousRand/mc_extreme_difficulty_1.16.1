package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalMeleeAttack;
import net.minecraft.server.v1_16_R1.*;

public class CustomEntityRabbit extends EntityRabbit {

    public CustomEntityRabbit(World world) {
        super(EntityTypes.RABBIT, world);
    }

    @Override
    public void setRabbitType(int i) {
        super.setRabbitType(i);

        if (i == 99) {
            this.goalSelector.a(4, new CustomPathfinderGoalKillerRabbitMeleeAttack(this)); /**uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal); this custom goal also allows the spider to continue attacking regardless of light level*/
            this.targetSelector.a(0, new PathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement); this custom goal also allows the spider to continue attacking regardless of light level*/
            this.targetSelector.a(1, new PathfinderGoalNearestAttackableTarget<>(this, EntityWolf.class, false));
        }
    }

    static class CustomPathfinderGoalKillerRabbitMeleeAttack extends CustomPathfinderGoalMeleeAttack {

        public CustomPathfinderGoalKillerRabbitMeleeAttack(EntityRabbit entityrabbit) {
            super(entityrabbit, 1.4D, false);
        }

        @Override
        protected double a(EntityLiving entityliving) {
            return (double) (4.0F + entityliving.getWidth());
        }
    }
}
