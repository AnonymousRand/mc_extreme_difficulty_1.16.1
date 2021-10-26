package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.*;
import com.google.common.collect.Maps;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R1.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomEntitySheepAggressive extends EntitySheep {

    private JavaPlugin plugin;
    public int attacks;
    private boolean a60, a100;

    public CustomEntitySheepAggressive(World world, JavaPlugin plugin) {
        super(EntityTypes.SHEEP, world);
        this.plugin = plugin;
        this.a(PathType.LAVA, 1.0F); //no longer avoids lava
        this.attacks = 0;
        this.a60 = false;
        this.a100 = false;

        try { //register attack attributes
            registerGenericAttribute(this.getBukkitEntity(), Attribute.GENERIC_ATTACK_DAMAGE);
            registerGenericAttribute(this.getBukkitEntity(), Attribute.GENERIC_ATTACK_KNOCKBACK);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initPathfinder() { /**sheep can't panic/breed/follow parent/be tempted with seeds/eat grass if they are attacking (higher goal priority)*/
        super.initPathfinder();
        this.goalSelector.a(0, new CustomPathfinderGoalPassiveMeleeAttack(this, 1.0, false)); /**uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal); this custom goal also allows the spider to continue attacking regardless of light level*/
        this.goalSelector.a(0, new NewPathfinderGoalPassiveMoveTowardsTarget(this, 1.0, 64.0f)); /**uses the custom goal that makes this mob actually move towards the player within 16 blocks*/
        this.goalSelector.a(3, new NewPathfinderGoalBreakBlocksAround(this, 20, 2, 0, 2, 1, true)); /**custom goal that breaks blocks around the mob periodically*/
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, false)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement); this custom goal also allows the spider to continue attacking regardless of light level*/
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
    public void die() {
        super.die();

        if (this.attacks >= 30) { /**after 30 attacks, aggressive sheep create a power 2 explosion on their location when killed*/
            this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 2.0f, false, Explosion.Effect.DESTROY);

            if (this.attacks >= 100) { /**after 100 attacks, aggressive sheep also summon an evoker on death*/
                CustomEntityEvoker newEvoker = new CustomEntityEvoker(this.getWorld(), this.plugin);
                newEvoker.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
                this.getWorld().addEntity(newEvoker, CreatureSpawnEvent.SpawnReason.NATURAL);
            }
        }
    }

    private double getFollowRange() {
        return this.attacks < 60 ? 64.0 : 128.0;
    }

    @Override
    public void tick() {
        super.tick();

        //todo: leave behind 3 by 3 area effect clouds wherever it goes

        if (this.attacks == 60 && !this.a60) { /**after 60 attacks, aggressive sheep gain speed 1*/
            this.a100 = true;
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 0));
        }

        if (this.attacks == 100 && !this.a100) { /**after 100 attacks, aggressive sheep get extra knockback*/
            this.a100 = true;
            this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(3.0);
        }

        if (this.ticksLived == 10) { /**aggressive sheep move 2.1x as fast, do 8 damage, have extra knockback, have 100 health, and have regeneration 2*/
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.483);
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(8.0);
            this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(1.5);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(100.0);
            this.setHealth(100.0f);
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
        }

        if (this.ticksLived % 40 == 10) { /**aggressive sheep have 64 block detection range (setting attribute doesn't work) (128 after 60 attacks)*/
            EntityPlayer player = this.getWorld().a(EntityPlayer.class, new CustomPathfinderTargetCondition(), this, this.locX(), this.locY(), this.locZ(), this.getBoundingBox().grow(this.getFollowRange(), 128.0, this.getFollowRange())); //get closest player within bounding box
            if (player != null && !player.isInvulnerable() && this.getGoalTarget() == null) {
                this.setGoalTarget(player);
            }

            if (this.getGoalTarget() != null) {
                EntityLiving target = this.getGoalTarget();

                if (target.isInvulnerable() || this.d(target.getPositionVector()) > Math.pow(this.getFollowRange(), 2)) {
                    this.setGoalTarget(null);
                }
            }
        }

        Location thisLoc = new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ());
        if (thisLoc.getBlock().getType() == org.bukkit.Material.COBWEB) { /**non-player mobs gain Speed 11 while in a cobweb (approx original speed)*/
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, 2, 10));
        }
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityhuman = this.world.findNearbyPlayer(this, -1.0D);

            if (entityhuman != null) {
                double d0 = Math.pow(entityhuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityhuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); //mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleepingdouble d0 = entityhuman.h(this);
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
