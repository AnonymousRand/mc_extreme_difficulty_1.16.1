package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IGoalRemovingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.VanillaPathfinderGoalsRemove;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack.CustomPathfinderGoalMeleeAttack;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityRabbit extends EntityRabbit implements ICustomHostile, IAttackLevelingMob, IGoalRemovingMob {

    private static final boolean IGNORE_LOS = false;
    private static final boolean IGNORE_Y = false;
    private int attacks;
    private boolean a5, a15, a25, die;

    public CustomEntityRabbit(World world) {
        super(EntityTypes.RABBIT, world);
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.attacks = 0;
        this.a5 = false;
        this.a15 = false;
        this.a25 = false;
        this.die = false;
        VanillaPathfinderGoalsRemove.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
        this.setRabbitType(99);
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder(); // todo custom melee
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget<>(this));                                /* Always retaliates against players and teleports to them if they are out of range/do not have line of sight, but doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */ // todo does the listener actually not work sometimes? also if this is no longer needed, don't remove old goal in igoalremovingmob
    }

    @Override
    public void setRabbitType(int i) {
        super.setRabbitType(i);

        if (i == 99) {
            // todo can literally just use new setminattackreach thingy
            this.goalSelector.a(4, new CustomEntityRabbit.PathfinderGoalKillerRabbitMeleeAttack<>(this, 1.5)); /* Killer rabbits move speed multiplier when angry 1.4 -> 1.5 */
            this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the nearest option */
            this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityWolf.class, false, false));

            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 2)); /* changing attributes don't work on rabbits so killer bunnies have speed 3 and jump boost 1 */
            this.addEffect(new MobEffect(MobEffects.JUMP, Integer.MAX_VALUE, 1));
        }
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damageAmount) {
        boolean damageSuccess = super.damageEntity(damageSource, damageAmount);
        if (damageSuccess && this.isAlive() && damageSource.getEntity() instanceof EntityPlayer && this.attacks >= 40) { /* after 40 attacks, killer bunnies duplicate when hit and not killed */
            new SpawnEntity(this.world, new CustomEntityRabbit(this.world), 1, null, null, this, false, true);
        }

        return super.damageEntity(damageSource, damageAmount);
    }

    public double getDetectionRange() { /* killer bunnies have 16 block detection range (28 after 5 attacks, 40 after 15 attacks) */
        return this.attacks < 5 ? 16.0 : this.attacks < 15 ? 28.0 : 40.0;
    }

    public boolean ignoresLOS() {
        return IGNORE_LOS;
    }

    public boolean ignoresY() {
        return IGNORE_Y;
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.world.findNearbyPlayer(this, -1.0);

            if (nearestPlayer != null) {
                /* Mobs only despawn along horizontal axes, so even at build height,
                   mobs will spawn below you and prevent sleeping */
                double distSqToNearestPlayer =
                        Math.pow(nearestPlayer.getPositionVector().getX() - this.getPositionVector().getX(), 2)
                        + Math.pow(nearestPlayer.getPositionVector().getZ() - this.getPositionVector().getZ(), 2);
                int forceDespawnDist = this.getEntityType().e().f();
                int forceDespawnDistSq = forceDespawnDist * forceDespawnDist;

                if (distSqToNearestPlayer > (double) forceDespawnDistSq
                        && this.isTypeNotPersistent(distSqToNearestPlayer)) {
                    this.die();
                }

                /* Random despawn distance increased to 40 blocks */
                int randomDespawnDist = this.getEntityType().e().g() + 8;
                int randomDespawnDistSq = randomDespawnDist * randomDespawnDist;

                if (this.ticksFarFromPlayer > 600
                        && random.nextInt(800) == 0
                        && distSqToNearestPlayer > (double) randomDespawnDistSq
                        && this.isTypeNotPersistent(distSqToNearestPlayer)) {
                    this.die();
                } else if (distSqToNearestPlayer < (double) randomDespawnDistSq) {
                    this.ticksFarFromPlayer = 0;
                }
            }
        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    @Override
    public int bL() {
        return Integer.MAX_VALUE;
    }

    public int getAttacks() {
        return this.attacks;
    }

    public void increaseAttacks(int increase) {
        this.attacks += increase;
    }

//    public int[] getAttacksThreshs() {
//        return this.attackLevelingController.getAttacksThreshs();
//    }

    @Override
    public void tick() {
        super.tick();

        if (this.getRabbitType() == 99) {
            if (this.attacks == 5 && !this.a5) { /* after 5 attacks, killer bunnies gain speed 4 */
                this.a5 = true;
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 3));
                this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); // update follow range
            }

            if (this.attacks == 15 && !this.a15) { /* after 15 attacks, killer bunnies gain speed 5 */
                this.a15 = true;
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 4));
                this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); // update follow range
            }

            if (this.attacks == 25 && !this.a25) { /* after 25 attacks, killer bunnies gain speed 6 and 10 max health and health */
                this.a25 = true;
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 5));
                ((LivingEntity) this.getBukkitEntity()).setMaxHealth(10.0);
                this.setHealth(10.0F);
            }

            if (this.getHealth() <= 0.0 && !this.die) {
                this.die = true;

                if (this.attacks >= 25) { /* after 25 attacks, killer bunnies explode when killed */
                    this.world.createExplosion(this, this.locX(), this.locY(), this.locZ(), 1.0F, false, Explosion.Effect.DESTROY);
                }
            }
        }
    }

    @Override
    public PathfinderGoalSelector getVanillaGoalSelector() {
        return super.goalSelector;
    }

    public PathfinderGoalSelector getVanillaTargetSelector() {
        return super.targetSelector;
    }

    static class PathfinderGoalKillerRabbitMeleeAttack<T extends CustomEntityRabbit & ICustomHostile>
            extends CustomPathfinderGoalMeleeAttack<T> {

        public PathfinderGoalKillerRabbitMeleeAttack(T entityRabbit, double speedTowardsTarget) {
            super(entityRabbit, speedTowardsTarget);
        }

        @Override
        protected double getAttackReachSq(EntityLiving target) {
            return (4.0F + target.getWidth());
        }
    }
}
