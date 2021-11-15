package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableMobShootArrowsNormally;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

public class CustomEntityPiglin extends EntityPiglin implements ICommonCustomMethods {

    public int attacks, veryAngryTicks;
    private boolean a10, a20, a35, a50, a75;
    private final NewPathfinderGoalBuffMobs buffPiglins = new NewPathfinderGoalBuffMobs(this, CustomEntityPiglin.class, this.buildBuffsHashmapPiglin(), 40, 20, Integer.MAX_VALUE, 0);
    private final NewPathfinderGoalBuffMobs buffMobs = new NewPathfinderGoalBuffMobs(this, EntityInsentient.class, this.buildBuffsHashmapInsentient(), 40, 50, Integer.MAX_VALUE, 0);
    private Field goalTarget;

    public CustomEntityPiglin(World world) {
        super(EntityTypes.PIGLIN, world);
        this.a(PathType.LAVA, 0.0F); /**no longer avoids lava*/
        this.a(PathType.DAMAGE_FIRE, 0.0F); /**no longer avoids fire*/
        double rand = random.nextDouble();
        this.setSlot(EnumItemSlot.MAINHAND, rand < 0.45 ? new ItemStack(Items.CROSSBOW) : rand < 0.9 ? new ItemStack(Items.GOLDEN_SWORD) : rand < 0.96 ? new ItemStack(Items.NETHERITE_HOE) : new ItemStack(Items.NETHERITE_SWORD)); /**piglins have a 45% chance to be armed with a crossbow or a sword each, a 6% chance to have a netherite hoe, and a 4% chance to have a netherite sword*/
        Arrays.fill(this.dropChanceHand, 0.0f); /**piglins can't drop the items they are holding*/
        this.attacks = 0;
        this.a10 = false;
        this.a20 = false;
        this.a50 = false;
        this.a35 = false;
        this.a75 = false;
        this.veryAngryTicks = 0;

        try {
            this.goalTarget = EntityInsentient.class.getDeclaredField("goalTarget");
            this.goalTarget.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(0, new CustomEntityPiglin.PathfinderGoalPiglinResetMemory(this)); /**custom goal that removes fear of zombie piglins etc.*/
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    protected HashMap<Integer, ArrayList<MobEffect>> buildBuffsHashmapPiglin() { /**buffs: after 20 attacks, all piglins within 40 block sphere get absorption 1, regen 2 and +5 attacks. After 35 attacks, all piglins within 40 block sphere get absorption 3, regen 3 and +5 attacks.*/
        HashMap<Integer, ArrayList<MobEffect>> buffs = new HashMap<>();

        ArrayList<MobEffect> attacks20 = new ArrayList<>();
        ArrayList<MobEffect> attacks35 = new ArrayList<>();

        attacks20.add(new MobEffect(MobEffects.ABSORBTION, Integer.MAX_VALUE, 0));
        attacks20.add(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
        attacks20.add(new MobEffect(MobEffects.UNLUCK, Integer.MAX_VALUE, 253));
        attacks35.add(new MobEffect(MobEffects.ABSORBTION, Integer.MAX_VALUE, 2));
        attacks35.add(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));

        buffs.put(20, attacks20);
        buffs.put(35, attacks35);

        return buffs;
    }

    protected HashMap<Integer, ArrayList<MobEffect>> buildBuffsHashmapInsentient() { /**buffs: after 50 attacks, all mobs within 40 block sphere get +10 attacks, and gold helmets, chestplates, and leggings, and gold swords if they doesn't have anything in their main hand*/
        HashMap<Integer, ArrayList<MobEffect>> buffs = new HashMap<>();
        ArrayList<MobEffect> attacks50 = new ArrayList<>();
        attacks50.add(new MobEffect(MobEffects.UNLUCK, Integer.MAX_VALUE, 254));
        buffs.put(50, attacks50);
        return buffs;
    }

    @Override
    public void a(EntityLiving entityliving, float f) { //shoot
        this.attacks++;
        this.setHealth((float) (this.getHealth() + 0.75)); /**piglins heal by 0.75 every time its attacks increase by 1*/

        if (this.attacks == 1) { /**first attack always shoots knockback arrows*/
            new RunnableMobShootArrowsNormally(this, entityliving, f, 15, 6, 25.0, random.nextDouble() < 0.2 ? 1 : 0, false, false);
        } else {
            int rand = random.nextInt(4);

            if (rand == 0) { /**shoots 15 arrows at a time with increased inaccuracy to seem like a cone; 25% of arrows shot are piercing 1*/
                new RunnableMobShootArrowsNormally(this, entityliving, f, 15, 1, 25.0, random.nextDouble() < 0.25 ? 1 : 0, false, false);
            } else {
                new RunnableMobShootArrowsNormally(this, entityliving, f, 15, 3 + rand, 25.0, random.nextDouble() < 0.25 ? 1 : 0, false, false); /**25% chance to shoot normal arrows, 25% chance to shoot arrows that each with a 1.25% chance to spawn a piglin; 25% chance to shoot arrows that give bad status effects, and 25% chance to shoot extreme knockback arrows*/
            }
        }
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (damagesource.getEntity() instanceof EntityPlayer && this.getHealth() - f > 0.0) {  /**piglins have a 10% chance to summon a baby piglin when it is hit by a player and not killed*/
            if (random.nextDouble() < 0.1) {
                CustomEntityPiglin newPiglin = new CustomEntityPiglin(this.getWorld());
                newPiglin.a(true);
                new SpawnLivingEntity(this.getWorld(), newPiglin, 1, null, null, this, false, true);
            }
        }

        return super.damageEntity(damagesource, f);
    }

    @Override
    public void setGoalTarget(EntityLiving entityLiving) {
        try {
            this.goalTarget.set(this, entityLiving);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public EntityLiving getGoalTarget() { //uses normal EntityInsentient getGoalTarget() method that doesn't use the piglin's memory modules because they were removed along with its brain and behavior goals
        try {
            if ((this.goalTarget.get(this)) != null) {
                return (EntityLiving) this.goalTarget.get(this);
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

        if (soundeffect == SoundEffects.ENTITY_PIGLIN_ANGRY && this.attacks >= 35) { /**after 35 attacks, piglins get absorption +1 when playing the angry sound*/
            if (this.hasEffect(MobEffects.ABSORBTION)) {
                this.addEffect(new MobEffect(MobEffects.ABSORBTION, Integer.MAX_VALUE, this.getEffect(MobEffects.ABSORBTION).getAmplifier() + 1));
            } else {
                this.addEffect(new MobEffect(MobEffects.ABSORBTION, Integer.MAX_VALUE, 0));
            }
        }
    }

    @Override
    public void die() {
        super.die();

        if (this.attacks >= 20) {
            if (this.attacks >= 35) { /**after 35 attacks, piglins spawn a zombie piglin when killed*/
                new SpawnLivingEntity(this.getWorld(), new CustomEntityZombiePig(this.getWorld()), 1, null, null, this, false, true);
            } else if (random.nextDouble() < 0.5) { /**after 20 attacks, piglins have a 20% chance to spawn a zombie piglin when killed*/
                new SpawnLivingEntity(this.getWorld(), new CustomEntityZombiePig(this.getWorld()), 1, null, null, this, false, true);
            }
        }
    }

    public double getFollowRange() { /**piglins have 32 block detection range (setting attribute doesn't work)*/
        return 32.0;
    }

    @Override
    public void tick() {
        super.tick();

        this.bA = 0; /**piglins never turn into zombie piglins*/

        if (this.veryAngryTicks > 0) {
            this.veryAngryTicks--;
        }

        if (this.attacks == 10 && !this.a10) { /**after 10 attacks, piglins get speed 2 and 60 max health*/
            this.a10 = true;
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 1));
            ((LivingEntity)(this.getBukkitEntity())).setMaxHealth(60.0);
        }

        if (this.attacks == 20 && !this.a20) {
            this.a20 = true;
            this.buffPiglins.e(); /**buffs are immediately applied the first time*/
        }

        if (this.attacks == 35 && !this.a35) { /**after 35 attacks, piglins get 80 max health*/
            this.a35 = true;
            ((LivingEntity)(this.getBukkitEntity())).setMaxHealth(80.0);
            this.buffPiglins.e(); /**buffs are immediately applied the first time*/
        }

        if (this.attacks == 50 && !this.a50) { /**after 50 attacks, piglins get gold helmets, chestplates and leggings if they didn't already have something equipped there*/
            this.a50 = true;
            this.buffMobs.e(); /**buffs are immediately applied the first time*/

            LivingEntity livingEntity = ((LivingEntity)this.getBukkitEntity());
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

        if (this.attacks == 75 && !this.a75) { /**after 75 attacks, piglins duplicate all piglins excluding itself within 30 blocks*/
            this.a75 = true;
            List<Entity> piglins = this.getWorld().getEntities(this, this.getBoundingBox().g(30.0), entity -> entity instanceof CustomEntityPiglin);

            for (Entity entity : piglins) {
                new SpawnLivingEntity(this.getWorld(), new CustomEntityPiglin(this.getWorld()), 1, null, null, entity, false, true);
            }
        }

        if (this.ticksLived == 10) { /**piglins have speed 1 and 40 max health*/
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 0));
            ((LivingEntity)(this.getBukkitEntity())).setMaxHealth(40.0);

            if (this.getItemInMainHand().getItem() == Items.CROSSBOW) { /**piglins continue attacking while trading*/
                this.goalSelector.a(1, new CustomPathfinderGoalCrossbowAttack<>(this, 1.0, 32.0F)); /**uses the custom goal that attacks even when line of sight is broken; since the behavior-controlled crossbow shots have not been removed, this can cause a faster, more irregular attacking rhythm (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal)*/
                this.goalSelector.a(0, new CustomEntityPiglin.PathfinderGoalPiglinArrowAttack(this, 1.0, 10, 40.0F)); /**for frenzied phase; uses the custom goal that attacks even when line of sight is broken*/
            } else {
                this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack(this, 1.0, true)); /**uses the custom melee attack goal that attacks even when line of sight is broken*/
                this.goalSelector.a(0, new CustomEntityPiglin.PathfinderGoalPiglinFasterMelee(this, 1.0, true)); /**for frenzied phase; uses the custom melee attack goal that attacks even when line of sight is broken*/
                this.goalSelector.a(0, new CustomEntityPiglin.PathfinderGoalPiglinExplode(this)); /**for frenzied phase; custom goal that allows sword piglins to explode instantly when close enough to player*/
            }

            this.goalSelector.a(0, this.buffPiglins);
            this.goalSelector.a(0, this.buffMobs);
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

                int k = this.getEntityType().e().g() + 8; /**random despawn distance increased to 40 blocks*/
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
            if (this.brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).isPresent()) { //constantly resets these specific memory modules to empty as these tasks are not supposed to be performed
                if (!(this.brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).get() instanceof EntityPlayer)) { /**piglins no longer retaliate against non-player mobs*/
                    this.brain.setMemory(MemoryModuleType.HURT_BY_ENTITY, Optional.empty());
                }
            }

            if (this.brain.getMemory(MemoryModuleType.ATTACK_TARGET).isPresent()) { /**piglins no longer attack wither skeletons etc.*/
                if (!(this.brain.getMemory(MemoryModuleType.ATTACK_TARGET).get() instanceof EntityPlayer)) {
                    this.brain.setMemory(MemoryModuleType.ATTACK_TARGET, Optional.empty());
                }
            }

            if (this.brain.getMemory(MemoryModuleType.ANGRY_AT).isPresent() && this.piglin.getGoalTarget() != null) { /**piglins no longer attack wither skeletons etc.*/
                if (this.brain.getMemory(MemoryModuleType.ANGRY_AT).get() != this.piglin.getGoalTarget().getUniqueID()) {
                    this.brain.setMemory(MemoryModuleType.ANGRY_AT, Optional.empty());
                }
            }

            this.brain.setMemory(MemoryModuleType.AVOID_TARGET, Optional.empty()); /**piglins no longer run from zombie piglins, soul fire etc., and no longer hunt hoglins*/
            this.brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMSIS, Optional.empty());
            this.brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, Optional.empty());
            this.brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, Optional.empty());
            this.brain.setMemory(MemoryModuleType.NEAREST_REPELLENT, Optional.empty());
        }
    }

    static class PathfinderGoalPiglinExplode extends PathfinderGoal { /**piglins explode within 2 blocks of player and die if baby*/

        private final CustomEntityPiglin piglin;
        private int cooldown;

        public PathfinderGoalPiglinExplode(CustomEntityPiglin piglin) {
            this.piglin = piglin;
            this.cooldown = 0;
        }

        @Override
        public boolean a() {
            if (--this.cooldown <= 0 && this.piglin.getGoalTarget() instanceof EntityPlayer) {
                if (!((EntityPlayer)this.piglin.getGoalTarget()).abilities.isInvulnerable && this.piglin.veryAngryTicks > 0) {
                    return this.piglin.getNormalDistanceSq(this.piglin.getPositionVector(), this.piglin.getGoalTarget().getPositionVector()) <= 4.0;
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
            this.cooldown = 400; /**can only explode once every 20 seconds*/
            this.piglin.getWorld().createExplosion(this.piglin, this.piglin.locX(), this.piglin.locY(), this.piglin.locZ(), 2.0F, false, Explosion.Effect.DESTROY);

            if (this.piglin.isBaby()) {
                this.piglin.die();
            }
        }
    }

    static class PathfinderGoalPiglinFasterMelee extends CustomPathfinderGoalMeleeAttack { /**piglins attack about 3 times faster*/

        private final CustomEntityPiglin piglin;

        public PathfinderGoalPiglinFasterMelee(CustomEntityPiglin piglin, double speedTowardsTarget, boolean useLongMemory) {
            super(piglin, speedTowardsTarget, useLongMemory);
            this.piglin = piglin;
        }

        @Override
        public boolean a() {
            long i = this.a.world.getTime();

            if (i - this.k < 8L) { //attacks about 2.5 times faster
                return false;
            } else {
                this.k = i;
                EntityLiving entityliving = this.piglin.getGoalTarget();

                if (entityliving == null) {
                    return false;
                } else if (!entityliving.isAlive()) {
                    return false;
                } else if (this.piglin.veryAngryTicks > 0) {
                    this.d = this.piglin.getNavigation().a((Entity) entityliving, 0);
                    return this.d != null || this.a(entityliving) >= this.piglin.g(entityliving.locX(), entityliving.locY(), entityliving.locZ());
                }
            }

            return false;
        }

        @Override
        public boolean b() {
            return this.a();
        }
    }

    static class PathfinderGoalPiglinArrowAttack extends CustomPathfinderGoalArrowAttack { /**piglins shoot twice a second*/

        private final CustomEntityPiglin piglin;

        public PathfinderGoalPiglinArrowAttack(CustomEntityPiglin piglin, double speedTowardsTarget, int delayTimer, float maxDistance) {
            super(piglin, speedTowardsTarget, delayTimer, maxDistance);
            this.piglin = piglin;
        }

        @Override
        public boolean a() {
            if (this.piglin.getGoalTarget() instanceof EntityPlayer) {
                if (!(((EntityPlayer)this.piglin.getGoalTarget()).abilities.isInvulnerable) && this.piglin.veryAngryTicks > 0) {
                    this.attackTarget = this.piglin.getGoalTarget();
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean b() {
            return this.a();
        }
    }
}
