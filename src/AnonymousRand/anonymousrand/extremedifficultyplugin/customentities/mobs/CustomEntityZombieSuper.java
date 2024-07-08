package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityZombieSuper extends EntityZombie implements ICustomHostile { // todo extend custom zombie?

    public PathfinderGoalSelector vanillaTargetSelector;

    public CustomEntityZombieSuper(World world) {
        super(EntityTypes.ZOMBIE, world);
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.vanillaTargetSelector = super.targetSelector;
        this.setBaby(false); /* super zombies move 2x faster, always summon a reinforcement when hit, and have 5 base health */
        this.setCanPickupLoot(true);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.46);
        this.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).setValue(1.0);
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(5.0);
        this.setHealth(5.0F);
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    @Override
    protected void initPathfinder() { /* No longer targets iron golems and villagers */
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 40, 2, 1, 2, 1, true)); /* Breaks most blocks around the mob periodically */
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 1.0)); /* Spawns lightning randomly */
        this.goalSelector.a(2, new CustomPathfinderGoalZombieAttack(this, 1.0D)); /* uses the custom melee attack goal that attacks regardless of the y-level */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Doesn't take into account y-level or line of sight to aggro a target or and maintain it as the target */
        this.targetSelector.a(4, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, EntityTurtle.bv));
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damageAmount) {
        if (super.damageEntity(damageSource, damageAmount)) {
            EntityLiving entityLiving = this.getGoalTarget();

            if (entityLiving == null) {
                entityLiving = (EntityLiving) damageSource.getEntity();
            }

            if (!(entityLiving instanceof EntityPlayer)) { /* only player damage can trigger reinforcement spawning */
                return true;
            }

            if ((double) random.nextFloat() < this.b(GenericAttributes.SPAWN_REINFORCEMENTS)) { /* zombies can now spawn reinforcements on any difficulty */
                for (int ii = 0; ii < (random.nextDouble() < 0.99 ? 1 : 30); ii++) { /* 1% chance to make 30 spawn attempts instead of 1 (on average, about half of them succeed) */
                    int i = MathHelper.floor(this.locX());
                    int j = MathHelper.floor(this.locY());
                    int k = MathHelper.floor(this.locZ());
                    CustomEntityZombie newZombie = new CustomEntityZombie(this.getWorld());

                    for (int l = 0; l < 50; ++l) {
                        int i1 = i + MathHelper.nextInt(random, 7, 40) * MathHelper.nextInt(random, -1, 1);
                        int j1 = j + MathHelper.nextInt(random, 7, 40) * MathHelper.nextInt(random, -1, 1);
                        int k1 = k + MathHelper.nextInt(random, 7, 40) * MathHelper.nextInt(random, -1, 1);
                        BlockPosition blockPosition = new BlockPosition(i1, j1, k1);
                        EntityTypes<?> entityTypes = newZombie.getEntityType();
                        EntityPositionTypes.Surface entityPositiontypes_surface = EntityPositionTypes.a(entityTypes);

                        if (SpawnerCreature.a(entityPositiontypes_surface, this.getWorld(), blockPosition, entityTypes) && EntityPositionTypes.a(entityTypes, this.getWorld(), EnumMobSpawn.REINFORCEMENT, blockPosition, this.getWorld().random)) {
                            newZombie.setPosition(i1, j1, k1);
                            if (!this.getWorld().isPlayerNearby(i1, j1, k1, 7.0D) && this.getWorld().i(newZombie) && this.getWorld().getCubes(newZombie) && !this.getWorld().containsLiquid(newZombie.getBoundingBox())) {
                                this.getWorld().addEntity(newZombie);
                                newZombie.prepare(this.getWorld(), this.getWorld().getDamageScaler(newZombie.getChunkCoordinates()), EnumMobSpawn.REINFORCEMENT, null, null);
                                this.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).addModifier(new AttributeModifier("Zombie reinforcement caller charge", -0.125, AttributeModifier.Operation.ADDITION)); /* zombies experience a 12.5% decrease in reinforcement summon chance instead of 5% if summoned reinforcements or was summoned as reinforcement (15% after 7 attacks) */
                                newZombie.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).addModifier(new AttributeModifier("Zombie reinforcement callee charge", -0.125, AttributeModifier.Operation.ADDITION));
                                break;
                            }
                        }
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public double getDetectionRange() { /* super zombies have 128 block detection range */
        return 128.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getWorld().isClientSide && this.isAlive() && !this.isNoAI()) { /* doesn't convert to drowned in water */
            this.drownedConversionTime = Integer.MAX_VALUE;
        }
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
}