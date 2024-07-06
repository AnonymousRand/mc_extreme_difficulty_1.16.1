package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.listeners.ListenerLightningStrike;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CustomMathHelper;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableThorLightningEffectStorm;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableTornado;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.Random;

public class CustomEntityZombieThor extends EntityZombie implements ICustomHostile { // todo expend custom zombie?

    public PathfinderGoalSelector vanillaTargetSelector;

    public CustomEntityZombieThor(World world) {
        super(EntityTypes.ZOMBIE, world);
        this.vanillaTargetSelector = super.targetSelector;
        this.a(PathType.LAVA, 0.0F); /* no longer avoids lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F); /* no longer avoids fire */
        this.getBukkitEntity().setCustomName("Thor");
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
        this.setSlot(EnumItemSlot.OFFHAND, new ItemStack(Items.IRON_AXE));
        this.setBaby(false);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.575); /* thor zombies move 2.5x faster and have 55 health, but doesn't summon reinforcements */
        this.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).setValue(0.0);
        this.addEffect(new MobEffect(MobEffects.WEAKNESS, Integer.MAX_VALUE, 1)); /* this allows the zombie to only do ~2 damage at a time instead of 6 */
        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(55.0);
        this.setHealth(55.0F);
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
        new RunnableThorLightningEffectStorm(this, 20, true).runTaskTimer(StaticPlugin.plugin, 0L, 2L); /* thor summons a vanilla lightning storm around it when first spawned for 2 seconds */
    }

    @Override
    public void initPathfinder() { /* no longer targets iron golems, villagers or turtles */
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 40, 1, 1, 1, 1, true)); /* custom goal that breaks blocks around the mob periodically except for diamond blocks, emerald blocks, nertherite blocks, and beacons */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /* custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /* custom goal that allows this mob to take certain buffs from bats etc. */
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 1.0)); /* custom goal that spawns lightning randomly */
        this.goalSelector.a(0, new NewPathfinderGoalTeleportTowardsPlayer(this, this.getFollowRange(), 300.0, 0.004)); /* custom goal that gives mob a chance every tick to teleport to within initial follow_range-2 to follow_range+13 blocks of nearest player if it has not seen a player target within follow range for 15 seconds */
        this.goalSelector.a(0, new CustomEntityZombieThor.PathfinderGoalThorSummonLightning(this)); /* custom goal that spawns lightning randomly within 20 blocks of thor on average every a second (75% chance to do no damage, 25% chance to be vanilla lightning) and also sometimes creates a vortex of harmless lightning around itself on average every 16 seconds and a tornado once on average after 20 seconds */
        this.goalSelector.a(2, new CustomPathfinderGoalZombieAttack(this, 1.0D)); /* uses the custom melee attack goal that attacks regardless of the y level */
        this.goalSelector.a(2, new NewPathfinderGoalShootLargeFireballs(this, 80, 0, true)); /* custom goal that allows thor to shoot a power 1 ghast fireball every 4 seconds that summons vanilla lightning */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
    }

    @Override
    public void die() {
        super.die();
        ListenerLightningStrike.numberOfThors = Math.max(0, ListenerLightningStrike.numberOfThors - 1); // to make sure nothing weird happens
    }

    public double getFollowRange() { /* thor zombies have 64 block detection range (setting attribute doesn't work) */
        return 64.0;
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (nearestPlayer != null) {
                /* Mobs only despawn along horizontal axes, so even if you are at y=256, mobs will still spawn below you and prevent sleeping */
                double distSquaredToNearestPlayer = Math.pow(nearestPlayer.getPositionVector().getX() - this.getPositionVector().getX(), 2)
                        + Math.pow(nearestPlayer.getPositionVector().getZ() - this.getPositionVector().getZ(), 2);
                int forceDespawnDist = this.getEntityType().e().f();
                int forceDespawnDistSquared = forceDespawnDist * forceDespawnDist;

                if (distSquaredToNearestPlayer > (double) forceDespawnDistSquared && this.isTypeNotPersistent(distSquaredToNearestPlayer)) {
                    this.die();
                }

                /* Random despawn distance increased to 64 blocks */
                int randomDespawnDist = this.getEntityType().e().g() + 32;
                int randomDespawnDistSquared = randomDespawnDist * randomDespawnDist;

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && distSquaredToNearestPlayer > (double)randomDespawnDistSquared
                        && this.isTypeNotPersistent(distSquaredToNearestPlayer)) {
                    this.die();
                } else if (distSquaredToNearestPlayer < (double) randomDespawnDistSquared) {
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

    @Override
    public void tick() {
        super.tick();

        ListenerLightningStrike.storm = true; /* thors can't summon other thors */

        if (!this.world.isClientSide && this.isAlive() && !this.isNoAI()) { /* doesn't convert to drowned */
            this.drownedConversionTime = Integer.MAX_VALUE;
        }

        if (this.ticksLived == 5) {
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "weather thunder"); /* thor causes thunderstorm */
        }
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
            this.tornado = true;
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
                Location bukkitLoc2 = CustomMathHelper.coordsFromHypotenuseAndAngle(this.bukkitWorld, this.blockPosition, 20.0, this.bukkitWorld.getHighestBlockYAt(new Location(this.bukkitWorld, this.blockPosition.getX(), this.blockPosition.getY(), this.blockPosition.getZ())), 361.0);
                if (random.nextDouble() < 0.25) {
                    this.bukkitWorld.strikeLightning(bukkitLoc2);
                } else {
                    this.bukkitWorld.strikeLightningEffect(bukkitLoc2);
                }
            }

            if (random.nextDouble() < 0.003125 && !this.storm) {
                new RunnableThorLightningEffectStorm(this, random.nextInt(6) + 25).runTaskTimer(StaticPlugin.plugin, 0L, 2L);
            }

            if (random.nextDouble() < 0.002 && !this.storm && !this.tornado) {
                this.tornado = true;
                new RunnableTornado(this.thor.getWorld(), this.blockPosition, 30.0, 120).runTaskTimer(StaticPlugin.plugin, 0L, 1L);
            }
        }
    }
}
