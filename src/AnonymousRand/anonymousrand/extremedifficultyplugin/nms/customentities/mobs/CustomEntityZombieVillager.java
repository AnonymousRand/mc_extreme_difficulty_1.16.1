package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.ExtremeDifficultyPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IGoalRemovingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.listeners.ListenerLightningStrike;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableLightningStorm;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.UUID;

public class CustomEntityZombieVillager extends EntityZombieVillager implements ICustomHostile, IAttackLevelingMob, IGoalRemovingMob {

    public PathfinderGoalSelector vanillaTargetSelector;
    private int attacks;
    private boolean a25, deathLightningStorm;

    public CustomEntityZombieVillager(World world) {
        super(EntityTypes.ZOMBIE_VILLAGER, world);
        this.vanillaTargetSelector = super.targetSelector;
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.attacks = 0;
        this.a25 = false;
        this.deathLightningStorm = false;
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.69); /* zombie villgers move 3x faster */
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
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
        this.targetSelector.a(4, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, EntityTurtle.bv));
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Ignores y-level, line of sight, or invis/skulls for initially finding a target and maintaining it as the target if it's a player */
    }

    @Override
    public void startConversion(@Nullable UUID uuid, int i) { /* zombie villagers can't be converted and instead summon 10 more zombie villagers when fed a golden apple */
        new SpawnEntity(this.world, new CustomEntityZombieVillager(this.world), 10, null, null, this, false, true);
        this.world.broadcastEntityEffect(this, (byte) 16);
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

        if (this.attacks == 25 && !this.a25) { /* after 25 attacks, zombie villagers get regen 4 */
            this.a25 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 3));
        }

        if (this.getHealth() <= 0.0 && this.attacks >= 40 && !this.deathLightningStorm) { // do this here instead of in die() so that the storm doesn't have to wait until the death animation finishes playing to start
            this.deathLightningStorm = true;
            ListenerLightningStrike.storm = true;
            new RunnableLightningStorm(this.world, new Location(this.world.getWorld(), this.locX(), this.locY(), this.locZ()), random.nextInt(10) + 50).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, random.nextInt(3) + 3); /* after 40 attacks, zombie villagers summon a lightning storm when killed */
        }
    }

    @Override
    public PathfinderGoalSelector getVanillaTargetSelector() {
        return this.vanillaTargetSelector;
    }
}
