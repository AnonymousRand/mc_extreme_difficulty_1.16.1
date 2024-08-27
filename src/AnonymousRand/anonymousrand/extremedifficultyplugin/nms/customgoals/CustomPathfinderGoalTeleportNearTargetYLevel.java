package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.CustomEntityCreeper;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CustomMathHelper;
import net.minecraft.server.v1_16_R1.*;

import java.util.Random;

public class CustomPathfinderGoalTeleportNearTargetYLevel extends PathfinderGoal {

    public EntityInsentient goalOwner;
    private final double yLevelDifferenceToActivate, initialRange, chancePerTick;
    private static final Random random = new Random();

    public CustomPathfinderGoalTeleportNearTargetYLevel(EntityInsentient goalOwner, double yLevelDifferenceToActivate, double initialRange, double chancePerTick) {
        this.goalOwner = goalOwner;
        this.yLevelDifferenceToActivate = yLevelDifferenceToActivate;
        this.initialRange = initialRange;
        this.chancePerTick = chancePerTick;
    }

    @Override
    public boolean a() {
        if (this.goalOwner.getGoalTarget() instanceof EntityPlayer) {
            if (Math.abs(this.goalOwner.getGoalTarget().locY() - this.goalOwner.locY()) > this.yLevelDifferenceToActivate && random.nextDouble() < this.chancePerTick) { /* every tick the creeper is more than 2.5 blocks of elevation different from its target, it has a 0.15% chance to teleport near or onto the target onto a block that is within 3 y-levels of the player */
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean b() {
        return this.a();
    }

    @Override
    public void e() {
        this.initiateTeleport(-1.0);
    }

    // todo customizeable teleport behavior instead of hardcoded if creeper/if endermite etc checks
    protected void initiateTeleport(double h) {
        if (h == -1.0) {
            h = this.initialRange;
        }

        EntityPlayer player;
        player = (EntityPlayer) this.goalOwner.getGoalTarget();

        BlockPosition pos = CustomMathHelper.coordsFromHypotAndAngle(new BlockPosition(player.locX(), player.locY(), player.locZ()), h, this.goalOwner.locY(), 361.0); // gets coords for a random angle (0-360) with fixed hypotenuse to teleport to (so possible teleport area is a washer-like disc around the player)
        BlockPosition pos2 = this.goalOwner.getWorld().getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, pos); // highest block at those coords

        if (pos2 != null) {
            if (Math.abs(this.goalOwner.getGoalTarget().locY() - pos2.getY()) < (this.goalOwner instanceof CustomEntityCreeper ? (((CustomEntityCreeper)this.goalOwner).isPowered() ? 5.0 : 3.0) : 1.0)) { // if the teleport allows the mob to be within a certain y-level of player
                if (pos2.getY() < 128.0) {
                    if (this.goalOwner instanceof CustomEntityCreeper) {
                        CustomEntityCreeper creeper = ((CustomEntityCreeper)this.goalOwner);
                        creeper.setMaxFuseTicks((((CustomEntityCreeper) this.goalOwner).isPowered() ? 30 : 25)); // increase fuse length by 67% (0% for charged creepers) if teleporting very close to player
                        creeper.setFuseTicks(0);
                    }

                    this.teleportTo(pos2);
                    return;
                }
            }
        }

        // else use recursive method that checks smaller areas each time
        if (h > 6.0) {
            this.initiateTeleport(h - 2.0);
        } else if (h > 1.0){
            this.initiateTeleport(h - 1.0);
        } else { // teleport onto player if that's the only available block
            if (this.goalOwner instanceof CustomEntityCreeper) {
                CustomEntityCreeper creeper = ((CustomEntityCreeper)this.goalOwner);
                creeper.setMaxFuseTicks(((CustomEntityCreeper) this.goalOwner).isPowered() ? 30 : 25); // increase fuse length by 67% (0% for charged creepers) if teleporting very close to player
                creeper.setFuseTicks(0);
            }

            this.teleportTo(new BlockPosition(player.locX(), player.locY(), player.locZ()));
        }
    }

    protected void teleportTo(BlockPosition pos) {
        BlockPosition.MutableBlockPosition blockPos_mutableblockPos = new BlockPosition.MutableBlockPosition(pos.getX(), pos.getY(), pos.getZ());

        while (blockPos_mutableblockPos.getY() > 0 && !this.goalOwner.getWorld().getType(blockPos_mutableblockPos).getMaterial().isSolid()) {
            blockPos_mutableblockPos.c(EnumDirection.DOWN);
        }

        IBlockData iblockdata = this.goalOwner.getWorld().getType(blockPos_mutableblockPos);

        if (iblockdata.getMaterial().isSolid()) {
            if (this.teleportHelper(pos.getX(), pos.getY(), pos.getZ(), true) && !this.goalOwner.isSilent()) {
                this.goalOwner.getWorld().playSound(null, this.goalOwner.lastX, this.goalOwner.lastY, this.goalOwner.lastZ, SoundEffects.ENTITY_ENDERMAN_TELEPORT, this.goalOwner.getSoundCategory(), 1.0F, 1.0F);
                this.goalOwner.playSound(SoundEffects.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
        }
    }

    protected boolean teleportHelper(double d0, double d1, double d2, boolean flag) { // called a() in original living entity/whatever class
        double d3 = this.goalOwner.locX();
        double d4 = this.goalOwner.locY();
        double d5 = this.goalOwner.locZ();
        double d6 = d1;
        boolean flag1 = false;
        BlockPosition blockPos = new BlockPosition(d0, d1, d2);
        World world = this.goalOwner.getWorld();

        if (world.isLoaded(blockPos)) {
            boolean flag2 = false;

            while (!flag2 && blockPos.getY() > 0) {
                BlockPosition blockPos1 = blockPos.down();
                IBlockData iblockdata = world.getType(blockPos1);

                if (iblockdata.getMaterial().isSolid()) {
                    flag2 = true;
                } else {
                    --d6;
                    blockPos = blockPos1;
                }
            }

            if (flag2) {
                this.goalOwner.enderTeleportTo(d0, d6, d2);
                if (world.getCubes(this.goalOwner)) { /* can teleport onto fluids */
                    flag1 = true;
                }
            }
        }

        if (!flag1) {
            this.goalOwner.enderTeleportTo(d3, d4, d5);
            return false;
        } else {
            if (flag) {
                world.broadcastEntityEffect(this.goalOwner, (byte) 46);
            }

            if (this.goalOwner instanceof EntityCreature) {
                this.goalOwner.getNavigation().o();
            }

            return true;
        }
    }
}
