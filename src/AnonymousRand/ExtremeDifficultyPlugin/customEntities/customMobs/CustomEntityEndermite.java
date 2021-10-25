package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.CustomEntityLightning;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderTargetCondition;
import AnonymousRand.ExtremeDifficultyPlugin.util.CoordsFromHypotenuse;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CustomEntityEndermite extends EntityEndermite {

    public int attacks;
    private boolean a40, a70, a100;

    public CustomEntityEndermite(World world) {
        super(EntityTypes.ENDERMITE, world);
        this.attacks = 0;
        this.a40 = false;
        this.a70 = false;
        this.a100 = false;
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, true)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (damagesource.getEntity() instanceof EntityPlayer && this.getHealth() - f > 0.0) { /**duplicates when hit by player and not killed*/
            CustomEntityEndermite newEndermite;

            newEndermite = new CustomEntityEndermite(this.getWorld());
            newEndermite.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
            this.getWorld().addEntity(newEndermite, CreatureSpawnEvent.SpawnReason.NATURAL);
        }

        return super.damageEntity(damagesource, f);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 40 && !this.a40) { /**after 40 attacks, endermites get more knockback*/
            this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(2.5);
        }

        if (this.attacks == 70 && !this.a70) { /**after 70 attacks, endemites get even more knockback, 14.9 max health and regen 1*/
            this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(3.5);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(14.9);
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
        }

        if (this.attacks == 100 && !this.a100) { /**after 100 attacks, endermites explode and die*/
            this.a100 = true;
            this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 2.0f, false, Explosion.Effect.DESTROY);
            this.die();
        }

        if (this.getGoalTarget() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)this.getGoalTarget();

            if (Math.abs(player.locY() - this.locY()) > 1 && random.nextDouble() < 0.01) { /**every tick the silverfish is more than 1 block of elevation different from its target, it has a 1% chance to teleport onto the player*/
                this.teleportTo(new BlockPosition(Math.floor(player.locX()), Math.floor(player.locY()), Math.floor(player.locZ())));
            }
        }

        if (this.ticksLived == 10) { /**endermites move 60% faster, have extra knockback, and have 10.5 health, but only do 1 damage*/
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.4);
            this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(1.5);
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(1.0);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(10.5);
            this.setHealth(10.5f);
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

    protected boolean teleportTo(BlockPosition pos) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition(pos.getX(), pos.getY(), pos.getZ());

        while (blockposition_mutableblockposition.getY() > 0 && !this.world.getType(blockposition_mutableblockposition).getMaterial().isSolid()) {
            blockposition_mutableblockposition.c(EnumDirection.DOWN);
        }

        IBlockData iblockdata = this.world.getType(blockposition_mutableblockposition);

        if (iblockdata.getMaterial().isSolid()) {
            boolean flag2 = this.a(pos.getX(), pos.getY(), pos.getZ(), true);

            if (flag2 && !this.isSilent()) {
                this.world.playSound((EntityHuman)null, this.lastX, this.lastY, this.lastZ, SoundEffects.ENTITY_ENDERMAN_TELEPORT, this.getSoundCategory(), 1.0F, 1.0F);
                this.playSound(SoundEffects.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }

            return flag2;
        } else {
            return false;
        }
    }

    @Override
    public boolean a(double d0, double d1, double d2, boolean flag) {
        double d3 = this.locX();
        double d4 = this.locY();
        double d5 = this.locZ();
        double d6 = d1;
        boolean flag1 = false;
        BlockPosition blockposition = new BlockPosition(d0, d1, d2);
        World world = this.world;

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
                this.enderTeleportTo(d0, d6, d2);
                if (world.getCubes(this)) { /**can teleport onto fluids*/
                    flag1 = true;
                }
            }
        }

        if (!flag1) {
            this.enderTeleportTo(d3, d4, d5);
            return false;
        } else {
            if (flag) {
                world.broadcastEntityEffect(this, (byte) 46);
            }

            if (this instanceof EntityCreature) {
                ((EntityCreature)this).getNavigation().o();
            }

            return true;
        }
    }
}
