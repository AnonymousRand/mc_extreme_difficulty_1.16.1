package AnonymousRand.anonymousrand.plugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.attackmvmt.CustomPathfinderGoalAttackMvmtMelee;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.target.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.attack.CustomPathfinderGoalAttackMelee;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_16_R1.*;

import java.util.UUID;

public class CustomEntityWolf extends EntityWolf implements ICustomHostile {

    private static final boolean IGNORE_LOS = false;
    private static final boolean IGNORE_Y = false;

    public CustomEntityWolf(World world) {
        super(EntityTypes.WOLF, world);
    }

    @Override
    public void initPathfinder() { /* no longer avoids llamas, breeds, follows/defends owners, begs, and attack skeletons */
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(4, new PathfinderGoalLeapAtTarget(this, 0.4F));
        this.goalSelector.a(5, new CustomPathfinderGoalAttackMelee<>(this)); /* uses the custom melee attack goal that attacks regardless of the y-level */
        this.goalSelector.a(5, new CustomPathfinderGoalAttackMvmtMelee<>(this));
        this.goalSelector.a(8, new PathfinderGoalRandomStrollLand(this, 1.0));
        this.goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 8.0F));
        this.goalSelector.a(10, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget<>(this, CustomEntityWolf.class));
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* always aggro at players; uses the custom goal which doesn't need line of sight to start attack (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
    }

    public double getDetectionRange() { /* wolves have 16 blocks detection range */
        return 16.0;
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

    @Override
    public void tick() {
        super.tick();

        if (this.isTamed() && this.ticksLived % 10 == 0) { /* wolves can't be tamed */
            this.setTamed(false);
            this.setOwnerUUID(UUID.randomUUID());
        }
    }
}
