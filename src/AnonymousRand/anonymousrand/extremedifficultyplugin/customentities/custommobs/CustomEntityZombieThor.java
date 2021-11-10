package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CoordsFromHypotenuse;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.RemovePathfinderGoals;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableThorLightningEffectStorm;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class CustomEntityZombieThor extends EntityZombie implements ICommonCustomMethods {

    private final JavaPlugin plugin;
    public PathfinderGoalSelector targetSelectorVanilla;

    public CustomEntityZombieThor(World world, JavaPlugin plugin) {
        super(EntityTypes.ZOMBIE, world);
        this.plugin = plugin;
        this.targetSelectorVanilla = super.targetSelector;
        this.a(PathType.LAVA, 0.0F); /**no longer avoids lava*/
        this.a(PathType.DAMAGE_FIRE, 0.0F); /**no longer avoids fire*/
        this.getBukkitEntity().setCustomName("Thor");
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
        this.setSlot(EnumItemSlot.OFFHAND, new ItemStack(Items.IRON_AXE));
        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "weather rain"); /**thor causes rain*/
    }

    @Override
    public void initPathfinder() { /***no longer targets iron golems*/
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 40, 1, 1, 1, 1, true)); /**custom goal that breaks blocks around the mob periodically*/
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 1.0)); /**custom goal that spawns lightning randomly*/
        this.goalSelector.a(0, new NewPathfinderGoalTeleportTowardsPlayer(this, this.getFollowRange(), 300, 0.004)); /**custom goal that gives mob a chance every tick to teleport to within initial follow_range-2 to follow_range+13 blocks of nearest player if it has not seen a player target within follow range for 15 seconds*/
        this.goalSelector.a(2, new CustomPathfinderGoalZombieAttack(this, 1.0D, true)); /**uses the custom melee attack goal that attacks even when line of sight is broken*/
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class, false));
        this.targetSelector.a(4, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, false, false, EntityTurtle.bv));
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    public double getFollowRange() { /**thor zombies have 64 block detection range (setting attribute doesn't work)*/
        return 64.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.world.isClientSide && this.isAlive() && !this.isNoAI()) { /**doesn't convert to drowned*/
            this.drownedConversionTime = Integer.MAX_VALUE;
        }

        if (this.ticksLived == 10) { /**thor zombies move 2.5x faster and have 55 health, but doesn't summon reinforcements*/
            this.setBaby(false);
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.575);
            this.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).setValue(0.0);
            this.addEffect(new MobEffect(MobEffects.WEAKNESS, Integer.MAX_VALUE, 1)); /**this allows the zombie to only do ~2 damage at a time instead of 6*/
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(55.0);
            this.setHealth(55.0F);
            this.goalSelector.a(0, new CustomEntityZombieThor.PathfinderGoalThorSummonLightning(this.plugin, this)); /**custom goal that spawns lightning randomly within 20 blocks of thor on average every 1.25 seconds (75% chance to do no damage, 25% chance to be vanilla lightning) and also sometimes creates a vortex of harmless lightning around itself on average every 10 seconds*/
            this.goalSelector.a(2, new NewPathfinderGoalShootLargeFireballs(this.plugin, this, 80, 0, true)); /**custom goal that allows thor to shoot a power 1 ghast fireball every 4 seconds that summons vanilla lightning*/
            RemovePathfinderGoals.removePathfinderGoals(this); //remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
            new RunnableThorLightningEffectStorm(this, 20, true).runTaskTimer(this.plugin, 0L, 2L); /**thor summons a vanilla lightning storm around it when first spawned for 2 seconds*/
        }
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityhuman = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (entityhuman != null) {
                double d0 = Math.pow(entityhuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityhuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); /**mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleepingdouble d0 = entityhuman.h(this);*/
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (d0 > (double)j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 32; /**random despawn distance increased to 64 blocks*/
                int l = k * k;

                if (this.ticksFarFromPlayer > 600 && this.random.nextInt(800) == 0 && d0 > (double)l && this.isTypeNotPersistent(d0)) {
                    this.die();
                } else if (d0 < (double)l) {
                    this.ticksFarFromPlayer = 0;
                }
            }

        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    @Override
    public double g(double d0, double d1, double d2) {
        double d3 = this.locX() - d0; /**for determining distance to entities, y level does not matter, eg. mob follow range, attacking (can hit player no matter the y level)*/
        double d5 = this.locZ() - d2;

        return d3 * d3 + d5 * d5;
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /**for determining distance to entities, y level does not matter, eg. mob follow range, attacking (can hit player no matter the y level)*/
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }

    @Override
    public int bL() { //getMaxFallHeight
        if (this.getGoalTarget() == null) {
            return 3;
        } else {
            int i = (int)(this.getHealth() * 20.0); /**mobs are willing to take 20 times the fall distance (same damage) to reach and do not stop taking falls if it is at less than 33% health*/

            return i + 3;
        }
    }

    public static class PathfinderGoalThorSummonLightning extends PathfinderGoal {

        private final JavaPlugin plugin;
        public final CustomEntityZombieThor thor;
        public final org.bukkit.World bukkitWorld;
        private final Location loc;
        private Location loc2;
        public boolean storm;
        private final CoordsFromHypotenuse coordsFromHypotenuse = new CoordsFromHypotenuse();
        private final Random random = new Random();

        public PathfinderGoalThorSummonLightning(JavaPlugin plugin, CustomEntityZombieThor thor) {
            this.plugin = plugin;
            this.thor = thor;
            this.bukkitWorld = thor.getWorld().getWorld();
            this.loc = new Location(this.bukkitWorld, thor.locX(), thor.locY(), thor.locZ());
            this.storm = false;
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
            if (this.random.nextDouble() < 0.04) {
                this.loc2 = coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(this.loc.getX(), this.loc.getY(), this.loc.getZ()), 20.0, this.bukkitWorld.getHighestBlockYAt(this.loc), 361.0);
                if (this.random.nextDouble() < 0.25) {
                    this.bukkitWorld.strikeLightning(this.loc2);
                } else {
                    this.bukkitWorld.strikeLightningEffect(this.loc2);
                }
            }

            if (this.random.nextDouble() < 0.005 && !this.storm) {
                new RunnableThorLightningEffectStorm(this, this.random.nextInt(11) + 30).runTaskTimer(this.plugin, 0L, 2L);
            }
        }
    }
}
