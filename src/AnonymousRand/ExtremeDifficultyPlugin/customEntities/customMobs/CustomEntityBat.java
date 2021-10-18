package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalMeleeAttack;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalPassiveMeleeAttack;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalPassiveMoveTowardsTarget;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Map;

public class CustomEntityBat extends EntityBat {

    public CustomEntityBat(World world) { /**bats are now aggressive*/
        super(EntityTypes.BAT, world);
        this.setAsleep(true);
        this.goalSelector.a(1, new CustomPathfinderGoalPassiveMeleeAttack(this, 1.0, false)); /**uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal); this custom goal also allows the spider to continue attacking regardless of light level*/
        //this.goalSelector.a(2, new CustomPathfinderGoalPassiveMoveTowardsTarget(this, 1.0, 16.0f)); /**uses the custom goal that makes this mob actually fly towards the player*/
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement); this custom goal also allows the spider to continue attacking regardless of light level*/

        try { //register attack attributes
            registerGenericAttribute(this.getBukkitEntity(), Attribute.GENERIC_ATTACK_DAMAGE);
            registerGenericAttribute(this.getBukkitEntity(), Attribute.GENERIC_ATTACK_KNOCKBACK);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
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
        net.minecraft.server.v1_16_R1.AttributeMapBase attributeMapBase = ((org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity)entity).getHandle().getAttributeMap();
        Map<AttributeBase, AttributeModifiable> map = (Map<net.minecraft.server.v1_16_R1.AttributeBase, net.minecraft.server.v1_16_R1.AttributeModifiable>)attributeMap.get(attributeMapBase);
        net.minecraft.server.v1_16_R1.AttributeBase attributeBase = org.bukkit.craftbukkit.v1_16_R1.attribute.CraftAttributeMap.toMinecraft(attribute);
        net.minecraft.server.v1_16_R1.AttributeModifiable attributeModifiable = new net.minecraft.server.v1_16_R1.AttributeModifiable(attributeBase, net.minecraft.server.v1_16_R1.AttributeModifiable::getAttribute);
        map.put(attributeBase, attributeModifiable);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived == 10) { /**bats do 1 damage, have 16 blocks of follow range and do even more knockback than a vanilla ravager*/
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(1.0);
            this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(2.0);
            this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(16);
        }

        Location thisLoc = new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ());
        if (thisLoc.getBlock().getType() == org.bukkit.Material.COBWEB) { /**non-player mobs gain Speed 11 while in a cobweb (approx original speed)*/
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, 2, 10));
        }
    }

    protected Field c1;
    protected PathfinderTargetCondition c;
    protected BlockPosition d;
    private EntityBat bat = new EntityBat(EntityTypes.BAT, this.getWorld());

    @Override
    protected void mobTick() {

        try { //reflection to get EntityBat.c
            c1 = EntityBat.class.getDeclaredField("c");
            c1.setAccessible(true);
            try {
                c = (PathfinderTargetCondition)c1.get(bat);
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        BlockPosition blockposition = this.getChunkCoordinates();
        BlockPosition blockposition1 = blockposition.up();

        if (this.isAsleep()) {
            boolean flag = this.isSilent();

            if (this.world.getType(blockposition1).isOccluding(this.world, blockposition)) {
                if (this.random.nextInt(200) == 0) {
                    this.aJ = (float) this.random.nextInt(360);
                }

                if (this.world.a(c, (EntityLiving) this) != null) {
                    this.setAsleep(false);
                    if (!flag) {
                        this.world.a((EntityHuman) null, 1025, blockposition, 0);
                    }
                }
            } else {
                this.setAsleep(false);
                if (!flag) {
                    this.world.a((EntityHuman) null, 1025, blockposition, 0);
                }
            }
        } else {
            if (this.d != null && (!this.world.isEmpty(this.d) || this.d.getY() < 1)) {
                this.d = null;
            }

            if (this.ticksLived % 3 == 0) { //updates path every 3 ticks
                this.d = null;
            }

            if (this.d == null && this.getGoalTarget() != null) { /**always flies towards goal target if possible; pathfinder goals and navigator doesn't work because bats' movement doesn't follow them, only this method*/
                this.d = new BlockPosition(this.getGoalTarget().locX(), this.getGoalTarget().locY(), this.getGoalTarget().locZ());
            } else if (d == null) {
                this.d = new BlockPosition(this.locX() + (double)this.random.nextInt(7) - (double)this.random.nextInt(7), this.locY() + (double)this.random.nextInt(6) - 2.0D, this.locZ() + (double)this.random.nextInt(7) - (double)this.random.nextInt(7));
            }

            double d0 = (double)this.d.getX() + 0.5D - this.locX();
            double d1 = (double)this.d.getY() + 0.1D - this.locY();
            double d2 = (double)this.d.getZ() + 0.5D - this.locZ();
            Vec3D vec3d = this.getMot();
            Vec3D vec3d1 = vec3d.add((Math.signum(d0) * 0.5D - vec3d.x) * 0.10000000149011612D, (Math.signum(d1) * 0.699999988079071D - vec3d.y) * 0.10000000149011612D, (Math.signum(d2) * 0.5D - vec3d.z) * 0.10000000149011612D);

            this.setMot(vec3d1);
            float f = (float) (MathHelper.d(vec3d1.z, vec3d1.x) * 57.2957763671875D) - 90.0F;
            float f1 = MathHelper.g(f - this.yaw);

            this.ba = 0.5F;
            this.yaw += f1;
        }
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable(damagesource)) {
            return false;
        } else {
            if (!this.world.isClientSide && this.isAsleep()) {
                this.setAsleep(false);
            }

            if (damagesource instanceof EntityDamageSource && this.getHealth() - f > 0.0) { /**summons 25 bats when hit and not killed*/
                EntityBat newBat;

                for (int i = 0; i < 25; i++) {
                    newBat = new EntityBat(EntityTypes.BAT, this.getWorld());
                    newBat.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
                    this.getWorld().addEntity(newBat, CreatureSpawnEvent.SpawnReason.SPAWNER);
                }
            }

            return super.damageEntity(damagesource, f);
        }
    }
}
