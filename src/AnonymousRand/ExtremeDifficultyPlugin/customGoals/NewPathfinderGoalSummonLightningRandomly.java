package AnonymousRand.ExtremeDifficultyPlugin.customGoals;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.misc.CustomEntityLightning;
import AnonymousRand.ExtremeDifficultyPlugin.util.CoordsFromHypotenuse;
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
        if (this.entity.world.isRainingAt(new BlockPosition(this.entity.locX(), this.entity.locY(), this.entity.locZ())) && random.nextDouble() < 0.0003 * (this.chanceMultiplier)) {
            return true;
        } else {
            return random.nextDouble() < 0.000025 * (this.chanceMultiplier);
        }
    }

    @Override
    public boolean b() {
        if (this.entity.world.isRainingAt(new BlockPosition(this.entity.locX(), this.entity.locY(), this.entity.locZ())) && random.nextDouble() < 0.0003 * (this.chanceMultiplier)) {
            return true;
        } else {
            return random.nextDouble() < 0.000025 * (this.chanceMultiplier);
        }
    }

    @Override
    public void e() {
        double hypo = random.nextDouble() * 50;
        BlockPosition pos = new BlockPosition(coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(new BlockPosition(this.entity.locX(), this.entity.locY(), this.entity.locZ()),  hypo, this.entity.locY(), 361.0));

        CustomEntityLightning lightning = new CustomEntityLightning(this.entity.getWorld());
        lightning.setLocation(pos.getX(), pos.getY(), pos.getZ(), 0.0f, 0.0f);
        this.entity.world.addEntity(lightning);
    }
}
