package AnonymousRand.ExtremeDifficultyPlugin.CustomGoals;

import AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.CustomMobs.CustomEntityCreeper;
import AnonymousRand.ExtremeDifficultyPlugin.util.CoordsFromHypotenuse;
import net.minecraft.server.v1_16_R1.*;

import java.util.Random;

public class NewPathfinderGoalTeleportToPlayerAdjustY extends PathfinderGoal {

    public EntityInsentient entity;
    private final double yLevelDifferenceToActivate, initialRange, chancePerTick;
    private final CoordsFromHypotenuse coordsFromHypotenuse = new CoordsFromHypotenuse();
    private final Random random = new Random();

    public NewPathfinderGoalTeleportToPlayerAdjustY(EntityInsentient entity, double yLevelDifferenceToActivate, double initialRange, double chancePerTick) {
        this.entity = entity;
        this.yLevelDifferenceToActivate = yLevelDifferenceToActivate;
        this.initialRange = initialRange;
        this.chancePerTick = chancePerTick;
    }

    @Override
    public boolean a() {
        if (this.entity.getGoalTarget() instanceof EntityPlayer) {
            if (Math.abs(this.entity.getGoalTarget().locY() - this.entity.locY()) > this.yLevelDifferenceToActivate && random.nextDouble() < this.chancePerTick) { /**every tick the creeper is more than 2.5 blocks of elevation different from its target, it has a 0.15% chance to teleport near or onto the target onto a block that is within 3 y levels of the player*/
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

    protected void initiateTeleport(double h) { //try to ensure that creeper ends up within detonating range of player or endermites/silverfish end up within attacking range of player
        if (h == -1.0) {
            h = this.initialRange;
        }

        EntityPlayer player;
        player = (EntityPlayer)this.entity.getGoalTarget();

        BlockPosition pos = coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(new BlockPosition(player.locX(), player.locY(), player.locZ()), h, this.entity.locY(), 361.0); //gets coords for a random angle (0-360) with fixed hypotenuse to teleport to (so possible teleport area is a washer-like disc around the player)
        BlockPosition pos2 = this.entity.getWorld().getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, pos); //highest block at those coords

        if (pos2 != null) {
            if (Math.abs(this.entity.getGoalTarget().locY() - pos2.getY()) < (this.entity instanceof CustomEntityCreeper ? (((CustomEntityCreeper)this.entity).isPowered() ? 5.0 : 3.0) : 1.0)) { //if the teleport allows the mob to be within a certain y level of player
                if (pos2.getY() < 128.0) {
                    if (this.entity instanceof CustomEntityCreeper) {
                        CustomEntityCreeper creeper = ((CustomEntityCreeper)this.entity);
                        creeper.maxFuseTicks = (((CustomEntityCreeper)this.entity).isPowered() ? 30 : 25); //increase fuse length by 67% (0% for charged creepers) if teleporting very close to player

                        try {
                            creeper.fuseTicks.setInt(creeper, 0);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    this.teleportTo(pos2);
                    return;
                }
            }
        }

        //else use recursive method that checks smaller areas each time
        if (h > 6.0) {
            this.initiateTeleport(h - 2.0);
        } else if (h > 1.0){
            this.initiateTeleport(h - 1.0);
        } else { //teleport onto player if that's the only available block
            if (this.entity instanceof CustomEntityCreeper) {
                CustomEntityCreeper creeper = ((CustomEntityCreeper)this.entity);
                creeper.maxFuseTicks = (((CustomEntityCreeper)this.entity).isPowered() ? 30 : 25); //increase fuse length by 67% (0% for charged creepers) if teleporting very close to player

                try {
                    creeper.fuseTicks.setInt(creeper, 0);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            this.teleportTo(new BlockPosition(player.locX(), player.locY(), player.locZ()));
        }
    }

    protected boolean teleportTo(BlockPosition pos) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition(pos.getX(), pos.getY(), pos.getZ());

        while (blockposition_mutableblockposition.getY() > 0 && !this.entity.world.getType(blockposition_mutableblockposition).getMaterial().isSolid()) {
            blockposition_mutableblockposition.c(EnumDirection.DOWN);
        }

        IBlockData iblockdata = this.entity.world.getType(blockposition_mutableblockposition);

        if (iblockdata.getMaterial().isSolid()) {
            boolean flag2 = this.teleportHelper(pos.getX(), pos.getY(), pos.getZ(), true);

            if (flag2 && !this.entity.isSilent()) {
                this.entity.world.playSound((EntityHuman)null, this.entity.lastX, this.entity.lastY, this.entity.lastZ, SoundEffects.ENTITY_ENDERMAN_TELEPORT, this.entity.getSoundCategory(), 1.0F, 1.0F);
                this.entity.playSound(SoundEffects.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }

            return flag2;
        } else {
            return false;
        }
    }

    protected boolean teleportHelper(double d0, double d1, double d2, boolean flag) { //called a() in original living entity/whatever class
        double d3 = this.entity.locX();
        double d4 = this.entity.locY();
        double d5 = this.entity.locZ();
        double d6 = d1;
        boolean flag1 = false;
        BlockPosition blockposition = new BlockPosition(d0, d1, d2);
        World world = this.entity.world;

        if (world.isLoaded(blockposition)) {
            boolean flag2 = false;

            while (!flag2 && blockposition.getY() > 0) {
                BlockPosition blockposition1 = blockposition.down();
                IBlockData iblockdata = world.getType(blockposition1);

                if (iblockdata.getMaterial().isSolid()) {
                    flag2 = true;
                } else {
                    --d6;
                    blockposition = blockposition1;
                }
            }

            if (flag2) {
                this.entity.enderTeleportTo(d0, d6, d2);
                if (world.getCubes(this.entity)) { /**can teleport onto fluids*/
                    flag1 = true;
                }
            }
        }

        if (!flag1) {
            this.entity.enderTeleportTo(d3, d4, d5);
            return false;
        } else {
            if (flag) {
                world.broadcastEntityEffect(this.entity, (byte) 46);
            }

            if (this.entity instanceof EntityCreature) {
                ((EntityCreature)this.entity).getNavigation().o();
            }

            return true;
        }
    }
}
