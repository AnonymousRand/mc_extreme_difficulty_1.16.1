package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalMeleeAttack;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalCobwebMoveFaster;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityRabbit extends EntityRabbit implements ICustomHostile, IAttackLevelingMob {

    public PathfinderGoalSelector vanillaTargetSelector;
    private int attacks;
    private boolean a5, a15, a25, die;

    public CustomEntityRabbit(World world) {
        super(EntityTypes.RABBIT, world);
        this.vanillaTargetSelector = super.targetSelector;
        this.a(PathType.LAVA, 0.0F); /* no longer avoids lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F); /* no longer avoids fire */
        this.attacks = 0;
        this.a5 = false;
        this.a15 = false;
        this.a25 = false;
        this.die = false;
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
        this.setRabbitType(99);
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /* custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /* custom goal that allows this mob to take certain buffs from bats etc. */
    }

    @Override
    public void setRabbitType(int i) {
        super.setRabbitType(i);

        if (i == 99) {
            this.goalSelector.a(4, new CustomEntityRabbit.PathfinderGoalKillerRabbitMeleeAttack(this)); /* uses the custom goal that attacks regardless of the y level (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal) */
            this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityWolf.class));
            this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */

            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 2)); /* changing attributes don't work on rabbits so killer bunnies have speed 3 and jump boost 1 */
            this.addEffect(new MobEffect(MobEffects.JUMP, Integer.MAX_VALUE, 1));
        }
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (damagesource.getEntity() instanceof EntityPlayer && this.getHealth() - f > 0.0 && this.attacks >= 40) { /* after 40 attacks, killer bunnies duplicate when hit and not killed */
            new SpawnEntity(this.getWorld(), new CustomEntityRabbit(this.getWorld()), 1, null, null, this, false, true);
        }

        return super.damageEntity(damagesource, f);
    }

    public double getFollowRange() { /* killer bunnies have 16 block detection range (setting attribute doesn't work) (28 after 5 attacks, 40 after 15 attacks) */
        return this.attacks < 5 ? 16.0 : this.attacks < 15 ? 28.0 : 40.0;
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (nearestPlayer != null) {
                /* Mobs only despawn along horizontal axes, so even if you are at y=256, mobs will still spawn below you and prevent sleeping */
                double distSquaredToNearestPlayer = Math.pow(nearestPlayer.getPositionVector().getX() - this.getPositionVector().getX(), 2)
                        + Math.pow(nearestPlayer.getPositionVector().getZ() - this.getPositionVector().getZ(), 2);
                int forceDespawnDist = this.getEntityType().e().f();
                int forceDespawnDistSquared = forceDespawnDist * forceDespawnDist;

                if (distSquaredToNearestPlayer > (double) forceDespawnDistSquared && this.isTypeNotPersistent(distSquaredToNearestPlayer)) {
                    this.die();
                }

                /* Random despawn distance increased to 40 blocks */
                int randomDespawnDist = this.getEntityType().e().g() + 8;
                int randomDespawnDistSquared = randomDespawnDist * randomDespawnDist;

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && distSquaredToNearestPlayer > (double)randomDespawnDistSquared
                        && this.isTypeNotPersistent(distSquaredToNearestPlayer)) {
                    this.die();
                } else if (distSquaredToNearestPlayer < (double) randomDespawnDistSquared) {
                    this.ticksFarFromPlayer = 0;
                }
            }

        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    @Override
    public double g(double x, double y, double z) {
        double distX = this.locX() - x;
        double distZ = this.locZ() - z;

        return distX * distX + distZ * distZ;
    }

    @Override
    public double d(Vec3D vec3d) {
        double distX = this.locX() - vec3d.x;
        double distZ = this.locZ() - vec3d.z;

        return distX * distX + distZ * distZ;
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

    @Override
    public void tick() {
        super.tick();

        if (this.getRabbitType() == 99) {
            if (this.attacks == 5 && !this.a5) { /* after 5 attacks, killer bunnies gain speed 4 */
                this.a5 = true;
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 3));
                this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); // updates follow range
            }

            if (this.attacks == 15 && !this.a15) { /* after 15 attacks, killer bunnies gain speed 5 */
                this.a15 = true;
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 4));
                this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); // updates follow range
            }

            if (this.attacks == 25 && !this.a25) { /* after 25 attacks, killer bunnies gain speed 6 and 10 max health and health */
                this.a25 = true;
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 5));
                ((LivingEntity)this.getBukkitEntity()).setMaxHealth(10.0);
                this.setHealth(10.0F);
            }

            if (this.getHealth() <= 0.0 && !this.die) {
                this.die = true;

                if (this.attacks >= 25) { /* after 25 attacks, killer bunnies explode when killed */
                    this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 1.0F, false, Explosion.Effect.DESTROY);
                }
            }
        }
    }

    static class PathfinderGoalKillerRabbitMeleeAttack extends CustomPathfinderGoalMeleeAttack {

        public PathfinderGoalKillerRabbitMeleeAttack(EntityRabbit entityRabbit) {
            super(entityRabbit, 1.4D);
        }

        @Override
        protected double a(EntityLiving entityLiving) {
            return (4.0F + entityLiving.getWidth());
        }
    }
}
