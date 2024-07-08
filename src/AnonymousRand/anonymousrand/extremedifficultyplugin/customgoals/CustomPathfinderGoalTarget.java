package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.*;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.GenericAttributes;
import net.minecraft.server.v1_16_R1.PathfinderGoalTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public abstract class CustomPathfinderGoalTarget extends PathfinderGoalTarget {

    public CustomPathfinderGoalTarget(EntityInsentient entityInsentient, boolean needSightToMaintainTarget, boolean nearbyOnly) {
        super(entityInsentient, needSightToMaintainTarget, nearbyOnly);
    }

    @Override
    protected double k() { // getFollowDistance from attribute; change this method instead of changing attribute as this method is called in super() of constructor (?), and takes the value of follow_range from that time only
        Entity bukkitEntity = this.e.getBukkitEntity();
        EntityType bukkitEntityType = bukkitEntity.getType();

        switch (bukkitEntityType) {
            case BAT:
                return ((CustomEntityBat)this.e).getDetectionRange();
            case BEE:
                return ((CustomEntityBee)this.e).getDetectionRange();
            case BLAZE:
                return ((CustomEntityBlaze)this.e).getDetectionRange();
            case CAVE_SPIDER:
                return ((CustomEntitySpiderCave)this.e).getDetectionRange();
            case CHICKEN:
                return ((CustomEntityChickenAggressive)this.e).getDetectionRange();
            case CREEPER:
                return ((CustomEntityCreeper)this.e).getDetectionRange();
            case DROWNED:
                return ((CustomEntityDrowned)this.e).getDetectionRange();
            case ENDER_DRAGON:
                return ((CustomEntityEnderDragon)this.e).getDetectionRange();
            case ENDERMAN:
                return ((CustomEntityEnderman)this.e).getDetectionRange();
            case ENDERMITE:
                return ((CustomEntityEndermite)this.e).getDetectionRange();
            case EVOKER:
                return ((CustomEntityEvoker)this.e).getDetectionRange();
            case GHAST:
                return ((CustomEntityGhast)this.e).getDetectionRange();
            case GUARDIAN:
                return ((CustomEntityGuardian)this.e).getDetectionRange();
            case ELDER_GUARDIAN:
                return ((CustomEntityGuardianElder)this.e).getDetectionRange();
            case HOGLIN:
                return ((CustomEntityHoglin)this.e).getDetectionRange();
            case HUSK:
                return ((CustomEntityZombieHusk)this.e).getDetectionRange();
            case MAGMA_CUBE:
                return ((CustomEntitySlimeMagmaCube)this.e).getDetectionRange();
            case ILLUSIONER:
                return ((CustomEntityIllusioner)this.e).getDetectionRange();
            case IRON_GOLEM:
                return ((CustomEntityIronGolem)this.e).getDetectionRange();
            case LLAMA:
                return ((CustomEntityLlama)this.e).getDetectionRange();
            case PHANTOM:
                return ((CustomEntityPhantom)this.e).getDetectionRange();
            case PIGLIN:
                return ((CustomEntityPiglin)this.e).getDetectionRange();
            case PILLAGER:
                return ((CustomEntityPillager)this.e).getDetectionRange();
            case PUFFERFISH:
                return ((CustomEntityPufferfish)this.e).getDetectionRange();
            case RABBIT:
                return ((CustomEntityRabbit)this.e).getDetectionRange();
            case RAVAGER:
                return ((CustomEntityRavager)this.e).getDetectionRange();
            case SHEEP:
                if (this.e instanceof CustomEntitySheepAggressive) {
                    return ((CustomEntitySheepAggressive)this.e).getDetectionRange();
                } else {
                    return ((CustomEntitySheep)this.e).getDetectionRange();
                }
            case SHULKER:
                return ((CustomEntityShulker)this.e).getDetectionRange();
            case SILVERFISH:
                return ((CustomEntitySilverfish)this.e).getDetectionRange();
            case SKELETON:
                return ((CustomEntitySkeleton)this.e).getDetectionRange();
            case SLIME:
                return ((CustomEntitySlime)this.e).getDetectionRange();
            case SPIDER:
                return ((CustomEntitySpider)this.e).getDetectionRange();
            case STRAY:
                return ((CustomEntitySkeletonStray)this.e).getDetectionRange();
            case TRADER_LLAMA:
                return ((CustomEntityLlamaTrader)this.e).getDetectionRange();
            case VEX:
                return ((CustomEntityVex)this.e).getDetectionRange();
            case VILLAGER:
                return ((CustomEntityVillagerAggressive)this.e).getDetectionRange();
            case VINDICATOR:
                return ((CustomEntityVindicator)this.e).getDetectionRange();
            case WITCH:
                return ((CustomEntityWitch)this.e).getDetectionRange();
            case WITHER:
                return ((CustomEntityWither)this.e).getDetectionRange();
            case WITHER_SKELETON:
                return ((CustomEntitySkeletonWither)this.e).getDetectionRange();
            case WOLF:
                return ((CustomEntityWolf)this.e).getDetectionRange();
            case ZOGLIN:
                return ((CustomEntityZoglin)this.e).getDetectionRange();
            case ZOMBIE:
                if (this.e instanceof CustomEntityZombieThor) {
                    return ((CustomEntityZombieThor)this.e).getDetectionRange();
                } else if (this.e instanceof CustomEntityZombieSuper) {
                    return ((CustomEntityZombieSuper)this.e).getDetectionRange();
                } else {
                    return ((CustomEntityZombie)this.e).getDetectionRange();
                }
            case ZOMBIE_VILLAGER:
                return ((CustomEntityZombieVillager)this.e).getDetectionRange();
            case ZOMBIFIED_PIGLIN:
                return ((CustomEntityZombiePig)this.e).getDetectionRange();
        }

        return this.e.b(GenericAttributes.FOLLOW_RANGE);
    }
}
