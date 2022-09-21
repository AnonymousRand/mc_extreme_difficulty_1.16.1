package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalMeleeAttack;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalCobwebMoveFaster;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.AccessPathfinderGoals;
import net.minecraft.server.v1_16_R1.*;

import java.util.Arrays;

public class CustomEntityVindicator extends EntityVindicator implements ICustomMob {

    public PathfinderGoalSelector targetSelectorVanilla;
    public int attacks;
    private boolean a10, a25, a35;

    public CustomEntityVindicator(World world) {
        super(EntityTypes.VINDICATOR, world);
        this.targetSelectorVanilla = super.targetSelector;
        this.a(PathType.LAVA, 0.0F); /** no longer avoids lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F); /** no longer avoids fire */
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.IRON_AXE)); // makes sure that it has an axe
        this.setSlot(EnumItemSlot.OFFHAND, new ItemStack(Items.IRON_AXE)); /** vindicators have axes everywhere visible */
        this.setSlot(EnumItemSlot.HEAD, new ItemStack(Items.IRON_AXE));
        Arrays.fill(this.dropChanceHand, 0.0f);  /** vindicators can't drop the items they are holding/items in armor slots */
        Arrays.fill(this.dropChanceArmor, 0.0f);
        this.attacks = 0;
        this.a10 = false;
        this.a25 = false;
        this.a35 = false;
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.665); /** vindicators move 90% faster and have strength 1 */
        this.addEffect(new MobEffect(MobEffects.INCREASE_DAMAGE, Integer.MAX_VALUE, 0));
        AccessPathfinderGoals.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /** custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /** custom goal that allows this mob to take certain buffs from bats etc. */
        this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack(this, 1.0D, true)); /** uses the custom melee attack goal that attacks regardless of the y level */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, false)); /** uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
    }

    public double getFollowRange() { /** vindicators have 24 block detection range (setting attribute doesn't work) (32 after 10 attacks) */
        return this.attacks < 10 ? 24.0 : 32.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 10 && !this.a10) { /** after 10 attacks, vindicators get diamond axes which increases damage by 0-1 */
            this.a10 = true;
            this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.DIAMOND_AXE));
            this.setSlot(EnumItemSlot.OFFHAND, new ItemStack(Items.DIAMOND_AXE));
            this.setSlot(EnumItemSlot.HEAD, new ItemStack(Items.DIAMOND_AXE));
            this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, true)); // updates follow range
        }

        if (this.attacks == 25 && !this.a25) { /** after 25 attacks, vindicators get strength 2 */
            this.a25 = true;
            this.addEffect(new MobEffect(MobEffects.INCREASE_DAMAGE, Integer.MAX_VALUE, 1));
        }

        if (this.attacks == 35 && !this.a35) { /** after 35 attacks, vindicators get netherite axes and strength 3 */
            this.a35 = true;
            this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.NETHERITE_AXE));
            this.setSlot(EnumItemSlot.OFFHAND, new ItemStack(Items.NETHERITE_AXE));
            this.setSlot(EnumItemSlot.HEAD, new ItemStack(Items.NETHERITE_AXE));
            this.addEffect(new MobEffect(MobEffects.INCREASE_DAMAGE, Integer.MAX_VALUE, 2));
        }
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
