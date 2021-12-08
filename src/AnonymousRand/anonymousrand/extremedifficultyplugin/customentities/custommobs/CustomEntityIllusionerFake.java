package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.RemovePathfinderGoals;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

import java.util.stream.Collectors;

public class CustomEntityIllusionerFake extends CustomEntityIllusioner {

    private final CustomEntityIllusioner parentIllusioner;
    private boolean a20, a40, deathExplosion;

    public CustomEntityIllusionerFake(World world, CustomEntityIllusioner parentIllusioner) {
        super(world);
        this.parentIllusioner = parentIllusioner;
        this.a20 = false;
        this.a40 = false;
        this.deathExplosion = false;
        float health = (float)(random.nextFloat() * 15.0 + 20.0);
        this.setHealth(health);
        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(health); /**fake illusioners have anywhere between 20 and 35 health*/
    }

    @Override
    public void initPathfinder() { /**no longer target iron golems or villagers, and only shoots arrows (can't apply spells)*/
        this.goalSelector.a(1, new EntityRaider.b<>(this));
        this.goalSelector.a(3, new PathfinderGoalRaid<>(this));
        this.goalSelector.a(5, new CustomEntityIllusioner.c(this));
        this.goalSelector.a(4, new CustomEntityIllusioner.d(this, 1.0499999523162842D, 1));

        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlockLookingAt(this)); /**custom goal that allows the mob to break the block it is looking at every 3 seconds as long as it has a target, it breaks the block that it is looking at up to 40 blocks away*/
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(1, new EntityIllagerWizard.b());
        this.goalSelector.a(6, new CustomPathfinderGoalBowShoot<>(this, 0.5D, random.nextInt(16) + 15, 32.0F)); /**uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal)*/
        this.goalSelector.a(8, new PathfinderGoalRandomStroll(this, 0.6D));
        this.goalSelector.a(9, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 3.0F, 1.0F));
        this.goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));
        this.targetSelector.a(1, (new CustomPathfinderGoalHurtByTarget(this, new Class[0])));
        this.targetSelector.a(2, (new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, false)).a(300)); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public void tick() {
        super.tick();

        this.attacks = 0; //no attack system

        if (this.getHealth() <= 0.0 && this.parentIllusioner.attacks >= 12 && !this.deathExplosion) { /**after 12 attacks, summoned fake illusioners explode when killed/replaced*/
            this.deathExplosion = true;
            this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 1.0F, false, Explosion.Effect.NONE);
        }

        if (this.parentIllusioner.attacks == 20 && !this.a20) { /**after 20 attacks, summoned fake illusioners attack faster*/
            this.a20 = true;

            for (PathfinderGoal goal : RemovePathfinderGoals.removePathfinderGoal(this.goalSelector.d().collect(Collectors.toSet()), CustomPathfinderGoalBowShoot.class)) {
                this.goalSelector.a(goal);
            }

            this.goalSelector.a(6, new CustomPathfinderGoalArrowAttack(this, 0.5D, random.nextInt(11) + 10, 32.0F)); //use this instead of bowshoot as bowshoot doesn't seem to be able to go below a certain attack speed
        }

        if (this.parentIllusioner.attacks == 40 && !this.a40) { /**after 40 attacks, summoned fake illusioners attack even faster*/
            this.a40 = true;

            for (PathfinderGoal goal : RemovePathfinderGoals.removePathfinderGoal(this.goalSelector.d().collect(Collectors.toSet()), CustomPathfinderGoalBowShoot.class)) {
                this.goalSelector.a(goal);
            }

            this.goalSelector.a(6, new CustomPathfinderGoalArrowAttack(this, 0.5D, random.nextInt(9) + 2, 32.0F));
        }

        if (this.ticksLived >= 1500) { /**fake illusioners die after 75 seconds*/
            this.die();
        }
    }
}
