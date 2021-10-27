package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.NewPathfinderGoalCobweb;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class CustomEntityLlamaTrader extends EntityLlamaTrader {

    public CustomEntityLlamaTrader(World world) {
        super(EntityTypes.TRADER_LLAMA, world);
    }

    @Override
    protected void initPathfinder() { /**llamas won't panic anymore, are always aggro towards players, don't stop attacking after spitting once, and no longer defend wolves*/
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(0, new NewPathfinderGoalCobweb(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(2, new PathfinderGoalLlamaFollow(this, 2.0999999046325684D));
        this.goalSelector.a(3, new PathfinderGoalArrowAttack(this, 1.25D, 40, 20.0F)); //why is this here???
        this.goalSelector.a(4, new PathfinderGoalBreed(this, 1.0D));
        this.goalSelector.a(5, new PathfinderGoalFollowParent(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalRandomStrollLand(this, 0.7D));
        this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, new Class[0])); /**uses this instead of a custom llama hurt by target goal which stops spitting after the first spit*/
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived == 10) { /**llamas have 40 health*/
            ((LivingEntity) this.getBukkitEntity()).setMaxHealth(40.0);
            this.setHealth(40.0f);
        }
    }
}
