package AnonymousRand.ExtremeDifficultyPlugin.customEntities.misc;

import net.minecraft.server.v1_16_R1.*;

public class CustomEntityTNTPrimed extends EntityTNTPrimed {

    protected float explosionPower;

    public CustomEntityTNTPrimed(World world, int fuseLength, float explosionPower) { //adjustable fuse length and explosion power
        super(EntityTypes.TNT, world);

        this.setFuseTicks(fuseLength);
        this.explosionPower = explosionPower;
    }

    @Override
    public void tick() {
        if (!this.isNoGravity()) {
            this.setMot(this.getMot().add(0.0D, -0.04D, 0.0D));
        }

        this.move(EnumMoveType.SELF, this.getMot());
        this.setMot(this.getMot().a(0.98D));
        if (this.onGround) {
            this.setMot(this.getMot().d(0.7D, -0.5D, 0.7D));
        }

        this.setFuseTicks(this.getFuseTicks() - 1);
        if (this.getFuseTicks() <= 0) {
            this.die();
            if (!this.world.isClientSide) {
                this.explode();
            }
        } else {
            this.aG();
            if (this.world.isClientSide) {
                this.world.addParticle(Particles.SMOKE, this.locX(), this.locY() + 0.5D, this.locZ(), 0.0D, 0.0D, 0.0D);
            }
        }

    }

    private void explode() {
        this.world.explode(this, this.locX(), this.e(0.0625D), this.locZ(), this.explosionPower, Explosion.Effect.BREAK);
    }
}
