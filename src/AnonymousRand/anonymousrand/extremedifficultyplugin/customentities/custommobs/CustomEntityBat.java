package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalCobweb;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalPassiveMeleeAttack;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalBuffMobs;
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

public class CustomEntityBat extends EntityBat {

    public int attacks;
    private boolean a5, a10, a20, a32, a45, firstDuplicate;

    private NewPathfinderGoalBuffMobs buffMobs= new NewPathfinderGoalBuffMobs(this, EntityLiving.class, this.buildBuffsHashmap(), 32, 5, 200, 101);

    public CustomEntityBat(World world) { /**bats are now aggressive*/
        super(EntityTypes.BAT, world);
        this.attacks = 0;
        this.a5 = false;
        this.a10 = false;
        this.a20 = false;
        this.a32 = false;
        this.a45 = false;
        this.firstDuplicate = true;

        try { //register attack attributes
            registerGenericAttribute(this.getBukkitEntity(), Attribute.GENERIC_ATTACK_DAMAGE);
            registerGenericAttribute(this.getBukkitEntity(), Attribute.GENERIC_ATTACK_KNOCKBACK);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initPathfinder() {
        this.goalSelector.a(0, new NewPathfinderGoalCobweb(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(1, new CustomPathfinderGoalPassiveMeleeAttack(this, 1.0, false)); /**uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal); this custom goal also allows the spider to continue attacking regardless of light level*/
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement); this custom goal also allows the spider to continue attacking regardless of light level*/
    }

    //registers new attributes via reflection; code from Spigot forums
    private static Field attributeMap;

    static {
        try {
            attributeMap = AttributeMapBase.class.getDeclaredField("b");
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

    protected HashMap<Integer, ArrayList<MobEffect>> buildBuffsHashmap() { /**buffs: after 5 attacks, all monsters within 32 block sphere get speed 1, strength 1, and regen 1 for 3 minutes. After 20 attacks, all monsters within 64 block sphere shoot an arrow every 20 ticks and potion durations are increased to 5 minutes. After 32 attacks, all monsters within 64 block sphere shoot an arrow every 14 ticks and spawn a silverfish every 12 seconds. After 45 attacks, all monsters within 64 block sphere get regen 2 for 5 minutes and shoot an arrow every 8 ticks*/
        HashMap<Integer, ArrayList<MobEffect>> buffs = new HashMap<>();

        ArrayList<MobEffect> attacks5 = new ArrayList<>();
        ArrayList<MobEffect> attacks20 = new ArrayList<>();
        ArrayList<MobEffect> attacks32 = new ArrayList<>();
        ArrayList<MobEffect> attacks45 = new ArrayList<>();

        attacks5.add(new MobEffect(MobEffects.REGENERATION, this.attacks < 20 ? 3600 : 6000, 0));
        attacks5.add(new MobEffect(MobEffects.FASTER_MOVEMENT, this.attacks < 20 ? 3600 : 6000, 0));
        attacks5.add(new MobEffect(MobEffects.INCREASE_DAMAGE, this.attacks < 20 ? 3600 : 6000, 0));
        attacks20.add(new MobEffect(MobEffects.HUNGER, Integer.MAX_VALUE, 252));
        attacks20.add(new MobEffect(MobEffects.REGENERATION, 6000, 1));
        attacks32.add(new MobEffect(MobEffects.HUNGER, Integer.MAX_VALUE, 253));
        attacks45.add(new MobEffect(MobEffects.HUNGER, Integer.MAX_VALUE, 254));

        buffs.put(5, attacks5);
        buffs.put(20, attacks20);
        buffs.put(32, attacks32);
        buffs.put(45, attacks45);

        return buffs;
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (damagesource.getEntity() instanceof EntityPlayer && this.getHealth() - f > 0.0 && this.firstDuplicate) { /**summons 15-20 bats when hit by player and not killed for the first time (also 2 aggressive bats after 45 attacks)*/
            this.firstDuplicate = false;
            new SpawnLivingEntity(this.getWorld(), new EntityBat(EntityTypes.BAT, this.getWorld()), this.random.nextInt(6) + 15, CreatureSpawnEvent.SpawnReason.DROWNED, null, this, false, false).run();

            if (this.attacks >= 45) {
                new SpawnLivingEntity(this.getWorld(), new CustomEntityBat(this.getWorld()), 2, null, null, this, false, false).run();
            }
        }

        return super.damageEntity(damagesource, f);
    }

    private Field c1;
    protected PathfinderTargetCondition c;
    protected BlockPosition d;
    private EntityBat bat = new EntityBat(EntityTypes.BAT, this.getWorld());

    @Override
    protected void mobTick() {

        try { //reflection to get EntityBat.c
            this.c1 = EntityBat.class.getDeclaredField("c");
            this.c1.setAccessible(true);
            try {
                this.c = (PathfinderTargetCondition) c1.get(bat);
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

            if (this.getWorld().getType(blockposition1).isOccluding(this.getWorld(), blockposition)) {
                if (this.random.nextInt(200) == 0) {
                    this.aJ = (float) this.random.nextInt(360);
                }

                if (this.getWorld().a(c, (EntityLiving) this) != null) {
                    this.setAsleep(false);
                    if (!flag) {
                        this.getWorld().a((EntityHuman) null, 1025, blockposition, 0);
                    }
                }
            } else {
                this.setAsleep(false);
                if (!flag) {
                    this.getWorld().a((EntityHuman) null, 1025, blockposition, 0);
                }
            }
        } else {
            if (this.d != null && (!this.getWorld().isEmpty(this.d) || this.d.getY() < 1)) {
                this.d = null;
            }

            if (this.ticksLived % 3 == 0) { //updates path every 3 ticks
                this.d = null;
            }

            if (this.d == null && this.getGoalTarget() != null) { /**always flies towards goal target if possible; pathfinder goals and navigator doesn't work because bats' movement doesn't follow them, only this method*/
                this.d = new BlockPosition(this.getGoalTarget().locX(), this.getGoalTarget().locY(), this.getGoalTarget().locZ());
            } else if (d == null) {
                this.d = new BlockPosition(this.locX() + (double) this.random.nextInt(7) - (double) this.random.nextInt(7), this.locY() + (double) this.random.nextInt(6) - 2.0D, this.locZ() + (double) this.random.nextInt(7) - (double) this.random.nextInt(7));
            }

            double d0 = (double) this.d.getX() + 0.5D - this.locX();
            double d1 = (double) this.d.getY() + 0.1D - this.locY();
            double d2 = (double) this.d.getZ() + 0.5D - this.locZ();
            Vec3D vec3d = this.getMot();
            Vec3D vec3d1 = vec3d.add((Math.signum(d0) * 0.5D - vec3d.x) * 0.10000000149011612D, (Math.signum(d1) * 0.699999988079071D - vec3d.y) * 0.10000000149011612D, (Math.signum(d2) * 0.5D - vec3d.z) * 0.10000000149011612D);

            this.setMot(vec3d1);
            float f = (float) (MathHelper.d(vec3d1.z, vec3d1.x) * 57.2957763671875D) - 90.0F;
            float f1 = MathHelper.g(f - this.yaw);

            this.ba = 0.5F;
            this.yaw += f1;
        }
    }

    public double getFollowRange() { /**bats have 16 block detection range (setting attribute doesn't work) (24 after 5 attacks, 32 after 10 attacks)*/
        return this.attacks < 5 ? 16.0 : this.attacks < 10 ? 24 : 32;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 5 && !this.a5) {
            this.a5 = true;
            buffMobs.e(); /**buffs are immediately applied the first time*/
        }

        if (this.attacks == 10 && !this.a10) { /**after 10 attacks, bats gain regen 2, speed 1, and 10 max health and health*/
            this.a10 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 0));
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(10.0);
            this.setHealth(10.0F);
        }

        if (this.attacks == 20 && !this.a20) { /**after 25 attacks, bats gain speed 2 and 12 max health and health*/
            this.a20 = true;
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 1));
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(12.0);
            this.setHealth(12.0F);

            this.goalSelector.a(this.buffMobs); //remove goal and replace
            this.buffMobs = new NewPathfinderGoalBuffMobs(this, EntityLiving.class, this.buildBuffsHashmap(), 64, 20, 200, 101);
            this.goalSelector.a(0, this.buffMobs);
            this.buffMobs.e(); /**buffs are immediately applied the first time*/
        }

        if (this.attacks == 32 && !this.a32) {
            this.a32 = true;
            this.buffMobs.e(); /**buffs are immediately applied the first time*/
        }

        if (this.attacks == 45 && !this.a45) { /**bats can duplicate again one time after 45 attacks*/
            this.a45 = true;
            this.firstDuplicate = true;
            this.buffMobs.e(); /**buffs are immediately applied the first time*/
        }

        if (this.ticksLived == 10) { /**bats do 1 damage and have extra knockback*/
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(1.0);
            this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(2.0);

            this.goalSelector.a(0, this.buffMobs); /**custom goal that provides the buffing mechanism*/
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

    @Override
    public int bL() { //getMaxFallHeight
        if (this.getGoalTarget() == null) {
            return 3;
        } else {
            int i = (int)(this.getHealth() * 20.0); /**mobs are willing to take 20 times the fall distance (same damage) to reach and do not stop taking falls if it is at less than 33% health*/

            return i + 3;
        }
    }
}
