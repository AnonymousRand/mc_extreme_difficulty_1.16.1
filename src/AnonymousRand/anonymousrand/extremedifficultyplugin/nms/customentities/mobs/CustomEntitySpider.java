package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableSpawnBlocksAround;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CustomEntitySpider extends EntitySpider implements ICustomHostile, IAttackLevelingMob {

    /* Ignores line of sight and y-level for initially finding a player target and maintaining it as the target,
       as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;
    private int attacks;
    private boolean a20, a25, a50, a80;

    public CustomEntitySpider(World world) {
        super(EntityTypes.SPIDER, world);
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.attacks = 0;
        this.a20 = false;
        this.a25 = false;
        this.a50 = false;
        this.a80 = false;
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.51); /* spiders move 70% faster but only do 1 damage and have 10 health */
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(1.0);
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(10.0);
        this.setHealth(10.0F);
    }

    @Override
    protected void initPathfinder() { /* no longer targets iron golems */
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 80, 1, 0, 1, 0, true)); /* Breaks most blocks around the mob periodically */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(1, new NewPathfinderGoalSpawnBlocksEntitiesOnMob(this, org.bukkit.Material.COBWEB, 1)); /* custom goal that allows spider to summon cobwebs on itself constantly */
        this.goalSelector.a(3, new PathfinderGoalLeapAtTarget(this, 0.4F));
        this.goalSelector.a(4, new CustomPathfinderGoalMeleeAttack<>(this));
        this.goalSelector.a(4, new CustomPathfinderGoalMeleeMovement<>(this));
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 0.8D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget<>(this));               /* Always retaliates against players and teleports to them if they are out of range/do not have line of sight, but doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the nearest option */
    }

    public double getDetectionRange() { /* spiders have 16 block detection range */
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

        if (this.attacks % 24 == 0 && this.a25) { // reset right before the next cycle
            this.a25 = false;
        }

        if (this.attacks % 25 == 0 && this.attacks != 0 && !this.a25) { /* every 25 attacks, spiders lay down cobwebs that last 5 seconds in a 3 by 3 cube around itself */
            this.a25 = true;
            new RunnableSpawnBlocksAround(this, org.bukkit.Material.COBWEB, 1).run();
        }

        if (this.attacks == 20 && !this.a20) { /* after 20 attacks, spiders gain speed 1 */
            this.a20 = true;
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 0));
        }

        if (this.attacks == 50 && !this.a50) { /* after 50 attacks, spiders summon 2 vanilla cave spiders */
            this.a50 = true;
            new SpawnEntity(this.world, new EntityCaveSpider(EntityTypes.CAVE_SPIDER, this.world), 2, CreatureSpawnEvent.SpawnReason.DROWNED, null, this, false, true);
        }

        if (this.attacks == 80 && !this.a80) { /* after 80 attacks, spiders summon 2 cave spiders */
            this.a80 = true;
            new SpawnEntity(this.world, new CustomEntitySpiderCave(this.world), 2, null, null, this, false, true);
        }

        if (this.isClimbing()) { /* spiders move vertically 2 times as fast (for some reason this still applies to jumping) */
            this.setMot(this.getMot().x, this.getMot().y * 2.0, this.getMot().z);
        }
    }
}
