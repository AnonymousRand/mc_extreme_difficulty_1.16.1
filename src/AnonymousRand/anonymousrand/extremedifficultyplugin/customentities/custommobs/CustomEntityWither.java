package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityWitherSkull;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableBreakBlocks;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableTornado;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.Random;

public class CustomEntityWither extends EntityWither implements ICustomMob {

    protected boolean dash;
    private static Field bB, bC;

    public CustomEntityWither(World world) {
        super(EntityTypes.WITHER, world);
        this.setInvul(100); /**withers only take 5 seconds to explode when spawned*/
        this.dash = false;
        double health = 200.0 + 70.0 * this.getWorld().getServer().getOnlinePlayers().size(); /**withers have 70 more health per player online, and 200 starting health*/
        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(health);
        this.setHealth((float)health);
        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "weather thunder"); /**wither causes thunderstorm*/
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
        this.goalSelector.a(0, new PathfinderGoalWitherDoNothingWhileInvulnerable());
        this.goalSelector.a(1, new CustomEntityWither.PathfinderGoalWitherDashAttack(this)); /**custom goal that allows the wither to do a bedrock-like dash attack (50% chance to occur every 25 seconds) that breaks blocks around it and does 6 damage to all nearby players*/
        this.goalSelector.a(2, new CustomPathfinderGoalArrowAttack(this, 1.0D, 5, 80.0F)); /**main head shoots a skull every 5 ticks and uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal)*/
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 8.0F));
        this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new CustomPathfinderGoalHurtByTarget(this, new Class[0])); /**custom goal that prevents mobs from retaliating against other mobs in case the mob damage event doesn't register and cancel the damage*/
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, false)); /**only attacks players; uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public void a(EntityLiving entityliving, float f) { //shoot for main head
        this.shootSkullToEntity(0, entityliving);
    }

    protected void shootSkullToEntity(int i, EntityLiving entityliving) {
        this.shootSkullToCoords(i, entityliving.locX(), entityliving.locY() + (double)entityliving.getHeadHeight() * 0.5D, entityliving.locZ(), false);
    }

    protected void shootSkullToEntity(int i, EntityLiving entityliving, boolean alwaysBlue) {
        this.shootSkullToCoords(i, entityliving.locX(), entityliving.locY() + (double)entityliving.getHeadHeight() * 0.5D, entityliving.locZ(), alwaysBlue);
    }

    protected void shootSkullToCoords(int i, double d0, double d1, double d2, boolean alwaysBlue) {
        if (this.dash) {
            return;
        }

        if (!this.isSilent() && random.nextDouble() < 0.05) { /**withers only play the skull shooting sound 5% of the time*/
            this.world.a((EntityHuman)null, 1024, this.getChunkCoordinates(), 0);
        }

        /**withers have higher chances to shoot blue skulls the more players are online*/
        boolean flag = this.random.nextFloat() < ((-0.3 + (Math.log10(this.getWorld().getServer().getOnlinePlayers().size() + 3.0) / (Math.log10(3.0) - 0.15))) / 150.0);
        double d3 = this.getHeadX(i);
        double d4 = this.getHeadY(i);
        double d5 = this.getHeadZ(i);
        double d6 = d0 - d3;
        double d7 = d1 - d4;
        double d8 = d2 - d5;
        CustomEntityWitherSkull entitywitherskull = new CustomEntityWitherSkull(this.world, this, d6, d7, d8);
        entitywitherskull.setShooter(this);

        if (flag || alwaysBlue) {
            entitywitherskull.setCharged(true);
        }

        entitywitherskull.setPositionRaw(d3, d4, d5);
        this.world.addEntity(entitywitherskull);
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (super.damageEntity(damagesource, f)) { /**wither now breaks all usual blocks including bedrock when damaged*/
            new RunnableWitherBreakBlocks(this, 1, 2, 1, 2, true).run();
            return true;
        }

        return false;
    }

    @Override
    public void setMot(double x, double y, double z) {
        if (this.dash && this.getGoalTarget() != null) { /**when wither is dashing, always move towards player*/
            EntityLiving target = this.getGoalTarget();
            super.setMot((target.locX() - this.locX()) / 14.0, (target.locY() - this.locY()) / 14.0, (target.locZ() - this.locZ()) / 14.0);
        } else {
            super.setMot(x, y, z);
        }
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
                new RunnableTornado(this.getWorld(), new BlockPosition(this.locX(), this.locY(), this.locZ()), 60, 100).runTaskTimer(StaticPlugin.plugin, 0L, 1L); /**withers summon a tornado after the spawn explosion*/

                if (!this.isSilent()) {
                    this.world.b(1023, this.getChunkCoordinates(), 0);
                }
            }

            this.setInvul(i);
        } else {
            for (i = 1; i < 3; ++i) { //main head: 0, right head: 1, left head: 2
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

            if (this.ticksLived % 20 == 0) {
                this.heal(1.0F);
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

        if (this.ticksLived % (1500 / Math.max(Math.floor(Math.log10(this.getWorld().getServer().getOnlinePlayers().size()) / Math.log10(2.0)), 1)) == 0) { /**every 75 / floor(log2(numofplayers)) seconds, withers summon a wither skeleton*/
            new SpawnEntity(this.getWorld(), new CustomEntitySkeletonWither(this.getWorld()), 1, null, null, this, false, true);
        }

        if (this.getGoalTarget() != null) {
            if (!this.getEntitySenses().a(this.getGoalTarget()) && random.nextDouble() < 0.05) { /**if the wither can't see its target, it shoots a blue skull on average every second*/
                this.shootSkullToEntity(0, this.getGoalTarget(), true);
            }
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

    protected double getHeadX(int i) { //util methods
        if (i <= 0) {
            return this.locX();
        } else {
            float f = (this.aH + (float) (180 * (i - 1))) * 0.017453292F;
            float f1 = MathHelper.cos(f);

            return this.locX() + (double) f1 * 1.3D;
        }
    }

    protected double getHeadY(int i) {
        return i <= 0 ? this.locY() + 3.0D : this.locY() + 2.2D;
    }

    protected double getHeadZ(int i) {
        if (i <= 0) {
            return this.locZ();
        } else {
            float f = (this.aH + (float) (180 * (i - 1))) * 0.017453292F;
            float f1 = MathHelper.sin(f);

            return this.locZ() + (double) f1 * 1.3D;
        }
    }

    class PathfinderGoalWitherDoNothingWhileInvulnerable extends PathfinderGoal {
        public PathfinderGoalWitherDoNothingWhileInvulnerable() {
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.JUMP, PathfinderGoal.Type.LOOK));
        }

        @Override
        public boolean a() {
            return CustomEntityWither.this.getInvul() > 0;
        }
    }

    static class PathfinderGoalWitherDashAttack extends PathfinderGoal {

        private final CustomEntityWither wither;
        private static final Random random = new Random();

        public PathfinderGoalWitherDashAttack(CustomEntityWither wither) {
            this.wither = wither;
        }

        @Override
        public boolean a() {
            return this.wither.getGoalTarget() != null && this.wither.ticksLived % 500 == 0 && random.nextDouble() < 0.5;
        }

        @Override
        public boolean b() {
            return this.a();
        }

        @Override
        public void e() {
            if (this.wither.getGoalTarget() == null) {
                return;
            }

            this.wither.dash = true;
            new RunnableWitherDashAttack(this.wither).runTaskTimer(StaticPlugin.plugin, 0L, 1L);
        }
    }

    static class RunnableWitherDashAttack extends BukkitRunnable {

        private final CustomEntityWither wither;

        public RunnableWitherDashAttack(CustomEntityWither wither) {
            this.wither = wither;
        }

        @Override
        public void run() {
            RunnableWitherBreakBlocks newRunnableWitherBreakBlocks =  new RunnableWitherBreakBlocks(this.wither, 4, 4, 4, 4, true);

            if (this.wither.getGoalTarget() == null) {
                this.wither.dash = false;
                this.cancel();
            }

            newRunnableWitherBreakBlocks.run();

            if (this.wither.ticksLived % 2 == 0) {
                this.wither.getWorld().getEntities(this.wither, this.wither.getBoundingBox().g(3.0), entity -> entity instanceof EntityPlayer).forEach(entity -> entity.damageEntity(DamageSource.GENERIC, 6.0F));
            }

            if (this.wither.getNormalDistanceSq(this.wither.getPositionVector(), this.wither.getGoalTarget().getPositionVector()) <= 4.0) {
                this.wither.dash = false;
                this.cancel();
            }
        }
    }

    static class RunnableWitherBreakBlocks extends RunnableBreakBlocks {
        public RunnableWitherBreakBlocks(Entity entity, int radX, int radY, int radZ, int yOffset, boolean removeFluids) {
            super(entity, radX, radY, radZ, yOffset, removeFluids);
            blockBreakable = (type) -> { /**wither can now break bedrock*/
                return type != org.bukkit.Material.END_GATEWAY && type != org.bukkit.Material.END_PORTAL && type != org.bukkit.Material.END_PORTAL_FRAME && type != org.bukkit.Material.COMMAND_BLOCK  && type != org.bukkit.Material.COMMAND_BLOCK_MINECART && type != org.bukkit.Material.STRUCTURE_BLOCK && type != org.bukkit.Material.JIGSAW && type != org.bukkit.Material.BARRIER && type != org.bukkit.Material.SPAWNER && type != org.bukkit.Material.FIRE && type != org.bukkit.Material.WITHER_ROSE && type != org.bukkit.Material.WATER && type != org.bukkit.Material.LAVA;
            };
        }

        @Override
        public void run() {
            this.cycles = 0;
            this.entity.getWorld().createExplosion(this.entity, this.entity.locX(), this.entity.locY(), this.entity.locZ(), 0.0F, false, Explosion.Effect.NONE); //constant explosion noises
            super.run();
        }
    }
}
