package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R1.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;
import java.util.Map;

public class CustomEntityChickenAggressive extends EntityChicken { //can't extend CustomEntityChicken as CustomEntityChicken has a function call in its tick() that spawns new aggressive chickens which would cause an infinite loop if we inherited from it

    public int attacks;
    private boolean a15, a30;

    public CustomEntityChickenAggressive(World world) {
        super(EntityTypes.CHICKEN, world);
        this.attacks = 0;
        this.a15 = false;
        this.a30 = false;

        try { //register attack attributes
            registerGenericAttribute(this.getBukkitEntity(), Attribute.GENERIC_ATTACK_DAMAGE);
            registerGenericAttribute(this.getBukkitEntity(), Attribute.GENERIC_ATTACK_KNOCKBACK);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initPathfinder() { /**chicken can't panic/breed/follow parent/be tempted with seeds*/
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(0, new NewPathfinderGoalCobweb(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(1, new CustomPathfinderGoalPassiveMeleeAttack(this, 1.0, false)); /**uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal); this custom goal also allows the spider to continue attacking regardless of light level*/
        this.goalSelector.a(2, new NewPathfinderGoalPassiveMoveTowardsTarget(this, 1.0, 16.0f)); /**uses the custom goal that makes this mob actually move towards the player within 16 blocks*/
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityMonster.class, false)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement); this custom goal also allows the spider to continue attacking regardless of light level*/
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**targets hostile mobs before players*/
        this.targetSelector.a(3, new CustomPathfinderGoalNearestAttackableTarget<>(this, CustomEntityChicken.class, false)); /**targets players before other non-aggressive chickens*/
        this.targetSelector.a(4, new CustomPathfinderGoalNearestAttackableTarget<>(this, CustomEntityChickenAggressive.class, false)); /**targets non-aggressive chickens before other aggressive chickens*/
        this.targetSelector.a(5, new CustomPathfinderGoalNearestAttackableTarget<>(this, CustomEntityChickenAggressiveExploding.class, false)); /**targets aggressive chickens before other exploding chickens*/
    }

    //registers new attributes via reflection; code from Spigot forums
    protected static Field attributeMap;

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
        Map<AttributeBase, AttributeModifiable> map = (Map<AttributeBase, AttributeModifiable>)attributeMap.get(attributeMapBase);
        AttributeBase attributeBase = CraftAttributeMap.toMinecraft(attribute);
        AttributeModifiable attributeModifiable = new AttributeModifiable(attributeBase, AttributeModifiable::getAttribute);
        map.put(attributeBase, attributeModifiable);
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (damagesource.getEntity() instanceof EntityPlayer && this.getHealth() - f > 0.0 && this.attacks >= 45) { /**after 45 attacks, aggressive chickens create a power 1 explosion on their location when hit and not killed*/
            this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 1.0f, false, Explosion.Effect.DESTROY);

            if (this.attacks >= 60) { /**after 60 attacks, aggressive chickens also duplicate into a custom exploding aggressive chicken when hit and not killed*/
                CustomEntityChickenAggressiveExploding newChicken = new CustomEntityChickenAggressiveExploding(this.getWorld());
                newChicken.setPosition(this.locX(), this.locY(), this.locZ());
                this.getWorld().addEntity(newChicken, CreatureSpawnEvent.SpawnReason.NATURAL);
            }
        }

        return super.damageEntity(damagesource, f);
    }

    public double getFollowRange() { /**aggressive chickens have 16 block detection range (setting attribute doesn't work)*/
        return 16.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 15 && !this.a15) { /**after 15 attacks, aggressive chicken get 5 damage and regen 1*/
            this.a15 = true;
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(5.0);
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 0));
        }

        if (this.attacks == 30 && !this.a30) { /**after 30 attacks, aggressive chickens gain regen 2 and 10 max health*/
            this.a30 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(10.0);
            this.setHealth(10.0f);
        }

        if (this.ticksLived == 10) { /**aggressive chickens move twice as fast and do 3 damage*/
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.5);
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(3.0);
        }

        if (this.ticksLived % 5 == 2) {
            if (this.getLastDamager() != null) {
                EntityLiving target = this.getLastDamager();

                if (!(target instanceof EntityPlayer)) { /**mobs only target players (in case mob damage listener doesn't register)*/
                    this.setLastDamager(null);
                }
            }
        }
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityhuman = this.world.findNearbyPlayer(this, -1.0D);

            if (entityhuman != null) {
                double d0 = Math.pow(entityhuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityhuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); /**mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleepingdouble d0 = entityhuman.h(this);*/
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (d0 > (double)j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 8; /**random despawn distance increased to 40 blocks*/
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
}
