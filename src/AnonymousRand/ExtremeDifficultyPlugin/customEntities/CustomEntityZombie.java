package AnonymousRand.ExtremeDifficultyPlugin.customEntities;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalZombieAttack;
import net.minecraft.server.v1_16_R1.*;

public class CustomEntityZombie extends EntityZombie {

    public CustomEntityZombie(World world) {
        super(EntityTypes.ZOMBIE, world);
    }

    @Override
    protected void m() {
        this.goalSelector.a(2, new CustomPathfinderGoalZombieAttack(this, 1.0D, false));
        this.goalSelector.a(6, new PathfinderGoalMoveThroughVillage(this, 1.0D, true, 4, this::eV));
        this.goalSelector.a(7, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.targetSelector.a(1, (new CustomPathfinderGoalHurtByTarget(this, new Class[0])).a(EntityPigZombie.class));
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false));
        this.targetSelector.a(3, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class, false));
        this.targetSelector.a(3, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityIronGolem.class, false));
        this.targetSelector.a(5, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, false, false, EntityTurtle.bv));
    }
}
