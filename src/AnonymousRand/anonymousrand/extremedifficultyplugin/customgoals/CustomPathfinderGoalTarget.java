package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.GenericAttributes;
import net.minecraft.server.v1_16_R1.PathfinderGoalTarget;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public abstract class CustomPathfinderGoalTarget extends PathfinderGoalTarget {

    public CustomPathfinderGoalTarget(EntityInsentient entityinsentient, boolean checkSight, boolean nearbyOnly) {
        super(entityinsentient, checkSight, nearbyOnly);
    }

    @Override
    protected double k() { //getFollowDistance from attribute; change this method instead of changing attribute as this method is called in super() of contructor (?), and takes the value of follow_range from that time only
        Entity bukkitEntity = this.e.getBukkitEntity();
        net.minecraft.server.v1_16_R1.Entity nmsEntity = ((CraftEntity)bukkitEntity).getHandle();
        EntityType type = bukkitEntity.getType();

        switch (type) {
            case BAT -> {
                return ((CustomEntityBat)nmsEntity).getFollowRange();
            }
            case BEE -> {
                return ((CustomEntityBee)nmsEntity).getFollowRange();
            }
            case BLAZE -> {
                return ((CustomEntityBlaze)nmsEntity).getFollowRange();
            }
            case CAVE_SPIDER -> {
                return ((CustomEntitySpiderCave)nmsEntity).getFollowRange();
            }
            case CHICKEN -> {
                return ((CustomEntityChickenAggressive)nmsEntity).getFollowRange();
            }
            case CREEPER -> {
                return ((CustomEntityCreeper)nmsEntity).getFollowRange();
            }
            case DROWNED -> {
                return ((CustomEntityDrowned)nmsEntity).getFollowRange();
            }
            case ENDERMAN -> {
                return ((CustomEntityEnderman)nmsEntity).getFollowRange();
            }
            case ENDERMITE -> {
                return ((CustomEntityEndermite)nmsEntity).getFollowRange();
            }
            case EVOKER -> {
                return ((CustomEntityEvoker)nmsEntity).getFollowRange();
            }
            case GUARDIAN -> {
                return ((CustomEntityGuardian)nmsEntity).getFollowRange();
            }
            case ELDER_GUARDIAN -> {
                return ((CustomEntityGuardianElder) nmsEntity).getFollowRange();
            }
            case HOGLIN -> {
                return ((CustomEntityHoglin)nmsEntity).getFollowRange();
            }
            case LLAMA -> {
                return ((CustomEntityLlama)nmsEntity).getFollowRange();
            }
            case RABBIT -> {
                return ((CustomEntityRabbit)nmsEntity).getFollowRange();
            }
            case RAVAGER -> {
                return ((CustomEntityRavager)nmsEntity).getFollowRange();
            }
            case SHEEP -> {
                return ((CustomEntitySheepAggressive)nmsEntity).getFollowRange();
            }
            case SILVERFISH -> {
                return ((CustomEntitySilverfish)nmsEntity).getFollowRange();
            }
            case SKELETON -> {
                return ((CustomEntitySkeleton)nmsEntity).getFollowRange();
            }
            case STRAY -> {
                return ((CustomEntitySkeletonStray)nmsEntity).getFollowRange();
            }
            case SPIDER -> {
                return ((CustomEntitySpider)nmsEntity).getFollowRange();
            }
            case TRADER_LLAMA -> {
                return ((CustomEntityLlamaTrader)nmsEntity).getFollowRange();
            }
            case ZOGLIN -> {
                return ((CustomEntityZoglin)nmsEntity).getFollowRange();
            }
            case ZOMBIE -> {
                return ((CustomEntityZombie)nmsEntity).getFollowRange();
            }
        }

        return this.e.b(GenericAttributes.FOLLOW_RANGE);
    }
}
