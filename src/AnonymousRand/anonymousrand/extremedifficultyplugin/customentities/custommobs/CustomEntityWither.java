package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableBreakBlocks;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class CustomEntityWither extends EntityWither implements ICommonCustomMethods {

    private static Field bB, bC;

    public CustomEntityWither(World world) {
        super(EntityTypes.WITHER, world);
        //this.setInvul(100); /**withers only take 5 seconds to explode when spawned*/ todo uncomment
        double health = 350.0 + 50.0 * this.getWorld().getServer().getOnlinePlayers().size(); /**withers have 50 more health per player online, and 350 starting health*/
        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(health);
        this.setHealth((float)health);
    }

    static {
        try {
            bB = EntityWither.class.getDeclaredField("bB");
            bB.setAccessible(true);
            bC = EntityWither.class.getDeclaredField("bC");
            bC.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initPathfinder() {
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(0, new CustomEntityWither.PathfinderGoalWitherDoNothing());
        this.goalSelector.a(2, new CustomPathfinderGoalArrowAttack(this, 1.0D, 1000000, 80.0F)); /**main head shoots a skull every 5 ticks and uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal)*/
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new CustomPathfinderGoalHurtByTarget(this, new Class[0])); /**custom goal that prevents mobs from retaliating against other mobs in case the mob damage event doesn't register and cancel the damage*/
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**only attacks players; uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public void a(EntityLiving entityliving, float f) { //shoot
        this.shootSkullToEntity(0, entityliving);
    }

    private void shootSkullToEntity(int i, EntityLiving entityliving) {
        this.shootSkullToCoords(i, entityliving.locX(), entityliving.locY() + (double)entityliving.getHeadHeight() * 0.5D, entityliving.locZ(), i == 0 && this.random.nextFloat() < (this.getWorld().getServer().getOnlinePlayers().size() < 4 ? 0.015 : 0.02)); /**withers have a 1.5% chance to shoot a blue skull (2% chance if more than 3 players online)*/
    }

    private void shootSkullToCoords(int i, double d0, double d1, double d2, boolean flag) {
        if (!this.isSilent()) {
            this.world.a((EntityHuman)null, 1024, this.getChunkCoordinates(), 0);
        }

        double d3 = this.getHeadX(i);
        double d4 = this.getHeadY(i);
        double d5 = this.getHeadZ(i);
        double d6 = d0 - d3;
        double d7 = d1 - d4;
        double d8 = d2 - d5;
        EntityWitherSkull entitywitherskull = new EntityWitherSkull(this.world, this, d6 * 1.5, d7 * 1.5, d8 * 1.5); /**wither skulls move 50% faster*/

        entitywitherskull.setShooter(this);
        if (flag) {
            entitywitherskull.setCharged(true);
        }

        entitywitherskull.setPositionRaw(d3, d4, d5);
        this.world.addEntity(entitywitherskull);
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (super.damageEntity(damagesource, f)) { /**wither now breaks all usual blocks including bedrock when damaged*/
            new RunnableBreakBlocksWither(this, 1, 2, 1, 2, true).run();
            return true;
        }

        return false;
    }

    @Override
    protected void mobTick() {
        int i;

        if (this.getGoalTarget() instanceof EntityPlayer) {
            this.setHeadTarget(0, this.getGoalTarget().getId());
        } else {
            this.setHeadTarget(0, 0);
        }

        if (this.getInvul() > 0) {
            i = this.getInvul() - 1;

            if (i <= 0) {
                this.world.createExplosion(this, this.locX(), this.getHeadY(), this.locZ(), 12.0F, true, Explosion.Effect.DESTROY); /**withers explode with power 12 instead of 7 when spawned and sets blocks on fire*/

                if (!this.isSilent()) {
                    this.world.b(1023, this.getChunkCoordinates(), 0);
                }
            }

            this.setInvul(i);
        } else {
            for (i = 0; i < 1; ++i) {
                if (this.getGoalTarget() instanceof EntityPlayer) {
                    this.setHeadTarget(i, this.getGoalTarget().getId());

                    if (this.ticksLived % 5 == 0) { /**side heads also shoot a skull every 5 ticks*/
                        this.setHeadTarget(i, 0);
                        this.shootSkullToEntity(i, (EntityLiving)this.getGoalTarget());
                    }
                } else {
                    this.setHeadTarget(i, 0);
                }
            }

            if (this.ticksLived % 20 == 0) { /**withers heal 2 hearts per second instead of 1*/
                this.heal(2.0F);
            }

            this.bossBattle.setProgress(this.getHealth() / this.getMaxHealth());
        }
    }

    public double getFollowRange() { /**withers have 80 block detection range (setting attribute doesn't work)*/
        return 80.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived % (1200 / Math.max(Math.floor(Math.log10(this.getWorld().getServer().getOnlinePlayers().size()) / Math.log10(2.0)), 1)) == 0) { /**every 60 / floor(log2(numofplayers)) seconds, withers summon a wither skeleton*/
            new SpawnLivingEntity(this.getWorld(), new CustomEntitySkeletonWither(this.getWorld()), 1, null, null, this, false, true);
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

    private double getHeadX(int i) { //util methods
        if (i <= 0) {
            return this.locX();
        } else {
            float f = (this.aH + (float) (180 * (i - 1))) * 0.017453292F;
            float f1 = MathHelper.cos(f);

            return this.locX() + (double) f1 * 1.3D;
        }
    }

    private double getHeadY(int i) {
        return i <= 0 ? this.locY() + 3.0D : this.locY() + 2.2D;
    }

    private double getHeadZ(int i) {
        if (i <= 0) {
            return this.locZ();
        } else {
            float f = (this.aH + (float) (180 * (i - 1))) * 0.017453292F;
            float f1 = MathHelper.sin(f);

            return this.locZ() + (double) f1 * 1.3D;
        }
    }

    class PathfinderGoalWitherDoNothing extends PathfinderGoal {

        public PathfinderGoalWitherDoNothing() {
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.JUMP, PathfinderGoal.Type.LOOK));
        }

        @Override
        public boolean a() {
            return CustomEntityWither.this.getInvul() > 0;
        }
    }

    static class RunnableBreakBlocksWither extends RunnableBreakBlocks {
        public RunnableBreakBlocksWither(Entity entity, int radX, int radY, int radZ, int yOffset, boolean removeFluids) {
            super(entity, radX, radY, radZ, yOffset, removeFluids);
            blockBreakable = (type) -> { /**wither can now break bedrock*/
                return type != org.bukkit.Material.END_GATEWAY && type != org.bukkit.Material.END_PORTAL && type != org.bukkit.Material.END_PORTAL_FRAME && type != org.bukkit.Material.COMMAND_BLOCK  && type != org.bukkit.Material.COMMAND_BLOCK_MINECART && type != org.bukkit.Material.STRUCTURE_BLOCK && type != org.bukkit.Material.JIGSAW && type != org.bukkit.Material.BARRIER && type != org.bukkit.Material.SPAWNER && type != org.bukkit.Material.FIRE && type != org.bukkit.Material.WITHER_ROSE;
            };
        }
    }
}
