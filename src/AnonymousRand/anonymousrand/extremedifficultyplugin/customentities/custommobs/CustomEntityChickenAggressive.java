package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R1.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;

import java.lang.reflect.Field;
import java.util.Map;

public class CustomEntityChickenAggressive extends EntityChicken implements ICustomHostile { // can't extend CustomEntityChicken as CustomEntityChicken has a function call in its tick() that spawns new aggressive chickens which would cause an infinite loop if we inherited from it

    private Field attributeMap;

    public CustomEntityChickenAggressive(World world) {
        super(EntityTypes.CHICKEN, world);
        this.initCustom();
    }

    ////////////////////////////  ICustomHostile  ////////////////////////////
    public void initCustom() {
        /** No longer avoids lava */
        this.a(PathType.LAVA, 0.0F);
        /** No longer avoids fire */
        this.a(PathType.DAMAGE_FIRE, 0.0F);

        this.initAttributes();
    }

    private void initAttributes() {
        // pull attributeMap using reflection; code from Spigot forums
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
        /** Aggressive chickens do 2 damage and move twice as fast */
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(2.0);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.5);
    }

    // from Spigot forums again
    private void registerGenericAttribute(org.bukkit.entity.Entity entity, Attribute attribute) throws IllegalAccessException {
        AttributeMapBase attributeMapBase = ((CraftLivingEntity)entity).getHandle().getAttributeMap();
        Map<AttributeBase, AttributeModifiable> map = (Map<AttributeBase, AttributeModifiable>)this.attributeMap.get(attributeMapBase);
        AttributeBase attributeBase = CraftAttributeMap.toMinecraft(attribute);
        AttributeModifiable attributeModifiable = new AttributeModifiable(attributeBase, AttributeModifiable::getAttribute);
        map.put(attributeBase, attributeModifiable);
    }

    public double getFollowRange() { /** aggressive chickens have 16 block detection range (setting attribute doesn't work) */
        return 16.0;
    }

    /////////////////////  Overridden vanilla functions  /////////////////////
    @Override
    protected void initPathfinder() { /** chicken can't panic/breed/follow parent/be tempted with seeds */
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /** custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /** custom goal that allows this mob to take certain buffs from bats etc. */
        this.goalSelector.a(1, new NewPathfinderGoalPassiveMeleeAttack(this, 1.0D)); /** uses the custom goal that attacks regardless of the y level (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal) */
        this.goalSelector.a(2, new NewPathfinderGoalPassiveMoveTowardsTarget(this, (float)this.getFollowRange())); /** uses the custom goal that makes this mob move towards the player */
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 6.0F));
        this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, CustomEntitySilverfish.class)); /** uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, CustomEntityEndermite.class)); /** attack priority: silverfish, endermite, other monsters, players, chickens, aggressive chickens, exploding aggressive chickens */
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityMonster.class));
        this.targetSelector.a(3, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class));
        this.targetSelector.a(4, new CustomPathfinderGoalNearestAttackableTarget<>(this, CustomEntityChicken.class));
        this.targetSelector.a(5, new CustomPathfinderGoalNearestAttackableTarget<>(this, CustomEntityChickenAggressive.class));
        this.targetSelector.a(6, new CustomPathfinderGoalNearestAttackableTarget<>(this, CustomEntityChickenAggressiveExploding.class));
        this.targetSelector.a(7, new CustomPathfinderGoalHurtByTarget(this, new Class[0])); /** custom goal that prevents mobs from retaliating against other mobs in case the mob damage event doesn't register and cancel the damage */
    }

    @Override
    public void tick() {
        super.tick();

        /** Aggressive chickens die after 30 seconds */
        if (this.ticksLived == 600) {
            this.die();
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
}
