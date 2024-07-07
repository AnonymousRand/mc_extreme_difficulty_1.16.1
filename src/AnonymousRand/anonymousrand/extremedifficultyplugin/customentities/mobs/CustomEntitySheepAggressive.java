package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R1.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.Map;

public class CustomEntitySheepAggressive extends EntitySheep implements ICustomHostile, IAttackLevelingMob {

    private int attacks;
    private boolean a20, a40, a65, die, launchHigh;
    private static Field attributeMap;

    public CustomEntitySheepAggressive(World world) {
        super(EntityTypes.SHEEP, world);
        /* No longer avoids lava and fire */
        this.a(PathType.LAVA, 0.0F);
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.setColor(EnumColor.PINK); // always pink
        this.attacks = 0;
        this.a20 = false;
        this.a40 = false;
        this.a65 = false;
        this.die = false;
        this.launchHigh = false;

        try { // register attack attributes
            registerGenericAttribute(this.getBukkitEntity(), Attribute.GENERIC_ATTACK_DAMAGE);
            registerGenericAttribute(this.getBukkitEntity(), Attribute.GENERIC_ATTACK_KNOCKBACK);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(10.0);/* aggressive sheep move 2.1x as fast, do 10 damage, have extra knockback, have 125 health, and have regen 3 */
        this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(2.0);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.483);
        this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(125.0);
        this.setHealth(125.0F);
    }

    // registers new attributes via reflection
    static {
        try {
            attributeMap = net.minecraft.server.v1_16_R1.AttributeMapBase.class.getDeclaredField("b");
            attributeMap.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void registerGenericAttribute(org.bukkit.entity.Entity entity, Attribute attribute) throws IllegalAccessException {
        AttributeMapBase attributeMapBase = ((CraftLivingEntity)entity).getHandle().getAttributeMap();
        Map<AttributeBase, AttributeModifiable> map = (Map<AttributeBase, AttributeModifiable>) attributeMap.get(attributeMapBase);
        AttributeBase attributeBase = CraftAttributeMap.toMinecraft(attribute);
        AttributeModifiable attributeModifiable = new AttributeModifiable(attributeBase, AttributeModifiable::getAttribute);
        map.put(attributeBase, attributeModifiable);
    }

    @Override
    protected void initPathfinder() { /* sheep can't panic/breed/follow parent/be tempted with seeds/eat grass if they are attacking (higher goal priority) */
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 20, 2, 0, 2, 1, true)); /* Breaks most blocks around the mob periodically */
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(1, new NewPathfinderGoalPassiveMeleeAttack(this, 1.0D)); /* Continues attacking regardless of y-level and line of sight (the old goal stopped the mob from attacking even if it had already recognized a target via CustomNearestAttackableTarget) */
        this.goalSelector.a(1, new NewPathfinderGoalPassiveMoveTowardsTarget(this, (float) this.getFollowRange())); /* Moves towards target, menacingly */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Doesn't take into account y-level or line of sight to aggro a target */
    }

    @Override
    protected void f(EntityLiving entityLiving) { /* sheep heals when its attack is blocked by a shield */
        this.heal(this.attacks < 15 ? 15.0F : 25.0F);
    }

    public boolean getLaunchHigh() {
        return this.launchHigh;
    }

    public void setLaunchHigh(boolean launchHigh) {
        this.launchHigh = launchHigh;
    }

    public double getFollowRange() { /* aggressive sheep have 64 block detection range (128 after 60 attacks) */
        return this.attacks < 20 ? 64.0 : 128.0;
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.getWorld().findNearbyPlayer(this, -1.0D);

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

        if (this.attacks == 20 && !this.a20) { /* after 20 attacks, aggressive sheep gain speed 1 */
            this.a65 = true;
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 0));
            this.goalSelector.a(0, new NewPathfinderGoalPassiveMoveTowardsTarget(this, (float) this.getFollowRange())); // update follow range
            this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); // update follow range
        }

        if (this.attacks == 40 && !this.a40) { /* after 40 attacks, aggressive sheep gain a slight knockback boost and regen 4 */
            this.a40 = true;
            this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(2.5);
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 3));
        }

        if (this.attacks == 65 && !this.a65) { /* after 65 attacks, aggressive sheep get extra knockback */
            this.a65 = true;
            this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(3.5);
        }

        if (this.getHealth() <= 0.0 && !this.die) {
            this.die = true;

            if (this.attacks >= 20) { /* after 20 attacks, aggressive sheep create a power 2 explosion on their location when killed */
                this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 2.0F, false, Explosion.Effect.DESTROY);

                if (this.attacks >= 65) { /* after 65 attacks, aggressive sheep summon an evoker when killed */
                    new SpawnEntity(this.getWorld(), new CustomEntityEvoker(this.getWorld()), 1, null, null, this, false, true);
                }
            }
        }
    }
}
