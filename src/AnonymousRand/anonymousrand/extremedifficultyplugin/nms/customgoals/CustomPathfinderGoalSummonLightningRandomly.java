package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.misc.CustomEntityLightning;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CustomMathHelper;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;

import java.util.Random;

public class CustomPathfinderGoalSummonLightningRandomly extends PathfinderGoal {

    private final EntityInsentient goalOwner;
    private final World nmsWorld;
    private final org.bukkit.World bukkitWorld;
    private final double chanceMultiplier;
    private static final Random random = new Random();

    public CustomPathfinderGoalSummonLightningRandomly(EntityInsentient goalOwner, double chanceMultiplier) {
        this.goalOwner = goalOwner;
        this.nmsWorld = goalOwner.getWorld();
        this.bukkitWorld = this.nmsWorld.getWorld();
        this.chanceMultiplier = chanceMultiplier;
    }

    @Override
    public boolean a() {
        if (this.goalOwner.getWorld().isRainingAt(new BlockPosition(this.goalOwner.locX(), this.goalOwner.locY(), this.goalOwner.locZ())) && random.nextDouble() < 0.0001 * (this.chanceMultiplier)) {
            return true;
        } else {
            return random.nextDouble() < 0.000025 * (this.chanceMultiplier);
        }
    }

    @Override
    public boolean b() {
        return this.a();
    }

    @Override
    public void e() {
        Location bukkitLoc = CustomMathHelper.coordsFromHypotAndAngle(this.bukkitWorld, new BlockPosition(this.goalOwner.locX(), this.goalOwner.locY(), this.goalOwner.locZ()), random.nextDouble() * 50.0, this.goalOwner.locY(), 361.0);
        new SpawnEntity(this.nmsWorld, new CustomEntityLightning(this.nmsWorld), 1, null, bukkitLoc, false);
    }
}
