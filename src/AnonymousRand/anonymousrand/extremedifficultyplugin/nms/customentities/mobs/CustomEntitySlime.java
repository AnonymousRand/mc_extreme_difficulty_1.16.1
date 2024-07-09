package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.NewPathfinderGoalBreakBlocksAround;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.NewPathfinderGoalSlimeMeleeAttack;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CustomEntitySlime extends EntitySlime implements ICustomHostile, IAttackLevelingMob {

    public PathfinderGoalSelector vanillaTargetSelector;
    private int attacks;
    private boolean a12, a35, deathExplosion;

    public CustomEntitySlime(World world) {
        super(EntityTypes.SLIME, world);
        this.vanillaTargetSelector = super.targetSelector;
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.attacks = 0;
        this.a12 = false;
        this.a35 = false;
        this.deathExplosion = false;
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    public CustomEntitySlime(World world, int size) {
        this(world);
        this.setSize(size, true);
    }

    @Override
    protected void initPathfinder() { /* no longer targets iron golems */
        super.initPathfinder();

        this.goalSelector.a(1, new NewPathfinderGoalSlimeMeleeAttack(this, 1.0)); /* small slimes also do damage; uses the custom goal that attacks regardless of the y-level (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal) */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Doesn't take into account y-level, line of sight, or invis/skulls to find a target; for some reason the slimes run away after a while without the extra parameters */
    }

    @Override
    public void setSize(int i, boolean flag) { /* toned down stats a bit to account for potential size 8 slimes */
        super.setSize(i, flag);
        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(1.0 + ((Math.log10(i) / Math.log10(2)) * ((2 * Math.log10(i) + 1) / (Math.log10(1.6))))); // approx: 1 health for size 1, 8.849 health for size 2, 22.596 health for size 4, 42.243 health for size 8
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(Math.ceil(0.325F + 0.05F * (float) i)); // 0.375 for 1, 0.425 for 2, 0.525 for 4, 0.725 for 8
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(Math.ceil(i / 1.5)); // 1 for 1, 2 for 2, 3 for 4, 6 for 8
        if (flag) {
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    protected void j(EntityLiving entityLiving) {} /* slimes use the NewPathfinderGoalSlimeMeleeAttack instead of this attack function */

    @Override
    public void die() {
        super.die();

        if (this.attacks >= 35) { /* after 35 attacks, slimes summon 6-8 mini-slimes when killed instead of 2-4 */
            int i = this.getSize();

            if (!this.world.isClientSide && i > 1 && this.dk()) {
                IChatBaseComponent ichatbasecomponent = this.getCustomName();
                boolean flag = this.isNoAI();
                float f = (float) i / 4.0F;
                int j = i / 2;

                for (int l = 0; l < 4; ++l) {
                    float f1 = ((float) (l % 2) - 0.5F) * f;
                    float f2 = ((float) (l / 2) - 0.5F) * f;
                    EntitySlime entitySlime = this.getEntityType().a(this.world);

                    if (this.isPersistent()) {
                        entitySlime.setPersistent();
                    }

                    entitySlime.setCustomName(ichatbasecomponent);
                    entitySlime.setNoAI(flag);
                    entitySlime.setInvulnerable(this.isInvulnerable());
                    entitySlime.setSize(j, true);
                    entitySlime.setPositionRotation(this.locX() + (double) f1, this.locY() + 0.5D, this.locZ() + (double) f2, random.nextFloat() * 360.0F, 0.0F);
                    this.world.addEntity(entitySlime, CreatureSpawnEvent.SpawnReason.SLIME_SPLIT);
                }
            }
        }
    }

    protected int eK() { /* slimes jump faster */
        return random.nextInt(3) + 6;
    }

    public double getDetectionRange() { /* slimes have 40 block detection range */
        return 40.0;
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.world.findNearbyPlayer(this, -1.0D);

            if (nearestPlayer != null) {
                /* Mobs only despawn along horizontal axes, so even at y=256, mobs will spawn below you and prevent sleeping */
                double distSqToNearestPlayer = Math.pow(nearestPlayer.getPositionVector().getX() - this.getPositionVector().getX(), 2)
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

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && distSqToNearestPlayer
                        > (double) randomDespawnDistSq && this.isTypeNotPersistent(distSqToNearestPlayer)) {
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

//    public int[] getAttacksThresholds() {
//        return this.attackLevelingController.getAttacksThresholds();
//    }

    @Override
    public void tick() {
        super.tick();

        if (this.getHealth() <= 0.0 && this.attacks >= 22 && !this.deathExplosion) { /* after 22 attacks, slimes explode when killed */
            this.deathExplosion = true;
            this.world.createExplosion(this, this.locX(), this.locY(), this.locZ(), (float) (Math.log10(this.getSize()) / Math.log10(2.0)), false, Explosion.Effect.DESTROY);
        }

        if (this.attacks == 12 && !this.a12) { /* after 12 attacks, slimes increase in size by 1 unless it is already at the largest possible size or is going to exceed it */
            this.a12 = true;

            if (this.getSize() < 8) {
                this.setSize(this.getSize() + 1, true);
            }
        }

        if (this.attacks == 35 && !this.a35) { /* after 35 attacks, slimes get extra knockback */
            this.a35 = true;
            this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(2.0);
        }

        if (this.ticksLived == 5) {
            if (this.getSize() > 3) {
                this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 40, this.getSize() / 4 + 1, this.getSize() / 4, this.getSize() / 4 + 1, this.getSize() / 4, false)); /* Breaks most blocks around the mob periodically */
            }
        }
    }
}
