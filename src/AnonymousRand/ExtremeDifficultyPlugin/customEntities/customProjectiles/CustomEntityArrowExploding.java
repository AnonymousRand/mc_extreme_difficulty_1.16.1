package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customProjectiles;

import net.minecraft.server.v1_16_R1.Explosion;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class CustomEntityArrowExploding extends CustomEntityArrow {

    public CustomEntityArrowExploding(World world, Vector a, byte pierce, ProjectileSource source) {
        super(world, a, pierce, source);
    }

    @Override
    protected void h() {
        ++this.despawnCounter;
        if (this.despawnCounter >= 30) { //arrows despawn after 1.5 seconds to reduce lag
            this.die();
        }
    }

    @Override
    public void die() {
        super.die();
        this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 1.0f, true, Explosion.Effect.DESTROY);
    }
}
