package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.ExtremeDifficultyPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IGoalRemovingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.listeners.ListenerLightningStrike;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CustomMathHelper;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableLightningEffectStorm;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableTornado;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.Random;

public class CustomEntityZombieThor extends EntityZombie implements ICustomHostile, IGoalRemovingMob { // todo expend custom zombie?

    /* Ignores y-level and line of sight for initially finding a player target and maintaining it
       as the target, as well as for retaliating against players */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;
    public PathfinderGoalSelector vanillaTargetSelector;

    public CustomEntityZombieThor(World world) {
        super(EntityTypes.ZOMBIE, world);
        this.vanillaTargetSelector = super.targetSelector;
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.getBukkitEntity().setCustomName("Thor");
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
        this.setSlot(EnumItemSlot.OFFHAND, new ItemStack(Items.IRON_AXE));
        this.setBaby(false);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.575); /* thor zombies move 2.5x faster and have 55 health, but doesn't summon reinforcements */
        this.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).setValue(0.0);
        this.addEffect(new MobEffect(MobEffects.WEAKNESS, Integer.MAX_VALUE, 1)); /* this allows the zombie to only do ~2 damage at a time instead of 6 */
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(55.0);
        this.setHealth(55.0F);
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
        new RunnableLightningEffectStorm(this, 20, true).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 2L); /* thor summons a vanilla lightning storm around it when first spawned for 2 seconds */
    }

    @Override
    public void initPathfinder() { /* no longer targets iron golems, villagers or turtles */
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 40, 1, 1, 1, 1, true)); /* Breaks most blocks around the mob periodically */
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 1.0)); /* Spawns lightning randomly */
        this.goalSelector.a(0, new NewPathfinderGoalTeleportNearTarget(this, this.getDetectionRange(), 300.0, 0.004)); /* Occasionally teleports to a spot near its target */
        this.goalSelector.a(0, new CustomEntityZombieThor.PathfinderGoalThorSummonLightning(this)); /* custom goal that spawns lightning randomly within 20 blocks of thor on average every a second (75% chance to do no damage, 25% chance to be vanilla lightning) and also sometimes creates a vortex of harmless lightning around itself on average every 16 seconds and a tornado once on average after 20 seconds */
        this.goalSelector.a(2, new CustomPathfinderGoalZombieAttack(this, 1.0D)); /* uses the custom melee attack goal that attacks regardless of the y-level */
        this.goalSelector.a(2, new NewPathfinderGoalShootLargeFireballs(this, 80, 0, true)); /* custom goal that allows thor to shoot a power 1 ghast fireball every 4 seconds that summons vanilla lightning */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, IGNORE_LOS, IGNORE_Y)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the closest option */
    }

    @Override
    public void die() {
        super.die();
        ListenerLightningStrike.numberOfThors = Math.max(0, ListenerLightningStrike.numberOfThors - 1); // to make sure nothing weird happens
    }

    public double getDetectionRange() { /* thor zombies have 64 block detection range */
        return 64.0;
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.world.findNearbyPlayer(this, -1.0D);

            if (nearestPlayer != null) {
                /* Mobs only despawn along horizontal axes, so even at build height, mobs will spawn below you and prevent sleeping */
                double distSqToNearestPlayer = Math.pow(nearestPlayer.getPositionVector().getX() - this.getPositionVector().getX(), 2)
                        + Math.pow(nearestPlayer.getPositionVector().getZ() - this.getPositionVector().getZ(), 2);
                int forceDespawnDist = this.getEntityType().e().f();
                int forceDespawnDistSq = forceDespawnDist * forceDespawnDist;

                if (distSqToNearestPlayer > (double) forceDespawnDistSq
                        && this.isTypeNotPersistent(distSqToNearestPlayer)) {
                    this.die();
                }

                /* Random despawn distance increased to 64 blocks */
                int randomDespawnDist = this.getEntityType().e().g() + 32;
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

    @Override
    public void tick() {
        super.tick();

        ListenerLightningStrike.storm = true; /* thors can't summon other thors */

        if (!this.world.isClientSide && this.isAlive()) { /* doesn't convert to drowned */
            this.drownedConversionTime = Integer.MAX_VALUE;
        }

        if (this.ticksLived == 5) {
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "weather thunder"); /* thor causes thunderstorm */
        }
    }

    @Override
    public PathfinderGoalSelector getVanillaTargetSelector() {
        return this.vanillaTargetSelector;
    }
    
    public boolean getIgnoreLOS() {
        return IGNORE_LOS;
    }
    
    public boolean getIgnoreY() {
        return IGNORE_Y;
    }

    public static class PathfinderGoalThorSummonLightning extends PathfinderGoal {

        public final CustomEntityZombieThor thor;
        public final org.bukkit.World bukkitWorld;
        private final BlockPosition blockPosition;
        public boolean storm, tornado;
        private static final Random random = new Random();

        public PathfinderGoalThorSummonLightning(CustomEntityZombieThor thor) {
            this.thor = thor;
            this.bukkitWorld = thor.getWorld().getWorld();
            this.blockPosition = new BlockPosition(thor.locX(), thor.locY(), thor.locZ());
            this.storm = false;
            this.tornado = true; // todo should this be false?
        }

        @Override
        public boolean a() {
            return this.thor.getGoalTarget() != null;
        }

        @Override
        public boolean b() {
            return this.a();
        }

        @Override
        public void e() {
            if (random.nextDouble() < 0.05) {
                Location bukkitLoc2 = CustomMathHelper.coordsFromHypotAndAngle(this.bukkitWorld, this.blockPosition, 20.0, this.bukkitWorld.getHighestBlockYAt(new Location(this.bukkitWorld, this.blockPosition.getX(), this.blockPosition.getY(), this.blockPosition.getZ())), 361.0);
                if (random.nextDouble() < 0.25) {
                    this.bukkitWorld.strikeLightning(bukkitLoc2);
                } else {
                    this.bukkitWorld.strikeLightningEffect(bukkitLoc2);
                }
            }

            if (random.nextDouble() < 0.003125 && !this.storm) {
                new RunnableLightningEffectStorm(this, random.nextInt(6) + 25).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 2L);
            }

            if (random.nextDouble() < 0.002 && !this.storm && !this.tornado) {
                this.tornado = true;
                new RunnableTornado(this.thor.getWorld(), this.blockPosition, 30.0, 120).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 1L);
            }
        }
    }
}
