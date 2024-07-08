package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.projectiles.CustomEntityWitherSkull;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityWitherMini extends CustomEntityWither {

    public CustomEntityWitherMini(World world) {
        super(world);
        this.setInvul(0); /* no birth animation/explosion */
        this.dash = false;
        double health = 50.0 + 5.0 * this.world.getServer().getOnlinePlayers().size(); /* mini withers have 5 more health per player online, and 50 starting health */
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(health);
        this.setHealth((float) health);
    }

    public CustomEntityWitherMini(World world, boolean scaleHealth) {
        this(world);

        if (!scaleHealth) { /* constant 50 health if spawning 1 for every player etc. */
            ((LivingEntity) this.getBukkitEntity()).setMaxHealth(50.0);
            this.setHealth(50.0F);
        }
    }


    @Override
    protected void initPathfinder() { /* no dash attack */
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new PathfinderGoalWitherDoNothingWhileInvulnerable());
        this.goalSelector.a(2, new CustomPathfinderGoalRangedAttack<>(this, 1.0D, 5, 80.0F)); /* main head shoots a skull every 5 ticks and uses the custom goal that attacks regardless of the y-level (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal) */
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 8.0F));
        this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new CustomPathfinderGoalHurtByTarget(this, new Class[0])); /* Doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* only attacks players; uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
    }

    @Override
    protected void shootSkullToCoords(int i, double d0, double d1, double d2, boolean alwaysBlue) {
        if (this.dash) {
            return;
        }

        if (!this.isSilent() && random.nextDouble() < 0.05) { /* mini withers only play the skull shooting sound 5% of the time */
            this.world.a(null, 1024, this.getChunkCoordinates(), 0);
        }

        double d3 = this.getHeadX(i);
        double d4 = this.getHeadY(i);
        double d5 = this.getHeadZ(i);
        double d6 = d0 - d3;
        double d7 = d1 - d4;
        double d8 = d2 - d5;
        CustomEntityWitherSkull entityWitherSkull = new CustomEntityWitherSkull(this.world, this, d6, d7, d8);
        entityWitherSkull.setShooter(this);

        if (this.random.nextFloat() < 0.05 || alwaysBlue) { /* mini withers shoot blue skulls 5% of the time */
            entityWitherSkull.setCharged(true);
        }

        entityWitherSkull.setPositionRaw(d3, d4, d5);
        this.world.addEntity(entityWitherSkull);
    }
}
