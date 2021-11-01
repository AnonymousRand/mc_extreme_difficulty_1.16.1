package AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.CustomMobs;

import AnonymousRand.ExtremeDifficultyPlugin.CustomGoals.NewPathfinderGoalCobweb;
import AnonymousRand.ExtremeDifficultyPlugin.CustomGoals.NewPathfinderGoalGetBuffedByMobs;
import AnonymousRand.ExtremeDifficultyPlugin.util.SpawnLivingEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityCow extends EntityCow {

    public CustomEntityCow(World world) {
        super(EntityTypes.COW, world);
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobweb(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived == 10) { /**cows move twice as fast and have 20 health*/
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.4);
            this.setHealth(20.0F);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(20.0);

            if (this.random.nextDouble() < 0.04) { /**cows have a 1 in 25 chance to spawn as a mooshroom*/
                new SpawnLivingEntity(this.getWorld(), new CustomEntityMushroomCow(this.getWorld()), 1, null, null, this, true, true).run();
            }
        }
    }
}
