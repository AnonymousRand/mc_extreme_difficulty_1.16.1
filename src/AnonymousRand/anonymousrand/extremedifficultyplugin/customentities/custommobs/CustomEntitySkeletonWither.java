package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntitySkeletonWither extends EntitySkeletonWither implements ICustomMob, IAttackLevelingMob {

    public PathfinderGoalSelector vanillaTargetSelector;
    private int attacks;
    private boolean a12, a50;

    public CustomEntitySkeletonWither(World world) {
        super(EntityTypes.WITHER_SKELETON, world);
        this.vanillaTargetSelector = super.targetSelector;
        this.a(PathType.DAMAGE_FIRE, 0.0F); /** no longer avoids fire */
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.STONE_SWORD)); // makes sure that it has a sword
        this.attacks = 0;
        this.a12 = false;
        this.a50 = false;
        this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(4.0); /** wither skeletons twice as fast and have extra knockback */
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.5);
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 20, 1, 1, 1, 1, true)); /** custom goal that breaks blocks around the mob periodically except for diamond blocks, emerald blocks, nertherite blocks, and beacons */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /** custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /** custom goal that allows this mob to take certain buffs from bats etc. */
        this.goalSelector.a(1, new NewPathfinderGoalSpawnBlocksEntitiesOnMob(this, org.bukkit.Material.SOUL_SOIL, 1, 1, 0, 1, -1.0, false)); /** custom goal that allows wither skeleton to summon soul sand in a 3 by 3 beneath itself constantly */
        this.goalSelector.a(1, new NewPathfinderGoalSpawnBlocksEntitiesOnMob(this, org.bukkit.Material.WITHER_ROSE, 1, 0, 0, 0, 0, false)); /** custom goal that allows wither skeleton to summon wither roses on itself constantly */
        this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack(this, 1.0D)); /** uses the custom melee attack goal that attacks regardless of the y level */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /** uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
    }

    @Override
    public boolean attackEntity(Entity entity) {
        if (!super.attackEntity(entity)) {
            return false;
        } else {
            if (entity instanceof EntityHuman) {
                ((EntityHuman)entity).addEffect(new MobEffect(MobEffects.WITHER, this.attacks < 8 ? 400 : this.attacks < 25 ? 600 : 900, 1)); /** wither skeletons apply wither 2 instead of wither 1 for 20 seconds instead of 10 (30 seconds after 8 attacks, 45 seconds after 25 attacks0 */
            }

            return true;
        }
    }

    public double getFollowRange() { /** wither skeletons have 40 blocks detection range */
        return 40.0;
    }

    public int getAttacks() {
        return this.attacks;
    }

    public void increaseAttacks(int increase) {
        this.attacks += increase;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 12 && !this.a12) { /** after 12 attacks, wither skeletons get regen 2 and 30 max health */
            this.a12 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(30.0);
        }

        if (this.attacks == 50 && !this.a50) { /** after 50 attacks, wither skeletons summon a mini wither */
            this.a50 = true;
            new SpawnEntity(this.getWorld(), new CustomEntityWitherMini(this.getWorld()), 1, null, null, this, false, false);
        }
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityHuman = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (entityHuman != null) {
                double d0 = Math.pow(entityHuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityHuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); /** mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleepingdouble d0 = entityHuman.h(this); */
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
        double d3 = this.locX() - d0; /** for determining distance to entities, y level does not matter for wither skeletons, e.g. mob follow range, attacking (can hit player no matter the y level) */
        double d5 = this.locZ() - d2;

        return d3 * d3 + d5 * d5;
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /** for determining distance to entities, y level does not matter for wither skeletons, e.g. mob follow range, attacking (can hit player no matter the y level) */
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }

    @Override
    public int bL() {
        return Integer.MAX_VALUE; /** mobs are willing to take any fall to reach the player as they don't take fall damage */
    }
}
