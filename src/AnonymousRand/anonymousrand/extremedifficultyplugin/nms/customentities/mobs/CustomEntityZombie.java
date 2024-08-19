package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.ExtremeDifficultyPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IGoalRemovingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.VanillaPathfinderGoalsRemove;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack.CustomPathfinderGoalMeleeAttack;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.movement.CustomPathfinderGoalMeleeMovement;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableMeteorRain;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;

public class CustomEntityZombie extends EntityZombie implements ICustomHostile, IAttackLevelingMob, IGoalRemovingMob {

    /* Ignores line of sight and y-level for initially finding a player target and maintaining it as the target,
     * as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;
    private int attacks;
    private boolean a7, a15, a25, a40, a50;
    private static Field bA;

    public CustomEntityZombie(World world) {
        super(EntityTypes.ZOMBIE, world);
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
        VanillaPathfinderGoalsRemove.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
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
        this.goalSelector.a(0, new CustomPathfinderGoalBreakBlockLookingAt(this)); /* custom goal that allows the mob to break the block it is looking at every 4 seconds as long as it has a target, it breaks the block that it is looking at up to 40 blocks away */
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new CustomPathfinderGoalSummonLightningRandomly(this, 1.0)); /* Spawns lightning randomly */
        this.goalSelector.a(0, new CustomPathfinderGoalTeleportNearTarget(this, this.getDetectionRange(), 300.0, 0.0015)); /* Occasionally teleports to a spot near its target */
        this.goalSelector.a(2, new CustomPathfinderGoalMeleeAttack<>(this)); /* uses the custom melee attack goal that attacks regardless of the y-level */
        this.goalSelector.a(2, new CustomPathfinderGoalMeleeMovement<>(this));
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget<>(this));                                /* Always retaliates against players and teleports to them if they are out of range/do not have line of sight, but doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */ // todo does the listener actually not work sometimes? also if this is no longer needed, don't remove old goal in igoalremovingmob
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the nearest option */
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, false, false, EntityTurtle.bv));
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damageAmount) {
        if (!super.damageEntity(damageSource, damageAmount)) {
            return false;
        }

        EntityLiving goalTarget = this.getGoalTarget();

        if (goalTarget == null) {
            goalTarget = (EntityLiving) damageSource.getEntity();
        }

        if (!(goalTarget instanceof EntityPlayer)) { /* only player damage can trigger reinforcement spawning */
            return true;
        }

        if (this.attacks >= 30) { /* after 30 attacks, zombies summon vanilla lightning on the player when it is hit */
            this.world.getWorld().strikeLightning(new Location(this.world.getWorld(), goalTarget.locX(), goalTarget.locY(), goalTarget.locZ()));
        }

        if ((double) random.nextDouble() < this.b(GenericAttributes.SPAWN_REINFORCEMENTS)) { /* zombies can now spawn reinforcements on any difficulty */
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
                EntityPositionTypes.Surface entityPositionTypes_Surface = EntityPositionTypes.a(entityTypes);

                if (SpawnerCreature.a(entityPositionTypes_Surface, this.world, blockPos, entityTypes) && EntityPositionTypes.a(entityTypes, this.world, EnumMobSpawn.REINFORCEMENT, blockPos, this.world.random)) {
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
    }

    public double getDetectionRange() { /* zombies have 40 block detection range */
        return 40.0;
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

    @Override
    public PathfinderGoalSelector getVanillaGoalSelector() {
        return super.goalSelector;
    }

    public PathfinderGoalSelector getVanillaTargetSelector() {
        return super.targetSelector;
    }
}
