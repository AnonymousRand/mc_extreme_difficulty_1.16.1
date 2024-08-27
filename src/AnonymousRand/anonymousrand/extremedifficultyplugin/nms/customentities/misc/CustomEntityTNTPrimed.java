package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.misc;

import net.minecraft.server.v1_16_R1.*;

public class CustomEntityTNTPrimed extends EntityTNTPrimed {

    protected float explosionPower;

    public CustomEntityTNTPrimed(World world, int fuseLength, float explosionPower) { // adjustable fuse length and explosion power
        super(EntityTypes.TNT, world);

        this.setFuseTicks(fuseLength);
        this.explosionPower = explosionPower;
    }

    @Override
    public void tick() {
        if (!this.isNoGravity()) {
            this.setMot(this.getMot().add(0.0, -0.04, 0.0));
        }

        this.move(EnumMoveType.SELF, this.getMot());
        this.setMot(this.getMot().a(0.98));
        if (this.onGround) {
            this.setMot(this.getMot().d(0.7, -0.5, 0.7));
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
                this.world.addParticle(Particles.SMOKE, this.locX(), this.locY() + 0.5, this.locZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    private void explode() {
        this.world.explode(this, this.locX(), this.e(0.0625), this.locZ(), this.explosionPower, Explosion.Effect.BREAK);
    }
}
