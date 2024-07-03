package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityZombieSuper extends EntityZombie implements ICustomHostile {

    public PathfinderGoalSelector vanillaTargetSelector;

    public CustomEntityZombieSuper(World world) {
        super(EntityTypes.ZOMBIE, world);
        this.a(PathType.LAVA, 0.0F); /** no longer avoids lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F); /** no longer avoids fire */
        this.vanillaTargetSelector = super.targetSelector;
        this.setBaby(false); /** super zombies move 3x faster, always summon a reinforcement when hit, and have 35 base health */
        this.setCanPickupLoot(true);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.69);
        this.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).setValue(1.0);
        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(35.0);
        this.setHealth(35.0F);
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    @Override
    protected void initPathfinder() { /** no longer targets iron golems */
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 40, 2, 1, 2, 1, true)); /** custom goal that breaks blocks around the mob periodically except for diamond blocks, emerald blocks, nertherite blocks, and beacons */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /** custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /** custom goal that allows this mob to take certain buffs from bats etc. */
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 1.0)); /** custom goal that spawns lightning randomly */
        this.goalSelector.a(2, new CustomPathfinderGoalZombieAttack(this, 1.0D)); /** uses the custom melee attack goal that attacks regardless of the y level */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /** uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class));
        this.targetSelector.a(4, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, EntityTurtle.bv));
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (super.damageEntity(damagesource, f)) {
            EntityLiving entityLiving = this.getGoalTarget();

            if (entityLiving == null) {
                entityLiving = (EntityLiving)damagesource.getEntity();
            }

            if (!(entityLiving instanceof EntityPlayer)) { /** only player damage can trigger reinforcement spawning */
                return true;
            }

            if ((double)random.nextFloat() < this.b(GenericAttributes.SPAWN_REINFORCEMENTS)) { /** zombies can now spawn reinforcements on any difficulty */
                for (int ii = 0; ii < (random.nextDouble() < 0.99 ? 1 : 30); ii++) { /** 1% chance to make 30 spawn attempts instead of 1 (on average, about half of them succeed) */
                    int i = MathHelper.floor(this.locX());
                    int j = MathHelper.floor(this.locY());
                    int k = MathHelper.floor(this.locZ());
                    CustomEntityZombie newZombie = new CustomEntityZombie(this.getWorld());

                    for (int l = 0; l < 50; ++l) {
                        int i1 = i + MathHelper.nextInt(random, 7, 40) * MathHelper.nextInt(random, -1, 1);
                        int j1 = j + MathHelper.nextInt(random, 7, 40) * MathHelper.nextInt(random, -1, 1);
                        int k1 = k + MathHelper.nextInt(random, 7, 40) * MathHelper.nextInt(random, -1, 1);
                        BlockPosition blockPosition = new BlockPosition(i1, j1, k1);
                        EntityTypes<?> entityTypes = newZombie.getEntityType();
                        EntityPositionTypes.Surface entityPositiontypes_surface = EntityPositionTypes.a(entityTypes);

                        if (SpawnerCreature.a(entityPositiontypes_surface, this.getWorld(), blockPosition, entityTypes) && EntityPositionTypes.a(entityTypes, this.getWorld(), EnumMobSpawn.REINFORCEMENT, blockPosition, this.getWorld().random)) {
                            newZombie.setPosition(i1, j1, k1);
                            if (!this.getWorld().isPlayerNearby(i1, j1, k1, 7.0D) && this.getWorld().i(newZombie) && this.getWorld().getCubes(newZombie) && !this.getWorld().containsLiquid(newZombie.getBoundingBox())) {
                                this.getWorld().addEntity(newZombie);
                                newZombie.prepare(this.getWorld(), this.getWorld().getDamageScaler(newZombie.getChunkCoordinates()), EnumMobSpawn.REINFORCEMENT, null, null);
                                this.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).addModifier(new AttributeModifier("Zombie reinforcement caller charge", -0.125, AttributeModifier.Operation.ADDITION)); /** zombies experience a 12.5% decrease in reinforcement summon chance instead of 5% if summoned reinforcements or was summoned as reinforcement (15% after 7 attacks) */
                                newZombie.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).addModifier(new AttributeModifier("Zombie reinforcement callee charge", -0.125, AttributeModifier.Operation.ADDITION));
                                break;
                            }
                        }
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public double getFollowRange() { /** super zombies have 128 block detection range (setting attribute doesn't work) */
        return 128.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.world.isClientSide && this.isAlive() && !this.isNoAI()) { /** doesn't convert to drowned in water */
            this.drownedConversionTime = Integer.MAX_VALUE;
        }
    }
    
    @Override
    public int bL() {
        return Integer.MAX_VALUE; /** mobs are willing to take any fall to reach the player as they don't take fall damage */
    }
}