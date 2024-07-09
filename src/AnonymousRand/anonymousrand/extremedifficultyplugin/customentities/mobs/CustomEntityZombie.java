package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.ExtremeDifficultyPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableMeteorRain;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;

public class CustomEntityZombie extends EntityZombie implements ICustomHostile, IAttackLevelingMob {

    public PathfinderGoalSelector vanillaTargetSelector;
    private int attacks;
    private boolean a7, a15, a25, a40, a50;
    private static Field bA;

    public CustomEntityZombie(World world) {
        super(EntityTypes.ZOMBIE, world);
        this.vanillaTargetSelector = super.targetSelector;
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.attacks = 0;
        this.a7 = false;
        this.a15 = false;
        this.a25 = false;
        this.a40 = false;
        this.a50 = false;
        this.expToDrop = 1; // to differentiate between vanilla villagers in listenermobspawnandreplace to avoid potential infinite loops
        this.setBaby(true);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.46); /* zombies are always babies, move 2x faster, and have a 20% chance to summon a reinforcement when hit by a player */
        this.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).setValue(0.2);
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    static {
        try {
            bA = EntityZombie.class.getDeclaredField("bA");
            bA.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initPathfinder() { /* No longer targets iron golems and villagers */
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlockLookingAt(this)); /* custom goal that allows the mob to break the block it is looking at every 4 seconds as long as it has a target, it breaks the block that it is looking at up to 40 blocks away */
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 1.0)); /* Spawns lightning randomly */
        this.goalSelector.a(0, new NewPathfinderGoalTeleportNearTarget(this, this.getDetectionRange(), 300.0, 0.0015)); /* Occasionally teleports to a spot near its target */
        this.goalSelector.a(2, new CustomPathfinderGoalZombieAttack(this, 1.0D)); /* uses the custom melee attack goal that attacks regardless of the y-level */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Doesn't take into account y-level, line of sight, or invis/skulls to initially find a target and maintain it as the target */
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

            if (this.attacks >= 30) { /* after 30 attacks, zombies summon vanilla lightning on the player when it is hit */
                this.world.getWorld().strikeLightning(new Location(this.world.getWorld(), entityLiving.locX(), entityLiving.locY(), entityLiving.locZ()));
            }

            if ((double) random.nextFloat() < this.b(GenericAttributes.SPAWN_REINFORCEMENTS)) { /* zombies can now spawn reinforcements on any difficulty */
                int i = MathHelper.floor(this.locX());
                int j = MathHelper.floor(this.locY());
                int k = MathHelper.floor(this.locZ());
                CustomEntityZombie newZombie = new CustomEntityZombie(this.world);

                for (int l = 0; l < 50; ++l) {
                    int i1 = i + MathHelper.nextInt(random, 7, 40) * MathHelper.nextInt(random, -1, 1);
                    int j1 = j + MathHelper.nextInt(random, 7, 40) * MathHelper.nextInt(random, -1, 1);
                    int k1 = k + MathHelper.nextInt(random, 7, 40) * MathHelper.nextInt(random, -1, 1);
                    BlockPosition blockPosition = new BlockPosition(i1, j1, k1);
                    EntityTypes<?> entityTypes = newZombie.getEntityType();
                    EntityPositionTypes.Surface entityPositionTypes_Surface = EntityPositionTypes.a(entityTypes);

                    if (SpawnerCreature.a(entityPositionTypes_Surface, this.world, blockPosition, entityTypes) && EntityPositionTypes.a(entityTypes, this.world, EnumMobSpawn.REINFORCEMENT, blockPosition, this.world.random)) {
                        newZombie.setPosition(i1, j1, k1);
                        if (!this.world.isPlayerNearby(i1, j1, k1, 7.0D) && this.world.i(newZombie) && this.world.getCubes(newZombie) && !this.world.containsLiquid(newZombie.getBoundingBox())) {
                            this.world.addEntity(newZombie);
                            newZombie.prepare(this.world, this.world.getDamageScaler(newZombie.getChunkCoordinates()), EnumMobSpawn.REINFORCEMENT, null, null);
                            this.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).addModifier(new AttributeModifier("Zombie reinforcement caller charge", -0.125, AttributeModifier.Operation.ADDITION)); /* zombies and their summoned reinforcement experience a 12.5% decrease in reinforcement summon chance instead of 5% if summoned reinforcements or was summoned as reinforcement */
                            newZombie.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).addModifier(new AttributeModifier("Zombie reinforcement callee charge", this.attacks < 7 ? -0.125 : -0.2, AttributeModifier.Operation.ADDITION));
                            break;
                        }
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public double getDetectionRange() { /* zombies have 40 block detection range */
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

        if (!this.world.isClientSide && this.isAlive() && !this.isNoAI()) { /* converts to drowned 4 times faster */
            if (this.isDrownConverting()) {
                this.drownedConversionTime -= 3;
                if (this.drownedConversionTime < 0) {
                    this.eQ();
                }
            } else if (this.eO()) {
                try {
                    if (this.a(TagsFluid.WATER)) {
                        bA.setInt(this, bA.getInt(this) + 3);
                    } else {
                        bA.setInt(this, -1);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        if (this.attacks == 7 && !this.a7) { /* after 7 attacks, zombies get 15% more reinforcement summon chance */
            this.a7 = true;
            this.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).setValue(0.35);
        }

        if (this.attacks == 15 && !this.a15) { /* after 15 attacks, zombies get regen 2, 30 max health and summon 2 vanilla zombies */
            this.a15 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
            ((LivingEntity) this.getBukkitEntity()).setMaxHealth(30.0);
            this.setHealth(30.0F);
            new SpawnEntity(this.world, new EntityZombie(EntityTypes.ZOMBIE, this.world), 2, CreatureSpawnEvent.SpawnReason.BEEHIVE, null, this, false, true);
        }

        if (this.attacks == 25 && !this.a25) { /* after 25 attacks, zombies get 40 max health and health and summon another baby zombie */
            this.a25 = true;
            ((LivingEntity) this.getBukkitEntity()).setMaxHealth(40.0);
            this.setHealth(40.0F);
            new SpawnEntity(this.world, new CustomEntityZombie(this.world), 1, null, null, this, false, true);
        }

        if (this.attacks == 50 && !this.a50) { /* after 50 attacks, zombies summon thor */
            this.a50 = true;
            new SpawnEntity(this.world, new CustomEntityZombieThor(this.world), 1, null, null, this, false, true);
        }

        if (this.getHealth() <= 0.0 && this.attacks >= 40 && !this.a40) { /* after 40 attacks, zombies summon a small meteor rain when it dies */
            this.a40 = true; // do this here instead of in die() so that the meteor rain doesn't have to wait until the death animation finishes playing to start

            new RunnableMeteorRain(this, 1, 40.0, 12).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 2L);
            new RunnableMeteorRain(this, 2, 40.0, 8).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 2L);
            new RunnableMeteorRain(this, 3, 40.0, 7).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 2L);
        }
    }
}
