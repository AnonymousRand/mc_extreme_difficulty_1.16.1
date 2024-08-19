package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.AttackLevelingController;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.VanillaPathfinderGoalsRemove;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack.CustomPathfinderGoalAttackRangedSkeleton;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attackmvmt.CustomPathfinderGoalAttackMvmtRangedSkeleton;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

import java.util.stream.Collectors;

public class CustomEntityIllusionerFake extends CustomEntityIllusioner {

    /* Ignores line of sight and y-level for initially finding a player target and maintaining it as the target,
     * as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;
    
    private AttackLevelingController attackLevelingController = null;
    private final CustomEntityIllusioner parentIllusioner;
    private boolean deathExplosion;

    public CustomEntityIllusionerFake(World world, CustomEntityIllusioner parentIllusioner) {
        super(world);
        this.parentIllusioner = parentIllusioner;
    }

    /* This will be called on super() constructor // todo make sure */
    // todo or just user super() and have protected. override annotation
    protected void initCustom() {
        this.initAttributes();

        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);

        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.BOW)); // makes sure that it has a bow
        this.deathExplosion = false;
    }

    protected void initAttributes() {
        float health = (float) (random.nextDouble() * 12.0 + 20.0); /* fake illusioners have anywhere between 20 and 32 health */
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(health);
        this.setHealth(health);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `IAttackLevelingMob`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    // todo can get rid of getters
    protected void initAttackLevelingMob() {
        this.attackLevelingController = new AttackLevelingController(20, 40);
    }

    public int getAttacks() {
        return this.attackLevelingController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        int[] attackThreshs = this.getAttacksThreshs();
        int[] threshsMet = this.attackLevelingController.increaseAttacks(increase);

        for (int threshMet : threshsMet) {
            if (threshMet == attackThreshs[0]) {
                /* After 20 attacks, summoned fake illusioners attack faster */
                for (PathfinderGoal goal : VanillaPathfinderGoalsRemove.getGoals(this.goalSelector.d().collect(Collectors.toSet()), CustomPathfinderGoalAttackRangedSkeleton.class)) {
                    ((CustomPathfinderGoalAttackRangedSkeleton<?>) goal).setAttackCooldown(random.nextInt(9) + 12);
                }
            } else if (threshMet == attackThreshs[1]) {
                /* After 40 attacks, summoned fake illusioners attack even faster */
                for (PathfinderGoal goal : VanillaPathfinderGoalsRemove.getGoals(this.goalSelector.d().collect(Collectors.toSet()), CustomPathfinderGoalAttackRangedSkeleton.class)) {
                    ((CustomPathfinderGoalAttackRangedSkeleton<?>) goal).setAttackCooldown(random.nextInt(4) + 5);
                }
            }
        }
    }

    public int[] getAttacksThreshs() {
        return this.attackLevelingController.getAttacksThreshs();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Overridden vanilla functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    // todo can just super()?
    public void initPathfinder() { /* no longer target iron golems or villagers, and only shoots arrows (can't apply spells) */
        this.goalSelector.a(1, new EntityRaider.b<>(this));
        this.goalSelector.a(3, new PathfinderGoalRaid<>(this));
        this.goalSelector.a(5, new CustomEntityIllusioner.c(this));
        this.goalSelector.a(4, new CustomEntityIllusioner.d(this, 1.0499999523162842D, 1));
        /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this));
        /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this));
        this.goalSelector.a(0, new CustomPathfinderGoalBreakBlockLookingAt(this)); /* custom goal that allows the mob to break the block it is looking at every 4 seconds as long as it has a target, it breaks the block that it is looking at up to 40 blocks away */
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(1, new EntityIllagerWizard.b());
        this.goalSelector.a(6, new CustomPathfinderGoalAttackRangedSkeleton<>(this, random.nextInt(11) + 20));
        this.goalSelector.a(6, new CustomPathfinderGoalAttackMvmtRangedSkeleton<>(this));
        this.goalSelector.a(8, new PathfinderGoalRandomStroll(this, 0.6D));
        this.goalSelector.a(9, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 3.0F, 1.0F));
        this.goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget<>(this));
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the nearest option */
        // todo test removing forget after 300 ticks
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getHealth() <= 0.0 && this.parentIllusioner.getAttacks() >= 12 && !this.deathExplosion) { /* after 12 attacks, summoned fake illusioners explode when killed */
            this.deathExplosion = true;
            this.world.createExplosion(this, this.locX(), this.locY(), this.locZ(), 1.0F, false, Explosion.Effect.NONE);
        }

        if (this.ticksLived >= 1500) { /* fake illusioners die after 75 seconds */
            this.die();
        }
    }
}
