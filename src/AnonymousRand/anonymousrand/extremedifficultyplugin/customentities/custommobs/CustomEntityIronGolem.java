package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityIronGolem extends EntityIronGolem implements ICustomMob {

    public int attacks;
    public double followRangeMultipler;

    public CustomEntityIronGolem(World world) {
        super(EntityTypes.IRON_GOLEM, world);
        this.a(PathType.LAVA, 0.0F); /** no longer avoids lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F); /** no longer avoids fire */
        this.attacks = 0;
        this.followRangeMultipler = 1.0;
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.5); /** iron golems move twice as fast but do half as much damage */
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(7.5);
    }

    @Override
    protected void initPathfinder() { /** no longer targets monsters or defends villages */
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 40, 2, 1, 2, 1, true)); /** custom goal that breaks blocks around the mob periodically */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /** custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /** custom goal that allows this mob to take certain buffs from bats etc. */
        this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack(this, 1.0D, true)); /** uses the custom melee attack goal that attacks regardless of the y level */
        this.goalSelector.a(2, new PathfinderGoalMoveTowardsTarget(this, 0.9D, 32.0F));
        this.goalSelector.a(2, new PathfinderGoalStrollVillage(this, 0.6D, false));
        this.goalSelector.a(4, new PathfinderGoalStrollVillageGolem(this, 0.6D));
        this.goalSelector.a(5, new PathfinderGoalOfferFlower(this));
        this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 6.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new CustomPathfinderGoalHurtByTarget(this, new Class[0]));
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, false)); /** always hostile to players; uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
        this.targetSelector.a(4, new PathfinderGoalUniversalAngerReset<>(this, false));
    }

    public void increaseStatsMultiply(double multipler) {
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue() * multipler);
        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(((LivingEntity)this.getBukkitEntity()).getMaxHealth() * multipler);
        this.setHealth((float)(this.getHealth() * multipler));
        this.followRangeMultipler *= multipler * 1.1;

        if (this.followRangeMultipler >= 5.0) {
            this.followRangeMultipler = 5.0;
        }

        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, false)); // updates follow range
    }

    public void increaseStatsAdd(double healthAdd, double damageAdd, double speedAdd) {
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue() + damageAdd);

        if (this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() <= (0.65 - speedAdd)) {
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() + speedAdd);
        }

        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(((LivingEntity)this.getBukkitEntity()).getMaxHealth() + healthAdd);
        this.setHealth((float)(this.getHealth() + healthAdd));
    }

    public double getFollowRange() { /** iron golems have 24 block detection range (setting attribute doesn't work) */
        return 24.0 * this.followRangeMultipler;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityhuman = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (entityhuman != null) {
                double d0 = Math.pow(entityhuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityhuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); /** mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleepingdouble d0 = entityhuman.h(this); */
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (d0 > (double)j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 8; /** random despawn distance increased to 40 blocks */
                int l = k * k;

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && d0 > (double)l && this.isTypeNotPersistent(d0)) {
                    this.die();
                } else if (d0 < (double)l) {
                    this.ticksFarFromPlayer = 0;
                }
            }

        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    @Override
    public double g(double d0, double d1, double d2) {
        double d3 = this.locX() - d0; /** for determining distance to entities, y level does not matter, eg. mob follow range, attacking (can hit player no matter the y level) */
        double d5 = this.locZ() - d2;

        return d3 * d3 + d5 * d5;
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /** for determining distance to entities, y level does not matter, eg. mob follow range, attacking (can hit player no matter the y level) */
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }

    @Override
    public int bL() { // getMaxFallHeight
        return Integer.MAX_VALUE; /** mobs are willing to take any fall to reach the player as they don't take fall damage */
    }
}