package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalMeleeAttack;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalCobwebMoveFaster;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import net.minecraft.server.v1_16_R1.*;

import java.util.Arrays;

public class CustomEntityVindicator extends EntityVindicator implements ICustomHostile, IAttackLevelingMob {

    public PathfinderGoalSelector vanillaTargetSelector;
    private int attacks;
    private boolean a10, a25, a35;

    public CustomEntityVindicator(World world) {
        super(EntityTypes.VINDICATOR, world);
        this.vanillaTargetSelector = super.targetSelector;
        this.a(PathType.LAVA, 0.0F); /* no longer avoids lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F); /* no longer avoids fire */
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.IRON_AXE)); // makes sure that it has an axe
        this.setSlot(EnumItemSlot.OFFHAND, new ItemStack(Items.IRON_AXE)); /* vindicators have axes everywhere visible */
        this.setSlot(EnumItemSlot.HEAD, new ItemStack(Items.IRON_AXE));
        Arrays.fill(this.dropChanceHand, 0.0f);  /* vindicators can't drop the items they are holding/items in armor slots */
        Arrays.fill(this.dropChanceArmor, 0.0f);
        this.attacks = 0;
        this.a10 = false;
        this.a25 = false;
        this.a35 = false;
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.665); /* vindicators move 90% faster and have strength 1 */
        this.addEffect(new MobEffect(MobEffects.INCREASE_DAMAGE, Integer.MAX_VALUE, 0));
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /* custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /* custom goal that allows this mob to take certain buffs from bats etc. */
        this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack(this, 1.0D)); /* uses the custom melee attack goal that attacks regardless of the y level */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
    }

    public double getFollowRange() { /* vindicators have 24 block detection range (setting attribute doesn't work) (32 after 10 attacks) */
        return this.attacks < 10 ? 24.0 : 32.0;
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (nearestPlayer != null) {
                /* Mobs only despawn along horizontal axes, so if you are at y=256, mobs will still spawn below you and prevent sleeping */
                double distSquaredToNearestPlayer = Math.pow(nearestPlayer.getPositionVector().getX() - this.getPositionVector().getX(), 2)
                        + Math.pow(nearestPlayer.getPositionVector().getZ() - this.getPositionVector().getZ(), 2);
                int forceDespawnDist = this.getEntityType().e().f();
                int forceDespawnDistSquared = forceDespawnDist * forceDespawnDist;

                if (distSquaredToNearestPlayer > (double) forceDespawnDistSquared && this.isTypeNotPersistent(distSquaredToNearestPlayer)) {
                    this.die();
                }

                /* Random despawn distance increased to 40 blocks */
                int randomDespawnDist = this.getEntityType().e().g() + 8;
                int randomDespawnDistSquared = randomDespawnDist * randomDespawnDist;

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && distSquaredToNearestPlayer > (double)randomDespawnDistSquared
                        && this.isTypeNotPersistent(distSquaredToNearestPlayer)) {
                    this.die();
                } else if (distSquaredToNearestPlayer < (double) randomDespawnDistSquared) {
                    this.ticksFarFromPlayer = 0;
                }
            }

        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    @Override
    public double g(double x, double y, double z) {
        double dist_x = this.locX() - x;
        double dist_z = this.locZ() - z;

        return dist_x * dist_x + dist_z * dist_z;
    }

    @Override
    public double d(Vec3D vec3d) {
        double dist_x = this.locX() - vec3d.x;
        double dist_z = this.locZ() - vec3d.z;

        return dist_x * dist_x + dist_z * dist_z;
    }

    @Override
    public int bL() {
        return Integer.MAX_VALUE;
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

        if (this.attacks == 10 && !this.a10) { /* after 10 attacks, vindicators get diamond axes which increases damage by 0-1 */
            this.a10 = true;
            this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.DIAMOND_AXE));
            this.setSlot(EnumItemSlot.OFFHAND, new ItemStack(Items.DIAMOND_AXE));
            this.setSlot(EnumItemSlot.HEAD, new ItemStack(Items.DIAMOND_AXE));
            this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); // updates follow range
        }

        if (this.attacks == 25 && !this.a25) { /* after 25 attacks, vindicators get strength 2 */
            this.a25 = true;
            this.addEffect(new MobEffect(MobEffects.INCREASE_DAMAGE, Integer.MAX_VALUE, 1));
        }

        if (this.attacks == 35 && !this.a35) { /* after 35 attacks, vindicators get netherite axes and strength 3 */
            this.a35 = true;
            this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.NETHERITE_AXE));
            this.setSlot(EnumItemSlot.OFFHAND, new ItemStack(Items.NETHERITE_AXE));
            this.setSlot(EnumItemSlot.HEAD, new ItemStack(Items.NETHERITE_AXE));
            this.addEffect(new MobEffect(MobEffects.INCREASE_DAMAGE, Integer.MAX_VALUE, 2));
        }
    }
}
