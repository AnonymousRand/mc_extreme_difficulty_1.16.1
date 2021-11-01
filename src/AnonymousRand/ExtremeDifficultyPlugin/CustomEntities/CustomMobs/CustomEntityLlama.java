package AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.CustomMobs;

import AnonymousRand.ExtremeDifficultyPlugin.CustomGoals.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityLlama extends EntityLlama {

    public CustomEntityLlama(World world) {
        super(EntityTypes.LLAMA, world);
    }

    @Override
    protected void initPathfinder() { /**llamas won't panic anymore, are always aggro towards players, don't stop attacking after spitting once, and no longer defend wolves*/
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(0, new NewPathfinderGoalCobweb(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(2, new PathfinderGoalLlamaFollow(this, 2.0999999046325684D));
        this.goalSelector.a(3, new CustomPathfinderGoalArrowAttack(this, 1.25D, 40, 20.0F)); /**uses the custom goal that attacks even when line of sight is broken*/
        this.goalSelector.a(4, new PathfinderGoalBreed(this, 1.0D));
        this.goalSelector.a(5, new PathfinderGoalFollowParent(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalRandomStrollLand(this, 0.7D));
        this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new CustomPathfinderGoalHurtByTarget(this, new Class[0])); /**custom goal that allows llama to keep spitting indefinitely and prevents mobs from retaliating against other mobs in case the mob damage event doesn't register and cancel the damage*/
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    public double getFollowRange() { /**llamas have 24 block detection range (setting attribute doesn't work)*/
        return 24.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived == 10) { /**llamas have 30 health*/
            ((LivingEntity) this.getBukkitEntity()).setMaxHealth(30.0);
            this.setHealth(30.0F);
        }

        if (this.ticksLived % 5 == 2) {
            if (this.getLastDamager() != null) {
                EntityLiving target = this.getLastDamager();

                if (!(target instanceof EntityPlayer)) { /**mobs only target players (in case mob damage listener doesn't register)*/
                    this.setLastDamager(null);
                }
            }
        }
    }
}
