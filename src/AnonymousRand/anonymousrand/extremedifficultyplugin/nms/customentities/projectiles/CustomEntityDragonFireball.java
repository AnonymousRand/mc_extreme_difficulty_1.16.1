package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.projectiles;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.misc.CustomEntityAreaEffectCloud;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;

import java.util.List;

public class CustomEntityDragonFireball extends EntityDragonFireball {

    private final boolean spawnMobs;

    public CustomEntityDragonFireball(World world, EntityLiving entityLiving, double d0, double d1, double d2, boolean spawnMobs) {
        super(world, entityLiving, d0, d1, d2);
        this.spawnMobs = spawnMobs;
    }

    @Override
    protected void a(MovingObjectPosition movingObjectPosition) {
        if (movingObjectPosition.getType() == MovingObjectPosition.EnumMovingObjectType.ENTITY) {
            if (!(((MovingObjectPositionEntity)movingObjectPosition).getEntity() instanceof EntityPlayer)) { /* area effect clouds can't collide with non-player mobs/entities */
                return;
            }
        } else if (movingObjectPosition.getType() != MovingObjectPosition.EnumMovingObjectType.BLOCK) {
            return;
        }

        Entity shooter = this.getShooter();
        List<EntityPlayer> nearbyPlayers;
        CustomEntityAreaEffectCloud entityAreaEffectCloud;
        boolean largerRadius = random.nextDouble() < 0.05;

        for (int i = -3; i < 3; i++) { /* area effect clouds are 6 blocks high and take 5 ticks less to start doing damage, but only last 5 seconds; 5% chance to create a wider area effect cloud with radius 7 */
            nearbyPlayers = this.world.a(EntityPlayer.class, this.getBoundingBox().grow(11.5, 128.0, 11.5));
            entityAreaEffectCloud = new CustomEntityAreaEffectCloud(this.world, largerRadius ? 7.0F : 3.0F, 100, 15);

            if (shooter instanceof EntityLiving) {
                entityAreaEffectCloud.setSource((EntityLiving) shooter);
            }

            entityAreaEffectCloud.setPosition(this.locX(), this.locY() + i, this.locZ());
            entityAreaEffectCloud.setParticle(Particles.DRAGON_BREATH);
            entityAreaEffectCloud.setRadiusPerTick((largerRadius ? 7.0F : 5.0F - entityAreaEffectCloud.getRadius()) / (float) entityAreaEffectCloud.getDuration()); /* area effect clouds only expand to a max of radius 5 and wide clouds don't expand */
            entityAreaEffectCloud.addEffect(new MobEffect(MobEffects.HARM, 1, 2));

            for (EntityPlayer player : nearbyPlayers) {
                if (this.d(player.getPositionVector()) < 64.0D) { /* area effect clouds snap on to location of closest player within 8 blocks horizontally */
                    entityAreaEffectCloud.setPosition(player.locX(), player.locY() + i, player.locZ());
                    break;
                }
            }

            this.world.triggerEffect(2006, this.getChunkCoordinates(), this.isSilent() ? -1 : 1);
            this.world.addEntity(entityAreaEffectCloud);
        }

        this.die();
    }

    @Override
    public void die() {
        super.die();

        if (this.spawnMobs) { /* most dragon fireballs have a chance to summon different mobs on impact */
            double rand = random.nextDouble();

            if (rand >= 0.55) {
                if (rand < 0.65) {
                    new SpawnEntity(this.world, new CustomEntityEnderman(this.world), random.nextInt(2) + 1, null, null, this, false, true);
                } else if (rand < 0.75) {
                    new SpawnEntity(this.world, new CustomEntityCreeper(this.world), random.nextInt(2) + 1, null, null, this, false, true);
                } else if (rand < 0.8) {
                    new SpawnEntity(this.world, new CustomEntityShulker(this.world), 1, null, null, this, false, false);
                } else if (rand < 0.85) {
                    new SpawnEntity(this.world, new CustomEntityRabbit(this.world), random.nextInt(2) + 4, null, null, this, false, true);
                } else if (rand < 0.9) {
                    new SpawnEntity(this.world, new CustomEntityVex(this.world), random.nextInt(2) + 3, null, null, this, false, true);
                } else if (rand < 0.95) {
                    new SpawnEntity(this.world, new CustomEntityLlama(this.world), 1, null, null, this, false, true);
                } else {
                    new SpawnEntity(this.world, new CustomEntityPiglin(this.world, true), random.nextInt(2) + 1, null, null, this, false, true);
                }
            }
        }
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /* for determining distance to entities, y-level does not matter, e.g. mob follow range, attacking (can hit player no matter the y-level) */
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }
}
