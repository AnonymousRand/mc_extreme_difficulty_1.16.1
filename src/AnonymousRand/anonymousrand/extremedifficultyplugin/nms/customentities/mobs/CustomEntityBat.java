package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.AttackLevelingController;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack.CustomPathfinderGoalMeleeAttack;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R1.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomEntityBat extends EntityBat implements ICustomHostile, IAttackLevelingMob {

    /* Ignores line of sight and y-level for initially finding a player target and maintaining it as the target,
     * as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;
    private CustomPathfinderGoalBuffMobs buffMobs;
    private BlockPosition targetPosition;
    private boolean firstDuplicate;
    private Field attributeMap;

    public CustomEntityBat(World world) {
        super(EntityTypes.BAT, world);
        /* Bats are now aggressive */
        this.initCustom();
        this.initAttackLevelingMob();
    }

    private void initCustom() {
        this.initAttributes();

        /* No longer avoids fire and lava (as if bats did in the first place) */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);

        this.firstDuplicate = true;
        // custom goal that provides the buffing mechanism
        this.buffMobs = new CustomPathfinderGoalBuffMobs(this, EntityInsentient.class, this.buildBuffsHashmap(),
                32, 4, 200, 101);
    }

    private void initAttributes() {
        // pull attributeMap using reflection
        // code from Spigot forums (https://www.spigotmc.org/threads/custom-entities-and-pathfindergoals-simplified-1-16-x.469053/)
        try {
            this.attributeMap = AttributeMapBase.class.getDeclaredField("b");
            this.attributeMap.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        // register new attributes using attributeMap
        try {
            registerGenericAttribute(this.getBukkitEntity(), Attribute.GENERIC_ATTACK_DAMAGE);
            registerGenericAttribute(this.getBukkitEntity(), Attribute.GENERIC_ATTACK_KNOCKBACK);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // set new attributes
        /* Bats do 1 damage and have extra knockback */
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(1.0);
        this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(2.0);
    }

    /* From Spigot forums again
     * (https://www.spigotmc.org/threads/custom-entities-and-pathfindergoals-simplified-1-16-x.469053/) */
    private void registerGenericAttribute(org.bukkit.entity.Entity bukkitEntity, Attribute attribute)
            throws IllegalAccessException {
        AttributeMapBase attributeMapBase = ((CraftLivingEntity) bukkitEntity).getHandle().getAttributeMap();
        Map<AttributeBase, AttributeModifiable> map = (Map<AttributeBase, AttributeModifiable>)
                this.attributeMap.get(attributeMapBase);
        AttributeBase attributeBase = CraftAttributeMap.toMinecraft(attribute);
        AttributeModifiable attributeModifiable = new AttributeModifiable(attributeBase,
                AttributeModifiable::getAttribute);
        map.put(attributeBase, attributeModifiable);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // ICustomHostile
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public double getDetectionRange() {
        /* Bats have 16 block detection range (24 after 8 attacks, 32 after 15 attacks) */
        // null check since getDetectionRange() is called in CustomPathfinderGoalTarget
        // which is called in CustomPathfinderGoalNearestAttackableTarget
        // which is called in initPathfinder() which is called in some EntityInsentient's constructor
        // which is before this.attackController can be initialized
        return (this.attackLevelingController == null || this.getAttacks() < this.getAttacksThresholds()[1]) ? 16.0
                : this.getAttacks() < this.getAttacksThresholds()[2] ? 24 : 32;
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

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // IAttackLevelingMob
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private AttackLevelingController attackLevelingController = null;

    private void initAttackLevelingMob() {
        this.attackLevelingController = new AttackLevelingController(4, 8, 15, 28, 40);
    }

    public int getAttacks() {
        return this.attackLevelingController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        for (int metThreshold : this.attackLevelingController.increaseAttacks(increase)) {
            int[] attackThresholds = this.getAttacksThresholds();
            if (metThreshold == attackThresholds[0]
                    || metThreshold == attackThresholds[3]
                    || metThreshold == attackThresholds[4]) {
                /* After 4 attacks, all mobs within 32 block sphere get speed 1, strength 1, and regen 2 for 4 minutes */
                /* After 28 attacks, all mobs within 64 block sphere shoot an arrow every 30 ticks and spawn a silverfish every 15 seconds */
                /* After 40 attacks, all mobs within 64 block sphere get regen 3 for 4 minutes and shoot an arrow every 20 ticks */
                buffMobs.e(); // immediately apply buffs
            } else if (metThreshold == attackThresholds[1]) {
                /* After 8 attacks, bats gain regen 2, speed 1, and 12 max health and health */
                this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 0));
                ((LivingEntity) this.getBukkitEntity()).setMaxHealth(12.0);
                this.setHealth(12.0F);
            } else if (metThreshold == attackThresholds[2]) {
                /* After 15 attacks, bats gain speed 2 and 15 max health and health */
                /* After 15 attacks, all mobs within 64 block sphere shoot an arrow every 40 ticks */
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 1));
                ((LivingEntity) this.getBukkitEntity()).setMaxHealth(15.0);
                this.setHealth(15.0F);

                this.goalSelector.a(this.buffMobs); // remove goal and replace // todo if not un-jankified: why? whats the diff between these and the first threshold's buff? why no replace after this?
                this.buffMobs = new CustomPathfinderGoalBuffMobs(this, EntityInsentient.class, this.buildBuffsHashmap(),
                        64, attackThresholds[2], 200, 101);
                this.goalSelector.a(0, this.buffMobs);
                this.buffMobs.e();
            }
        }
    }

    public int[] getAttacksThresholds() {
        return this.attackLevelingController.getAttacksThresholds();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Other custom functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected HashMap<Integer, ArrayList<MobEffect>> buildBuffsHashmap() {
        HashMap<Integer, ArrayList<MobEffect>> buffs = new HashMap<>();

        ArrayList<MobEffect> attacks_1 = new ArrayList<>();
        ArrayList<MobEffect> attacks_2 = new ArrayList<>();
        ArrayList<MobEffect> attacks_3 = new ArrayList<>();
        ArrayList<MobEffect> attacks_4 = new ArrayList<>();

        attacks_1.add(new MobEffect(MobEffects.REGENERATION, 4800, 1));
        attacks_1.add(new MobEffect(MobEffects.FASTER_MOVEMENT, 4800, 0));
        attacks_1.add(new MobEffect(MobEffects.INCREASE_DAMAGE, 4800, 0));
        attacks_2.add(new MobEffect(MobEffects.HUNGER, Integer.MAX_VALUE, 252));
        attacks_2.add(new MobEffect(MobEffects.REGENERATION, 4800, 2));
        attacks_3.add(new MobEffect(MobEffects.HUNGER, Integer.MAX_VALUE, 253));
        attacks_4.add(new MobEffect(MobEffects.HUNGER, Integer.MAX_VALUE, 254));

        buffs.put(4, attacks_1);
        buffs.put(15, attacks_2);
        buffs.put(28, attacks_3);
        buffs.put(40, attacks_4);

        return buffs;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Overridden vanilla functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initPathfinder() {
        //this.goalSelector.a(0, this.buffMobs); // todo if un-janking of buffMobs means this needs to be an actual goal: uncomment
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this));                              /* Still moves fast in cobwebs */
        this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack<>(this));
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets to the nearest option */
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damageAmount) {
        boolean damageSuccess = super.damageEntity(damageSource, damageAmount);
        /* Bats summon 6-8 vanilla bats when hit by player and not killed for the first time */
        if (damageSuccess && this.isAlive() && damageSource.getEntity() instanceof EntityPlayer
                && this.firstDuplicate) {
            this.firstDuplicate = false;
            new SpawnEntity(this.world, new EntityBat(EntityTypes.BAT, this.world), random.nextInt(3) + 6,
                    CreatureSpawnEvent.SpawnReason.DROWNED, null, this, false, false);

            /* After 32 attacks, an additional aggressive bat is summoned every time bat is hit by player
               and not killed */
            if (this.getAttacks() >= this.getAttacksThresholds()[4]) {
                new SpawnEntity(this.world, new CustomEntityBat(this.world), 1, null,
                        null, this, false, false);
            }
        }

        return damageSuccess;
    }

    /* Override pathfinding (it doesn't use a goal for some reason; yes that took me hours to figure out) */
    @Override
    protected void mobTick() {
        if (this.isAsleep()) {
            /* Bats don't sleep */
            this.setAsleep(false);
            if (!this.isSilent()) {
                this.world.a((EntityHuman) null, 1025, this.getChunkCoordinates(), 0);
            }
        } else {
            if (this.targetPosition != null
                    && (!this.world.isEmpty(this.targetPosition) || this.targetPosition.getY() < 1)) {
                this.targetPosition = null;
            }

            // updates path every 3 ticks
            if (this.ticksLived % 3 == 0) {
                this.targetPosition = null;
            }

            if (this.targetPosition == null) {
                // always flies towards goal target if possible
                if (this.getGoalTarget() != null) {
                    this.targetPosition =
                            new BlockPosition(this.getGoalTarget().locX(), this.getGoalTarget().locY(),
                            this.getGoalTarget().locZ());
                } else { // default random movement if no target
                    this.targetPosition =
                            new BlockPosition(this.locX() + (double) random.nextInt(7) - (double) random.nextInt(7),
                            this.locY() + (double) random.nextInt(6) - 2.0D,
                            this.locZ() + (double) random.nextInt(7) - (double) random.nextInt(7));
                }
            }

            double d0 = (double) this.targetPosition.getX() + 0.5D - this.locX();
            double d1 = (double) this.targetPosition.getY() + 0.1D - this.locY();
            double d2 = (double) this.targetPosition.getZ() + 0.5D - this.locZ();
            Vec3D currentMotion = this.getMot();
            Vec3D newMotion =
                    currentMotion.add((Math.signum(d0) * 0.5D - currentMotion.x) * 0.1D,
                    (Math.signum(d1) * 1.0 - currentMotion.y) * 0.1D,
                    (Math.signum(d2) * 0.5D - currentMotion.z) * 0.1D);

            this.setMot(newMotion);
            float f = (float) (MathHelper.d(newMotion.z, newMotion.x) * 57.2957763671875D) - 90.0F;
            float yawIncrease = MathHelper.g(f - this.yaw);

            this.ba = 0.5F;
            this.yaw += yawIncrease;
        }
    }
}
