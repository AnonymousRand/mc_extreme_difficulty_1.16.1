package AnonymousRand.anonymousrand.plugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.CustomPathfinderGoalMoveFasterInCobweb;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.CustomPathfinderGoalGetBuffedByMobs;
import net.minecraft.server.v1_16_R1.*;

import java.lang.reflect.Field;

public class CustomEntityPufferfish extends EntityPufferFish implements ICustomHostile {

    /* Ignores line of sight for initially finding a player target and maintaining it as the target,
     * as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = false;
    private int lastStingTicks;
    private static Field jumpTicks;

    public CustomEntityPufferfish(World world) {
        super(EntityTypes.PUFFERFISH, world);
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.lastStingTicks = 0;
    }

    static {
        try {
            jumpTicks = EntityLiving.class.getDeclaredField("jumpTicks");
            jumpTicks.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* this mob now seeks out players; uses the custom goal which doesn't need line of sight to start attack (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
    }

    @Override
    public void pickup(EntityHuman entityHuman) { // onCollideWithPlayer
        if (entityHuman.abilities.isInvulnerable) {
            return;
        }

        int i = this.getPuffState() + 1;

        if (!this.isSilent() && (this.ticksLived - this.lastStingTicks) > 100) {
            this.lastStingTicks = this.ticksLived; /* only plays sting sound once per 5 seconds */
            ((EntityPlayer) entityHuman).playerConnection.sendPacket(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.j, 0.0F));
        }

        entityHuman.addEffect(new MobEffect(MobEffects.WITHER, 80 * i, 2)); /* poison from direct contact changed from poison 1 to wither 3, and duration increased from 50 ticks per puff state to 80 */
    }

    public double getDetectionRange() { /* pufferfish have 32 block detection range */
        return 32.0;
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

                /* Random despawn distance increased to 40 blocks */
                int randomDespawnDist = this.getEntityType().e().g() + 8;
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
    public void tick() {
        super.tick();

        if (this.ticksLived % 3 == 0) {
            this.world.getEntities(this, this.getBoundingBox().grow(5.0, 128.0, 5.0), entity -> entity instanceof EntityPlayer).forEach(entity -> this.pickup((EntityHuman) entity)); /* pufferfish have a poison/wither range of 5 blocks horizontally */
        }
    }

    @Override
    public void movementTick() { /* uses the movementick() method from entityLiving class so pufferfish no longer damage other mobs besides players */
        int jumpTicksTemp;

        try {
            jumpTicksTemp = jumpTicks.getInt(this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        if (jumpTicksTemp > 0) {
            --jumpTicksTemp;
        }

        if (this.cr()) {
            this.bb = 0;
            this.c(this.locX(), this.locY(), this.locZ());
        }

        if (this.bb > 0) {
            double d0 = this.locX() + (this.bc - this.locX()) / (double) this.bb;
            double d1 = this.locY() + (this.bd - this.locY()) / (double) this.bb;
            double d2 = this.locZ() + (this.be - this.locZ()) / (double) this.bb;
            double d3 = MathHelper.g(this.bf - (double) this.yaw);

            this.yaw = (float) ((double) this.yaw + d3 / (double) this.bb);
            this.pitch = (float) ((double) this.pitch + (this.bg - (double) this.pitch) / (double) this.bb);
            --this.bb;
            this.setPosition(d0, d1, d2);
            this.setYawPitch(this.yaw, this.pitch);
        } else if (!this.doAITick()) {
            this.setMot(this.getMot().a(0.98));
        }

        if (this.bi > 0) {
            this.aJ = (float) ((double) this.aJ + MathHelper.g(this.bh - (double) this.aJ) / (double) this.bi);
            --this.bi;
        }

        Vec3D vec3d = this.getMot();
        double d4 = vec3d.x;
        double d5 = vec3d.y;
        double d6 = vec3d.z;

        if (Math.abs(vec3d.x) < 0.003) {
            d4 = 0.0;
        }

        if (Math.abs(vec3d.y) < 0.003) {
            d5 = 0.0;
        }

        if (Math.abs(vec3d.z) < 0.003) {
            d6 = 0.0;
        }

        this.setMot(d4, d5, d6);
        this.world.getMethodProfiler().enter("ai");
        if (this.isFrozen()) {
            this.jumping = false;
            this.aY = 0.0F;
            this.ba = 0.0F;
        } else if (this.doAITick()) {
            this.world.getMethodProfiler().enter("newAi");
            this.doTick();
            this.world.getMethodProfiler().exit();
        }

        this.world.getMethodProfiler().exit();
        this.world.getMethodProfiler().enter("jump");
        if (this.jumping && this.cS()) {
            double d7;

            if (this.aN()) {
                d7 = this.b(TagsFluid.LAVA);
            } else {
                d7 = this.b(TagsFluid.WATER);
            }

            boolean flag = this.isInWater() && d7 > 0.0;
            double d8 = this.cw();

            if (flag && (!this.onGround || d7 > d8)) {
                this.c(TagsFluid.WATER);
            } else if (this.aN() && (!this.onGround || d7 > d8)) {
                this.c(TagsFluid.LAVA);
            } else if ((this.onGround || flag && d7 <= d8) && jumpTicksTemp == 0) {
                this.jump();
                jumpTicksTemp = 10;
            }
        } else {
            jumpTicksTemp = 0;
        }

        this.world.getMethodProfiler().exit();
        this.world.getMethodProfiler().enter("travel");
        this.aY *= 0.98F;
        this.ba *= 0.98F;
        this.t();
        AxisAlignedBB axisalignedbb = this.getBoundingBox();

        this.f(new Vec3D(this.aY, this.aZ, this.ba));
        this.world.getMethodProfiler().exit();
        this.world.getMethodProfiler().enter("push");
        if (this.bm > 0) {
            --this.bm;
            this.a(axisalignedbb, this.getBoundingBox());
        }

        this.collideNearby();

        try {
            jumpTicks.setInt(this, jumpTicksTemp);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        this.world.getMethodProfiler().exit();
    }

    private void t() { // util method from entityLiving class
        boolean flag = this.getFlag(7);

        if (flag && !this.onGround && !this.isPassenger() && !this.hasEffect(MobEffects.LEVITATION)) {
            ItemStack itemstack = this.getEquipment(EnumItemSlot.CHEST);

            if (itemstack.getItem() == Items.ELYTRA && ItemElytra.d(itemstack)) {
                if (!this.world.isClientSide && (this.bl + 1) % 20 == 0) {
                    itemstack.damage(1, this, (entityLiving)-> entityLiving.broadcastItemBreak(EnumItemSlot.CHEST));
                }
            } else {
                flag = false;
            }
        } else {
            flag = false;
        }

        if (!this.world.isClientSide) {
            this.setFlag(7, flag);
        }
    }
}
