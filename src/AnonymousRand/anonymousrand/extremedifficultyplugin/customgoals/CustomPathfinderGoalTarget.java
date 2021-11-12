package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.GenericAttributes;
import net.minecraft.server.v1_16_R1.PathfinderGoalTarget;
import org.bukkit.Bukkit;
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
        EntityType type = bukkitEntity.getType();

        switch (type) {
            case BAT -> {
                return ((CustomEntityBat)this.e).getFollowRange();
            }
            case BEE -> {
                return ((CustomEntityBee)this.e).getFollowRange();
            }
            case BLAZE -> {
                return ((CustomEntityBlaze)this.e).getFollowRange();
            }
            case CAVE_SPIDER -> {
                return ((CustomEntitySpiderCave)this.e).getFollowRange();
            }
            case CHICKEN -> {
                return ((CustomEntityChickenAggressive)this.e).getFollowRange();
            }
            case CREEPER -> {
                return ((CustomEntityCreeper)this.e).getFollowRange();
            }
            case DROWNED -> {
                return ((CustomEntityDrowned)this.e).getFollowRange();
            }
            case ENDERMAN -> {
                return ((CustomEntityEnderman)this.e).getFollowRange();
            }
            case ENDERMITE -> {
                return ((CustomEntityEndermite)this.e).getFollowRange();
            }
            case EVOKER -> {
                return ((CustomEntityEvoker)this.e).getFollowRange();
            }
            case GHAST -> {
                return ((CustomEntityGhast)this.e).getFollowRange();
            }
            case GUARDIAN -> {
                return ((CustomEntityGuardian)this.e).getFollowRange();
            }
            case ELDER_GUARDIAN -> {
                return ((CustomEntityGuardianElder)this.e).getFollowRange();
            }
            case HOGLIN -> {
                return ((CustomEntityHoglin)this.e).getFollowRange();
            }
            case HUSK -> {
                return ((CustomEntityZombieHusk)this.e).getFollowRange();
            }
            case MAGMA_CUBE -> {
                return ((CustomEntitySlimeMagmaCube)this.e).getFollowRange();
            }
            case ILLUSIONER -> {
                return ((CustomEntityIllagerIllusioner)this.e).getFollowRange();
            }
            case IRON_GOLEM -> {
                return ((CustomEntityIronGolem)this.e).getFollowRange();
            }
            case LLAMA -> {
                return ((CustomEntityLlama)this.e).getFollowRange();
            }
            case PHANTOM -> {
                return ((CustomEntityPhantom)this.e).getFollowRange();
            }
            case PIGLIN -> {
                return ((CustomEntityPiglin)this.e).getFollowRange();
            }
            case PILLAGER -> {
                return ((CustomEntityPillager)this.e).getFollowRange();
            }
            case RABBIT -> {
                return ((CustomEntityRabbit)this.e).getFollowRange();
            }
            case RAVAGER -> {
                return ((CustomEntityRavager)this.e).getFollowRange();
            }
            case SHEEP -> {
                return ((CustomEntitySheepAggressive)this.e).getFollowRange();
            }
            case SILVERFISH -> {
                return ((CustomEntitySilverfish)this.e).getFollowRange();
            }
            case SKELETON -> {
                return ((CustomEntitySkeleton)this.e).getFollowRange();
            }
            case SLIME -> {
                return ((CustomEntitySlime)this.e).getFollowRange();
            }
            case SPIDER -> {
                return ((CustomEntitySpider)this.e).getFollowRange();
            }
            case STRAY -> {
                return ((CustomEntitySkeletonStray)this.e).getFollowRange();
            }
            case TRADER_LLAMA -> {
                return ((CustomEntityLlamaTrader)this.e).getFollowRange();
            }
            case ZOGLIN -> {
                return ((CustomEntityZoglin)this.e).getFollowRange();
            }
            case ZOMBIE -> {
                if (this.e instanceof CustomEntityZombieThor) {
                    return ((CustomEntityZombieThor)this.e).getFollowRange();
                } else if (this.e instanceof CustomEntityZombieSuper) {
                    return ((CustomEntityZombieSuper)this.e).getFollowRange();
                } else {
                    return ((CustomEntityZombie)this.e).getFollowRange();
                }
            }
            case ZOMBIE_VILLAGER -> {
                return ((CustomEntityZombieVillager)this.e).getFollowRange();
            }
        }

        return this.e.b(GenericAttributes.FOLLOW_RANGE);
    }
}
