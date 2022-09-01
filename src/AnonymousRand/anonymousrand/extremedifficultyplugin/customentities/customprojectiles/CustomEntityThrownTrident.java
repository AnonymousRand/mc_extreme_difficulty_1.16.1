package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;

public class CustomEntityThrownTrident extends EntityThrownTrident {

    private static Field ap;

    public CustomEntityThrownTrident(World world, Vector a, byte pierce, @Nullable ProjectileSource source) {
        super(EntityTypes.TRIDENT, world);

        double x = a.getX(), y = a.getY(), z = a.getZ(); // similar code from projectile's shoot method; passing in a vec3d directly to this constructor doesn't seem to get the correct vector so must convert vector to vec3d instead
        Vec3D v = (new Vec3D(x, y, z).d());
        this.setMot(v);
        this.setPierceLevel(pierce);

        if (source instanceof Entity) {
            this.setShooter(((CraftEntity)source).getHandle());
        }
    }

    static {
        try {
            ap = EntityThrownTrident.class.getDeclaredField("ap");
            ap.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void a(MovingObjectPositionEntity movingobjectpositionentity) {
        net.minecraft.server.v1_16_R1.Entity entity = movingobjectpositionentity.getEntity();
        float f = 4.0F; /** trident damage decreased from 8 to 4.0 */

        if (entity instanceof EntityLiving) {
            EntityLiving entityliving = (EntityLiving) entity;

            f += EnchantmentManager.a(this.trident, entityliving.getMonsterType());
        }

        net.minecraft.server.v1_16_R1.Entity entity1 = this.getShooter();
        DamageSource damagesource = DamageSource.a((net.minecraft.server.v1_16_R1.Entity)this, (net.minecraft.server.v1_16_R1.Entity)(entity1 == null ? this : entity1));

        try {
            ap.setBoolean(this, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        SoundEffect soundeffect = SoundEffects.ITEM_TRIDENT_HIT;

        if (entity.damageEntity(damagesource, f)) {
            if (entity.getEntityType() == EntityTypes.ENDERMAN) {
                return;
            }

            if (entity instanceof EntityLiving) {
                EntityLiving entityliving1 = (EntityLiving) entity;

                if (entity1 instanceof EntityLiving) {
                    EnchantmentManager.a(entityliving1, entity1);
                    EnchantmentManager.b((EntityLiving) entity1, (net.minecraft.server.v1_16_R1.Entity)entityliving1);
                }

                this.a(entityliving1);
            }
        }

        this.setMot(this.getMot().d(-0.01D, -0.1D, -0.01D));
        float f1 = 1.0F;

        if (this.world instanceof WorldServer && this.world.T() && EnchantmentManager.h(this.trident)) {
            BlockPosition blockposition = entity.getChunkCoordinates();

            if (this.world.f(blockposition)) {
                EntityLightning entitylightning = (EntityLightning) EntityTypes.LIGHTNING_BOLT.a(this.world);

                entitylightning.c(Vec3D.c((BaseBlockPosition) blockposition));
                entitylightning.d(entity1 instanceof EntityPlayer ? (EntityPlayer) entity1 : null);
                this.world.addEntity(entitylightning);
                soundeffect = SoundEffects.ITEM_TRIDENT_THUNDER;
                f1 = 5.0F;
            }
        }

        this.playSound(soundeffect, f1, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived >= 120) { /** tridents despawn after 6 seconds */
            this.die();
        }
    }

    @Override
    public void die() {
        super.die();

        if (random.nextDouble() < 0.2) {
            this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 0.5F, false, Explosion.Effect.DESTROY); /** tridents have a 20% chance to explode when they die */
        }
    }
}
