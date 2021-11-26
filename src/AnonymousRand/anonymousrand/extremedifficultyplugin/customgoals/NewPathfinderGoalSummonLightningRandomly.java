package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityLightning;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CustomMathHelper;
import net.minecraft.server.v1_16_R1.*;

import java.util.Random;

public class NewPathfinderGoalSummonLightningRandomly extends PathfinderGoal {

    public EntityInsentient entity;
    private final double chanceMultiplier;
    private BlockPosition pos;
    private CustomEntityLightning lightning;
    private static final Random random = new Random();

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
        this.pos = new BlockPosition(CustomMathHelper.coordsFromHypotenuseAndAngle(new BlockPosition(this.entity.locX(), this.entity.locY(), this.entity.locZ()),  hypo, this.entity.locY(), 361.0));

        this.lightning = new CustomEntityLightning(this.entity.getWorld());
        this.lightning.setPosition(this.pos.getX(), this.pos.getY(), this.pos.getZ());
        this.entity.world.addEntity(this.lightning);
    }
}
