package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableMeteorRain;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;

public class CustomEntityZombie extends EntityZombie implements ICustomMob, IAttackLevelingMob {

    public PathfinderGoalSelector vanillaTargetSelector;
    private int attacks;
    private boolean a7, a15, a25, a40, a50;
    private static Field bA;

    public CustomEntityZombie(World world) {
        super(EntityTypes.ZOMBIE, world);
        this.vanillaTargetSelector = super.targetSelector;
        this.a(PathType.LAVA, 0.0F); /** no longer avoids lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F); /** no longer avoids fire */
        this.attacks = 0;
        this.a7 = false;
        this.a15 = false;
        this.a25 = false;
        this.a40 = false;
        this.a50 = false;
        this.expToDrop = 1; // to differentiate between vanilla villagers in listenermobspawnandreplace to avoid potential infinite loops
        this.setBaby(true);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.46); /** zombies are always babies, move 2x faster, and have a 20% chance to summon a reinforcement when hit by a player */
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
    protected void initPathfinder() { /** no longer targets iron golems */
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlockLookingAt(this)); /** custom goal that allows the mob to break the block it is looking at every 4 seconds as long as it has a target, it breaks the block that it is looking at up to 40 blocks away */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /** custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /** custom goal that allows this mob to take certain buffs from bats etc. */
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 1.0)); /** custom goal that spawns lightning randomly */
        this.goalSelector.a(0, new NewPathfinderGoalTeleportTowardsPlayer(this, this.getFollowRange(), 300.0, 0.0015)); /** custom goal that gives mob a chance every tick to teleport to within initial follow_range-2 to follow_range+13 blocks of nearest player if it has not seen a player target within follow range for 15 seconds */
        this.goalSelector.a(2, new CustomPathfinderGoalZombieAttack(this, 1.0D)); /** uses the custom melee attack goal that attacks regardless of the y level */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /** uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class));
        this.targetSelector.a(4, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, EntityTurtle.bv));
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (super.damageEntity(damagesource, f)) {
            EntityLiving entityLiving = this.getGoalTarget();

            if (entityLiving == null) {
                entityLiving = (EntityLiving)damagesource.getEntity();
            }

            if (!(entityLiving instanceof EntityPlayer)) { /** only player damage can trigger reinforcement spawning */
                return true;
            }

            if (this.attacks >= 30) { /** after 30 attacks, zombies summon vanilla lightning on the player when it is hit */
                this.getWorld().getWorld().strikeLightning(new Location(this.getWorld().getWorld(), entityLiving.locX(), entityLiving.locY(), entityLiving.locZ()));
            }

            if ((double)random.nextFloat() < this.b(GenericAttributes.SPAWN_REINFORCEMENTS)) { /** zombies can now spawn reinforcements on any difficulty */
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
                    EntityPositionTypes.Surface entityPositionTypes_Surface = EntityPositionTypes.a(entityTypes);

                    if (SpawnerCreature.a(entityPositionTypes_Surface, this.getWorld(), blockPosition, entityTypes) && EntityPositionTypes.a(entityTypes, this.getWorld(), EnumMobSpawn.REINFORCEMENT, blockPosition, this.getWorld().random)) {
                        newZombie.setPosition(i1, j1, k1);
                        if (!this.getWorld().isPlayerNearby(i1, j1, k1, 7.0D) && this.getWorld().i(newZombie) && this.getWorld().getCubes(newZombie) && !this.getWorld().containsLiquid(newZombie.getBoundingBox())) {
                            this.getWorld().addEntity(newZombie);
                            newZombie.prepare(this.getWorld(), this.getWorld().getDamageScaler(newZombie.getChunkCoordinates()), EnumMobSpawn.REINFORCEMENT, null, null);
                            this.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).addModifier(new AttributeModifier("Zombie reinforcement caller charge", -0.125, AttributeModifier.Operation.ADDITION)); /** zombies and their summoned reinforcement experience a 12.5% decrease in reinforcement summon chance instead of 5% if summoned reinforcements or was summoned as reinforcement */
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

    public double getFollowRange() { /** zombies have 40 block detection range (setting attribute doesn't work) */
        return 40.0;
    }

    public int getAttacks() {
        return this.attacks;
    }

    public void incrementAttacks(int increase) {
        this.attacks += increase;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.world.isClientSide && this.isAlive() && !this.isNoAI()) { /** converts to drowned 4 times faster */
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

        if (this.attacks == 7 && !this.a7) { /** after 7 attacks, zombies get 15% more reinforcement summon chance */
            this.a7 = true;
            this.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).setValue(0.35);
        }

        if (this.attacks == 15 && !this.a15) { /** after 15 attacks, zombies get regen 2, 30 max health and summon 2 vanilla zombies */
            this.a15 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(30.0);
            this.setHealth(30.0F);
            new SpawnEntity(this.getWorld(), new EntityZombie(EntityTypes.ZOMBIE, this.getWorld()), 2, CreatureSpawnEvent.SpawnReason.BEEHIVE, null, this, false, true);
        }

        if (this.attacks == 25 && !this.a25) { /** after 25 attacks, zombies get 40 max health and health and summon another baby zombie */
            this.a25 = true;
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(40.0);
            this.setHealth(40.0F);
            new SpawnEntity(this.getWorld(), new CustomEntityZombie(this.getWorld()), 1, null, null, this, false, true);
        }

        if (this.attacks == 50 && !this.a50) { /** after 50 attacks, zombies summon thor */
            this.a50 = true;
            new SpawnEntity(this.getWorld(), new CustomEntityZombieThor(this.getWorld()), 1, null, null, this, false, true);
        }

        if (this.getHealth() <= 0.0 && this.attacks >= 40 && !this.a40) { /** after 40 attacks, zombies summon a small meteor rain when it dies */
            this.a40 = true; // do this here instead of in die() so that the meteor rain doesn't have to wait until the death animation finishes playing to start

            new RunnableMeteorRain(this, 1, 40.0, 12).runTaskTimer(StaticPlugin.plugin, 0L, 2L);
            new RunnableMeteorRain(this, 2, 40.0, 8).runTaskTimer(StaticPlugin.plugin, 0L, 2L);
            new RunnableMeteorRain(this, 3, 40.0, 7).runTaskTimer(StaticPlugin.plugin, 0L, 2L);
        }
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityHuman = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (entityHuman != null) {
                double d0 = Math.pow(entityHuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityHuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); /** mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleepingdouble d0 = entityHuman.h(this); */
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (d0 > (double)j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 8; /** random despawn distance increased to 40 blocks */
                int l = k * k;

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && d0 > (double)l && this.isTypeNotPersistent(d0)) {
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
        double d3 = this.locX() - d0; /** for determining distance to entities, y level does not matter, e.g. mob follow range, attacking (can hit player no matter the y level) */
        double d5 = this.locZ() - d2;

        return d3 * d3 + d5 * d5;
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /** for determining distance to entities, y level does not matter, e.g. mob follow range, attacking (can hit player no matter the y level) */
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }

    @Override
    public int bL() {
        return Integer.MAX_VALUE; /** mobs are willing to take any fall to reach the player as they don't take fall damage */
    }
}
