package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableSpawnBlocksAround;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntitySilverfish extends EntitySilverfish implements ICustomHostile, IAttackLevelingMob {

    public PathfinderGoalSelector vanillaTargetSelector;
    private int attacks;
    private boolean a15, a90;

    public CustomEntitySilverfish(World world) {
        super(EntityTypes.SILVERFISH, world);
        this.vanillaTargetSelector = super.targetSelector;
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.attacks = 0;
        this.a15 = false;
        this.a90 = false;
        this.getBukkitEntity().setCustomName("Having a good time?");
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.375); /* silverfish move 50% faster and have 9 health */
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(9.0);
        this.setHealth(9.0F);
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 100, 1, 0, 1, 0, true)); /* Breaks most blocks around the mob periodically */
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new NewPathfinderGoalTeleportNearTargetYLevel(this, 1.0, random.nextDouble() * 3.0, 0.0075)); /* Occasionally teleports to a spot closer in y-level to its target */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Doesn't take into account y-level or line of sight to aggro a target */
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damageAmount) {
        boolean tookDamage = super.damageEntity(damageSource, damageAmount);
        if (tookDamage && damageSource.getEntity() instanceof EntityPlayer && !this.killed) { /* duplicates when hit by player and not killed */
            new SpawnEntity(this.getWorld(), new CustomEntitySilverfish(this.getWorld()), 1, null, null, this, false, true);
        }

        return tookDamage;
    }

    public double getFollowRange() { /* silverfish have 20 block detection range */
        return 20.0;
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.getWorld().findNearbyPlayer(this, -1.0D);

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

        if (this.attacks == 15 && !this.a15) { /* after 15 attacks, silverfish gain speed 3 */
            this.a15 = true;
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 2));
        }

        if (this.attacks == 90 && !this.a90) { /* after 90 attacks, silverfish spawns a 5 by 5 by 5 block of invested stone around it and dies */
            this.a90 = true;
            new RunnableSpawnBlocksAround(this, org.bukkit.Material.INFESTED_STONE, 2).run();
            this.die();
        }
    }
}
