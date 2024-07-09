package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.misc.CustomEntityAreaEffectCloud;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntitySpiderCave extends EntityCaveSpider implements ICustomHostile, IAttackLevelingMob {

    private int attacks;
    private boolean a25, a60;
    private final CustomEntityAreaEffectCloud newAEC;

    public CustomEntitySpiderCave(World world) {
        super(EntityTypes.CAVE_SPIDER, world);
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.attacks = 0;
        this.a25 = false;
        this.a60 = false;
        this.newAEC = new CustomEntityAreaEffectCloud(this.world, 0.0F,40, 39);
        this.newAEC.addEffect(new MobEffect(MobEffects.HARM, 0));
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.51); /* cave spiders move 70% faster but only do 1 damage and have 8 health */
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(1.0);
        this.setHealth(8.0F);
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(8.0);
    }

    @Override
    protected void initPathfinder() {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 80, 1, 0, 1, 0, true)); /* Breaks most blocks around the mob periodically */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(1, new NewPathfinderGoalSpawnBlocksEntitiesOnMob(this, org.bukkit.Material.COBWEB, 1)); /* custom goal that allows cave spider to summon cobwebs on itself constantly */
        this.goalSelector.a(3, new PathfinderGoalLeapAtTarget(this, 0.4F));
        this.goalSelector.a(4, new CustomPathfinderGoalMeleeAttack(this, 1.0)); /* Continues attacking regardless of y-level and line of sight (the old goal stopped the mob from attacking even if it has a target via CustomNearestAttackableTarget) */
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 0.8D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget(this));               /* Doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Doesn't take into account y-level, line of sight, or invis/skulls to initially find a target and maintain it as the target */
    }

    public double getDetectionRange() { /* cave spiders have 16 block detection range */
        return 16.0;
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

        if (this.ticksLived == 500) { /* duplicates if it has been alive for 25 seconds */
            new SpawnEntity(this.world, new CustomEntitySpiderCave(this.world), 1, null, null, this, false, true);
            this.getBukkitEntity().setCustomName("Good luck making me despawn"); // doesn't despawn and doesn't count towards mob cap
        } else if (this.ticksLived == 4800) { /* explodes and dies after 4 minutes to reduce lag */
            this.world.createExplosion(this, this.locX(), this.locY(), this.locZ(), 2.0F, false, Explosion.Effect.NONE);
            this.die();
        }

        if (this.attacks == 25 && !this.a25) { /* after 25 attacks, cave spiders gain speed 2 */
            this.a25 = true;
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 1));
        }

        if (this.attacks == 60 && !this.a60) { /* after 60 attacks, cave spiders summon area effect clouds wherever it goes in addition to cobwebs */
            this.a60 = true;
            this.goalSelector.a(1, new NewPathfinderGoalSpawnBlocksEntitiesOnMob(this, this.newAEC, 4));
        }

        if (this.isClimbing()) { /* cave spiders move vertically 2 times as fast (for some reason this still applies to jumping) */
            this.setMot(this.getMot().x, this.getMot().y * 2.0, this.getMot().z);
        }
    }
}
