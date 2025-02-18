package AnonymousRand.anonymousrand.plugin.nms.customentities.projectiles;

import net.minecraft.server.v1_16_R1.Explosion;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class CustomEntityArrowExploding extends CustomEntityArrow {

    private float yield;

    public CustomEntityArrowExploding(World world, Vector a, byte pierce, ProjectileSource source, float yield) {
        super(world, a, pierce, source);
        this.yield = yield;
    }

    public CustomEntityArrowExploding(World world) {
        super(world);
    }

    @Override
    public void die() {
        super.die();
        this.world.createExplosion(this, this.locX(), this.locY(), this.locZ(), this.yield, false, Explosion.Effect.DESTROY); /* these arrows explode when they die */
    }
}
