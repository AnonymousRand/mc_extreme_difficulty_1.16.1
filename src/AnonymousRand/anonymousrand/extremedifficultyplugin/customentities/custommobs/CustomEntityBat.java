package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.AttackLevelingController;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
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

    private AttackLevelingController attackLevelingController;
    private NewPathfinderGoalBuffMobs buffMobs;
    private boolean firstDuplicate;

    private static final CustomPathfinderTargetCondition customPathfinderTargetCondition = (new CustomPathfinderTargetCondition()).a(4.0D).b();
    private BlockPosition targetPosition;
    private Field attributeMap;

    public CustomEntityBat(World world) {
        super(EntityTypes.BAT, world);
        /* Bats are now aggressive */
        this.initCustom();
        this.initAttackLevelingMob();
    }

    private void initCustom() {
        this.initAttributes();

        /* No longer avoids lava and fire (as if bats did in the first place) */
        this.a(PathType.LAVA, 0.0F);
        this.a(PathType.DAMAGE_FIRE, 0.0F);

        this.firstDuplicate = true;
        // custom goal that provides the buffing mechanism
        this.buffMobs = new NewPathfinderGoalBuffMobs(this, EntityInsentient.class, this.buildBuffsHashmap(),
                32, 3, 200, 101);
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

    // from Spigot forums again (https://www.spigotmc.org/threads/custom-entities-and-pathfindergoals-simplified-1-16-x.469053/)
    private void registerGenericAttribute(org.bukkit.entity.Entity bukkitEntity, Attribute attribute) throws IllegalAccessException {
        AttributeMapBase attributeMapBase = ((CraftLivingEntity) bukkitEntity).getHandle().getAttributeMap();
        Map<AttributeBase, AttributeModifiable> map = (Map<AttributeBase, AttributeModifiable>) this.attributeMap.get(attributeMapBase);
        AttributeBase attributeBase = CraftAttributeMap.toMinecraft(attribute);
        AttributeModifiable attributeModifiable = new AttributeModifiable(attributeBase, AttributeModifiable::getAttribute);
        map.put(attributeBase, attributeModifiable);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                      ICustomHostile                                       //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public double getFollowRange() {
        /* Bats have 16 block detection range (setting attribute doesn't work) (24 after 7 attacks, 32 after 12 attacks) */
        // null check since getFollowRange() is called in CustomPathfinderGoalTarget
        // which is called in CustomPathfinderGoalNearestAttackableTarget
        // which is called in initPathfinder() which is called in super constructor I think
        // which is called obviously before initAttackLevelingMob()/before this.attackController can be initialized in any other way
        return (this.attackLevelingController == null || this.attackLevelingController.getAttacks() < this.attackLevelingController.getAttacksThresholds()[1])
                ? 16.0 : this.attackLevelingController.getAttacks() < this.attackLevelingController.getAttacksThresholds()[2] ? 24 : 32;
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

                /* Random despawn distance increased to 40 blocks */
                int randomDespawnDist = this.getEntityType().e().g() + 8;
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

        if (random.nextDouble() < 0.1) { // todo what happens if this is removed
            return distX * distX + Math.pow(this.locY() - y, 2) + distZ * distZ;
        } else {
            return distX * distX + distZ * distZ;
        }
    }

    @Override
    public double d(Vec3D vec3d) {
        double distX = this.locX() - vec3d.x;
        double distZ = this.locZ() - vec3d.z;

        if (random.nextDouble() < 0.1) {
            return distX * distX + Math.pow(this.locY() - vec3d.y, 2) + distZ * distZ;
        } else {
            return distX * distX + distZ * distZ;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                    IAttackLevelingMob                                     //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void initAttackLevelingMob() {
        this.attackLevelingController = new AttackLevelingController(3, 7, 12, 24, 32);
    }

    public int getAttacks() {
        return this.attackLevelingController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        for (int metThreshold : this.attackLevelingController.increaseAttacks(increase)) {
            int[] attackThresholds = this.attackLevelingController.getAttacksThresholds();
            if (metThreshold == attackThresholds[0] || metThreshold == attackThresholds[3]) {
                /* After 3 attacks, all mobs within 32 block sphere get speed 1, strength 1, and regen 2 for 4 minutes */
                /* After 24 attacks, all mobs within 64 block sphere shoot an arrow every 14 ticks and spawn a silverfish every 12 seconds */
                buffMobs.e(); // immediately apply buffs
            } else if (metThreshold == attackThresholds[1]) {
                /* After 7 attacks, bats gain regen 2, speed 1, and 12 max health and health */
                this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 0));
                ((LivingEntity)this.getBukkitEntity()).setMaxHealth(12.0);
                this.setHealth(12.0F);
            } else if (metThreshold == attackThresholds[2]) {
                /* After 12 attacks, bats gain speed 2 and 15 max health and health */
                /* After 12 attacks, all mobs within 64 block sphere get strength 2 and shoot an arrow every 20 ticks */
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 1));
                ((LivingEntity)this.getBukkitEntity()).setMaxHealth(15.0);
                this.setHealth(15.0F);

                this.goalSelector.a(this.buffMobs); // remove goal and replace
                this.buffMobs = new NewPathfinderGoalBuffMobs(this, EntityLiving.class, this.buildBuffsHashmap(),
                        64, 20, 200, 101);
                this.goalSelector.a(0, this.buffMobs);
                this.buffMobs.e();
            } else if (metThreshold == attackThresholds[4]) {
                /* After 32 attacks, bats can duplicate again when hit by player and not killed */
                /* After 32 attacks, all mobs within 64 block sphere get regen 3 for 4 minutes and shoot an arrow every 8 ticks */
                this.firstDuplicate = true;
                this.buffMobs.e();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                  Other custom functions                                   //
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
        attacks_2.add(new MobEffect(MobEffects.INCREASE_DAMAGE, 4800, 1));
        attacks_2.add(new MobEffect(MobEffects.REGENERATION, 4800, 2));
        attacks_3.add(new MobEffect(MobEffects.HUNGER, Integer.MAX_VALUE, 253));
        attacks_4.add(new MobEffect(MobEffects.HUNGER, Integer.MAX_VALUE, 254));

        buffs.put(3, attacks_1);
        buffs.put(12, attacks_2);
        buffs.put(24, attacks_3);
        buffs.put(32, attacks_4);

        return buffs;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                               Overridden vanilla functions                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initPathfinder() {
        //this.goalSelector.a(0, this.buffMobs); // todo should this be uncommented?
        /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this));
        /* Doesn't need line of sight to continue attacking, and occasionally ignores y-level range limitations */
        this.goalSelector.a(1, new NewPathfinderGoalPassiveMeleeAttack(this, 1.0D));
        /* Doesn't need line of sight to find targets and start attacking */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class));
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        /* Summons 8-10 vanilla bats when hit by player and not killed for the first time */
        if (damagesource.getEntity() instanceof EntityPlayer && this.getHealth() - f > 0.0 && this.firstDuplicate) {
            this.firstDuplicate = false;
            new SpawnEntity(this.getWorld(), new EntityBat(EntityTypes.BAT, this.getWorld()), random.nextInt(3) + 8,
                    CreatureSpawnEvent.SpawnReason.DROWNED, null, this, false, false);

            /* After 32 attacks, an additional aggressive bat is summoned every time bat is hit by player and not killed */
            if (this.getAttacks() >= this.attackLevelingController.getAttacksThresholds()[4]) {
                new SpawnEntity(this.getWorld(), new CustomEntityBat(this.getWorld()), 1, null,
                        null, this, false, false);
            }
        }

        return super.damageEntity(damagesource, f);
    }

    // Override pathfinding (it doesn't use a goal for some reason; yes that took me hours to figure out)
    @Override
    protected void mobTick() {
        BlockPosition blockPosition = this.getChunkCoordinates();
        BlockPosition blockPositionAbove = blockPosition.up();

        if (this.isAsleep()) {
            boolean isSilent = this.isSilent();

            if (this.getWorld().getType(blockPositionAbove).isOccluding(this.getWorld(), blockPosition)) {
                if (random.nextInt(200) == 0) {
                    this.aJ = (float) random.nextInt(360);
                }

                if (this.getWorld().a(customPathfinderTargetCondition, (EntityLiving) this) != null) {
                    this.setAsleep(false);
                    if (!isSilent) {
                        this.getWorld().a((EntityHuman) null, 1025, blockPosition, 0);
                    }
                }
            } else {
                this.setAsleep(false);
                if (!isSilent) {
                    this.getWorld().a((EntityHuman) null, 1025, blockPosition, 0);
                }
            }
        } else {
            if (this.targetPosition != null && (!this.getWorld().isEmpty(this.targetPosition) || this.targetPosition.getY() < 1)) {
                this.targetPosition = null;
            }

            if (this.ticksLived % 3 == 0) { // updates path every 3 ticks
                this.targetPosition = null;
            }

            if (this.targetPosition == null) {
                /* Always flies towards goal target if possible */
                if (this.getGoalTarget() != null) {
                    this.targetPosition = new BlockPosition(this.getGoalTarget().locX(), this.getGoalTarget().locY(), this.getGoalTarget().locZ());
                } else { // default random movement if no target
                    this.targetPosition = new BlockPosition(this.locX() + (double) random.nextInt(7) - (double) random.nextInt(7),
                            this.locY() + (double) random.nextInt(6) - 2.0D,
                            this.locZ() + (double) random.nextInt(7) - (double) random.nextInt(7));
                }
            }

            double d0 = (double)this.targetPosition.getX() + 0.5D - this.locX();
            double d1 = (double)this.targetPosition.getY() + 0.1D - this.locY();
            double d2 = (double)this.targetPosition.getZ() + 0.5D - this.locZ();
            Vec3D currentMotion = this.getMot();
            Vec3D newMotion = currentMotion.add((Math.signum(d0) * 0.5D - currentMotion.x) * 0.1D, (Math.signum(d1)
                    * 0.7D - currentMotion.y) * 0.1D, (Math.signum(d2) * 0.5D - currentMotion.z) * 0.1D);

            this.setMot(newMotion);
            float f = (float)(MathHelper.d(newMotion.z, newMotion.x) * 57.2957763671875D) - 90.0F;
            float yawIncrease = MathHelper.g(f - this.yaw);

            this.ba = 0.5F;
            this.yaw += yawIncrease;
            /* Bats don't sleep */
        }
    }
}