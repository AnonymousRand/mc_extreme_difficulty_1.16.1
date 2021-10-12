package AnonymousRand.ExtremeDifficultyPlugin.customGoals;

import net.minecraft.server.v1_16_R1.EntityZombie;

public class CustomPathfinderGoalZombieAttack extends CustomPathfinderGoalMeleeAttack { //todo: apply to all applicable mobs

    private final EntityZombie b;

    public CustomPathfinderGoalZombieAttack(EntityZombie entityzombie, double d0, boolean flag) {
        super(entityzombie, d0, flag);
        this.b = entityzombie;
    }
}
