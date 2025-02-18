package AnonymousRand.anonymousrand.plugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.IGoalRemovingMob;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.VanillaPathfinderGoalsRemove;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.attack.CustomPathfinderGoalAttackMelee;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.attackmvmt.CustomPathfinderGoalAttackMvmtMelee;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.target.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityZombieSuper extends EntityZombie implements ICustomHostile, IGoalRemovingMob { // todo extend custom zombie?

    /* Ignores line of sight and y-level for initially finding a player target and maintaining it as the target,
     * as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;
    public CustomEntityZombieSuper(World world) {
        super(EntityTypes.ZOMBIE, world);
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.setBaby(false); /* super zombies move 2x faster, always summon a reinforcement when hit, and have 5 base health */
        this.setCanPickupLoot(true);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.46);
        this.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).setValue(1.0);
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(5.0);
        this.setHealth(5.0F);
        VanillaPathfinderGoalsRemove.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    @Override
    protected void initPathfinder() { /* No longer targets iron golems, turtles, and villagers */
        super.initPathfinder();
        this.goalSelector.a(0, new CustomPathfinderGoalBreakBlocksAround(this, 40, 2, 1, 2, 1, true)); /* Breaks most blocks around the mob periodically */
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new CustomPathfinderGoalSummonLightningRandomly(this, 1.0)); /* Spawns lightning randomly */
        this.goalSelector.a(2, new CustomPathfinderGoalAttackMelee<>(this)); /* uses the custom melee attack goal that attacks regardless of the y-level */
        this.goalSelector.a(2, new CustomPathfinderGoalAttackMvmtMelee<>(this));
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget<>(this));                                /* Always retaliates against players and teleports to them if they are out of range/do not have line of sight, but doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */ // todo does the listener actually not work sometimes? also if this is no longer needed, don't remove old goal in igoalremovingmob
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the nearest option */
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damageAmount) {
        if (!super.damageEntity(damageSource, damageAmount)) {
            return false;
        }

        EntityLiving attackTarget = this.getGoalTarget();

        if (attackTarget == null) {
            attackTarget = (EntityLiving) damageSource.getEntity();
        }

        if (!(attackTarget instanceof EntityPlayer)) { /* only player damage can trigger reinforcement spawning */
            return true;
        }

        if ((double) random.nextDouble() < this.b(GenericAttributes.SPAWN_REINFORCEMENTS)) { /* zombies can now spawn reinforcements on any difficulty */
            for (int ii = 0; ii < (random.nextDouble() < 0.99 ? 1 : 30); ii++) { /* 1% chance to make 30 spawn attempts instead of 1 (on average, about half of them succeed) */
                int i = MathHelper.floor(this.locX());
                int j = MathHelper.floor(this.locY());
                int k = MathHelper.floor(this.locZ());
                CustomEntityZombie newZombie = new CustomEntityZombie(this.world);

                for (int l = 0; l < 50; ++l) {
                    int i1 = i + MathHelper.nextInt(random, 7, 40) * MathHelper.nextInt(random, -1, 1);
                    int j1 = j + MathHelper.nextInt(random, 7, 40) * MathHelper.nextInt(random, -1, 1);
                    int k1 = k + MathHelper.nextInt(random, 7, 40) * MathHelper.nextInt(random, -1, 1);
                    BlockPosition blockPos = new BlockPosition(i1, j1, k1);
                    EntityTypes<?> entityTypes = newZombie.getEntityType();
                    EntityPositionTypes.Surface entityPositiontypes_surface = EntityPositionTypes.a(entityTypes);

                    if (SpawnerCreature.a(entityPositiontypes_surface, this.world, blockPos, entityTypes) && EntityPositionTypes.a(entityTypes, this.world, EnumMobSpawn.REINFORCEMENT, blockPos, this.world.random)) {
                        newZombie.setPosition(i1, j1, k1);
                        if (!this.world.isPlayerNearby(i1, j1, k1, 7.0) && this.world.i(newZombie) && this.world.getCubes(newZombie) && !this.world.containsLiquid(newZombie.getBoundingBox())) {
                            this.world.addEntity(newZombie);
                            newZombie.prepare(this.world, this.world.getDamageScaler(newZombie.getChunkCoordinates()), EnumMobSpawn.REINFORCEMENT, null, null);
                            this.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).addModifier(new AttributeModifier("Zombie reinforcement caller charge", -0.125, AttributeModifier.Operation.ADDITION)); /* zombies experience a 12.5% decrease in reinforcement summon chance instead of 5% if summoned reinforcements or was summoned as reinforcement (15% after 7 attacks) */
                            newZombie.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).addModifier(new AttributeModifier("Zombie reinforcement callee charge", -0.125, AttributeModifier.Operation.ADDITION));
                            break;
                        }
                    }
                }
            }
        }

        return true;
    }

    public double getDetectionRange() { /* super zombies have 128 block detection range */
        return 128.0;
    }

    public boolean ignoresLOS() {
        return IGNORE_LOS;
    }

    public boolean ignoresY() {
        return IGNORE_Y;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.world.isClientSide && this.isAlive() && !this.isNoAI()) { /* doesn't convert to drowned in water */
            this.drownedConversionTime = Integer.MAX_VALUE;
        }
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
    public PathfinderGoalSelector getVanillaGoalSelector() {
        return super.goalSelector;
    }

    public PathfinderGoalSelector getVanillaTargetSelector() {
        return super.targetSelector;
    }
}
