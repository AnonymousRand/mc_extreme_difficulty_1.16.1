package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityLightning;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CoordsFromHypotenuse;
import net.minecraft.server.v1_16_R1.*;

import java.util.Random;

public class NewPathfinderGoalSummonLightningRandomly extends PathfinderGoal {

    public EntityInsentient entity;
    private final double chanceMultiplier;
    private final CoordsFromHypotenuse coordsFromHypotenuse = new CoordsFromHypotenuse();
    private final Random random = new Random();

    public NewPathfinderGoalSummonLightningRandomly(EntityInsentient entity, double chanceMultiplier) {
        this.entity = entity;
        this.chanceMultiplier = chanceMultiplier;
    }

    @Override
    public boolean a() {
        if (this.entity.world.isRainingAt(new BlockPosition(this.entity.locX(), this.entity.locY(), this.entity.locZ())) && random.nextDouble() < 0.0002 * (this.chanceMultiplier)) {
            return true;
        } else {
            return random.nextDouble() < 0.00004 * (this.chanceMultiplier);
        }
    }

    @Override
    public boolean b() {
        return this.a();
    }

    @Override
    public void e() {
        double hypo = random.nextDouble() * 50.0;
        BlockPosition pos = new BlockPosition(coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(new BlockPosition(this.entity.locX(), this.entity.locY(), this.entity.locZ()),  hypo, this.entity.locY(), 361.0));

        CustomEntityLightning lightning = new CustomEntityLightning(this.entity.getWorld());
        lightning.setLocation(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
        this.entity.world.addEntity(lightning);
    }
}
