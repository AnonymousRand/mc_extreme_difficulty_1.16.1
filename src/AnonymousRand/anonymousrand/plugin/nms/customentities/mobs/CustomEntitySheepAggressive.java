package AnonymousRand.anonymousrand.plugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.attack.CustomPathfinderGoalAttackMelee;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.attackmvmt.CustomPathfinderGoalAttackMvmtMelee;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.plugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R1.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.Map;

public class CustomEntitySheepAggressive extends EntitySheep implements ICustomHostile, IAttackLevelingMob {

    private static final boolean IGNORE_LOS = false;
    private static final boolean IGNORE_Y = false;

    private int attacks;
    private boolean a20, a40, a65, die, launchHigh;
    private static Field attributeMap;

    public CustomEntitySheepAggressive(World world) {
        super(EntityTypes.SHEEP, world);
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
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

    /* Registers new attributes via reflection */
    static {
        try {
            attributeMap = net.minecraft.server.v1_16_R1.AttributeMapBase.class.getDeclaredField("b");
            attributeMap.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void registerGenericAttribute(org.bukkit.entity.Entity entity, Attribute attribute) throws IllegalAccessException {
        AttributeMapBase attributeMapBase = ((CraftLivingEntity) entity).getHandle().getAttributeMap();
        Map<AttributeBase, AttributeModifiable> map = (Map<AttributeBase, AttributeModifiable>) attributeMap.get(attributeMapBase);
        AttributeBase attributeBase = CraftAttributeMap.toMinecraft(attribute);
        AttributeModifiable attributeModifiable = new AttributeModifiable(attributeBase, AttributeModifiable::getAttribute);
        map.put(attributeBase, attributeModifiable);
    }

    @Override
    protected void initPathfinder() { /* sheep can't panic/breed/follow parent/be tempted with seeds/eat grass if they are attack (higher goal priority) */
        super.initPathfinder();
        this.goalSelector.a(0, new CustomPathfinderGoalBreakBlocksAround(this, 20, 2, 0, 2, 1, true)); /* Breaks most blocks around the mob periodically */
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(1, new CustomPathfinderGoalAttackMelee<>(this));
        this.goalSelector.a(1, new CustomPathfinderGoalAttackMvmtMelee<>(this));
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the nearest option */
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

    public double getDetectionRange() { /* aggressive sheep have 64 block detection range (128 after 60 attacks) */
        return this.attacks < 20 ? 64.0 : 128.0;
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

                /* Random despawn distance increased to 64 blocks */
                int randomDespawnDist = this.getEntityType().e().g() + 32;
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

//    public int[] getAttacksThreshs() {
//        return this.attackLevelingController.getAttacksThreshs();
//    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 20 && !this.a20) { /* after 20 attacks, aggressive sheep gain speed 1 */
            this.a65 = true;
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 0));
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
                this.world.createExplosion(this, this.locX(), this.locY(), this.locZ(), 2.0F, false, Explosion.Effect.DESTROY);

                if (this.attacks >= 65) { /* after 65 attacks, aggressive sheep summon an evoker when killed */
                    new SpawnEntity(this.world, new CustomEntityEvoker(this.world), 1, null, null, this, false, true);
                }
            }
        }
    }
}
