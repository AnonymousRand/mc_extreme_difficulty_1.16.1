package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableMobShootArrows;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

public class CustomEntityPiglin extends EntityPiglin implements ICustomHostile, IAttackLevelingMob {

    private int attacks;
    public int frenzyTicks;
    private boolean a10, a20, a40, a55;
    private final NewPathfinderGoalBuffMobs buffPiglins = new NewPathfinderGoalBuffMobs(this, CustomEntityPiglin.class, this.buildBuffsHashmapPiglin(), 40, 20, Integer.MAX_VALUE, 1);
    private final NewPathfinderGoalBuffMobs buffMobs = new NewPathfinderGoalBuffMobs(this, EntityInsentient.class, this.buildBuffsHashmapInsentient(), 40, 50, Integer.MAX_VALUE, 1);
    private static Field goalTarget;

    public CustomEntityPiglin(World world) {
        super(EntityTypes.PIGLIN, world);
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        double rand = random.nextDouble();
        this.setSlot(EnumItemSlot.MAINHAND, rand < 0.45 ? new ItemStack(Items.CROSSBOW) : rand < 0.9 ? new ItemStack(Items.GOLDEN_SWORD) : rand < 0.95 ? new ItemStack(Items.NETHERITE_HOE) : new ItemStack(Items.NETHERITE_SWORD)); /* piglins have a 45% chance to be armed with a crossbow or a sword each, a 5% chance to have a netherite hoe, and a 5% chance to have a netherite sword */
        Arrays.fill(this.dropChanceHand, 0.0f); /* piglins can't drop the items they are holding */
        Arrays.fill(this.dropChanceArmor, 0.0f); /* piglins can't drop armor */
        this.attacks = 0;
        this.a10 = false;
        this.a20 = false;
        this.a55 = false;
        this.a40 = false;
        this.frenzyTicks = 0;
        this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 0)); /* piglins have speed 1, 50 max health and 30 health */
        ((LivingEntity) (this.getBukkitEntity())).setMaxHealth(50.0);
        this.setHealth(30.0F);

        if (this.getItemInMainHand().getItem() == Items.CROSSBOW) { /* piglins continue attacking while trading */
            /* Crossbow piglins shoot once every 1.5 seconds, twice as fast when frenzied */
            this.goalSelector.a(0, new CustomEntityPiglin.PathfinderGoalPiglinRangedCrossbowAttack<>(this, 1.0, 30, 15, 40.0F)); /* uses the custom goal that attacks regardless of the y-level */
        } else {
            this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack(this, 1.0)); /* uses the custom melee attack goal that attacks regardless of the y-level */
            this.goalSelector.a(0, new CustomEntityPiglin.PathfinderGoalPiglinFasterMelee(this, 1.0)); /* for frenzied phase; uses the custom melee attack goal that attacks regardless of the y-level */
            this.goalSelector.a(0, new CustomEntityPiglin.PathfinderGoalPiglinExplode(this)); /* for frenzied phase; custom goal that allows sword piglins to explode instantly when close enough to player */
        }

        this.goalSelector.a(0, this.buffPiglins);
        this.goalSelector.a(0, this.buffMobs);
    }

    public CustomEntityPiglin(World world, boolean alwaysNetheriteSword) {
        this(world);

        if (alwaysNetheriteSword) {
            this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
        }
    }

    static {
        try {
            goalTarget = EntityInsentient.class.getDeclaredField("goalTarget");
            goalTarget.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new CustomEntityPiglin.PathfinderGoalPiglinResetMemory(this)); /* custom goal that removes fear of zombie piglins etc. */
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Doesn't take into account y-level, line of sight, or invis/skulls to initially find a target and maintain it as the target */
    }

    protected HashMap<Integer, ArrayList<MobEffect>> buildBuffsHashmapPiglin() { /* buffs: after 20 attacks, all piglins within 40 block sphere get absorption 1, regen 2 and +5 attacks. After 40 attacks, all piglins within 40 block sphere get absorption 3, regen 3 and +5 attacks. */
        HashMap<Integer, ArrayList<MobEffect>> buffs = new HashMap<>();

        ArrayList<MobEffect> attacks20 = new ArrayList<>();
        ArrayList<MobEffect> attacks40 = new ArrayList<>();

        attacks20.add(new MobEffect(MobEffects.ABSORBTION, Integer.MAX_VALUE, 0));
        attacks20.add(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
        attacks20.add(new MobEffect(MobEffects.UNLUCK, Integer.MAX_VALUE, 253));
        attacks40.add(new MobEffect(MobEffects.ABSORBTION, Integer.MAX_VALUE, 2));
        attacks40.add(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));

        buffs.put(20, attacks20);
        buffs.put(40, attacks40);

        return buffs;
    }

    protected HashMap<Integer, ArrayList<MobEffect>> buildBuffsHashmapInsentient() { /* buffs: after 55 attacks, all mobs within 40 block sphere get +10 attacks, and gold helmets, chestplates, and leggings, and gold swords if they don't have anything in their main hand */
        HashMap<Integer, ArrayList<MobEffect>> buffs = new HashMap<>();
        ArrayList<MobEffect> attacks55 = new ArrayList<>();
        attacks55.add(new MobEffect(MobEffects.UNLUCK, Integer.MAX_VALUE, 254));
        buffs.put(55, attacks55);
        return buffs;
    }

    @Override
    public void a(EntityLiving entityLiving, float f) { // shoot()
        this.attacks++;
        this.setHealth((float) (this.getHealth() + 0.75)); /* piglins heal by 0.75 every time its attacks increase by 1 */

        if (this.attacks == 1) { /* first attack always shoots knockback arrows */
            new RunnableMobShootArrows(this, entityLiving, 15, 6, 25.0, random.nextDouble() < 0.2 ? 1 : 0, false, false).run();
        } else {
            int rand = random.nextInt(4);

            if (rand < 2) { /* shoots 15 arrows at a time with increased inaccuracy to seem like a cone; 25% of arrows shot are piercing 1 */
                new RunnableMobShootArrows(this, entityLiving, 15, 1, 25.0, random.nextDouble() < 0.25 ? 1 : 0, false, false).run(); /* 50% chance to shoot normal arrows, 25% chance to shoot arrows that give bad status effects, and 25% chance to shoot extreme knockback arrows */
            } else {
                new RunnableMobShootArrows(this, entityLiving, 15, 2 + rand, 25.0, random.nextDouble() < 0.25 ? 1 : 0, false, false).run();
            }
        }
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damageAmount) {
        boolean tookDamage = super.damageEntity(damageSource, damageAmount);
        if (tookDamage && damageSource.getEntity() instanceof EntityPlayer && this.isAlive() && !this.isBaby()) {  /* adult piglins have q 7.5% chance to summon a baby piglin when it is hit by a player and not killed */
            if (random.nextDouble() < 0.075) {
                CustomEntityPiglin newPiglin = new CustomEntityPiglin(this.world);
                newPiglin.a(true);
                new SpawnEntity(this.world, newPiglin, 1, null, null, this, false, true);
            }
        }

        return tookDamage;
    }

    @Override
    public void setGoalTarget(EntityLiving entityLiving) {
        try {
            goalTarget.set(this, entityLiving);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public EntityLiving getGoalTarget() { // uses normal EntityInsentient getGoalTarget() method that doesn't use the piglin's memory modules because they were removed along with its brain and behavior goals
        try {
            if ((goalTarget.get(this)) != null) {
                return (EntityLiving) goalTarget.get(this);
            } else {
                return null;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void playSound(SoundEffect soundeffect, float soundVolume, float pitch) {
        super.playSound(soundeffect, soundVolume, pitch);

        if (soundeffect == SoundEffects.ENTITY_PIGLIN_ANGRY && this.attacks >= 35) { /* after 35 attacks, piglins get +2.5 max health every time they play the angry sound */
            LivingEntity bukkitEntity = (LivingEntity) this.getBukkitEntity();
            bukkitEntity.setMaxHealth(bukkitEntity.getMaxHealth() + 2.5);
        }
    }

    @Override
    public void die() {
        super.die();

        if (this.attacks >= 20) {
            if (this.attacks >= 55) { /* after 55 attacks, piglins spawn a zombie piglin when killed */
                new SpawnEntity(this.world, new CustomEntityZombiePig(this.world), 1, null, null, this, false, true);
            } else if (random.nextDouble() < 0.5) { /* after 20 attacks, piglins have a 20% chance to spawn a zombie piglin when killed */
                new SpawnEntity(this.world, new CustomEntityZombiePig(this.world), 1, null, null, this, false, true);
            }
        }
    }

    public double getDetectionRange() { /* piglins have 32 block detection range */
        return 32.0;
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityHuman = this.world.findNearbyPlayer(this, -1.0D);

            if (entityHuman != null) {
                /* Mobs only despawn along horizontal axes, so even at y=256, mobs will spawn below you and prevent sleeping */
                double distToNearestPlayer = Math.pow(entityHuman.getPositionVector().getX() - this.getPositionVector().getX(), 2)
                        + Math.pow(entityHuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2);
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (distToNearestPlayer > (double) j && this.isTypeNotPersistent(distToNearestPlayer)) {
                    this.die();
                }

                /* Random despawn distance increased to 40 blocks */
                int k = this.getEntityType().e().g() + 8;
                int l = k * k;

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && distToNearestPlayer > (double) l
                        && this.isTypeNotPersistent(distToNearestPlayer)) {
                    this.die();
                } else if (distToNearestPlayer < (double) l) {
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

        this.bA = 0; /* piglins never turn into zombie piglins */

        if (this.frenzyTicks > 0) {
            this.frenzyTicks--;
        }

        if (this.attacks == 10 && !this.a10) { /* after 10 attacks, piglins get speed 2 and 60 max health */
            this.a10 = true;
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 1));
            ((LivingEntity) (this.getBukkitEntity())).setMaxHealth(60.0);
        }

        if (this.attacks == 20 && !this.a20) {
            this.a20 = true;
            this.buffPiglins.e(); /* buffs are immediately applied the first time */
        }

        if (this.attacks == 40 && !this.a40) { /* after 40 attacks, piglins get 75 max health */
            this.a40 = true;
            ((LivingEntity) (this.getBukkitEntity())).setMaxHealth(75.0);
            this.buffPiglins.e(); /* buffs are immediately applied the first time */
        }

        if (this.attacks == 55 && !this.a55) { /* after 55 attacks, piglins get gold helmets, chestplates and leggings if they didn't already have something equipped there */
            this.a55 = true;
            this.buffMobs.e(); /* buffs are immediately applied the first time */

            LivingEntity livingEntity = ((LivingEntity) this.getBukkitEntity());
            if (livingEntity.getEquipment().getHelmet().getType() == org.bukkit.Material.AIR) {
                livingEntity.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLDEN_HELMET));
            }

            if (livingEntity.getEquipment().getChestplate().getType() == org.bukkit.Material.AIR) {
                livingEntity.getEquipment().setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLDEN_CHESTPLATE));
            }

            if (livingEntity.getEquipment().getLeggings().getType() == org.bukkit.Material.AIR) {
                livingEntity.getEquipment().setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLDEN_LEGGINGS));
            }
        }
    }

    static class PathfinderGoalPiglinResetMemory extends PathfinderGoal {

        private final CustomEntityPiglin piglin;
        private final BehaviorController<EntityPiglin> brain;

        public PathfinderGoalPiglinResetMemory(CustomEntityPiglin piglin) {
            this.piglin = piglin;
            this.brain = piglin.getBehaviorController();
        }

        @Override
        public boolean a() {
            return true;
        }

        @Override
        public boolean b() {
            return this.a();
        }

        @Override
        public void e() {
            if (this.brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).isPresent()) { // constantly resets these specific memory modules to empty as these tasks are not supposed to be performed
                if (!(this.brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).get() instanceof EntityPlayer)) { /* piglins no longer retaliate against non-player mobs */
                    this.brain.setMemory(MemoryModuleType.HURT_BY_ENTITY, Optional.empty());
                }
            }

            if (this.brain.getMemory(MemoryModuleType.ATTACK_TARGET).isPresent()) { /* piglins no longer attack wither skeletons etc. */
                if (!(this.brain.getMemory(MemoryModuleType.ATTACK_TARGET).get() instanceof EntityPlayer)) {
                    this.brain.setMemory(MemoryModuleType.ATTACK_TARGET, Optional.empty());
                }
            }

            if (this.brain.getMemory(MemoryModuleType.ANGRY_AT).isPresent() && this.piglin.getGoalTarget() != null) { /* piglins no longer attack wither skeletons etc. */
                if (this.brain.getMemory(MemoryModuleType.ANGRY_AT).get() != this.piglin.getGoalTarget().getUniqueID()) {
                    this.brain.setMemory(MemoryModuleType.ANGRY_AT, Optional.empty());
                }
            }

            this.brain.setMemory(MemoryModuleType.AVOID_TARGET, Optional.empty()); /* piglins no longer run from zombie piglins, soul fire etc., and no longer hunt hoglins */
            this.brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMSIS, Optional.empty());
            this.brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, Optional.empty());
            this.brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, Optional.empty());
            this.brain.setMemory(MemoryModuleType.NEAREST_REPELLENT, Optional.empty());
        }
    }

    static class PathfinderGoalPiglinExplode extends PathfinderGoal { /* piglins explode within 2 blocks of player and die if baby */

        private final CustomEntityPiglin piglin;
        private int cooldown;

        public PathfinderGoalPiglinExplode(CustomEntityPiglin piglin) {
            this.piglin = piglin;
            this.cooldown = 0;
        }

        @Override
        public boolean a() {
            if (this.piglin.frenzyTicks > 0 && --this.cooldown <= 0 && this.piglin.getGoalTarget() instanceof EntityPlayer) {
                if (!((EntityPlayer) this.piglin.getGoalTarget()).abilities.isInvulnerable ) {
                    return this.piglin.getDistSq(this.piglin.getPositionVector(), this.piglin.getGoalTarget().getPositionVector()) <= 4.0;
                }
            }

            return false;
        }

        @Override
        public boolean b() {
            return this.a();
        }

        @Override
        public void c() {
            this.cooldown = 400; /* can only explode once every 20 seconds */
            this.piglin.getWorld().createExplosion(this.piglin, this.piglin.locX(), this.piglin.locY(), this.piglin.locZ(), 2.0F, false, Explosion.Effect.DESTROY);

            if (this.piglin.isBaby()) {
                this.piglin.die();
            }
        }
    }

    static class PathfinderGoalPiglinFasterMelee extends CustomPathfinderGoalMeleeAttack { /* piglins attack 2 times faster when frenzied */

        private final CustomEntityPiglin piglin;

        public PathfinderGoalPiglinFasterMelee(CustomEntityPiglin piglin, double speedTowardsTarget) {
            super(piglin, speedTowardsTarget);
            this.piglin = piglin;
        }

        @Override
        public boolean a() {
            long i = this.a.getWorld().getTime();

            if (i - this.k < 5L) { // attacks 2 times faster
                return false;
            } else if (this.piglin.frenzyTicks > 0) {
                this.k = i;
                EntityLiving entityLiving = this.piglin.getGoalTarget();

                if (entityLiving == null || !entityLiving.isAlive()) {
                    return false;
                }

                this.d = this.piglin.getNavigation().a(entityLiving, 0);
                return this.d != null || this.a(entityLiving) >= this.piglin.g(entityLiving.locX(), entityLiving.locY(), entityLiving.locZ());
            }

            return false;
        }

        @Override
        public boolean b() {
            return this.a();
        }
    }

    static class PathfinderGoalPiglinRangedCrossbowAttack<T extends CustomEntityPiglin> extends CustomPathfinderGoalRangedCrossbowAttack<T> {

        private final int attackIntervalNormal;
        private final int attackIntervalFrenzied;

        public PathfinderGoalPiglinRangedCrossbowAttack(T piglin, double speedTowardsTarget, int attackIntervalNormal, int attackIntervalFrenzied, float maxDistance) {
            super(piglin, speedTowardsTarget, attackIntervalNormal, maxDistance);
            this.attackIntervalNormal = attackIntervalNormal;
            this.attackIntervalFrenzied = attackIntervalFrenzied;
        }

        @Override
        public void e() {
            // control frenzied vs. non-frenzied attack speeds with hopefully optimal short-circuit evals
            if (this.attackInterval == this.attackIntervalFrenzied && this.entity.frenzyTicks <= 0) {
                this.attackInterval = this.attackIntervalNormal;
            } else if (this.entity.frenzyTicks > 0 && this.attackInterval == this.attackIntervalNormal) {
                this.attackInterval = this.attackIntervalFrenzied;
            }

            super.e();
        }
    }
}
